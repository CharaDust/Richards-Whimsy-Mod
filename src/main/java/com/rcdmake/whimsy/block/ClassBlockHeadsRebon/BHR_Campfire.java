package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_WorkBenchLevel;
import com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_CampfireMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易篝火（{@code bhr_campfire}）—— BHR 模块的第二台制作台，也是第一台<b>带燃料</b>的制作台。
 *
 * <h2>外观规格（需求原文逐条落地）</h2>
 * <ul>
 *   <li><b>井字 + X 字同时显示</b>：模型是手写的复合模型（不是某个原版父级），把两套平面拼在一个文件里 ——
 *       <b>井</b>（原版 {@code block/crop} 的四片竖直平面，x=4 / x=12 / z=4 / z=12，小麦那套）
 *       <b>+ X</b>（原版 {@code block/cross} 的两片 45° 斜插平面，各绕 y 轴转 45°）；
 *       两张贴图 {@code block/bhr/campfire_full.png} 与 {@code campfire_empty.png} 都是 <b>16×128 = 8 帧</b>；</li>
 *   <li><b>动画速度 = 原版火</b>：原版 {@code fire_0.png.mcmeta} <b>不写 frametime</b>,
 *       即默认 {@code frametime = 1}（每游戏刻换一帧，20 fps）。本方块的两张图同样用
 *       {@code frametime: 1}，8 帧一轮 = 0.4 秒（证据：从 26.2 客户端 jar 里读出的
 *       {@code assets/minecraft/textures/block/fire_0.png.mcmeta}，其中只有 {@code frames} 重排，
 *       没有 {@code frametime} 键；对比 {@code campfire_fire.png.mcmeta} 是 {@code frametime: 2}）；</li>
 *   <li><b>0.25 格碰撞箱</b>：{@link #SHAPE} = {@code (0,0,0)→(16,4,16)}，即 4/16 高，
 *       站立面积仍是整格（需求写的是「0.25 格高」，不是「0.25 格小方块」）；</li>
 *   <li><b>物品栏是 2D 图像</b>：{@code items/bhr_campfire.json} 指向
 *       {@code models/item/bhr_campfire.json}（{@code parent: item/generated}，贴图
 *       {@code item/bhr/campfire.png}），<b>不</b>复用方块的复合模型 ——
 *       否则物品栏里会显示成 3D 交叉平面。</li>
 * </ul>
 *
 * <h2>两种状态贴图：只看燃料存量</h2>
 * <p>{@code lit=true} → {@code campfire_full}（发光 15 级）；{@code lit=false} → {@code campfire_empty}。
 * <p>⚠️ 判定依据是<b>燃料</b>，不是「是否正在制作」：{@code fuel > }{@link #LIT_MIN_FUEL_TICKS}
 * （2 个单位 = 600 tick）才用燃烧图，否则用熄灭图。状态由方块实体每 tick 依据燃料改写。
 *
 * <h2>燃料：以「单位燃烧时间」计价</h2>
 * <p>每台带燃料的制作台各自定义一个<b>单位燃烧时间</b>（本机 {@link #UNIT_BURN_SECONDS} = 15 秒），
 * 燃料表只写「一件顶几个单位」：木棍 <b>1</b>、松果 <b>2</b>、木板（任意）<b>5</b>。
 * 燃料槽最高存 {@link #MAX_FUEL_UNITS} 倍单位燃烧时间（10 单位 = 3000 tick）。
 * <p>这些值<b>完全由本方块决定</b>，与物品自带的燃料属性（{@code FuelValues} /
 * {@code minecraft:fuel} 组件）无关 —— 改这张表就改了这台机器的燃烧效率，不影响熔炉。
 * <p>界面上左侧那三个凹槽就是这张表的<b>示意</b>：点它就从玩家背包里抽走该物品并折算成燃料值
 * （见 {@link BHR_CampfireEntity#addFuel(Player, int, boolean)}）。
 * 木板一档有多个候选 → 与需求九宫格一样显示<b>替换标记</b>、<b>右键切换首选消耗</b>。
 *
 * <h2>战斗/交互</h2>
 * <p>右键开界面（{@link BHR_CampfireMenu} + {@code BHR_CampfireScreen}）。
 * 与制作台一致，只覆写 {@code useWithoutItem}：手持方块右键时会落到这里，不会把方块摆上去。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_campfire}　<b>模块</b> BHR　<b>父级</b> 营火（属性复制）</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@link #LIT} 火是否亮、{@link #LEVEL} 等级 1~2）、
 *       层 4 属性工厂、层 8 状态模式 / 放置（按物品组件还原等级）/ 方块实体 / <b>ticker</b>、
 *       层 9 交互与掉落（掉落带等级）、层 10 形状（0.25 碰撞箱）、
 *       层 12 额外注册表（方块实体类型 / 菜单类型）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（燃料表写在 {@link #FUELS}）、层 6 家族（不继承
 *       {@code CampfireBlock}：那套是「4 根木头 + 烤食物 + 水桶熄灭」的状态机，
 *       和这里的「制作台」逻辑没有一处重合）、层 7 codec（{@code Block} 已实现）、层 11 预留区</li>
 *   <li><b>能力阶梯</b>：停在<b>第 3 级</b>「覆写本类方法 + 数据驱动制品」—— 形状、掉落、右键三件在本类内解决</li>
 * </ul>
 */
public class BHR_Campfire extends Block implements EntityBlock {

	// ── 层 1 ｜ 规格常量 ──

	/** 注册路径（方块与方块物品同名）。 */
	public static final String REGISTRY_NAME = "bhr_campfire";

	/** 界面标题的语言键。 */
	public static final String TITLE_KEY = "container." + "richards-whimsy-mod." + REGISTRY_NAME;

	/** 这台机器的初始等级（新造出来 / 没带等级组件的篝火都是 1 级）。 */
	public static final int LEVEL_MIN = 1;

	/**
	 * 等级上限。
	 *
	 * <p>与制作台（1~29 级）不同，简易篝火只有 <b>2 级</b>：普通模式恒定 1 级，专家模式可以用
	 * 一条 {@code upgrade_to: 2} 的升级配方升到 2 级，解锁 4 条烹饪配方
	 * （解包数据：Clay ×10 + Iron Ingot ×3 + Tin Foil ×5，180 秒）。
	 */
	public static final int LEVEL_MAX = 2;

	/**
	 * 等级方块状态（序列化名 {@code level}）。
	 *
	 * <p>与制作台同一个做法：等级放<b>方块状态</b>而不是方块实体字段 ——
	 * 这样 {@link #getDrops} 能直接从 {@code state} 里读出等级写进物品组件，
	 * 不必再去 loot 参数里捞方块实体。
	 * <p>两级共用同一套模型（{@code bhr_campfire_lit} / {@code _unlit}），所以
	 * {@code blockstates/bhr_campfire.json} 里只是把 4 个组合都指向同样两个模型。
	 */
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", LEVEL_MIN, LEVEL_MAX);

	/**
	 * <b>单位燃烧时间</b>（秒）—— 每台带燃料的制作台各自定义一份，这是简易篝火的 15 秒。
	 *
	 * <p>燃料值一律用它做量纲：木棍 1 单位、松果 2 单位、木板 5 单位。
	 * 以后新加一台带燃料的机器，照抄这一节常量（换成自己的秒数）即可，
	 * {@link #fuels()} 里的「单位数」不用改 —— 换机器就是换「一刻度值多少 tick」。
	 */
	public static final int UNIT_BURN_SECONDS = 15;

	/** 单位燃烧时间（tick）= 15 s × 20 = 300。 */
	public static final int UNIT_BURN_TICKS = UNIT_BURN_SECONDS * 20;

	/** 燃料上限 = <b>10 倍</b>单位燃烧时间。 */
	public static final int MAX_FUEL_UNITS = 10;

	/** 燃料上限（tick）= 10 × 300 = 3000（2.5 分钟）。 */
	public static final int MAX_FUEL_TICKS = UNIT_BURN_TICKS * MAX_FUEL_UNITS;

	/**
	 * 贴图切换到「熄灭」的燃料阈值：<b>燃料 &gt; 2 单位</b>显示 {@code campfire_full}（燃烧图），
	 * 小于等于 2 单位显示 {@code campfire_empty}。
	 *
	 * <p>⚠️ 这个状态<b>只看燃料存量</b>，与「是否正在制作」无关 —— 燃料在界面外只会在制作时消耗，
	 * 所以有存货的篝火会一直亮着，直到掉到 2 单位以下。
	 */
	public static final int LIT_MIN_FUEL_UNITS = 2;

	/** 阈值（tick）= 2 × 300 = 600。{@code fuel > 它} 才用燃烧贴图。 */
	public static final int LIT_MIN_FUEL_TICKS = LIT_MIN_FUEL_UNITS * UNIT_BURN_TICKS;

	/**
	 * 0.25 格高（4/16）的碰撞箱与轮廓形状。
	 * <p>Footprint 仍是整格 16×16 —— 需求要的是「矮」，不是「小」。
	 */
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

	/**
	 * 燃料档位：一格 = 一类可烧物品（界面上左侧三个凹槽一一对应）。
	 *
	 * <p><b>为什么除了候选列表还要一个标签</b>（2026-11 补）：
	 * 本档的候选是<b>写死的物品清单</b>，而配方那边用的是标签
	 * （{@code #minecraft:planks}）—— 于是「果木木板」这种进标签的新木板，
	 * 合成认、添燃料不认。这里加一个可选的 {@link TagKey}，把标签成员<b>展开进候选</b>：
	 * 接受判定、图标、右键轮换全部走 {@link #allCandidates()}，与需求九宫格的
	 * {@code BHR_FoolIngredient#candidates()} 完全对称 —— 能烧的物品也一定轮得到。
	 * <p>显式清单仍然保留，因为它决定<b>顺序</b>（原版木板在前，后接标签里新增的），
	 * 右键轮换就按这个顺序走。
	 *
	 * @param candidates 显式候选（按顺序；也是标签展开的起点，见 {@link #allCandidates()}）
	 * @param units      每件折算的单位燃烧时间（1 单位 = {@link BHR_Campfire#UNIT_BURN_TICKS} tick）
	 * @param tag        额外接受的标签（展开进候选，{@code null} = 本档只看候选列表）
	 */
	public record Fuel(List<Item> candidates, int units, @org.jspecify.annotations.Nullable TagKey<Item> tag) {

		/**
		 * 「显式候选 + 标签展开」的缓存 —— 与 {@code BHR_FoolIngredient.CANDIDATE_CACHE} 同一套写法。
		 * <p>界面每帧都会问一次候选（画图标、数件数），展开标签要遍历物品注册表，不缓存会白烧 CPU。
		 * <p>⚠️ 与九宫格同一条已知限制：缓存跟着 {@code Fuel} 的值走，<b>数据包重载后不刷新</b>。
		 * 本模组的标签是 jar 内置的，正常游玩不会变；真要热改标签请重启。
		 */
		private static final java.util.Map<Fuel, List<Item>> CANDIDATE_CACHE =
				new java.util.concurrent.ConcurrentHashMap<>();

		/** 只要候选、不看标签的写法（木棍 / 松果两档用它）。 */
		public Fuel(List<Item> candidates, int units) {
			this(candidates, units, null);
		}

		/**
		 * 这一档能接受的全部具体物品：<b>先枚举候选、后标签展开</b>（去重），顺序稳定。
		 *
		 * <p>与需求九宫格的 {@code BHR_FoolIngredient#candidates()} 完全同一套语义 ——
		 * 界面据此画替换标记、右键在它们之间轮换首选。
		 * 所以「果木木板」这类只因标签而入档的物品，<b>也会进右键轮换</b>，不再是「能烧但选不中」。
		 */
		public List<Item> allCandidates() {
			return CANDIDATE_CACHE.computeIfAbsent(this, fuel -> {
				List<Item> result = new ArrayList<>(fuel.candidates);
				if (fuel.tag != null) {
					// 26.2 没有 Registry#getTag：遍历物品注册表挑出命中标签的（与九宫格同一写法）
					for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
						if (!result.contains(item)
								&& net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(item).is(fuel.tag)) {
							result.add(item);
						}
					}
				}
				return List.copyOf(result);
			});
		}

		/** 这一类里是否有多种可选物品（有 → 界面显示替换标记、右键可切换首选）。 */
		public boolean replaceable() {
			return this.allCandidates().size() > 1;
		}

		/** 这一档在实际消耗时能顶多少 tick。 */
		public int ticks() {
			return this.units * UNIT_BURN_TICKS;
		}

		/** 界面图标：{@code preferredIndex} 超出范围时退回第一个候选。 */
		public ItemStack icon(int preferredIndex) {
			List<Item> all = this.allCandidates();
			int index = preferredIndex >= 0 && preferredIndex < all.size() ? preferredIndex : 0;
			return new ItemStack(all.get(index));
		}

		/** 默认图标（第一个候选）。 */
		public ItemStack icon() {
			return this.icon(0);
		}

		/**
		 * 这一档是否收这件物品：等价于「它在 {@link #allCandidates()} 里」。
		 * <p>标签为 {@code null} 时退回纯候选判断（与加标签之前的行为完全一致）。
		 */
		public boolean accepts(ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			List<Item> all = this.allCandidates();
			for (Item candidate : all) {
				if (stack.is(candidate)) {
					return true;
				}
			}
			return false;
		}

		/** 某个具体物品在这一档里的下标（不在候选里返回 -1）。标签成员也会给正下标。 */
		public int indexOf(Item item) {
			return this.allCandidates().indexOf(item);
		}
	}

	/**
	 * 燃料档位数（= 界面上的燃料示意槽数量）。
	 * <p>是字面量而不是 {@code fuels().size()}：菜单里那几个槽位下标要当 {@code switch} 的 case 标签，
	 * 必须是编译期常量（见 {@code BHR_CampfireMenu.FUEL_SLOTS} 的注释），两者在 {@code OnInit()} 里对账。
	 */
	public static final int FUEL_COUNT = 3;

	/**
	 * 懒构建的燃料表（松果是本模组物品，它在主类的<b>物品区</b>才注册，晚于方块区）。
	 *
	 * <p>用注册表按 id 取物品而不是读 {@code BHR_PineCone.BHR_PINE_CONE} 静态字段：
	 * 两者运行时等价，但前者不引入「方块类初始化时那个字段还是 null」的隐患。
	 *
	 * <p>⚠️ 另一个实测过的坑：<b>{@code new ItemStack(item)} 在 {@code onInitialize()} 阶段会抛
	 * 「Components not bound yet」</b>（26.2 的物品组件 Holder 要等注册表冻结后才绑上）。
	 * 所以 {@link Fuel#icon()} 只能在运行时（界面绘制 / 点击）调用，绝不能在类初始化里预先造好 ItemStack。
	 */
	private static List<Fuel> FUELS;

	/** 松果的注册名（燃料表第 2 档）。 */
	private static final String PINE_CONE_ID = "bhr_pine_cone";

	/**
	 * 本方块支持的三种燃料（顺序 = 界面上从上到下三个凹槽）。
	 *
	 * <p><b>数值口径</b>：一律用「单位燃烧时间」的倍数表达 ——
	 * 木棍 1 单位、松果 2 单位、木板 5 单位；1 单位 = {@link #UNIT_BURN_TICKS}（15 秒）。
	 * 这些值<b>由本方块决定</b>，与物品自带的燃料属性（{@code FuelValues} / {@code minecraft:fuel} 组件）无关。
	 *
	 * <p>木板一档有 11 个候选（原版全部木板）→ {@link Fuel#replaceable()} 为真，
	 * 界面上会显示替换标记，右键可切换首选消耗，与需求九宫格完全同一套交互。
	 *
	 * <p><b>木板档还额外挂了 {@code #minecraft:planks} 标签</b>（{@link ItemTags#PLANKS}）：
	 * 只要物品进了这个标签就算燃料，哪怕它不在上面那 11 个候选里 ——
	 * 例如本模组的果木木板 {@code bhr_fruit_planks}，以及以后任何按标签接入的木板。
	 * 这正是「配方认标签、添燃料却不认」那个不一致的修法。
	 *
	 * <p>⚠️ 必须懒构建：{@link com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PineCone#BHR_PINE_CONE}
	 * 是在主类的<b>物品区</b>注册的，晚于方块区（本类 {@code OnInit()}）。静态字段直接引用它会在
	 * 类初始化时拿到 {@code null}。
	 */
	public static List<Fuel> fuels() {
		if (FUELS == null) {
			Item pineCone = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(ModRegister.id(PINE_CONE_ID));
			FUELS = List.of(
					new Fuel(List.of(Items.STICK), 1),
					new Fuel(List.of(java.util.Objects.requireNonNull(
							pineCone, "找不到松果（" + PINE_CONE_ID + "）：燃料表必须在物品注册之后才构建")), 2),
					new Fuel(List.of(
							Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
							Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS,
							Items.BAMBOO_PLANKS, Items.CRIMSON_PLANKS, Items.WARPED_PLANKS), 5,
							// 标签兜底：任何进了 #minecraft:planks 的物品都算这一档的燃料
							// （果木木板 / 以后新增的木板 / 其它模组的木板，不必再往上面那份清单里加）
							ItemTags.PLANKS));
			// 下标表（菜单里那些 switch case）用的是字面量 FUEL_COUNT，这里对账一次：
			// 加了一档却忘了改字面量的话，启动后第一次开界面就会立刻炸出来，而不是错位一格。
			if (FUELS.size() != FUEL_COUNT) {
				throw new IllegalStateException("燃料档位数不一致：FUEL_COUNT=" + FUEL_COUNT
						+ " / fuels()=" + FUELS.size());
			}
		}
		return FUELS;
	}

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（在 {@link #OnInit()} 里赋值）。 */
	public static Block BHR_CAMPFIRE;

	/** 方块物品（掉落 / 创造栏都要它的实例）。 */
	public static BlockItem BHR_CAMPFIRE_ITEM;

	// ── 层 3 ｜ 状态属性 ──

	/** 是否在燃烧（= 正在制作且还有燃料）。原版的同名字段，直接复用以便与原版工具链互通。 */
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	// ── 层 4 ｜ 属性工厂 ──

	/**
	 * 简易篝火的属性：复制原版营火（爆炸抗性 / 火焰相关布尔值这些跟着走），然后改成
	 * 「矮一点、不遮挡、有火就发光」。
	 *
	 * <ul>
	 *   <li>{@code noOcclusion()} —— 交叉模型不是实心方块，不写会让贴图被邻面剔除；</li>
	 *   <li>{@code lightLevel(...)} —— {@code lit ? 15 : 0}（与原版营火同亮）；</li>
	 *   <li>{@code strength(0.5F)} —— 徒手可挖，但不至于像草一样一碰就没；</li>
	 *   <li>{@code sound(SoundType.WOOD)} —— 木柴手感。</li>
	 * </ul>
	 */
	public static BlockBehaviour.Properties campfireProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
				.noOcclusion()
				.lightLevel(state -> state.getValue(LIT) ? 15 : 0)
				.strength(0.5F)
				.sound(SoundType.WOOD);
	}

	// ── 层 5 ｜ 构造器 ──

	public BHR_Campfire(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(LEVEL, LEVEL_MIN));
	}

	// ── 层 8 ｜ 状态模式 / 方块实体 / ticker ──

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, LEVEL);
	}

	/**
	 * 放置时从物品的 {@code workbench_level} 组件还原等级（与制作台同一套组件、同一套写法）。
	 * <p>没有组件（手改 / 用 {@code /setblock}）就是 {@link #LEVEL_MIN} 级。
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(LEVEL, levelOf(context.getItemInHand()))
				.setValue(LIT, false);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BHR_CampfireEntity(pos, state);
	}

	/**
	 * 每 tick 推进制作与燃料。
	 *
	 * <p>⚠️ 与制作台<b>刻意不同</b>：制作台的进度是「当前游戏刻 - 本份开始刻」现算的，
	 * 所以它不需要 ticker；篝火会<b>因为没燃料而暂停</b>（升级配方除外，见
	 * {@link BHR_CampfireEntity#isFuelFreeCraft()}），时间差模型无法表达「暂停」，
	 * 只能逐 tick 累加进度。于是本方块注册 ticker（与服务端熔炉同一路数）。
	 * <p>客户端返回 {@code null}：燃料/进度是服务端权威状态，客户端只读同步过去的数据槽。
	 */
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return (tickLevel, pos, tickState, blockEntity) -> {
			if (blockEntity instanceof BHR_CampfireEntity campfire) {
				campfire.serverTick();
			}
		};
	}

	// ── 层 9 ｜ 交互 / 掉落 ──

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			BHR_CampfireEntity entity = level.getBlockEntity(pos) instanceof BHR_CampfireEntity campfire
					? campfire
					: null;
			serverPlayer.openMenu(new SimpleMenuProvider(
					(containerId, inventory, ignored) -> new BHR_CampfireMenu(containerId, inventory,
							ContainerLevelAccess.create(level, pos), entity),
					title()));
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * 掉落：一件篝火，<b>带着当前等级</b>。
	 * <p>用覆写而不是战利品表，与制作台同一处理由：不用为它单写一条 loot_table JSON，
	 * 且爆炸 / 其它掉落路径一并覆盖。
	 */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = new ArrayList<>(1);
		drops.add(createStack(state.getValue(LEVEL)));
		return drops;
	}

	/** 创造模式中键取到的是<b>同等级</b>的篝火（与 {@link #getDrops} 同一份构造）。 */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return createStack(state.getValue(LEVEL));
	}

	// ── 层 10 ｜ 形状 ──

	/**
	 * 碰撞形状：0.25 格高（4/16），可以直接踩上去、也能被跳过去。
	 *
	 * <p>⚠️ 只覆写<b>碰撞</b>，<b>不</b>覆写 {@code getShape}（轮廓 / 准星判定）——
	 * 轮廓保持父类的整格，这样玩家对着火焰上半部分右键也算点到方块。
	 * 如果两者都缩到 4px，交叉贴图上层那 12 像素会变成「点不到」，界面几乎打不开。
	 * 原版植物反过来（碰撞空、轮廓整格），理由是同一个。
	 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return SHAPE;
	}

	// ── 层 12 ｜ 初始化 ──

	/**
	 * 注册：方块 → 方块物品 → 方块实体类型 → 菜单类型（顺序即依赖顺序）。
	 *
	 * <p><b>方块物品的属性里挂了等级组件的「原型值」</b>（与制作台同一手法，
	 * 见 {@code BHR_WorkBenchLevel} 类注释）。原型值让 1 级也带组件，于是：
	 * <ul>
	 *   <li>描述栏（lore）里对<b>1 级也显示</b>「等级：1」；</li>
	 *   <li>{@code BHR_WorkBenchLevel.set(stack, 1)} 刻意不写补丁 → 「新造的 1 级」与「挖回来的 1 级」
	 *       是同一个物品堆（不会因补丁差异而分裂堆叠）。</li>
	 * </ul>
	 * ⚠️ 原型组件必须等组件类型注册完才能挂 —— 所以本类的 {@code OnInit()} 必须排在
	 * {@code BHR_WorkBench.OnInit()}（它负责注册组件）之后，主类里的调用顺序就是这个约束。
	 */
	public static void OnInit() {
		BHR_CAMPFIRE = ModRegister.registerBlock(REGISTRY_NAME,
				new BHR_Campfire(campfireProperties().setId(ModRegister.blockKey(REGISTRY_NAME))));
		BHR_CAMPFIRE_ITEM = ModRegister.register(REGISTRY_NAME,
				new BlockItem(BHR_CAMPFIRE, new Item.Properties()
						.setId(ModRegister.itemKey(REGISTRY_NAME))
						.component(BHR_WorkBenchLevel.component(),
								new BHR_WorkBenchLevel.Level(LEVEL_MIN))));
		// 方块实体类型要求方块已在注册表里（它的 isValid 比对的就是注册后的实例）
		BHR_CampfireEntity.OnInit(BHR_CAMPFIRE);
		// 打开界面的数据包要写菜单的注册名，所以必须早于任何一次右键
		BHR_CampfireMenu.OnInit();
	}

	// ── 共享助手 ──

	/** 界面标题（不带等级：等级只影响配方栏里哪些配方可用）。 */
	public static Component title() {
		return Component.translatable(TITLE_KEY);
	}

	/**
	 * 造一个指定等级的篝火物品堆（{@link #getDrops} 与「选取方块」共用）。
	 *
	 * <p>等级写进物品的 {@code workbench_level} 组件（与制作台<b>共用同一个组件</b>，
	 * 见 {@code BHR_WorkBenchLevel}）：1 级不写补丁（等于「新造出来的篝火」），
	 * 2 级才在补丁里写值 —— 于是 1 级与 2 级的篝火是<b>两个不同的物品堆</b>，不会混着堆叠。
	 *
	 * @param level 等级，调用方保证已夹到 {@link #LEVEL_MIN}~{@link #LEVEL_MAX}
	 *              （方块状态取出的值天然合法）
	 */
	public static ItemStack createStack(int level) {
		ItemStack stack = new ItemStack(BHR_CAMPFIRE_ITEM);
		return BHR_WorkBenchLevel.set(stack, Math.clamp(level, LEVEL_MIN, LEVEL_MAX));
	}

	/** 读物品堆上的等级并夹到本机器的 1~2 级（组件里存的是 1~29 的通用等级）。 */
	public static int levelOf(net.minecraft.world.item.ItemInstance stack) {
		return Math.clamp(BHR_WorkBenchLevel.levelOf(stack), LEVEL_MIN, LEVEL_MAX);
	}

	/** 第 {@code index} 档燃料；越界返回 {@code null}（界面上会直接跳过那一格）。 */
	public static Fuel fuel(int index) {
		List<Fuel> list = fuels();
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}
}
