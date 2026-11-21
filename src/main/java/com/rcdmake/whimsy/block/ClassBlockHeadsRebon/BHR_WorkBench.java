package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_WorkBenchLevel;
import com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_WorkBenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 制作台（{@code bhr_workbench}）—— 方块愚者（BHR）模块的合成方块（工作台类）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_WorkBench.md}）：
 * <ul>
 *   <li>父级：工作台（{@code minecraft:crafting_table}）；</li>
 *   <li><b>29 个等级</b>（1~29），每级一套<b>侧面</b>贴图（{@code workbench_side_1} ~ {@code workbench_side_29}）；</li>
 *   <li>顶面 / 底面贴图按档位共用：1~8 → {@code workbench_end_1}、9~17 → {@code workbench_end_9}、
 *       18~26 → {@code workbench_end_18}、27~29 → {@code workbench_end_27}；</li>
 *   <li>挖取时<b>瞬间破坏</b>（硬度 0，无需工具、不需要正确工具），掉落物<b>保留当前等级</b>；</li>
 *   <li>相同等级的制作台可以堆叠，不同等级不可堆叠；</li>
 *   <li>音效按同一套档位分组：1~8 泥坯（{@code PACKED_MUD}）、9~17 石头（{@code STONE}）、
 *       18~26 铜块（{@code COPPER}）、27~29 铁块（{@code IRON}）；</li>
 *   <li><b>右键打开界面</b>（2026-11 第一版，只做壳）：面板与大箱子同尺寸，标题是
 *       「制作台 - 等级 N」，「物品栏」在原位；<b>上半部分还不能存东西</b>，
 *       Shift 只在「快捷栏 ↔ 背包」之间搬运。见 {@link BHR_WorkBenchMenu}。</li>
 * </ul>
 *
 * <h2>等级为什么是「方块状态」而不是方块实体的字段</h2>
 * <p>等级同时决定<b>贴图</b>（模型要按变体选）与<b>音效</b>（{@code getSoundType(BlockState)} 只有状态参数），
 * 这两件都发生在「引擎按方块状态查表」的路径上，方块实体不参与。所以等级落在整型属性
 * {@link #LEVEL}（1~29，序列化名 {@code level}）上；方块实体
 * {@link BHR_WorkBenchEntity} 只是读它。等级随区块存档，物品侧另用组件
 * {@link BHR_WorkBenchLevel}（{@code richards-whimsy-mod:workbench_level}）携带，
 * 于是「挖下来 → 再放下」是同一个数，且不同等级天然是不同物品堆（原版堆叠规则白送）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_workbench}　<b>模块</b> BHR　<b>父级</b> 工作台</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@link #LEVEL}）、层 8 状态模式 / 放置、
 *       层 9 掉落与选取、层 10 声音组、层 12 额外注册表（方块实体类型）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（1 / 29 直接写在属性声明里）、层 6 家族（不需要切割类机制）、
 *       层 7 codec（{@code Block} 已实现）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在<b>第 3 级</b>「覆写本类方法」——
 *       等级贴图 / 音效 / 掉落三件都能在本类内解决（层 8~10 覆写 + 数据驱动制品），
 *       没有一处需要事件或 Mixin。**没有**停在第 2 级是因为第 2 级（纯属性声明）一次只能给
 *       一个 {@code Properties}，而这里要按状态分四档音效、按状态换 29 套模型。</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()}
 *       里声明（29 个状态，{@code blockstates/bhr_workbench.json} 手写）　③☑
 *       {@code ItemModelGenerator.COMPONENT_SELECT_ITEMS}（物品图标按组件值选 29 套模型中的一套，
 *       见 {@link BHR_WorkBenchLevel} 的「图标」一节）　④☑ 中英
 *       {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}　⑥☑ 贴图
 *       {@code block/bhr/workbench_side_1..29} + {@code workbench_end_{1,9,18,27}} /
 *       模型 {@code models/block/bhr_workbench_1..29}　⑦☐ 配方与战利品表 ——
 *       掉落由 {@link #getDrops} 直接给出（要按等级写组件，战利品表做不到），配方属于「获取方式」阶段</li>
 * </ul>
 */
public class BHR_WorkBench extends Block implements EntityBlock {

	// ── 层 1 ｜ 规格常量 ──

	/** 注册路径（方块与它的方块物品同名，分属两个注册表）。 */
	public static final String REGISTRY_NAME = "bhr_workbench";

	/** 等级下限。 */
	public static final int LEVEL_MIN = BHR_WorkBenchLevel.MIN_LEVEL;

	/** 等级上限。 */
	public static final int LEVEL_MAX = BHR_WorkBenchLevel.MAX_LEVEL;

	/** 侧 / 顶贴图与音效共用的档位边界（见类注释的档位表）。 */
	private static final int BAND_1_END = 8;
	private static final int BAND_9_END = 17;
	private static final int BAND_18_END = 26;

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_WORK_BENCH;

	/** 方块物品（层 2）：掉落、选取方块与创造栏都要它的实例，所以单独留句柄。 */
	public static BlockItem BHR_WORK_BENCH_ITEM;

	// ── 层 3 ｜ 状态属性字段 ──

	/** 状态属性字段（层 3）：整数 {@code level}，1~29 —— 29 个方块状态，一个状态一套模型。 */
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", LEVEL_MIN, LEVEL_MAX);

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/**
	 * 制作台的属性（层 4）。
	 *
	 * <p>复制原版工作台（地图颜色、阻挡 / 窒息这些布尔值都跟着走），然后：
	 * <ul>
	 *   <li>{@code instabreak()} —— 硬度 0 + 不需要正确工具，也就是「挖取是瞬间的」；</li>
	 *   <li>{@code sound(SoundType.PACKED_MUD)} —— 只是 1 级的默认值；四档音效的真正来源是
	 *       {@link #getSoundType(BlockState)}（属性里的 {@code soundType} 是按方块给的，不是按状态）。</li>
	 * </ul>
	 */
	public static BlockBehaviour.Properties workBenchProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
				.instabreak()
				.sound(SoundType.PACKED_MUD);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/** 构造器（层 5）：装配 + 给状态初值（1 级）。 */
	public BHR_WorkBench(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LEVEL, LEVEL_MIN));
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/** 状态模式（层 8）：声明本方块有 {@code level} 这一个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	/**
	 * 放置时定等级（层 8）：读手上那一堆物品的等级组件。
	 *
	 * <p>没有组件（新合成的 1 级、或非物品来源，如结构方块 / 指令）时按 1 级处理 ——
	 * 夹取逻辑在 {@link BHR_WorkBenchLevel#levelOf}，越界值不会让
	 * {@code setValue} 抛异常。
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(LEVEL, BHR_WorkBenchLevel.levelOf(context.getItemInHand()));
	}

	/**
	 * 方块实体工厂（层 8）：实现 {@link EntityBlock} 的唯一强制方法。
	 * <p>返回值不能为 {@code null}，否则放下方块不会有实体。
	 */
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BHR_WorkBenchEntity(pos, state);
	}

	// ── 层 9 ｜ 交互与判定 ──

	/**
	 * 右键打开界面（层 9）。
	 *
	 * <p>26.2 的交互入口是 {@code useItemOn} / {@code useWithoutItem} 两个方法：
	 * 父类的 {@code useItemOn} 对普通方块返回 {@code TRY_WITH_EMPTY_HAND}，
	 * 于是「手里拿着东西右键」会落到 {@code useWithoutItem}；只覆写这一个就够，
	 * 也顺带避免手持方块时把方块摆上去（与原版箱子的行为一致）。
	 *
	 * <p>界面本身在 {@link BHR_WorkBenchMenu}（菜单）与
	 * {@code BHR_WorkBenchScreen}（客户端界面）里。标题带上当前等级，
	 * 由 {@link #titleFor} 生成后随打开界面的数据包发到客户端。
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			int workBenchLevel = state.getValue(LEVEL);
			BHR_WorkBenchEntity entity = level.getBlockEntity(pos) instanceof BHR_WorkBenchEntity workBench
					? workBench
					: null;
			serverPlayer.openMenu(new SimpleMenuProvider(
					(containerId, inventory, ignored) -> new BHR_WorkBenchMenu(containerId, inventory,
							ContainerLevelAccess.create(level, pos), entity),
					titleFor(workBenchLevel)));
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * 方块实体 tick（层 8）：制作进度每 tick 推进一次。
	 *
	 * <p>只服务端要 tick；客户端的进度靠菜单数据槽同步，所以客户端直接返回 null。
	 * <p>签名必须保留 {@code <T extends BlockEntity>} 的泛型（父接口就是这么声明的），
	 * 所以这里转型到本方块实体 —— 方块实体类型注册时已经保证只有本类。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends net.minecraft.world.level.block.entity.BlockEntity>
			net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
					Level level, BlockState state,
					net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return (net.minecraft.world.level.block.entity.BlockEntityTicker<T>)
				(net.minecraft.world.level.block.entity.BlockEntityTicker<BHR_WorkBenchEntity>) BHR_WorkBenchEntity::tick;
	}

	/**
	 * 掉落（层 9）：<b>一个</b>带等级的方块物品。
	 *
	 * <p>为什么用覆写而不用战利品表：等级来自方块状态，而 {@code minecraft:set_components}
	 * 只能写死常量 —— 要覆盖 29 个等级就得写 29 条「状态 → 固定组件」的战利品表条目；
	 * 这里一行 {@link #createStack} 就够，且爆炸 / 其它掉落路径也一并覆盖
	 * （它们走的是同一条 {@code getDrops}）。
	 *
	 * <p>返回的是<b>可变列表</b>：原版与部分模组的掉落处理会往里加东西。
	 */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = new ArrayList<>(1);
		drops.add(createStack(state.getValue(LEVEL)));
		return drops;
	}

	/**
	 * 创造模式「选取方块」（层 9）：鼠标中键取到的是<b>这一级</b>的制作台，而不是 1 级。
	 */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return createStack(state.getValue(LEVEL));
	}

	// ── 层 10 ｜ 形状与外观 ──

	/**
	 * 声音组（层 10）：按等级分四档（1~8 泥土 / 9~17 石头 / 18~26 铜块 / 27~29 铁块）。
	 *
	 * <p>破坏 / 踩踏 / 放置 / 打击 / 落地五种音效都走这里，所以四档一次覆盖全部。
	 * 档位边界与顶面 / 底面贴图的共用边界<b>故意一致</b>（同一张 {@link #band} 表）。
	 */
	@Override
	protected SoundType getSoundType(BlockState state) {
		return soundFor(state.getValue(LEVEL));
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）：组件 → 方块 → 方块物品 → 方块实体类型，顺序即依赖顺序。 */
	public static void OnInit() {
		// 组件必须先注册：物品在被创建时就可能读它（掉落 / 放置 / 创造栏）
		BHR_WorkBenchLevel.OnInit();

		BHR_WORK_BENCH = new BHR_WorkBench(workBenchProperties()
				.setId(ModRegister.blockKey(REGISTRY_NAME)));
		ModRegister.registerBlock(REGISTRY_NAME, BHR_WORK_BENCH);
		// 方块物品带一个**原型组件**（1 级）：于是任何新造出来的物品堆都读得到等级 ——
		// 图标按等级选模型、描述栏显示「等级：1」这两件事对 1 级同样生效，
		// 而 2~29 级才是补丁里的值（详见 BHR_WorkBenchLevel 的类注释）。
		BHR_WORK_BENCH_ITEM = ModRegister.register(REGISTRY_NAME,
				new BlockItem(BHR_WORK_BENCH, new Item.Properties()
						.setId(ModRegister.itemKey(REGISTRY_NAME))
						.component(BHR_WorkBenchLevel.component(), new BHR_WorkBenchLevel.Level(LEVEL_MIN))));

		// 方块实体类型要求方块已经在注册表里（isValid 比对的就是注册后的实例）
		BHR_WorkBenchEntity.OnInit(BHR_WORK_BENCH);

		// 界面：菜单类型（打开界面的数据包要写它的注册名，所以必须早于任何一次右键）
		BHR_WorkBenchMenu.OnInit();
	}

	// ── 共享助手（供本类与掉落 / 选取 / 创造栏使用）──

	/** 界面标题的语言键（{@code %s} 是等级）：形如「制作台 - 等级 1」。 */
	public static final String TITLE_KEY = "container." + "richards-whimsy-mod." + REGISTRY_NAME;

	/** 生成界面标题（标题随「打开界面」的数据包发到客户端，所以用可翻译组件 + 等级参数）。 */
	public static Component titleFor(int level) {
		return Component.translatable(TITLE_KEY, level);
	}

	/**
	 * 造一个指定等级的制作台物品堆（{@link #getDrops} 与选取方块共用）。
	 *
	 * @param level 等级，调用方保证已夹到 {@link #LEVEL_MIN}~{@link #LEVEL_MAX}（方块状态取出的值天然合法）
	 */
	public static ItemStack createStack(int level) {
		return BHR_WorkBenchLevel.set(new ItemStack(BHR_WORK_BENCH_ITEM), level);
	}

	/**
	 * 等级 → 档位（1 / 9 / 18 / 27）。
	 *
	 * <p>这张表同时是<b>顶面 / 底面贴图</b>的共用分组（{@code workbench_end_1 / _9 / _18 / _27}）
	 * 与<b>声音组</b>的分档依据 —— 贴图那侧体现在资源文件
	 * （{@code blockstates/bhr_workbench.json} 与 {@code models/block/bhr_workbench_N.json}），
	 * 代码里只有音效真正用到本方法。
	 */
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

	/** 等级 → 声音组（见 {@link #getSoundType}）。 */
	public static SoundType soundFor(int level) {
		return switch (band(level)) {
			case 1 -> SoundType.PACKED_MUD;
			case 9 -> SoundType.STONE;
			case 18 -> SoundType.COPPER;
			default -> SoundType.IRON;
		};
	}
}
