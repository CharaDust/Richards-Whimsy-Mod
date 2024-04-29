package com.rcdmake.whimsy.recipe.ClassDEV;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.DEV_SimpleWorker;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class DEV_SimpleWorker_R implements Recipe<SimpleInventory> {

//    private final Identifier id;
    private final ItemStack output;
    private final List<Ingredient> recipeItems;

    public DEV_SimpleWorker_R(List<Ingredient> recipeItems, ItemStack output) {
        // 构造函数形参
//        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    // 匹配
    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        // 客户端世界禁止匹配
        if (world.isClient()) {
            return false;
        }
        // 前一个 0 指的是json文件中配方输入的索引值，后一个 0 指的是工作方块的输入格子代号
        return recipeItems.get(0).test(inventory.getStack(0));
    }

    // 工作结果，一般输出 output
    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    // 工作结果，一般输出 output
    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList list = DefaultedList.ofSize(this.recipeItems.size());
        list.addAll(recipeItems);
        return list;
    }

    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output.copy();
    }

    public static class Type implements RecipeType<DEV_SimpleWorker_R> {
        private Type() { }
        // 类型实例
        public static final Type INSTANCE = new Type();
        // 类型字符串
        public static final String ID = "simple_working";
    }

    // 序列化（读取文件）
    public static class Serializer implements RecipeSerializer<DEV_SimpleWorker_R> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "simple_working";
        // 自定义JSON编码器
        public static final Codec<DEV_SimpleWorker_R> CODEC = RecordCodecBuilder.create(in -> in.group(
                validateAmount(Ingredient.DISALLOW_EMPTY_CODEC, 9).fieldOf("ingredients").forGetter(DEV_SimpleWorker_R::getIngredients),
                ItemStack.RECIPE_RESULT_CODEC.fieldOf("output").forGetter(r -> r.output)
        ).apply(in, DEV_SimpleWorker_R::new));

        private static Codec<List<Ingredient>> validateAmount(Codec<Ingredient> delegate, int max) {
            return Codecs.validate(Codecs.validate(
                    delegate.listOf(), list -> list.size() > max ? DataResult.error(() -> "Recipe has too many ingredients!") : DataResult.success(list)
            ), list -> list.isEmpty() ? DataResult.error(() -> "Recipe has no ingredients!") : DataResult.success(list));
        }
        // 编码器
        @Override
        public Codec<DEV_SimpleWorker_R> codec() {
            return CODEC;
        }

        @Override
        public DEV_SimpleWorker_R read(PacketByteBuf buf) {
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromPacket(buf));
            }

            ItemStack output = buf.readItemStack();
            return new DEV_SimpleWorker_R(inputs, output);
        }

        @Override
        public void write(PacketByteBuf buf, DEV_SimpleWorker_R recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.write(buf);
            }
            buf.writeItemStack(recipe.getResult(null));
        }
    }

    // OnInit
    public static void OnInit(){
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(RichardsWhimsyMod.MOD_ID, DEV_SimpleWorker_R.Serializer.ID),
                DEV_SimpleWorker_R.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, new Identifier(RichardsWhimsyMod.MOD_ID, DEV_SimpleWorker_R.Type.ID),
                DEV_SimpleWorker_R.Type.INSTANCE);
    }
}