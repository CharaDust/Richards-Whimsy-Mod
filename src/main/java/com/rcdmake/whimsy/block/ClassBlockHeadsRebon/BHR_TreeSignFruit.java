package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Set;

/**
 * 果木告示牌（{@code bhr_fruit_sign} 立牌 + {@code bhr_fruit_wall_sign} 墙牌）——
 * 果木（{@link BHR_TreePlanksFruit}）的告示牌。<b>一块物品、两个方块</b>。
 *
 * <p>wiki 规格：父级 = <b>橡木告示牌 / 橡木墙牌</b>
 * （{@code Blocks.OAK_SIGN} / {@code Blocks.OAK_WALL_SIGN}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类提供：
 * <ul>
 *   <li>立牌 {@link StandingSignBlock}：{@code ROTATION}（16 档）+ {@code WATERLOGGED}；</li>
 *   <li>墙牌 {@link WallSignBlock}：{@code FACING} + {@code WATERLOGGED}。</li>
 * </ul>
 * 两者都是<b>多状态</b>，blockstate 与模型手写（照抄原版 {@code oak_sign} / {@code oak_wall_sign}）。
 *
 * <h2>为什么这一类比别的方块长（三个 26.2 的硬要求）</h2>
 * <ol>
 *   <li><b>方块实体类型必须自己注册，并且必须用「自己的类型」造实体。</b>
 *       {@code SignBlock#newBlockEntity} 里造的是 {@code new SignBlockEntity(pos, state)}，
 *       那个构造器把类型写死成 {@code BlockEntityTypes.SIGN}；而 {@code SIGN} 的
 *       {@code validBlocks} 里没有我们的方块 —— 存档读取时会因「类型与方块不匹配」被判无效，
 *       牌上的字<b>悄悄丢掉</b>。所以：注册自己的 {@code BlockEntityType}，
 *       并在两个子类里覆写 {@code newBlockEntity} 传入它。
 *       26.2 的 {@code SignBlockEntity(BlockEntityType<? extends SignBlockEntity>, BlockPos, BlockState)}
 *       正是为这种场景留下的口子。</li>
 *   <li><b>ticker 也要跟着换类型。</b>{@code SignBlock#getTicker} 用
 *       {@code createTickerHelper(type, BlockEntityTypes.SIGN, ...)} 判定，我们的类型永远匹配不上，
 *       于是编辑锁（{@code playerWhoMayEdit}）不会被清理 —— 表现为「别人刚摸过，你就点不开」。
 *       覆写后指向自己的类型即可。</li>
 *   <li><b>物品是 {@link SignItem}，不是普通的 {@code BlockItem}。</b>
 *       它负责「贴墙放成墙牌、地面放成立牌」，并自带 {@code 16} 的堆叠上限
 *       （原版告示牌就是 16）。物品只有一件，挂在立牌名下（{@code bhr_fruit_sign}），
 *       墙牌 {@code bhr_fruit_wall_sign} <b>没有</b>物品形式。</li>
 * </ol>
 *
 * <h2>客户端渲染（层 C）</h2>
 * 牌面木头由<b>方块模型</b>画，牌上的文字由<b>方块实体渲染器</b>画。原版把
 * {@code StandingSignRenderer} 注册给了 {@code BlockEntityTypes.SIGN}；
 * 我们的自定义类型没有渲染器 → 牌子能放、字看不见。所以客户端入口
 * {@code RichardsWhimsyModClient} 里补一条
 * {@code BlockEntityRenderers.register(BHR_FRUIT_SIGN_ENTITY, StandingSignRenderer::new)}。
 * 这条<b>绝不能</b>写进本类（专用服务端会 {@code ClassNotFound}）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_sign} / {@code bhr_fruit_wall_sign}　<b>模块</b> BHR
 *       　<b>父级</b> 橡木告示牌（<b>层 6 家族</b>：继承 {@link StandingSignBlock} / {@link WallSignBlock}）</li>
 *   <li><b>启用的可选层</b>：层 8 生命周期（{@code newBlockEntity} / {@code getTicker} ——
 *       只换类型，不写逻辑）、层 12 额外注册表（方块实体类型 + 可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3（父类自带）、层 7 codec（父类已实现）、
 *       层 9 / 10（父类自带）、层 11、层 M</li>
 *   <li><b>能力阶梯</b>：第 0/1 级 + 第 2 级（两个覆写，纯粹是「把类型钉成自己的」，
 *       不改任何行为）+ 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☑ {@code ITEM_MODEL_OVERRIDES}（{@code item/bhr_fruit_sign}）
 *       　④☑ 中英 lang（立牌 / 墙牌的 {@code block.} 两条 + 物品的 {@code item.} 一条）
 *       　⑤☑ 创造栏　⑥☑ 贴图 {@code bhr/fruit_sign.png} + {@code item/bhr/fruit_sign.png}；模型 5 份
 *       　⑦◐ 标签 {@code #minecraft:standing_signs} / {@code #minecraft:wall_signs}（方块）与
 *       {@code #minecraft:signs}（物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeSignFruit {

	/** 立牌注册名（同时也是物品的注册名）。 */
	public static final String REGISTRY_NAME = "bhr_fruit_sign";

	/** 墙牌注册名（没有物品形式）。 */
	public static final String WALL_REGISTRY_NAME = "bhr_fruit_wall_sign";

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 立牌方块（地面），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_SIGN;

	/** 墙牌方块（贴墙），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_WALL_SIGN;

	/** 方块实体类型（两个方块共用）。 */
	public static BlockEntityType<SignBlockEntity> BHR_FRUIT_SIGN_ENTITY;

	/** 唯一的物品形式（挂立牌的名字）。 */
	public static Item BHR_FRUIT_SIGN_ITEM;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 立牌的属性：完整复制橡木告示牌。 */
	public static BlockBehaviour.Properties fruitSignProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN);
	}

	/**
	 * 墙牌的属性：完整复制橡木墙牌，<b>但必须把翻译键改回自己的</b>。
	 *
	 * <p>⚠️ 26.2 的原版里，墙牌属性是从「立牌」派生的
	 * （{@code Blocks.OAK_WALL_SIGN} = {@code wallVariant(OAK_SIGN, true)...}），
	 * 而 {@code ofFullCopy} 会把这个<b>翻译键依赖</b>一起复制过来 ——
	 * 实测：直接 {@code ofFullCopy(Blocks.OAK_WALL_SIGN)} 得到的是
	 * {@code getDescriptionId() == "block.minecraft.oak_sign"}，
	 * 于是 datagen 里登记的「墙牌」翻译会写成原版橡木告示牌那一条（同名覆盖/丢失）。
	 * 这里用 {@code overrideDescription} 把它钉回 {@code block.richards-whimsy-mod.bhr_fruit_wall_sign}。
	 */
	public static BlockBehaviour.Properties fruitWallSignProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
				.overrideDescription("block." + com.rcdmake.whimsy.RichardsWhimsyMod.MOD_ID + "." + WALL_REGISTRY_NAME);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	private BHR_TreeSignFruit() {
	}

	// ── 层 6 ｜ 家族/变体：两个「只换实体类型」的子类 ──

	/**
	 * 立牌：只把方块实体类型钉成自己的，其余（旋转、含水、放置、编辑）全部继承父类。
	 */
	public static class Standing extends StandingSignBlock {

		public Standing(WoodType type, BlockBehaviour.Properties properties) {
			super(type, properties);
		}

		/**
		 * 造方块实体时传<b>自己的</b>类型（父类会传死写的 {@code BlockEntityTypes.SIGN}，
		 * 那会让存档里的字失效，见类注释第 1 条）。
		 */
		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return createEntity(pos, state);
		}

		/** ticker 同理：父类只认 {@code BlockEntityTypes.SIGN}，换成自己的类型编辑锁才会被清理。 */
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
				BlockEntityType<T> type) {
			return createTickerHelper(type, BHR_FRUIT_SIGN_ENTITY, SignBlockEntity::tick);
		}
	}

	/** 墙牌：同 {@link Standing}，状态是 {@code FACING} 而非 {@code ROTATION}。 */
	public static class Wall extends WallSignBlock {

		public Wall(WoodType type, BlockBehaviour.Properties properties) {
			super(type, properties);
		}

		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return createEntity(pos, state);
		}

		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
				BlockEntityType<T> type) {
			return createTickerHelper(type, BHR_FRUIT_SIGN_ENTITY, SignBlockEntity::tick);
		}
	}

	/**
	 * 造一个「挂着自家类型」的告示牌方块实体。
	 *
	 * <p>⚠️ 这里读的是静态字段 {@link #BHR_FRUIT_SIGN_ENTITY}，而它<b>正是在构造这个类型时才被赋值</b> ——
	 * 之所以合法，是因为这个 lambda 只在「已经注册完、游戏里真的放下一个告示牌」时才被调用，
	 * 那时字段早就有值了。
	 */
	private static SignBlockEntity createEntity(BlockPos pos, BlockState state) {
		return new SignBlockEntity(BHR_FRUIT_SIGN_ENTITY, pos, state);
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册两个方块、方块实体类型与唯一的物品。
	 *
	 * <p>顺序是硬要求：方块必须先就位（方块实体类型的 {@code validBlocks} 要引用它们）。
	 */
	public static void OnInit() {
		// 木种取 WoodType.OAK —— 与「父级 = 橡木告示牌」一致（决定音效；单据的 {@code wood_type} 字段也用它）
		BHR_FRUIT_SIGN = ModRegister.registerBlock(REGISTRY_NAME,
				new Standing(WoodType.OAK,
						fruitSignProperties().setId(ModRegister.blockKey(REGISTRY_NAME))));

		BHR_FRUIT_WALL_SIGN = ModRegister.registerBlock(WALL_REGISTRY_NAME,
				new Wall(WoodType.OAK,
						fruitWallSignProperties().setId(ModRegister.blockKey(WALL_REGISTRY_NAME))));

		BHR_FRUIT_SIGN_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ModRegister.id(REGISTRY_NAME),
				new BlockEntityType<>(BHR_TreeSignFruit::createEntity,
						Set.of(BHR_FRUIT_SIGN, BHR_FRUIT_WALL_SIGN)));

		// 唯一的物品：SignItem 负责「贴墙放成墙牌 / 地面放成立牌」，堆叠上限与原版一致 = 16
		BHR_FRUIT_SIGN_ITEM = ModRegister.register(REGISTRY_NAME,
				new SignItem(BHR_FRUIT_SIGN, BHR_FRUIT_WALL_SIGN,
						new Item.Properties()
								.stacksTo(16)
								.setId(ModRegister.itemKey(REGISTRY_NAME))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_SIGN, IGNITE_ODDS, BURN_ODDS);
		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_WALL_SIGN, IGNITE_ODDS, BURN_ODDS);
	}
}
