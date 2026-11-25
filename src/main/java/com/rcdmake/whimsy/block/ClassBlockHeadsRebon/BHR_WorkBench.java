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
 * 制作台（{@code bhr_workbench}）—— 方块愚者（BHR）模块的<b>工作面</b>之一。
 *
 * <p>自 2026-12 起「工作面」的公共逻辑（等级状态、掉落、开界面、配方选择、进度、存档、菜单、界面）
 * 全部收在 {@code com.rcdmake.whimsy.surface} 包里，本类只声明差异：
 * <ul>
 *   <li>注册名 {@code bhr_workbench}，等级 <b>1~29</b>；</li>
 *   <li>无燃料；底图 {@code workbench.png}；面板不右移；</li>
 *   <li>侧面贴图 29 套、顶底面按 1~8 / 9~17 / 18~26 / 27~29 四档共用；</li>
 *   <li>音效按同一套档位分四档（泥坯 / 石头 / 铜块 / 铁块），见 {@link #getSoundType}。</li>
 * </ul>
 */
public class BHR_WorkBench extends WorkingSurfaceBlock {

	/** 注册路径（方块与它的方块物品同名，分属两个注册表）。 */
	public static final String REGISTRY_NAME = "bhr_workbench";

	/** 本机规格：29 级、无燃料、workbench 底图、面板不右移。 */
	public static final WorkingSurfaceSpec SPEC = new WorkingSurfaceSpec(
			REGISTRY_NAME,
			"textures/gui/container/workbench.png",
			WorkingSurfaceSpec.LEVEL_ABS_MAX,
			0, 0, 0,
			0,
			null);

	/** 等级下限上限（兼容老引用）。 */
	public static final int LEVEL_MIN = WorkingSurfaceSpec.LEVEL_MIN;
	public static final int LEVEL_MAX = WorkingSurfaceSpec.LEVEL_ABS_MAX;

	/** 等级状态属性（与其它工作面共用同一个属性对象）。 */
	public static final IntegerProperty LEVEL = WorkingSurfaceBlock.LEVEL;

	/** 侧 / 顶贴图与音效共用的档位边界。 */
	private static final int BAND_1_END = 8;
	private static final int BAND_9_END = 17;
	private static final int BAND_18_END = 26;

	/** 方块本体（在 {@link #OnInit()} 里赋值）。 */
	public static Block BHR_WORK_BENCH;

	/** 方块物品（掉落、选取方块与创造栏都要它的实例）。 */
	public static BlockItem BHR_WORK_BENCH_ITEM;

	/** 属性：复制原版工作台，改成「瞬间破坏」+ 默认泥坯音效（真实音效按等级分四档）。 */
	public static BlockBehaviour.Properties workBenchProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
				.instabreak()
				.sound(SoundType.PACKED_MUD);
	}

	public BHR_WorkBench(BlockBehaviour.Properties properties) {
		super(properties, SPEC);
	}

	/** 声音组：按等级分四档（档位边界与顶底面贴图的共用边界一致）。 */
	@Override
	protected SoundType getSoundType(net.minecraft.world.level.block.state.BlockState state) {
		return soundFor(state.getValue(LEVEL));
	}

	/** 注册（顺序即依赖顺序）：组件 → 方块 → 方块物品 → 方块实体类型 → 菜单类型。 */
	public static void OnInit() {
		// 组件必须先注册：物品在被创建时就可能读它（掉落 / 放置 / 创造栏）
		BHR_WorkBenchLevel.OnInit();

		BHR_WORK_BENCH = new BHR_WorkBench(workBenchProperties().setId(ModRegister.blockKey(REGISTRY_NAME)));
		ModRegister.registerBlock(REGISTRY_NAME, BHR_WORK_BENCH);
		// 方块物品带一个**原型组件**（1 级）：任何新造出来的物品堆都读得到等级
		BHR_WORK_BENCH_ITEM = ModRegister.register(REGISTRY_NAME,
				new BlockItem(BHR_WORK_BENCH, new Item.Properties()
						.setId(ModRegister.itemKey(REGISTRY_NAME))
						.component(BHR_WorkBenchLevel.component(), new BHR_WorkBenchLevel.Level(LEVEL_MIN))));

		WorkingSurfaceEntity.registerType(REGISTRY_NAME, BHR_WORK_BENCH);
		WorkingSurfaceMenu.register(SPEC);
	}

	/** 等级 → 档位（1 / 9 / 18 / 27）：顶底面贴图与音效共用。 */
	public static int band(int level) {
		if (level <= BAND_1_END) {
			return 1;
		}
		if (level <= BAND_9_END) {
			return 9;
		}
		if (level <= BAND_18_END) {
			return 18;
		}
		return 27;
	}

	/** 等级 → 声音组。 */
	public static SoundType soundFor(int level) {
		return switch (band(level)) {
			case 1 -> SoundType.PACKED_MUD;
			case 9 -> SoundType.STONE;
			case 18 -> SoundType.COPPER;
			default -> SoundType.IRON;
		};
	}
}
