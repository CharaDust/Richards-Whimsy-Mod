package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

/**
 * 通灵树苗（{@code rdt_spectre_sapling}）—— RDT 的树苗方块，
 * 与通灵原木（{@link RDT_TreeLogSpectre}）、通灵树叶（{@link RDT_TreeLeavesSpectre}）同属「通灵树」。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_TreeSaplingSpectre.md}）：
 * <ul>
 *   <li>父级：橡树树苗；构造表逐行「父级」，只有引燃 / 烧毁两行是明文值 <b>60 / 100</b></li>
 *   <li>渲染：使用原版 X 型植物模型（{@code block/cross}）；贴图 {@code rdt/spectre_sapling.png}
 *       实测 Alpha 取值为 {0, 195} → 半透明档（与树叶同源）</li>
 *   <li>方块状态：<b>照抄原版树苗</b> —— 只有 {@code STAGE}（0 / 1）</li>
 *   <li>长出来的树：<b>形状抄原版白桦</b>，材质换成通灵原木 + 通灵树叶</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类用到的部分，逐条核验过）</h2>
 * <table border="1">
 *   <caption>改名 / 结构变化</caption>
 *   <tr><th>1.20.4（Yarn）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code AbstractBlock.Settings.copy(Blocks.OAK_SAPLING)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)}</td><td>「父级 = 橡树树苗」</td></tr>
 *   <tr><td>{@code SaplingBlock} 继承 {@code BushBlock}</td>
 *       <td>26.2 继承 {@link net.minecraft.world.level.block.VegetationBlock}</td>
 *       <td>{@code BushBlock} 已改名；「能不能种在这块地上」由 {@code VegetationBlock#mayPlaceOn} 决定</td></tr>
 *   <tr><td>{@code TreeGrower}</td><td>{@code TreeGrower}（仍在，构造器形参变多）</td>
 *       <td>26.2 的 8 形参构造器形参顺序已逐字节核对，见下</td></tr>
 * </table>
 *
 * <h2>⭐ 为什么继承 {@code SaplingBlock}（第 0 级「继承对的父类」的教科书例子）</h2>
 * wiki 要求的三件事 —— <b>种植、长大、骨粉</b> —— 原版 {@code SaplingBlock} 一件不落全都有，
 * 而且方块状态就一个 {@code STAGE}，与本方块的需求完全重合。所以本类<b>没有一条业务覆写</b>：
 * <pre>{@code
 * 层 3 状态属性     STAGE = BlockStateProperties.STAGE（父类声明）
 * 层 8 createBlockStateDefinition  → builder.add(STAGE)（父类）
 * 层 8 randomTick   → getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0 → advanceTree（父类）
 * 层 8 advanceTree  → treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random)（父类）
 * 层 8 performBonemeal / isBonemealSuccess / isValidBonemealTarget → 骨粉（父类，BonemealableBlock）
 * 层 9 canSurvive   → 下方 mayPlaceOn（父类 VegetationBlock）
 * }</pre>
 * 本类真正新增的只有两件「配置」：<b>拿哪个 TreeGrower</b>（层 1 的常量）和<b>可燃性登记</b>（层 12）。
 *
 * <h2>⭐⭐ {@code TreeGrower} 的形参顺序（逐字节核对，别看名字猜）</h2>
 * 26.2 的 {@code TreeGrower} 有两个 public 构造器，本类用形参最全的那个。
 * 它的<b>形参顺序不是文档能查到的</b>（无 javap、无源码时最容易写反），所以这里用
 * {@code tools/api-dump.init.gradle} 的 {@code dump.ctor} 模式扫了构造器字节码里的
 * {@code aload_N → putfield} 配对，得到 8 个形参与字段的<b>一一对应</b>：
 * <pre>{@code
 * public TreeGrower(String name, float secondaryChance,
 *                   Optional megaTree, Optional secondaryMegaTree,
 *                   Optional tree, Optional secondaryTree,
 *                   Optional flowers, Optional secondaryFlowers)
 * }</pre>
 * 于是「只长普通树、不要 2×2 巨树、不要蜂巢变体」的写法就是：
 * <pre>{@code
 * new TreeGrower(名字, 0.0F,
 *     Optional.empty(),          // megaTree：空 = 不会去凑 2×2 巨树
 *     Optional.empty(),          // secondaryMegaTree
 *     Optional.of(通灵树),        // tree：正常单体树走这一个
 *     Optional.of(通灵树),        // secondaryTree：防御性填同一个（chance=0 时根本选不到）
 *     Optional.empty(),          // flowers：空 = 不要求附近有花
 *     Optional.empty());         // secondaryFlowers
 * }</pre>
 * <p>⚠️ <b>{@code megaTree} 一定留空</b>：它非空会让 {@code growTree} 先去做
 * 「2×2 树苗」判定（原版丛林 / 深色橡木就是这么长成巨树的）——
 * 通灵树是白桦那种单体树，被拖进巨树分支就长不出来了。
 * <p>构造器内部会把名字登记进静态 {@code GROWERS} 表（已核验：常量池里有 {@code GROWERS}，
 * 且 26.2 <b>没有</b>「重名抛异常」），{@code TreeGrower.CODEC} 因此能按名字往返 ——
 * 本模组的 {@code SaplingBlock.CODEC} 也就跟着能用。
 *
 * <h2>⭐ 长出来的树 = 白桦形状（数据驱动，Java 侧一行形状代码都没有）</h2>
 * 树形状不写在 Java 里，而是一份<b>配置特征（configured feature）JSON</b>：
 * {@code src/main/resources/data/richards-whimsy-mod/worldgen/configured_feature/rdt_spectre_tree.json}。
 * 本类只持有它的 {@link ResourceKey}（{@link #SPECTRE_TREE}），
 * {@code growTree} 运行时去「已配置特征」注册表里按 key 取。
 * <p>那份 JSON 是<b>照抄原版 {@code data/minecraft/worldgen/configured_feature/birch.json}</b>
 * （逐行从 26.2 客户端 jar 提取）后只换两个方块：
 * <pre>{@code
 * trunk_placer    : straight_trunk_placer  base_height=5 height_rand_a=2 height_rand_b=0   ← 白桦树干 5~7 格
 * foliage_placer  : blob_foliage_placer    height=3 offset=0 radius=2                     ← 白桦树冠
 * minimum_size    : two_layers_feature_size
 * trunk_provider  : richards-whimsy-mod:rdt_spectre_log      （axis=y）   ← 原版是 birch_log
 * foliage_provider: richards-whimsy-mod:rdt_spectre_leaves   （distance=7, persistent=false, waterlogged=false）
 * }</pre>
 * 「白桦形状」的全部秘密就是这两行 placer 参数（橡木的 {@code base_height} 是 4，白桦是 5）。
 * <p>⚠️ {@code foliage_provider} 里那三个属性是<b>必须写全</b>的：它要求的属性名与本方块
 * （继承原版树叶）的状态名一一对应（{@code distance / persistent / waterlogged}）——
 * 少写或改名，树一长出来就会因为「状态不合法」报错。
 * <p>⚠️ 树叶长出来会不会立刻枯萎，取决于 {@code rdt_spectre_log} 在不在 {@code #minecraft:logs}
 * 标签里（26.2 的树叶距离场读的是 {@code #minecraft:prevents_nearby_leaf_decay} → {@code #minecraft:logs}）。
 * 这一条由 {@code data/minecraft/tags/block/logs.json} 落地，理由写在
 * {@link RDT_TreeLeavesSpectre} 的类注释里。
 *
 * <h2>种植：能种在哪</h2>
 * 父类 {@code VegetationBlock#mayPlaceOn} 的实现是
 * {@code state.is(BlockTags.SUPPORTS_VEGETATION)}（已核验字节码），
 * 即原版泥土 / 草方块 / 耕地 / 苔藓块那一整类 —— <b>与橡树树苗完全同一份清单</b>，无需额外代码。
 * 下方方块被挖掉时，{@code updateShape} 会让树苗掉落（父类 {@code canSurvive} 判定），
 * 这也是「父级」那一行的既有行为。
 *
 * <h2>⚠️ 水会冲掉树苗（与原版一致，刻意不修）</h2>
 * 原版树苗的属性带 {@code noCollision()}（空碰撞形状），因此 {@code blocksMotion()} 为 false，
 * 水流判定链的第一道闸门 {@code canHoldAnyFluid} 就会放行 → 水把树苗冲掉。
 * 这是<b>原版的既有行为</b>，wiki 的构造表逐行都是「父级」，所以本类不修。
 * <p>如果以后要让它防水，按范式 §7 硬约束第 4 条加一条 {@code forceSolidOn()} 即可
 * （不要只写 {@code noCollision()} —— 「空碰撞 + 想挡水」必须是
 * {@code noCollision()} <b>+</b> {@code forceSolidOn()} 这一对）。
 * <p>对比：通灵原木需要那条 {@code forceSolidOn()}（它不可含水），通灵树叶不需要
 * （它实现了 {@code SimpleWaterloggedBlock} → 属于 {@code LiquidBlockContainer} 那条特权分支，水只会把它变成含水树叶）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_spectre_sapling}　<b>模块</b> RDT　<b>父级</b> 橡树树苗
 *       （<b>层 6 家族</b>：继承原版 {@code SaplingBlock}，STAGE 状态 / 种植 / 随机刻 / 骨粉 / 生长全白拿）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（生长器名 / 配置特征 key / 可燃性）、层 12 额外注册表（{@code FlammableBlockRegistry}）</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（父类给 {{@code STAGE}}）、层 7 codec（父类 {@code SaplingBlock.CODEC} 已实现）、
 *       层 8 生命周期（随机刻 / 骨粉 / 生长全在父类）、层 9 交互与判定（{@code canSurvive} 在父类）、
 *       层 10 形状与外观（父类 {@code getShape} 给 X 型植物的 12/16 高形状）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：用到第 0 级（继承家族类）、第 1 级（属性复制 + 生长器常量）、
 *       第 3 级（可燃性登记表 + <b>数据驱动制品</b>：配置特征 JSON 与 {@code #minecraft:logs} 标签）——
 *       最高停在<b>第 3 级</b>，<b>没有覆写、更没有 Mixin</b>。
 *       为什么停在第 3 级：「树长成什么样」在原版是<b>数据</b>（configured feature JSON），
 *       Java 侧唯一能做的就是把 key 交给 {@code TreeGrower}；可燃性同理没有 {@code Properties} 开关。</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明
 *       （{@code STAGE} 是多状态，blockstate 手写，照抄原版 = 一个 <b>catch-all 变体 {@code ""}</b>）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}（「木材」组）
 *       　⑥☑ 贴图 {@code rdt/spectre_sapling.png}（现成）；模型 {@code models/block/rdt_spectre_sapling.json}
 *       （{@code parent: block/cross}，与通灵树叶一样<b>不</b>手工写 {@code models/item/*}：
 *       26.2 的物品外观由 {@code items/<id>.json} 决定，datagen 会自动指向 {@code block/<id>}）
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做
 *       （<b>例外</b>：{@code data/minecraft/tags/block/logs.json} 本轮已落地，见上）</li>
 * </ul>
 */
