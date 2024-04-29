package com.rcdmake.whimsy.block.ClassDEV.BlockEntity;

import com.mojang.serialization.MapCodec;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.Entity.DEV_EntityInt_E;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;

public class DEV_EntityInt extends BlockWithEntity implements BlockEntityProvider {

    // super
    public DEV_EntityInt(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    // 创建方块，使用构造函数
    public static final Block DEV_ENTITY_INT = new DEV_EntityInt(FabricBlockSettings.copyOf(Blocks.STONE));

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // 连接方块实体与方块
    /**
     * 一旦创建并注册了 BlockEntityType，就可以在 Block 类中简单地实现 BlockEntityProvider。
     * <br>
     * 每次放置方块，就会产生对应的方块实体。
     * */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DEV_EntityInt_E(pos, state);
    }

    // 渲染方块
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // 由于继承了BlockWithEntity，这个默认为INVISIBLE，所以我们需要更改它！
        return BlockRenderType.MODEL;
    }
    // 方块实体刻（可选）
    /**
     * 该条帮助内容摘自 Chat-GPT4 <br />
     *
     * 获取方块实体的定时更新处理器（Ticker）。
     * <p>
     * 此方法用于为特定类型的方块实体提供一个定时调用的更新函数。
     * 它会检查提供的方块实体类型是否与期望的类型匹配。如果匹配，返回一个lambda表达式，
     * 用于每个游戏刻更新方块实体的状态。
     *
     * @param world 当前的世界对象，提供方块实体存在的环境。
     * @param state 方块的当前状态，包含方块的详细信息。
     * @param type 请求的方块实体类型。
     * @param <T> 方块实体的类型参数。
     * @return 如果类型匹配，则返回一个定时更新处理器；否则返回 null。
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, DEV_EntityInt_E.DEV_ENTITY_INT_E,
                (world1, pos, state1, be) -> ((DEV_EntityInt_E) be).tick(world1, pos, state1, be));
    }

    // 点击事件
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if(be instanceof DEV_EntityInt_E){
                int num = ((DEV_EntityInt_E) be).getBe_num();
                outputMessageServer(player, num);
            }
        }
        if (world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if(be instanceof DEV_EntityInt_E){
                int num = ((DEV_EntityInt_E) be).getBe_num();
                outputMessageClient(player, num);
            }
        }
        return ActionResult.SUCCESS;
    }

    // 发送消息：服务端
    private void outputMessageServer(PlayerEntity player, int num){
        player.sendMessage(Text.literal(
                "Tick passed: "
                + num
                + "ticks in Server."
        ), false);
    }

    // 发送消息：客户端
    private void outputMessageClient(PlayerEntity player, int num){
        player.sendMessage(Text.literal(
                "Tick passed: "
                        + num
                        + "ticks in Client(Your Hard Drive)."
        ), false);
    }

    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_entity_int", DEV_ENTITY_INT);
    }
}
