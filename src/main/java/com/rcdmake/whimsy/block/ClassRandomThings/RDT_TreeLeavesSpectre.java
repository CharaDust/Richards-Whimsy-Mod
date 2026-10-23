package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 通灵树叶（{@code rdt_spectre_leaves}）—— RDT 的树叶方块，
 * 与通灵原木（{@link RDT_TreeLogSpectre}）、通灵树苗（{@link RDT_TreeSaplingSpectre}）同属「通灵树」。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_TreeLeavesSpectre.md}）：
 * <ul>
 *   <li>父级：橡树树叶；窒息 ✗、视野阻挡 ✗、光线阻挡 ✗、表面生成生物 ✗，其余「父级」</li>
 *   <li><b>碰撞：顶面可站立；按住 Shift 穿过；侧面与身位在方块内可穿过</b>
 *       （2026-10 统一：与通灵原木 {@link RDT_TreeLogSpectre}、通灵木板 {@link RDT_TreePlanksSpectre}
 *       共用 {@link RDT_SpectrePassThrough} 一套实现）</li>
 *   <li>引燃几率 / 烧毁几率：wiki 明文 <b>60 / 100</b>（与树苗同档，比通灵原木的 30 / 60 高一档）</li>
 *   <li>渲染：<b>透明状态：依据纹理 Alpha 通道透明</b>（用带 {@code trsp} 的那张半透明贴图）</li>
 *   <li>方块状态：<b>照抄原版树叶</b> —— {@code DISTANCE} / {@code PERSISTENT} / {@code WATERLOGGED}</li>
 *   <li>获取方式：合适工具是锄 / 剑 / 剪刀，剪刀或精准采集掉落自身（战利品表按全项目既定决定暂缓）</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类用到的部分，逐条核验过）</h2>
 * <table border="1">
 *   <caption>改名 / 结构变化</caption>
 *   <tr><th>1.20.4（Yarn）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)}</td><td>「父级 = 橡树树叶」</td></tr>
 *   <tr><td>{@code LeavesBlock}（可直接继承）</td>
 *       <td><b>{@code LeavesBlock} 是 {@code public abstract}</b>，多了两个抽象方法：
 *       {@code codec()} 与 {@code spawnFallingLeavesParticle(Level, BlockPos, RandomSource)}</td>
 *       <td>26.2 起原版给树叶「飘落粒子」加了钩子，所以不能直接继承它（原因见下）</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap.putBlock(..., cutoutMipped())}</td>
 *       <td><b>已删除</b></td><td>26.1 起透明档由<b>贴图 Alpha 的取值集合</b>自动判定</td></tr>
 *   <tr><td>{@code slowMovement(...)}</td><td>{@code Entity.makeStuckInBlock(state, vec)}</td>
 *       <td>方块内减速（三块通灵木料共用 {@link RDT_SpectrePassThrough#dragInside}）</td></tr>
 *   <tr><td>{@code ShapeContext}</td><td>{@code CollisionContext}</td>
 *       <td>碰撞上下文；26.2 的 {@code EntityCollisionContext#getEntity()} 是 public，
 *       所以「实体查询 vs 非实体查询」判得出来（统一规则的第二条分支就靠它）</td></tr>
 * </table>
 *
 * <h2>⭐ 为什么继承 {@code UntintedParticleLeavesBlock} 而不是 {@code LeavesBlock}</h2>
 * 26.2 的 {@code LeavesBlock} 是<b>抽象类</b>（逐字节核验 {@code minecraft-merged-deobf-26.2.jar}）：
 * <pre>{@code
 * public abstract MapCodec<? extends LeavesBlock> codec();                       // 抽象，必须实现
 * protected abstract void spawnFallingLeavesParticle(Level, BlockPos, RandomSource);  // 抽象，必须实现
 * protected LeavesBlock(float leafParticleChance, BlockBehaviour.Properties properties);  // 注意 float 在前
 * }</pre>
 * 两个抽象方法都是因为 26.2 新增了「飘落树叶粒子」（{@code animateTick} → {@code makeFallingLeavesParticles}）：
 * 粒子怎么画、画成什么颜色，由子类决定。原版因此给了两个现成的具体子类
 * （{@code UntintedParticleLeavesBlock} = 固定粒子；{@code TintedParticleLeavesBlock} = 按方块染色），
 * 原版自己的 {@code oak_leaves} / {@code birch_leaves} 用的就是 {@code TintedParticleLeavesBlock}。
 * <p>本方块取<b>不染色的那个</b>：通灵树叶是一张画好的淡蓝半透明贴图，<b>不参与生物群系染色</b>，
 * 所以固定粒子才是对的选择（也省掉层 C 的颜色提供器接线）。这就是范式 §3 第 0 级
 * 「继承<b>对的</b>父类」——多下一层选择（继承 {@code LeavesBlock}）反而要自己写两份抽象实现。
 *
 * <h2>方块状态：照抄原版（28 个状态）</h2>
 * 状态全部由父类提供，本类<b>一行都不写</b>：
 * <pre>{@code
 * DISTANCE     = BlockStateProperties.DISTANCE      // 1~7，7 个取值
 * PERSISTENT   = BlockStateProperties.PERSISTENT    // 布尔（人为放置 = true）
 * WATERLOGGED  = BlockStateProperties.WATERLOGGED   // 布尔
 * }</pre>
 * 7 × 2 × 2 = <b>28</b>，与原版 {@code oak_leaves} 完全一致。
 * 三者在 {@code LeavesBlock.<clinit>} 里是 {@code BlockStateProperties} 同名常量的<b>别名</b>
 * （已核验：{@code getstatic BlockStateProperties.DISTANCE → putstatic LeavesBlock.DISTANCE}），
 * 所以与 BHR 那边一样：<b>要用原版共享常量，别自建同名的 Property</b>，否则 identity 检查对不上。
 *
 * <h2>⭐⭐ 枯萎（凋零）机制：完全继承原版，本类不碰</h2>
 * wiki 的「消失流程」= 原版树叶的距离机制，逐条核验 26.2 源码结构后确认<b>不需要任何覆写</b>：
 * <pre>{@code
 * decaying(state)        = !PERSISTENT && DISTANCE == 7      // 判定该不该掉
 * randomTick(...)        → decaying ? { dropResources(...); level.removeBlock(pos, false); } : 不长
 * isRandomlyTicking(...) = PERSISTENT == false && DISTANCE != 7   // 稳定后不再 tick
 * updateShape(...)       → updateDistance(...)                    // 邻居变化时重算距离场
 * getStateForPlacement() → PERSISTENT = true                      // 人为放置 = 持久
 * }</pre>
 * 「距离」是<b>图连通距离</b>而不是欧氏距离：邻居里只要有<b>能阻断凋零的方块</b>就取 0，
 * 其余取「邻居的距离 + 1」，取六个方向的最小值。所以砍掉树干后，树叶会一圈一圈（7 → 6 → … → 1）
 * 逐层传下去，最外层（{@code DISTANCE == 7}）先掉 —— 这正是原版树叶的连锁枯萎。
 *
 * <h2>⚠️⚠️ 关键坑（本项目实测发现）：通灵原木必须进 {@code #minecraft:logs}</h2>
 * 26.2 的「什么方块算树干」<b>不再直接读 {@code BlockTags.LOGS}</b>，而是读一个新标签
 * {@code BlockTags.PREVENTS_NEARBY_LEAF_DECAY}（{@code getOptionalDistanceAt} 里
 * {@code getstatic net/minecraft/tags/BlockTags.PREVENTS_NEARBY_LEAF_DECAY}，逐字节核验），
 * 而它的原版定义是<b>一条转发</b>：
 * <pre>{@code
 * // data/minecraft/tags/block/prevents_nearby_leaf_decay.json
 * { "values": [ "#minecraft:logs" ] }
 * }</pre>
 * 两者是等价集合，但<b>模组方块得自己进标签</b>——原版方块进 {@code #minecraft:logs} 是数据包写死的。
 * 因此本模组补了一份 {@code data/minecraft/tags/block/logs.json}，把 {@code rdt_spectre_log} 加进去
 * （标签在多份数据包之间默认是<b>并集</b>，不写 {@code "replace": true} 就不会挤掉原版那些原木）。
 * <p><b>不加会怎样</b>：通灵树苗长出的树里，树叶算不出「旁边有树干」→ 距离全为 7 →
 * 长出来的瞬间就开始连锁枯萎，整棵树「刚长出来就秃」。
 * <p>另注：{@code #minecraft:logs} 只解决<b>功能</b>。挖掘工具标签（{@code mineable/axe}、
 * {@code mineable/hoe}、{@code sword_efficient}）与战利品表同属「获取方式」阶段，全项目暂缓。
 *
 * <h2>水为什么冲不掉它（与通灵原木的处置不一样，2026-10 统一后依然如此）</h2>
 * 通灵原木 / 通灵木板是「空碰撞形状」+ {@code forceSolidOn()} 才挡住水的（见
 * {@link RDT_SpectrePassThrough#waterProof} 与 {@code RDT_TreeLogSpectre} 的长注释）。
 * 树叶<b>不需要、也不能</b>那一条：它实现了 {@code SimpleWaterloggedBlock}，而该接口继承
 * {@code LiquidBlockContainer}（已核验接口表：{@code [BucketPickup, LiquidBlockContainer]}）。
 * 水流判定链 {@code FlowingFluid.canHoldAnyFluid} 的<b>第一条</b>就是
 * {@code if (Block instanceof LiquidBlockContainer) return true;}，于是水走的是
 * 「{@code placeLiquid} → 变成含水树叶」，而不是「销毁方块」。原版树叶不被水冲掉就是这个原因。
 * <p>⚠️ <b>加了 {@code forceSolidOn()} 反而有害</b>：它会把 {@code blocksMotion()} 钉成 true，
 * 连带把树叶的「寻路可穿过」与「流体可替换」一起改掉 —— 原版树叶不是那个手感。
 * 所以本方块调 {@link RDT_SpectrePassThrough#passThrough} 但<b>不调</b>
 * {@link RDT_SpectrePassThrough#waterProof}，这条分工写在该类的对照表里。
 *
 * <h2>渲染：透明档自动判定（层 C 不需要任何接线）</h2>
 * 贴图 {@code rdt/spectre_trsp_leaves.png} 的 Alpha 实测只有<b>两个取值：0（42.19%）与 195（57.81%）</b>
 * （16×16，脚本统计）。26.1 起的渲染档判定看的是<b>Alpha 取值集合</b>：
 * <ul>
 *   <li>{@code {0, 255}} → 镂空（cutout）—— 隔壁的 {@code spectre_leaves.png} 就是这一档；</li>
 *   <li>出现<b>既非 0 也非 255</b> 的值 → <b>半透明（translucent）</b> —— 本方块用的 {@code trsp} 那张正是这一档，
 *       与通灵原木（{@code spectre_trsp_log_side.png} 整图 Alpha 恒 195）同类。</li>
 * </ul>
 * 所以「树叶用带 trsp 的那张贴图」不是审美偏好，而是<b>让渲染档落在半透明上的唯一开关</b>。
 * 模型用 {@code block/cube_all}（后果与理由见「关键机制」末条），六个面都带 {@code cullface}，
 * 于是 {@code skipRendering} 那条「同类贴合面不渲染」才真正生效（范式 §7 第 5 条）。
 *
 * <h2>关键机制与踩坑</h2>
 * <ol>
 *   <li><b>行走阻力</b>：wiki 只说「稍有阻力」，没给数。取 <b>0.85（三轴同乘）</b>，
 *       三块通灵木料共用 {@link RDT_SpectrePassThrough#INSIDE_DRAG} 一个常量 ——
 *       同一种树的三块手感应当一致。</li>
 *   <li><b>碰撞：本类在 2026-10 改成了「与木料同一套规则」</b>。原先照抄原版树叶——形状恒为空；
 *       现在转发到 {@link RDT_SpectrePassThrough#collisionShape}，于是多出「<b>顶面站得住</b>」
 *       与「按住 Shift 从顶面穿下去」，而<b>侧面与身位在方块内仍然穿得过</b>（那两条分支没变）。
 *       <br>唯一需要填的是「父级形状」：树叶的父级是橡树树叶，原版树叶的碰撞形状本来就是空的，
 *       所以这里填 {@link Shapes#empty()} —— 于是「寻路可穿过 / 不是红石导体 / 顶面不是支撑面」
 *       这些<b>非实体查询</b>的结果继续与原版树叶一致，本次只多了「站上去」这一条。
 *       （对照：木料那两块填 {@link Shapes#block()}。）</li>
 *   <li><b>四行「✗」仍然一行属性都不用自己写</b>：{@code ofFullCopy(Blocks.OAK_LEAVES)}
 *       本来就带 {@code noOcclusion / isSuffocating(never) / isValidSpawn(never) / isViewBlocking(never)}，
 *       这正是「父级 = 橡树树叶」的收益。
 *       <br>附带好处：{@code isViewBlocking(never)} 也在父级里，所以本方块<b>不会</b>踩通灵原木那次
 *       「穿过时满屏一张方块贴图」的坑（原版「方块屏幕效果」的采样条件就是它）。
 *       <b>2026-10 统一后这一条更重要</b>：非实体查询的兜底形状变成整块，
 *       若没有 {@code isViewBlocking(never)} 兜着，屏幕效果就会把树叶贴图铺满整屏。</li>
 *   <li><b>飘落粒子</b>：26.2 新增。本类给 {@code 0.01F} 的几率与固定粒子
 *       {@code ParticleTypes.PALE_OAK_LEAVES}（淡白叶片，与「通灵」观感一致）。
 *       <b>0.01F 是照原版橡树树叶档位取的工程值</b>，未逐字节核对原版该处实参；
 *       想要更多/更少飘叶就改这一条。</li>
 *   <li><b>codec 用继承来的那份</b>：{@code UntintedParticleLeavesBlock.CODEC} 已经实现
 *       {@code (float, ParticleOptions, Properties)} 的序列化，本类没有新增构造参数，
 *       所以<b>不必</b>像 {@code MangroveLeavesBlock} 那样再声明一个 CODEC。
 *       代价是「按 block type 反序列化会得到父类型」—— 与范式 §8 TODO 第 1 条同源
 *       （{@code BLOCK_TYPE} 全项目未登记，实测无症状）。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_spectre_leaves}　<b>模块</b> RDT　<b>父级</b> 橡树树叶
 *       （<b>层 6 家族</b>：继承原版 {@code UntintedParticleLeavesBlock}，三状态 + 枯萎 + 飘落粒子全白拿）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（粒子几率 / 可燃性；减速乘数与碰撞规则已<b>上提</b>到
 *       {@link RDT_SpectrePassThrough}）、层 9 交互与判定（{@code entityInside} 减速转发）、
 *       层 10 形状与外观（{@code getCollisionShape} 转发共用规则，2026-10 新增）、
 *       层 12 额外注册表（{@code FlammableBlockRegistry}）</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（父类给全）、层 4 属性工厂只做「复制父级 + 公共开关」、
 *       层 5 构造器只做装配、层 7 codec（父类已实现）、层 8 生命周期（枯萎全在父类）、
 *       层 11 预留区、层 C 客户端（26.1 起无需接线）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：用到第 0 级（继承家族类）、第 1 级（属性复制 + 公共开关）、
 *       第 2 级（{@code entityInside} + {@code getCollisionShape} 两处覆写）、
 *       第 3 级（可燃性登记表 + {@code #minecraft:logs} 标签）—— 最高停在<b>第 3 级</b>，<b>没有</b>动 Mixin。
 *       为什么停在第 3 级：可燃性原版没有「按方块设」的 {@code Properties} 开关，
 *       树干识别是纯数据驱动（标签），两者在模组侧都只能走「登记 / 数据」这一级。</li>
 *   <li><b>共享实现</b>：碰撞形状与方块内减速已与通灵原木 {@link RDT_TreeLogSpectre}、
 *       通灵木板 {@link RDT_TreePlanksSpectre} 统一到 {@link RDT_SpectrePassThrough}（2026-10）。
 *       本方块与那两块的两处差别：父级形状填 {@link Shapes#empty()}（不是整块）、
 *       <b>不调</b> {@code waterProof()}（可含水的方块不能 forceSolidOn）。</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明
 *       （{@code DISTANCE / PERSISTENT / WATERLOGGED} 是多状态，blockstate 手写，
 *       且照抄原版写法 = 一个 <b>catch-all 变体 {@code ""}</b>）　③☒ 物品模型无例外（物品图标直接用方块模型）
 *       　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}（「木材」组，排在通灵原木之后）
 *       　⑥☑ 贴图 {@code rdt/spectre_trsp_leaves.png}（现成）；模型 {@code models/block/rdt_spectre_leaves.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做
 *       （<b>例外</b>：{@code data/minecraft/tags/block/logs.json} 本轮已落地，因为它是枯萎功能的必需项，不是获取方式项）</li>
 * </ul>
 */
public class RDT_TreeLeavesSpectre extends UntintedParticleLeavesBlock {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 飘落树叶的几率（26.2 父类构造器的第一个形参，已由字节码核对形参顺序：
	 * {@code (float leafParticleChance, ParticleOptions leafParticle, Properties)}）。
	 * <p>取原版橡树树叶那一档的 {@code 0.01F}（工程值，未逐字节核对原版实参）——
	 * 含义是每次 {@code animateTick} 有 1% 几率飘一片叶子。
	 */
	private static final float LEAF_PARTICLE_CHANCE = 0.01F;

	/**
	 * 引燃几率（Fabric {@code FlammableBlockRegistry} 的 {@code igniteOdds}）。
	 * <p>wiki 明文写 60（不是「父级」）：比通灵原木的 30 高一档，与树苗同档。
	 * 参考值：原版树叶 / 羊毛 (30, 60)、原版树苗 (60, 100)。
	 */
	private static final int IGNITE_ODDS = 60;

	/** 烧毁几率（{@code burnOdds}）：wiki 明文写 100，与树苗同档。 */
	private static final int BURN_ODDS = 100;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SPECTRE_LEAVES;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 通灵树叶的属性（层 4）：复制原版橡树树叶 + 三块共用的公共开关。
	 *
	 * <p>wiki「构造」表里只有四行不是「父级」（窒息 ✗、视野阻挡 ✗、光线阻挡 ✗、表面生成生物 ✗），
	 * 这四行原版橡树树叶的属性里全都有，{@link RDT_SpectrePassThrough#passThrough} 只是把它们
	 * 显式再写一遍（幂等），换来的是三块通灵木料的属性来源<b>读起来一模一样</b>。
	 *
	 * <p>⚠️ 与另两块的<b>关键差别</b>：这里<b>没有</b> {@code waterProof()}（即 {@code forceSolidOn()}）。
	 * 树叶可含水，水的处理走原版水logged 那条路，加了 forceSolidOn 反而会改掉寻路手感 ——
	 * 理由写在 {@link RDT_SpectrePassThrough#waterProof} 的注释里。
	 */
	public static BlockBehaviour.Properties spectreLeavesProperties() {
		return RDT_SpectrePassThrough.passThrough(
				BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5）：只做装配。
	 * <p>父类构造器是 {@code UntintedParticleLeavesBlock(float leafParticleChance, ParticleOptions, Properties)}
	 * —— 形参顺序由字节码核对过（形参 #1 落进父类的 {@code leafParticleChance}，形参 #2 落进 {@code leafParticle}）。
	 * <p>粒子选 {@code ParticleTypes.PALE_OAK_LEAVES}（淡白飘落叶片）：本方块<b>不参与生物群系染色</b>
	 * （贴图是自己画的淡蓝半透明图），所以固定粒子比染色粒子更贴切，也省掉一个客户端颜色提供器。
	 * <p>三个方块状态的初值（{@code DISTANCE=7 / PERSISTENT=false / WATERLOGGED=false}）由父类
	 * {@code registerDefaultState} 定死，本类不需要也不能再定一次。
	 */
	public RDT_TreeLeavesSpectre(BlockBehaviour.Properties properties) {
		super(LEAF_PARTICLE_CHANCE, ParticleTypes.PALE_OAK_LEAVES, properties);
	}

	// ── 层 9 ｜ 交互与判定 ──
	/**
	 * 方块内减速（层 9）：wiki 的「玩家可以无窒息但稍有阻力地穿过」。
	 * <p>转发给三块共用的 {@link RDT_SpectrePassThrough#dragInside}（乘数 0.85，三块同值）。
	 */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean inside) {
		RDT_SpectrePassThrough.dragInside(entity, state);
	}

	// ── 层 10 ｜ 形状与外观 ──
	/**
	 * 碰撞形状（层 10，<b>2026-10 新增</b>）：<b>顶面可站立；按住 Shift 从顶面穿下去；
	 * 侧面与身位在方块内一律穿得过</b>。
	 *
	 * <p>此前本方块照抄原版树叶（形状恒为空、只有「稍有阻力地穿过」）；现在与通灵原木 /
	 * 通灵木板统一到 {@link RDT_SpectrePassThrough#collisionShape}，就多出了「顶面站得住」。
	 * 规则本体与四个分支的理由都在那边（三块共用，别在这里抄第二份）。
	 *
	 * <p>本类只填第三个参数 —— <b>父级形状</b>：树叶的父级是橡树树叶，而原版树叶的碰撞形状
	 * 本来就是空的，所以填 {@link Shapes#empty()}。这个参数只影响<b>非实体查询</b>
	 * （光照 / 支撑 / 渲染 / 红石 / 寻路），所以填空 = 树叶在这些方面保持原版行为；
	 * 实体拿到的永远是「顶面整块 or 空」，与它无关。
	 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return RDT_SpectrePassThrough.collisionShape(pos, context, Shapes.empty());
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品，并登记可燃性（层 12）。
	 *
	 * <p>可燃性走 Fabric 的 {@link FlammableBlockRegistry}（26.2 版形参名就是
	 * {@code igniteOdds, burnOdds}，对应原版 {@code FireBlock.setFlammable} 的同名参数）——
	 * 原版没有「按方块设可燃性」的 {@code Properties} 开关，只能这样登记，
	 * 且必须在模组初始化阶段、方块本体注册之后。
	 *
	 * <p>⚠️ 别指望「父级 = 橡树树叶」把可燃性一起复制过来：{@code ofFullCopy} 复制的是
	 * 方块属性，火焰那张表是<b>另一套注册表</b>，模组方块不在里面。
	 * <p>⚠️ 本方块能不能枯萎，取决于 {@code rdt_spectre_log} 在不在 {@code #minecraft:logs} 里
	 * （见 {@code data/minecraft/tags/block/logs.json}），与本方法无关。
	 */
	public static void OnInit() {
		RDT_SPECTRE_LEAVES = ModRegister.registerBlockWithItem("rdt_spectre_leaves",
				new RDT_TreeLeavesSpectre(spectreLeavesProperties()
						.setId(ModRegister.blockKey("rdt_spectre_leaves"))));

		// 引燃几率 60 / 烧毁几率 100（wiki 那两行明文值）
		FlammableBlockRegistry.getDefaultInstance().add(RDT_SPECTRE_LEAVES, IGNITE_ODDS, BURN_ODDS);
	}
}