public class RDT_TreeSaplingSpectre extends SaplingBlock {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 生长器名字。会由 {@code TreeGrower} 构造器登记进静态 {@code GROWERS} 表，
	 * 同时也是 {@code TreeGrower.CODEC} / {@code SaplingBlock.CODEC} 往返时用的键
	 * —— <b>一旦发布就不能改</b>（与方块状态的序列化名同一条规矩）。
	 * 取「通灵」的意译，与模块内其它注册名（{@code rdt_spectre_*}）同源。
	 */
	private static final String GROWER_NAME = "spectre";

	/**
	 * 次选树的概率。0 = 永远只用 {@link #SPECTRE_TREE} 那一棵，
	 * 不掷骰子去选 secondary 变体（通灵树没有变体）。
	 */
	private static final float SECONDARY_CHANCE = 0.0F;

	/**
	 * 通灵树的「已配置特征」key（层 1）：指向
	 * {@code data/richards-whimsy-mod/worldgen/configured_feature/rdt_spectre_tree.json}。
	 * <p>这只是<b>一个 key</b>，不触碰任何注册表 —— 所以写在静态字段里绝对安全
	 * （真正取特征是 {@code growTree} 运行时的事，靠数据包在启动时加载好）。
	 */
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPECTRE_TREE =
			ResourceKey.create(Registries.CONFIGURED_FEATURE, ModRegister.id("rdt_spectre_tree"));

