package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static net.minecraft.state.property.Properties.FACING;
import static net.minecraft.state.property.Properties.BLOCK_FACE;

public class BHR_Gems extends Block {
    // super
    public BHR_Gems(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(BLOCK_FACE, BlockFace.WALL)
                .with(FACING, Direction.NORTH)
        );
    }

    // 创建方块，使用构造函数类型
    public static final Block BHR_GEM_AMETHYST = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .luminance(state -> 5)
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );
    public static final Block BHR_GEM_SAPPHIRE = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .luminance(state -> 7)
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );
    public static final Block BHR_GEM_EMERALD = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .luminance(state -> 9)
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );
    public static final Block BHR_GEM_RUBY = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .luminance(state -> 11)
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );
    public static final Block BHR_GEM_DIAMOND = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .luminance(state -> 13)
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );

    // 覆盖 appendProperties 并加入 BLOCK_FACE FACING 属性：
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_FACE, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction dir = ctx.getSide();
        BlockFace blockFace;
        Direction facing = ctx.getPlayerLookDirection().getOpposite();

        if (dir == Direction.UP) {
            blockFace = BlockFace.FLOOR;
        } else if (dir == Direction.DOWN) {
            blockFace = BlockFace.CEILING;
        } else {
            blockFace = BlockFace.WALL;
            facing = dir.getOpposite();
        }
        if (facing == Direction.UP) {
            facing = Direction.NORTH;
        }
        else if (facing == Direction.DOWN) {
            facing = Direction.SOUTH;
        }

        return this.getDefaultState().with(BLOCK_FACE, blockFace).with(FACING, facing);
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void OnInit() {
        register_Block_and_BlockItem("bhr_gem_amethyst", BHR_GEM_AMETHYST);
        register_Block_and_BlockItem("bhr_gem_sapphire", BHR_GEM_SAPPHIRE);
        register_Block_and_BlockItem("bhr_gem_emerald", BHR_GEM_EMERALD);
        register_Block_and_BlockItem("bhr_gem_ruby", BHR_GEM_RUBY);
        register_Block_and_BlockItem("bhr_gem_diamond", BHR_GEM_DIAMOND);
    }
}
