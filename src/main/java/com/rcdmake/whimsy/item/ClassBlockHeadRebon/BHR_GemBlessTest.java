package com.rcdmake.whimsy.item.ClassBlockHeadRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.WI_Prospector;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BHR_GemBlessTest extends Item {
    // super
    public BHR_GemBlessTest(Settings settings) {
        super(settings);
    }

    // 注册物品，使用构造函数
    public static final Item GEM_BLESS_TEST = registerItems("bhr_gem_bless_test", new BHR_GemBlessTest(new FabricItemSettings()));
    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name), item);
    }

    // 执行结果逻辑：右键点击返回NBT值
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // 物品在主手
        ItemStack itemStack = player.getStackInHand(hand);
        // 确保为服务端世界
        if (!world.isClient) {
            NbtCompound nbt = itemStack.getNbt();
            // 检测标签内容是否含有NBT，如果为是，则...
            if (nbt != null && nbt.contains("GemBlessing")) {
                int val = nbt.getInt("GemBlessing");
                // 发送NBT包含的值
                player.sendMessage(Text.literal("GemBlessing: " + val), false);
            }
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }
    // OnInit
    public static void OnInit(){
    }

}