	/**
	 * 通灵树的生长器（层 1）：形参顺序已逐字节核对，含义见类注释里那张表。
	 * <p>只填 {@code tree} 与 {@code secondaryTree}，其余四个留空：
	 * {@code megaTree} 留空是<b>必须</b>的（否则会被拖进「2×2 巨树」分支）。
	 * <p>{@code secondaryTree} 填同一个 key 是防御性的：{@code secondaryChance = 0} 时它选不到，
	 * 但万一将来有人把概率调大，也不会因为「次选为空」而炸。
	 */
	private static final TreeGrower SPECTRE_GROWER = new TreeGrower(
			GROWER_NAME,
			SECONDARY_CHANCE,
			Optional.empty(),            // megaTree：空 = 不参与 2×2 巨树
			Optional.empty(),            // secondaryMegaTree
			Optional.of(SPECTRE_TREE),   // tree：单体树走这一个
			Optional.of(SPECTRE_TREE),   // secondaryTree（防御性）
			Optional.empty(),            // flowers：空 = 不要求附近有花
			Optional.empty());           // secondaryFlowers

	/**
	 * 引燃几率（{@code igniteOdds}）：wiki 明文 60。
	 * <p>参考值：原版树苗就是 (60, 100)、树叶 / 羊毛 (30, 60)、原木 (5, 5)。
	 */
	private static final int IGNITE_ODDS = 60;

