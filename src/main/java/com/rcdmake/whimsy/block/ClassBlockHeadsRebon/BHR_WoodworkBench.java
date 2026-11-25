package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_WorkBenchLevel;
import com.rcdmake.whimsy.surface.WorkingSurfaceBlock;
import com.rcdmake.whimsy.surface.WorkingSurfaceEntity;
import com.rcdmake.whimsy.surface.WorkingSurfaceMenu;
import com.rcdmake.whimsy.surface.WorkingSurfaceSpec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 木工台（{@code bhr_woodwork_bench}）—— 工作面之一，制作台的「木工版」。
 *
 * <p>The Blockheads 的 Woodwork Bench：与 Workbench 逻辑完全相同（同样的等级、同样的进度条、
 * 同样的配方栏），只是<b>配方表不同</b>、且是木制品（徒手可挖、木质音效）。
 *
 * <p>本类只声明差异，其余全部继承自 {@code WorkingSurfaceBlock} 与 {@code surface} 包：
 * <ul>
 *   <li>注册名 {@code bhr_woodwork_bench}；等级 <b>1~3</b>（目前只有 L1 配方，L2 / L3 配方以后追加）；</li>
 *   <li>无燃料；底图复用 {@code workbench.png}；面板不右移；</li>
 *   <li>外观只有一张模型（{@code block/bhr_woodwork_bench}，顶面 + 侧面两张贴图），
 *       等级目前不改外观 —— blockstate 用 catch-all 单变体覆盖全部等级值；</li>
 *   <li>配方目录 {@code data/richards-whimsy-mod/bhr_recipe/bhr_woodwork_bench/}。</li>
 * </ul>
 */
public class BHR_WoodworkBench extends WorkingSurfaceBlock {

	/** 注册路径。 */
	public static final String REGISTRY_NAME = "bhr_woodwork_bench";

	/** 等级上限：Blockheads 的 Woodwork Bench 最高 3 级。 */
	public static final int LEVEL_MAX = 3;

	/** 本机规格：3 级、无燃料、workbench 底图、面板不右移。 */
	public static final WorkingSurfaceSpec SPEC = new WorkingSurfaceSpec(
			REGISTRY_NAME,
			"textures/gui/container/workbench.png",
			LEVEL_MAX,
			0, 0, 0,
			0,
			null);

	/** 等级状态属性（与其它工作面共用同一个属性对象）。 */
	public static final IntegerProperty LEVEL = WorkingSurfaceBlock.LEVEL;

	/** 方块与方块物品。 */
	public static Block BHR_WOODWORK_BENCH;
	public static BlockItem BHR_WOODWORK_BENCH_ITEM;

	/** 属性：复制原版工作台，改成「瞬间破坏 + 木质音效」（Blockheads 里木工台是木制品，徒手可挖）。 */
	public static BlockBehaviour.Properties woodworkBenchProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
				.instabreak()
				.sound(SoundType.WOOD);
	}

	public BHR_WoodworkBench(BlockBehaviour.Properties properties) {
		super(properties, SPEC);
	}

	/** 注册：方块 → 方块物品 → 方块实体类型 → 菜单类型。 */
	public static void OnInit() {
		BHR_WOODWORK_BENCH = ModRegister.registerBlock(REGISTRY_NAME,
				new BHR_WoodworkBench(woodworkBenchProperties().setId(ModRegister.blockKey(REGISTRY_NAME))));
		BHR_WOODWORK_BENCH_ITEM = ModRegister.register(REGISTRY_NAME,
				new BlockItem(BHR_WOODWORK_BENCH, new Item.Properties()
						.setId(ModRegister.itemKey(REGISTRY_NAME))
						.component(BHR_WorkBenchLevel.component(),
								new BHR_WorkBenchLevel.Level(WorkingSurfaceSpec.LEVEL_MIN))));

		WorkingSurfaceEntity.registerType(REGISTRY_NAME, BHR_WOODWORK_BENCH);
		WorkingSurfaceMenu.register(SPEC);
	}
}
