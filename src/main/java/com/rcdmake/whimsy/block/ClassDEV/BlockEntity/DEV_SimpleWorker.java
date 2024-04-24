package com.rcdmake.whimsy.block.ClassDEV.BlockEntity;

import com.mojang.serialization.MapCodec;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.Entity.DEV_SimpleWorker_E;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DEV_SimpleWorker extends BlockWithEntity implements BlockEntityProvider {
    protected DEV_SimpleWorker(Settings settings) {
        super(settings);
    }

    // 实现抽象方法（自动生成）
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    // 创建方块实体
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DEV_SimpleWorker_E(pos, state);
    }

    // 创建方块实体形状，此处为完整方块
    public static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    // 定义方块外选框的形状
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // 定义方块渲染模式
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // 方块实体被替换时行为（如掉落物品等）
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onStateReplaced(state, world, pos, newState, isMoving);
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof DEV_SimpleWorker_E) {
                ItemScatterer.spawn(world, pos, (DEV_SimpleWorker_E)blockEntity);
                world.updateComparators(pos, this);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){

        // 执行成功
        return ActionResult.SUCCESS;
    }

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
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, DEV_SimpleWorker_E.DEV_SIMPLE_WORKER_ENTITY,
                // 存疑
                (world1, pos, state1, blockEntity) -> ((DEV_SimpleWorker_E) blockEntity).tick(world1, pos, state1));
    }

    // 创建方块
    public static final Block DEV_SIMPLE_WORKER = new DEV_SimpleWorker(FabricBlockSettings.copyOf(Blocks.STONE));

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_simple_worker", DEV_SIMPLE_WORKER);
    }
}