	/** 烧毁几率（{@code burnOdds}）：wiki 明文 100（与树叶同档、却比原木高一档）。 */
	private static final int BURN_ODDS = 100;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SPECTRE_SAPLING;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 通灵树苗的属性（层 4）：wiki 构造表逐行都是「父级」→ 完整复制橡树树苗。
	 *
	 * <p>不复制的只有可燃性（那张表在 {@code FlammableBlockRegistry} 里，见 {@link #OnInit()}）。
	 * <p>注意复制的 <b>包含</b> {@code noCollision()}：原版树苗是「可穿过、但外形是 X 型植物」，
	 * 碰撞由 {@code SaplingBlock#getShape} 给的是<b>外观形状</b>而不是碰撞箱。
	 * 副作用（水会冲掉它）与原版一致，见类注释。
	 */
	public static BlockBehaviour.Properties spectreSaplingProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5）：把生长器交给父类，剩下全归父类管。
	 * <p>父类构造器是 {@code protected SaplingBlock(TreeGrower, BlockBehaviour.Properties)}
	 * —— <b>TreeGrower 在前</b>（已核验形参顺序、且形参 #1 落进父类的 {@code treeGrower} 字段）；
	 * 它内部会 {@code createBlockStateDefinition} + {@code registerDefaultState(STAGE=0)}。
	 */
	public RDT_TreeSaplingSpectre(BlockBehaviour.Properties properties) {
		super(SPECTRE_GROWER, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品，并登记可燃性（层 12）。
	 *
	 * <p>可燃性走 Fabric 的 {@link FlammableBlockRegistry} —— 原版没有「按方块设可燃性」的
	 * {@code Properties} 开关，只能这样登记（与通灵原木 / 通灵树叶同一套）。
	 *
	 * <p>本方法<b>不</b>负责「树长什么样」：那是
	 * {@code data/richards-whimsy-mod/worldgen/configured_feature/rdt_spectre_tree.json} 的事，
	 * 本类只把它的 key（{@link #SPECTRE_TREE}）交给了 {@link #SPECTRE_GROWER}。
	 */
	public static void OnInit() {
		RDT_SPECTRE_SAPLING = ModRegister.registerBlockWithItem("rdt_spectre_sapling",
				new RDT_TreeSaplingSpectre(spectreSaplingProperties()
						.setId(ModRegister.blockKey("rdt_spectre_sapling"))));

		// 引燃几率 60 / 烧毁几率 100（wiki 那两行明文值）
		FlammableBlockRegistry.getDefaultInstance().add(RDT_SPECTRE_SAPLING, IGNITE_ODDS, BURN_ODDS);
	}
}