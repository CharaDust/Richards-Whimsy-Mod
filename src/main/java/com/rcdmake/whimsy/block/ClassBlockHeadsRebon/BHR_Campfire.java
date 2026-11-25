package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_WorkBenchLevel;
import com.rcdmake.whimsy.surface.FuelOption;
import com.rcdmake.whimsy.surface.WorkingSurfaceBlock;
import com.rcdmake.whimsy.surface.WorkingSurfaceEntity;
import com.rcdmake.whimsy.surface.WorkingSurfaceMenu;
import com.rcdmake.whimsy.surface.WorkingSurfaceSpec;
import com.rcdmake.whimsy.surface.WorkingSurfaceStateSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * 简易篝火（{@code bhr_campfire}）—— 工作面之一，也是<b>唯一烧燃料</b>的那一台。
 *
 * <p>公共逻辑（等级、掉落、开界面、配方、进度、存档）继承自 {@code WorkingSurfaceBlock}；
 * 本类只声明：
 * <ul>
 *   <li>等级 <b>1~2</b>；底图 {@code fuel_workbench.png}；面板右移 18px；三档燃料；</li>
 *   <li>外观状态 {@code lit}：燃料 &gt; 2 单位时点亮（与制作状态无关）；</li>
 *   <li>0.25 格高的碰撞箱（轮廓仍是整格，否则上层像素点不到、界面开不了）；</li>
 *   <li><b>用随机刻代替每 tick ticker</b>：结算燃料区间、把 {@code lit} 状态跟上。</li>
 * </ul>
 *
 * <h2>为什么不再有 getTicker</h2>
 * <p>进度的「缺燃料暂停」现在由实体的<b>区间结算</b>表达（见 {@code WorkingSurfaceEntity} 类注释），
 * 时间轴是解析的，所以每 tick 推进不再必要。方块侧只需要一个低频心跳：
 * 随机刻里做一次 O(1) 结算 + 外观同步。打开界面时会立即结算一次（用户要求）。
 */
public class BHR_Campfire extends WorkingSurfaceBlock implements WorkingSurfaceStateSync {

	/** 注册路径。 */
	public static final String REGISTRY_NAME = "bhr_campfire";

	/** 等级下限 / 上限。 */
	public static final int LEVEL_MIN = WorkingSurfaceSpec.LEVEL_MIN;
	public static final int LEVEL_MAX = 2;

	/** 单位燃烧时间（秒）—— 木棍 1 单位、松果 2 单位、木板 5 单位。 */
	public static final int UNIT_BURN_SECONDS = 15;

	/** 单位燃烧时间（tick）。 */
	public static final int UNIT_BURN_TICKS = UNIT_BURN_SECONDS * 20;

	/** 燃料上限 = 10 倍单位燃烧时间。 */
	public static final int MAX_FUEL_UNITS = 10;

	/** 燃料上限（tick）。 */
	public static final int MAX_FUEL_TICKS = UNIT_BURN_TICKS * MAX_FUEL_UNITS;

	/** 贴图切到「熄灭」的燃料阈值（&gt; 2 单位才亮）。 */
	public static final int LIT_MIN_FUEL_UNITS = 2;

	/** 阈值（tick）。 */
	public static final int LIT_MIN_FUEL_TICKS = LIT_MIN_FUEL_UNITS * UNIT_BURN_TICKS;

	/** 燃料档位数（= 界面上的燃料示意槽数量）。 */
	public static final int FUEL_COUNT = 3;

	/** 本机规格：2 级、3 档燃料、fuel_workbench 底图、面板右移 18。 */
	public static final WorkingSurfaceSpec SPEC = new WorkingSurfaceSpec(
			REGISTRY_NAME,
			"textures/gui/container/fuel_workbench.png",
			LEVEL_MAX,
			FUEL_COUNT,
			UNIT_BURN_TICKS,
			MAX_FUEL_TICKS,
			18,
			BHR_Campfire::fuelList);

	/** 等级状态属性（与制作台共用同一个属性对象）。 */
	public static final IntegerProperty LEVEL = WorkingSurfaceBlock.LEVEL;

	/** 是否在燃烧（= 燃料充足）。原版的同名字段，便于与原版工具链互通。 */
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	/** 0.25 格高的碰撞箱。 */
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

	/** 松果的注册名（燃料表第 2 档）。 */
	private static final String PINE_CONE_ID = "bhr_pine_cone";

