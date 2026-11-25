package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 喂食箱子（{@code bhr_feeder_chest}）—— 方块愚者（BHR）的「动物自助取食」容器方块。
 *
 * <p>行为规格：
 * <ul>
 *   <li>27 格容器（与箱子 / 木桶同级），内容随存档保存，破坏时原样吐出；</li>
 *   <li>朝向按熔炉的规则：<b>面朝玩家</b>放置（{@code FACING = HORIZONTAL_FACING}）；</li>
 *   <li>面朝方向<b>正前方那一格</b>是动物的取食位：动物会自己寻路过去，箱内有它能吃的食物时
 *       吃一个，效果与玩家手持食物右键喂它一致（成年进求偶 / 幼崽加速成长）；</li>
 *   <li>没有开盖动画、没有开盖音效（按需求刻意不做）——因此<b>没有</b> {@code ContainerOpenersCounter}。</li>
 * </ul>
 *
 * <h2>取向说明（为什么是「动物请求」而不是「箱子轮询」）</h2>
 * 两种实现都试过定级（见 {@code doc/agent/deepseek/13-动物取食容器-范式定级调查.md} §6）：
 * <ul>
 *   <li>「动物到站后自己请求」= 喂食落在已有 Goal 的一个分支里，<b>阶梯 0</b>，
 *       且 30 秒冷却由原版的求偶计时器（600 tick）免费提供；</li>
 *   <li>「箱子每 tick 扫前方一格」= 要再挂一条 {@code getTicker}（层 8 / <b>阶梯 2</b>），
 *       而且动物反正还是要靠 Goal 走过去，等于同一上限上多挂一层机制。</li>
 * </ul>
 * 按范式 §3 铁律「能用上一级就不许用下一级」，选前者 —— 所以本方块<b>没有 ticker</b>。
 *
 * <h2>界面为什么直接用原版箱子壳</h2>
 * {@code ChestMenu.threeRows(id, inventory, container)} 是原版 {@code BarrelBlockEntity}
 * 自己就在用的入口（反编译 :64），而 {@code ServerPlayer} 对所有菜单统一安装
 * {@code ContainerSynchronizer}（{@code ServerPlayer:276/459}），所以自定义 {@code Container}
 * 不需要注册 {@code MenuType}、不需要 {@code MenuScreens.register}、也不需要碰类调整文件。
 * 标题由 {@code MenuProvider#getDisplayName()} 随打开界面的数据包发给客户端。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b>：喂食箱子　<b>英文名</b>：Feeder Chest</li>
 *   <li><b>注册名</b>：{@code bhr_feeder_chest}　<b>模块</b>：BHR　<b>父级</b>：木桶（{@code Blocks.BARREL}）</li>
 *   <li><b>wiki 页</b>：{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_FeederChest.md}</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@link #FACING}）、层 8 状态模式 / 放置 / 方块实体工厂、
 *       层 9 交互（{@code useWithoutItem}）与比较器读数（{@code hasAnalogOutputSignal} +
 *       {@code getAnalogOutputSignal}）、层 12 额外注册表（方块实体类型 + 实体加载事件）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（27 格写在 {@link BHR_FeederChestEntity#CONTAINER_SIZE}）、
 *       层 6 家族（<b>刻意不继承</b> {@code BarrelBlockEntity}：它唯一的构造器把类型写死成
 *       {@code BlockEntityTypes.BARREL}，继承会让存档静默丢物品）、层 7 codec（{@code Block} 已实现，
 *       <b>不要</b>改继承 {@code HorizontalDirectionalBlock}——它的 {@code codec()} 是抽象的，见
 *       {@code BHR_GemAmethyst:52}）、层 10 形状（整方块）、层 11 预留区、层 C 客户端（复用原版界面，
 *       无客户端代码）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：方块本体停在<b>第 2 级</b>「覆写本类方法」；整条功能的上限是<b>第 4 级</b>
 *       （动物侧靠 {@code ServerEntityEvents.ENTITY_LOAD} 挂 Goal，见 {@link BHR_FeederChestGoal}）。
 *       没有停在第 3 级是因为方块实体类型登记（{@code BlockEntityType}）本身就在层 12 的登记动作里，
 *       用的是同一个 {@code Registry.register}。</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()}
 *       里声明（多状态，{@code blockstates/bhr_feeder_chest.json} 手写）　③☒ 不需要
 *       {@code ItemModelGenerator.ITEM_MODEL_OVERRIDES}（物品模型就是方块模型）　④☑ 中英
 *       {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}　⑥☑ 贴图
 *       {@code block/bhr/feeder_chest_{front,side,top}} + 模型 {@code models/block/bhr_feeder_chest}
 *       （{@code parent: minecraft:block/orientable}）　⑦☑ 战利品表
 *       {@code data/richards-whimsy-mod/loot_table/blocks/bhr_feeder_chest.json}；
 *       配方属「获取方式」阶段，暂缓</li>
 *   <li><b>未启用（刻意）</b>：{@code WorldlyContainer}（漏斗的<b>按面筛选</b>）——
 *       漏斗本身**不需要它**：{@code HopperBlockEntity#getBlockContainer} 对
 *       「有方块实体且实体实现了 {@code Container}」的方块直接取用，非 {@code WorldlyContainer}
 *       时槽位退回「全部、双向」（反编译 {@code HopperBlockEntity:170-185/298/306/375}），
 *       所以本方块已经能被漏斗抽取与插入。只有以后想限「顶面只进、侧面只出」时才需要那三个方法。</li>
 * </ul>
 */
public class BHR_FeederChest extends Block implements EntityBlock {

	// ── 层 1 ｜ 规格常量 ──

	/** 注册路径（方块与它的方块物品同名，分属两个注册表）。 */
	public static final String REGISTRY_NAME = "bhr_feeder_chest";

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FEEDER_CHEST;

	/** 方块物品：创造栏与掉落都要它的实例，所以单独留句柄。 */
	public static BlockItem BHR_FEEDER_CHEST_ITEM;

	// ── 层 3 ｜ 状态属性字段 ──

	/**
	 * 朝向（层 3）：与熔炉同一个原版常量 {@code BlockStateProperties.HORIZONTAL_FACING}，
	 * 即只取水平四个方向（{@code north/east/south/west}），没有上下。
	 */
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/**
	 * 喂食箱子的属性（层 4）：完整复制木桶。
	 *
	 * <p>这样硬度 / 爆炸抗性 / 音效组（{@code SoundType.WOOD}）/ 是否阻挡都跟着原版木桶走，
	 * 与需求「像木桶或箱子一样存储物品」的观感一致；要改哪一项就在这一行下面追加并注明来源。
	 */
	public static BlockBehaviour.Properties feederChestProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/** 构造器（层 5）：装配 + 给状态初值（面朝北；放置时由层 8 覆盖）。 */
	public BHR_FeederChest(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/** 状态模式（层 8）：本方块有 {@code facing} 这一个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/**
	 * 放置时定朝向（层 8）：照抄原版熔炉 —— 面朝玩家。
	 * <p>{@code getHorizontalDirection()} 是玩家看的方向，取反就是「从方块看向玩家」，
	 * 也就是方块正面对准玩家的方向（{@code AbstractFurnaceBlock:50-51} 同款）。
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/**
	 * 方块实体工厂（层 8）：实现 {@link EntityBlock} 的唯一强制方法。
	 *
	 * <p>⚠️ 必须造<b>本模组自己的</b>实体类，且实体构造器里要用自己的 {@code BlockEntityType}：
	 * 用原版桶/箱的实体类型会被判定「类型与方块不匹配」，<b>存档读取时内容静默丢失</b>。
	 * 这个坑在告示牌上已经踩过一次（见 {@code BHR_TreeSignFruit} 类注释）。
	 */
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BHR_FeederChestEntity(pos, state);
	}

	// ── 层 9 ｜ 交互与判定 ──

	/**
	 * 右键打开界面（层 9）：直接复用原版三行箱子界面。
	 *
	 * <p>只覆写 {@code useWithoutItem}：父类的 {@code useItemOn} 对普通方块返回
	 * {@code TRY_WITH_EMPTY_HAND}，所以「手里拿着东西右键」也会落到这里 ——
	 * 顺带避免手持方块时把方块摆到箱子上（与原版箱子一致）。
	 *
	 * <p>界面写法是 {@code ChestMenu.threeRows}，与原版木桶同一条路（反编译
	 * {@code BarrelBlockEntity:64}）：不注册 {@code MenuType}、不写客户端 {@code Screen}。
	 * 标题取方块自己的描述 id，随打开数据包发到客户端。
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player instanceof ServerPlayer serverPlayer
				&& level.getBlockEntity(pos) instanceof BHR_FeederChestEntity chest) {
			serverPlayer.openMenu(new SimpleMenuProvider(
					(containerId, inventory, ignored) -> ChestMenu.threeRows(containerId, inventory, chest),
					Component.translatable(this.getDescriptionId())));
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * 比较器读数（层 9）：开启「我会输出红石信号」。
	 *
	 * <p>原版把这件事拆成两个方法：这个方法回答「要不要来问我」，
	 * {@link #getAnalogOutputSignal} 回答「信号多大」。不覆写第一个的话，后者永远不会被调用。
	 */
	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	/**
	 * 比较器读数（层 9）：读数 = 容器充满度 × 15，与箱子 / 木桶 / 漏斗同一套算法。
	 *
	 * <p>充满度由 {@code AbstractContainerMenu.getRedstoneSignalFromContainer} 按
	 * 「每个槽位的占比之和 / 槽数」算出来（反编译 :741），所以只要把方块实体交给它即可。
	 * 注意 26.2 的签名比旧版本多了一个 {@link Direction} 参数（{@code BlockBehaviour:300}）。
	 */
	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 初始化（层 12）：注册方块 → 方块物品 → 方块实体类型 → 实体加载钩子。
	 *
	 * <p>顺序要紧：{@link BHR_FeederChestEntity#OnInit(Block)} 需要已注册的方块实例来填
	 * {@code BlockEntityType} 的合法方块集合；最后那条事件必须在任何实体加载之前就位。
	 */
	public static void OnInit() {
		BHR_FEEDER_CHEST = ModRegister.registerBlockWithItem(REGISTRY_NAME,
				new BHR_FeederChest(feederChestProperties()
						.setId(ModRegister.blockKey(REGISTRY_NAME))));
		BHR_FEEDER_CHEST_ITEM = (BlockItem) BHR_FEEDER_CHEST.asItem();

		BHR_FeederChestEntity.OnInit(BHR_FEEDER_CHEST);

		// 阶梯 4：原版动物没有「走向某个方块」的 goal，只能从外部挂上去（详见 Goal 的类注释）。
		BHR_FeederChestGoal.register();
	}

	// ── 层 C ｜ 客户端接线：无（复用原版箱子界面）──

	// ── 层 M ｜ 定制 Mixin：无 ──
}