	/** 方块与方块物品。 */
	public static Block BHR_CAMPFIRE;
	public static BlockItem BHR_CAMPFIRE_ITEM;

	/** 懒构建的燃料表。 */
	private static List<FuelOption> FUELS;

	/** 属性：复制原版营火，改成「矮一点、不遮挡、有火就发光」。 */
	public static BlockBehaviour.Properties campfireProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
				.noOcclusion()
				.randomTicks()
				.lightLevel(state -> state.getValue(LIT) ? 15 : 0)
				.strength(0.5F)
				.sound(SoundType.WOOD);
	}

	public BHR_Campfire(BlockBehaviour.Properties properties) {
		super(properties, SPEC);
		this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
	}

	/**
	 * 燃料表：木棍 1 单位 / 松果 2 单位 / 木板 5 单位（木板档额外挂 {@code #minecraft:planks} 标签兜底）。
	 *
	 * <p>⚠️ 必须懒构建：松果在物品区才注册，晚于本类的 {@code OnInit()}。
	 */
	public static List<FuelOption> fuelList() {
		if (FUELS == null) {
			Item pineCone = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(ModRegister.id(PINE_CONE_ID));
			FUELS = List.of(
					new FuelOption(List.of(Items.STICK), 1),
					new FuelOption(List.of(java.util.Objects.requireNonNull(
							pineCone, "找不到松果（" + PINE_CONE_ID + "）：燃料表必须在物品注册之后才构建")), 2),
					new FuelOption(List.of(
							Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
							Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS,
							Items.BAMBOO_PLANKS, Items.CRIMSON_PLANKS, Items.WARPED_PLANKS), 5,
							ItemTags.PLANKS));
			if (FUELS.size() != FUEL_COUNT) {
				throw new IllegalStateException("燃料档位数不一致：FUEL_COUNT=" + FUEL_COUNT
						+ " / fuelList()=" + FUELS.size());
			}
		}
		return FUELS;
	}

	/** 第 {@code index} 档燃料；越界返回 {@code null}。 */
	public static FuelOption fuel(int index) {
		return SPEC.fuel(index);
	}

	/** 注册：方块 → 方块物品 → 方块实体类型 → 菜单类型。 */
	public static void OnInit() {
		BHR_CAMPFIRE = ModRegister.registerBlock(REGISTRY_NAME,
				new BHR_Campfire(campfireProperties().setId(ModRegister.blockKey(REGISTRY_NAME))));
		BHR_CAMPFIRE_ITEM = ModRegister.register(REGISTRY_NAME,
				new BlockItem(BHR_CAMPFIRE, new Item.Properties()
						.setId(ModRegister.itemKey(REGISTRY_NAME))
						.component(BHR_WorkBenchLevel.component(), new BHR_WorkBenchLevel.Level(LEVEL_MIN))));

		WorkingSurfaceEntity.registerType(REGISTRY_NAME, BHR_CAMPFIRE);
		WorkingSurfaceMenu.register(SPEC);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, LEVEL);
	}

	@Override
	public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(LIT, false);
	}

	// ── 心跳：随机刻 ──

	/**
	 * 随机刻：结算燃料区间 + 刷 {@code lit}。
	 *
	 * <p>这是本方块<b>唯一</b>的周期工作，一次只做几个整数比较，不随 tick 频率跑。
	 * 界面打开时由菜单的 {@code broadcastChanges} 立即结算，不依赖这条路径。
	 */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.randomTick(state, level, pos, random);
		if (level.getBlockEntity(pos) instanceof WorkingSurfaceEntity surface) {
			surface.pulse();
		}
	}

	/** 实体状态变化时把 {@code lit} 跟上（只在真的需要翻转时写世界）。 */
	@Override
	public void onSurfaceStateChanged(Level level, BlockPos pos, BlockState state, WorkingSurfaceEntity entity) {
		if (level.isClientSide()) {
			return;
		}
		boolean shouldLight = entity.fuelTicksRemaining(level.getGameTime()) > LIT_MIN_FUEL_TICKS;
		if (state.getValue(LIT) != shouldLight) {
			level.setBlockAndUpdate(pos, state.setValue(LIT, shouldLight));
		}
	}

	// ── 形状 ──

	/** 碰撞形状 0.25 格高；轮廓保持父类的整格（否则上层 12px 点不到，界面打不开）。 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return SHAPE;
	}
}
