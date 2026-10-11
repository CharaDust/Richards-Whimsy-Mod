package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_ExpertMode;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * 松木树叶（{@code bhr_pine_leaves}）—— BHR 的树叶方块，wiki 标注「具有生长性」。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_TreeLeavesPine.md}）：
 * <ul>
 *   <li>父级：橡木树叶；「构造」表逐行都是「父级」</li>
 *   <li>渲染：透明方块；改写方法：环境光遮蔽与树叶一致、返回透明方块状态</li>
 *   <li>方块状态：{@code PERSISTENT}（人为放置 TRUE）、{@code WATERLOGGED}（机制同原版）、
 *       {@code BHR_HORMONE_TYPE}（NONE / DOMINANT / SUPPRESSIVE；人为放置 NONE）、
 *       {@code BHR_HORMONE_COUNT}（[0,15]；人为放置 0）</li>
 *   <li><b>事件</b>（wiki 2026-10 已把这两条从「备注」移入「事件」小节，本类均已实现）：
 *       <ol>
 *         <li><b>消失流程</b>（方块更新时）：周围一格（xyz±1）无<b>支撑</b>且 {@code persistent=false}
 *             → 按存档游戏模式分流（原版极限 / BHR 专家 → 直接消失；其余 → 带掉落物破坏）。
 *             支撑清单 = 松木树叶 + 全部可种植基底（wiki 2026-10 把「土壤也算支撑」补了进去）。
 *             <b>{@code suppressive} 叶另有一套</b>（wiki 2026-10，<b>二次修正为几何判定</b>）：水平 xz±1 内
 *             既没有松木原木、也没有<b>更靠近圆心</b>的松木树叶 → 消失（等价于「没连在树上」）。
 *             同一套判定<b>在随机刻里还会再跑一次作兜底</b>（2026-10：远端高激素叶可能收不到更新），
 *             兜底触发时<b>直接消失、不掉落</b>；方块更新那条路上，非极限 / 非专家模式只有
 *             <b>1/5 走带掉落物破坏</b>，其余 4/5 也是直接消失；</li>
 *         <li><b>生长</b>（随机刻时）：{@code persistent=false & type!=none & count!=0}，
 *             再<b>按激素类型分派方向</b>（<b>wiki 2026-10 更正：原文两条先决条件写成 {@code !=} 是笔误</b>）：
 *             ①{@code type=dominant} → 向上生长（<b>不掷骰子</b>，2026-10 起去掉 50% 判定；
 *             还要「上方空气<b>或松木树叶</b>（2026-10：可顶穿已有树冠，否则两棵树挨着种时后一棵会被顶住）
 *             + 下方可种植基底」，
 *             然后<b>三条动作同时做</b>：上方长叶 / 四向铺 {@code suppressive} 种子叶
 *             （计数 = 当前计数的 <b>1/3 向下取整</b>，wiki 2026-10 由 1/2 改）/ 自身变松木原木；
 *             种子叶只在<b>离土壤生长方块 &gt; 3 格</b>时才铺，保留底部裸露的树干）；
 *             ②{@code type=suppressive} → 向外侧生长（<b>1/3 几率</b>，wiki 2026-10 由 1/2 降；
 *             距离场：计数 = 半径 − 到树干中心的距离<b>取整</b>，<b>半径来源只看 {@code suppressive} 叶</b>
 *             （2026-10 修正：混入邻树顶端的高激素 {@code dominant} 叶会把树冠撑成巨圆），
 *             <b>并且只往离圆心更远的格子长</b>（2026-10：与存活判定互补，杜绝抖动），
 *             按空位统一决策，逐圈铺出近似的圆，见 {@code growSideways} 的注释）。</li>
 *       </ol></li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类用到的部分）</h2>
 * <table border="1">
 *   <caption>改名 / 语义变化</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)}</td><td>「父级 = 橡木树叶」</td></tr>
 *   <tr><td>{@code Waterloggable} 接口</td>
 *       <td>{@code SimpleWaterloggedBlock} 接口</td>
 *       <td>白拿 {@code canPlaceLiquid / placeLiquid / pickupBlock / getPickupSound} 四个默认实现；
 *       <b>但 {@code WATERLOGGED} 常量不在接口里</b>，必须自己声明（见层 3）</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel(...)}</td>
 *       <td>{@code getShadeBrightness(...)}</td><td>环境光遮蔽亮度；树叶取值 0.2F</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap.putBlock(..., cutoutMipped())}</td>
 *       <td><b>已删除</b></td><td>26.1 起透明由贴图 Alpha 自动决定（详见 {@code DEV_GhostBlock} 对照表）</td></tr>
 * </table>
 *
 * <h2>关键机制与踩坑</h2>
 * <ol>
 *   <li><b>为什么不继承 {@code LeavesBlock}</b>：原版叶子自带 {@code DISTANCE}(0~7) 状态与距离衰减 tick，
 *       与本 wiki 的状态清单（只有 PERSISTENT / WATERLOGGED / 两个 BHR_ 状态）不符；且备注里的消失逻辑是
 *       「周围一格无松木树叶 / 松木原木」的自定义判定，不是原版距离传播。所以只借「父级属性」，不借
 *       「父级类」——这正是范式 §3 第 0 级那句「继承<b>对的</b>父类」的反面用法。</li>
 *   <li><b>PERSISTENT 默认值</b>：默认状态保持 {@code false}（自然生成时交给将来「消失流程」接管），
 *       人为放置时由 {@link #getStateForPlacement} 落成 {@code true} —— 与原版叶子一致。
 *       常量取原版共享的 {@code BlockStateProperties.PERSISTENT}，不要用
 *       {@code BooleanProperty.create("persistent")} 自建实例：序列化名虽同，identity 检查对不上。</li>
 *   <li><b>WATERLOGGED 必须自己声明</b>：26.2 的 {@code SimpleWaterloggedBlock} 接口里没有这个常量
 *       （它内部一律用 {@code BlockStateProperties.WATERLOGGED}）；照旧 Yarn 时代「接口自带」的写法
 *       直接引用，就是本类最初那三处「找不到符号」编译错误。原版 {@code LeavesBlock} / {@code SlabBlock}
 *       也都是各自声明一行。</li>
 *   <li><b>含水的两条不能省</b>：{@link #getFluidState}（否则 WATERLOGGED 只是装饰状态，水不显示也不
 *       参与流体判定）与 {@link #updateShape} 里的水刻调度（原版 {@code LeavesBlock.updateShape} 第一段）。
 *       这两条与「消失流程」无关，是 WATERLOGGED 的完整实现。</li>
 *   <li><b>消失流程（已实现，wiki「事件」第 1 条）</b>：{@code neighborChanged} 只负责<b>排计划刻</b>，
 *       真正的删除在 {@code tick} 里做 —— 一次删除会引发周围 6 格的邻居更新，在
 *       {@code neighborChanged} 里直接删就是递归，一片大树叶云足以把栈吃穿；排成 1 tick 之后
 *       每次消失都是独立的一刻，连锁照样发生而栈深度恒为 1。
 *       分流：原版极限模式 / BHR 规则 {@code richards-whimsy-mod:bhr_decaying_leaves_no_drop}
 *       → {@code removeBlock}（不掉落）；
 *       其余 → <b>1/5 几率</b> {@code destroyBlock(pos, true)}（走战利品表），4/5 也是 {@code removeBlock}
 *       （wiki 2026-10：减少粒子、鼓励上手挖树叶）。两者都是 flag 3，wiki 的
 *       「触发周围方块更新」自带。该规则由 {@link BHR_ExpertMode#leavesNoDrop} 读取 ——
 *       读的是细调条目本身，不是 {@code bhr_expert_mode} 开关（理由见 {@link #isVanishWithoutDrops}）。
 *       <p><b>随机刻兜底</b>（wiki 2026-10）：{@code randomTick} 开头会用同一套 {@link #shouldVanish}
 *       再判一次「该不该存在」，不该就 {@code removeBlock} —— <b>直接消失、不掉落</b>。
 *       它存在的意义是「密集种植时远端的叶收不到方块更新」；正常长出来的树走不到这里
 *       （侧路叶子与树干同时生成，判定必然通过）。
 *       <p>支撑清单里<b>土壤也算</b>（wiki 2026-10 更新）：{@link #hasSupport} 复用
 *       {@link #isPlantableBase}，于是「长在土壤上的叶子」不会被判死 —— 砍掉树干后，
 *       树冠里的叶子周围既没有原木也没有土壤，照样连锁消失。
 *       <p><b>{@code suppressive} 叶换了一套判定</b>（wiki 2026-10）：水平 xz±1 内要有原木、
 *       或一片<b>更靠近圆心</b>的叶，否则消失。因为树冠的计数越靠圆心越大，「旁边有更靠内的」
 *       就等于「还有通向圆心的内侧邻居」，而圆心处正是原木 —— 这套判定描述的是<b>连通性</b>，
 *       和上面那套「支撑」是两回事，所以按类型分开走（见 {@link #hasOutwardSupport}）。
 *       <p><b>踩过的坑：存活判定与生长判定必须互相咬合。</b>生长侧原先把「{@code round(d) ≤ R}」
 *       当作唯一条件，于是会往圆内补洞；补进洞里的叶内侧往往也是洞，水平邻圈里找不到激素更高的叶
 *       → 被判死 → 生长又把它补回来，<b>树叶就在这些位置反复出现和消失</b>（密集种植时才出现的局面：
 *       树冠互相重叠、顶穿、局部脱落后留下洞）。修法两步：存活判定改用几何「更靠近圆心」，
 *       生长侧加「只往圆外长」—— 两边互为补集，新叶必然有更靠内的邻居。
 *       <p>{@code PERSISTENT=true} 在两种判定里都保留「不凋零」：wiki 把它定义为「人为放置」的标记，
 *       原版手放树叶也不凋零；想让 suppressive 无条件凋零，去掉 {@link #shouldVanish} 里那个 if 即可。</li>
 *   <li><b>生长（已实现，wiki「事件」第 2 条）</b>：共享前置是「非持久 + 类型非 NONE + 计数非 0」，
 *       然后<b>按类型分派</b>（wiki 2026-10 更正：原文的 {@code !=} 是笔误）——
 *       {@code dominant} 走向上生长（<b>无骰子</b>：过了「上方空气<b>或松木树叶</b> + 下方可种植基底」
 *       门槛就做 —— 「或松木树叶」是 2026-10 为 #3「第二棵树被顶住」加的：是树叶就静默顶掉，
 *       非树叶仍然挡住），
 *       {@code suppressive} 走向外侧生长（<b>1/3 骰子</b>，2026-10 由 1/2 降）。
 *       <p>向上生长那三条是<b>同时做</b>（2026-10 更正，不是三选一）：上方放一片（计数 -1）、
 *       四向各放一片种子叶（类型转 {@code suppressive}、<b>计数取当前的 1/3 向下取整</b>，
 *       2026-10 由 1/2 改并定为向下取整）、自身变松木原木。
 *       其中②还有一道高度门槛（wiki 2026-10 新增）：<b>离土壤生长方块的距离 &gt; 3 格才铺</b>，
 *       于是树干最下面 3 格只有原木、露出裸干；距离由 {@link #distanceToSoilBase} 沿树干往下数得到
 *       （「生长方块」指红壤 / 原生泥土那一类种树的土，不含原木本身）。
 *       三条自洽：自身变原木后，上方那片的下方正好是原木 → 它下次还能继续长（原木 → 叶 → 原木…）
 *       —— 这正是 wiki 把「松木原木」也列进基底清单的原因；同时圆心处那片 {@code dominant} 叶退场，
 *       树冠那一层的半径来源只剩种子叶（{@code R = 1 + 计数/2}），侧向的距离场才自洽。
 *       <p>向外侧生长用<b>距离场</b>：计数 = 半径 − 到树干中心的水平距离，所以两者可以互相反推。
 *       规则是「按空位统一决策」：对本叶四周每个空格，取相邻各<b>{@code suppressive}</b>叶里 {@code R} 的最大值，
 *       若 {@code round(d) ≤ R} 就长出计数 {@code R − round(d)} 的叶 —— <b>不是</b> {@code H(源) − 1}。
 *       <p><b>踩过的坑：半径来源必须筛类型。</b>第一版不筛，于是相邻另一棵松树顶端的 {@code dominant} 叶
 *       （计数可到 15）被当成半径来源，两棵树挨着种时树冠互相撑成巨圆、远端激素值一度到 7
 *       （正常侧边叶不会超过 {@code 1 + 父叶/3} ≈ 6）。现在只认 {@code suppressive} 叶。
 *       圆心靠 {@link #findTrunk} 在<b>同一层</b>找最近的松木原木得到（每层圆心一定有原木，见上一条）。
 *       <p><b>踩过的坑：距离必须取整。</b>第一版按 {@code H = floor(R − d)} 存、{@code R = d + H} 反推，
 *       实测长出来是<b>菱形</b>：floor 让每次反推的 R 都小一截，轴向格子亏 0（d 是整数）、
 *       斜向格子每步都亏且沿路径累积，于是斜向越长越短。改成 {@code round(d)} 之后
 *       {@code round(d) + H == R} 对每格恰好成立，场整数精确、不再漂移，等值线就是干净的离散圆。
 *       <p>两处刻意处置：① 新叶的 {@code WATERLOGGED} 强制为 {@code false}（wiki 说「其余状态保持不变」，
 *       但新叶长在空气里，带着 {@code WATERLOGGED=true} 会让那一格变成水源）；② 铺叶时只铺到<b>空气</b>
 *       格上，不会覆盖地形。基底清单用<b>懒加载的 {@code Set}（{@link #isPlantableBase}）</b>：
 *       清单里有本模组方块，写死在静态初始化里只会存下一堆 {@code null}。</li>
 *   <li><b>BHR_HORMONE_TYPE 的序列化名</b>：{@code none / dominant / suppressive}。
 *       一旦写进 blockstate 或存档就<b>不能改</b> —— 参照 {@code DEV_StateEnum} 的教训。</li>
 *   <li><b>⭐ blockstate 怎么把 192 个状态压成 2 个模型</b>：外观只有两种 ——
 *       <b>{@code PERSISTENT=TRUE} 或 {@code BHR_HORMONE_TYPE=NONE} 或 {@code BHR_HORMONE_COUNT=0}
 *       → {@code bhr_pine_leaves}</b>；三条都不满足 → {@code bhr_pine_leaves_outside}。
 *       <ul>
 *         <li>这种「或」在 {@code variants} 里表达不了：variants 的键是「属性=单值」的合取
 *             （{@code |} 只在 multipart 的 {@code when} 里可用），而 26.2 对<b>重叠</b>的 variants 键
 *             会直接抛 {@code Overlapping definition on state}；</li>
 *         <li>所以手写的 {@code blockstates/bhr_pine_leaves.json} 用 <b>{@code multipart} 的两条互斥选择器</b>：
 *             ①{@code when = OR[persistent=true, bhr_hormone_type=none, bhr_hormone_count=0]} → 内层模型；
 *             ②{@code when = {persistent=false, bhr_hormone_type=dominant|suppressive,
 *             bhr_hormone_count=1|…|15}} → 外层模型（同一个 {@code when} 对象里的多个键之间是<b>与</b>，
 *             {@code |} 是取值枚举 —— ②正好是①的补集）；</li>
 *         <li>两个语法都在原版数据包里能找到范本：多键 {@code when} + {@code |} 见
 *             {@code minecraft:blockstates/redstone_wire.json}，{@code OR} 见 {@code oak_fence.json}。
 *             {@code !x}（逐值取反）虽然 26.2 的 {@code KeyValueCondition} 也支持（原版没用过），
 *             这里刻意<b>不</b>用它：留一份原版验证过的语法，将来别人改也不容易踩空；</li>
 *         <li>multipart 的命中是<b>叠加</b>（命中几条就渲染几个模型），所以两条必须互为正反，
 *             否则同一格会重叠渲染两块；</li>
 *         <li>{@code WATERLOGGED} 不影响外观 → 不进任何条件（它只影响流体，见第 4 条）。</li>
 *       </ul></li>
 *   <li><b>渲染层 / 上色属于层 C</b>：26.1 起 {@code BlockRenderLayerMap} 已删除（透明由贴图 Alpha 自动
 *       决定），所以本类<b>不需要</b>任何客户端接线；任何方块颜色提供器仍写
 *       {@code RichardsWhimsyModClient}，本类不出现 {@code BlockColorRegistry} 之类。</li>
 *   <li><b>环境光遮蔽取值</b>：{@code BlockBehaviour.getShadeBrightness} 的默认实现是
 *       {@code isCollisionShapeFullBlock ? 0.2F : 1.0F}；树叶是整块碰撞箱 → 原版叶子就是 <b>0.2F</b>
 *       （wiki 里「我忘了值，需要查询」的答案）。本类显式覆写以示契约。</li>
 *   <li><b>propagatesSkylightDown 必须显式覆写</b>：它的默认实现是
 *       {@code !整块形状 && 流体为空}，而树叶形状是整块 → 默认会返回 {@code false}（天光透不过）。
 *       显式返回 {@code true} 才等于 wiki 的「返回透明方块状态」。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_pine_leaves}　<b>模块</b> BHR　<b>父级</b> 橡木树叶
 *       （wiki 页头原写 {@code bhr_pine_leave}（单数），已确认是笔误，按复数落）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（消失延迟、生长骰子）、层 3 状态属性（四个）、
 *       层 8 状态模式与生命周期（{@code createBlockStateDefinition} / {@code getStateForPlacement} /
 *       {@code getFluidState} / {@code updateShape} 的水刻 / <b>{@code neighborChanged} + {@code tick}
 *       （消失流程）/ {@code randomTick}（生长）</b>）、层 10 形状与外观；层 6 家族刻意不走（理由见「关键机制」第 1 条）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 7 codec（继承 {@code Block} 已实现）、层 9 交互与判定、
 *       层 11 预留区（见该层注释）、层 C 客户端（26.1 起无需注册渲染层）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 多状态 + 含水必须覆写；四条属性里三条靠原版共享
 *       常量，最后一条 {@code BHR_HORMONE_TYPE} 是本模组自己的枚举</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()}
 *       里声明（多状态，blockstate 手写）　③☒ 物品模型无例外（物品图标直接用方块模型）
 *       　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}（树木组，排在松木原木之后）
 *       　⑥☑ 贴图 {@code bhr/pine_leaves.png} / {@code bhr/pine_leaves_outside.png}；模型
 *       {@code models/block/bhr_pine_leaves.json} + {@code bhr_pine_leaves_outside.json}；
 *       blockstate 手写（合并写法见「关键机制」第 8 条）
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_TreeLeavesPine extends Block implements SimpleWaterloggedBlock {

	// ── 层 1 ｜ 规格常量 ──

	/** 消失流程的延迟（tick）：排 1 tick 的计划刻（理由见层 8 的 {@code neighborChanged}）。 */
	private static final int VANISH_DELAY_TICKS = 1;

	/**
	 * {@code suppressive} 叶生长的骰子面数：wiki 2026-10「生长几率由 1/2 降为 1/3」→
	 * {@code random.nextInt(SIDE_GROW_CHANCE_DENOMINATOR) == 0}。
	 * <p>骰子只属于这一支：{@code dominant} 已改成「过了门槛就做」，不再掷骰子。
	 */
	private static final int SIDE_GROW_CHANCE_DENOMINATOR = 3;

	/**
	 * 树冠半径上限（wiki 2026-10 新增）。
	 * <p>推导：种子叶的计数 = 父叶计数 / 3（向下取整）≤ 5（计数上限 15），而它在 {@code d = 1} 处，
	 * 于是 {@code R = 1 + 5 = 6} 就是正常情况下的最大半径。
	 * <p>卡这个上限是为了挡住「两棵树挨着种、激素场互相传递把 R 越抬越高」造成的异常：
	 * 实测出现过伸出去好几格、宽度 3 的叶子长条，其计数一路保持 3~5（正常边缘早该归零）。
	 * 手工 {@code /setblock} 出来的大计数叶同样会被卡到 6 —— 有意的取舍：宁可小，不要失控。
	 */
	private static final int MAX_CANOPY_RADIUS = 6;

	/**
	 * 「带掉落物破坏」的骰子面数（wiki 2026-10）：非极限 / 非专家模式下，方块更新触发的消失
	 * 只有 1/5（20%）走带掉落物的破坏，其余 4/5 直接消失。
	 * <p>wiki 的理由：减少粒子效果，并致敬《The Blockheads》—— 树叶主要靠<b>挖</b>来收获掉落物，
	 * 而不是等它自己掉。
	 */
	private static final int VANISH_DROP_CHANCE_DENOMINATOR = 5;

	/**
	 * 找树干（= 这一层树冠的圆心）时的水平扫描半径。
	 * <p>种子叶的半径 {@code R = 1 + 父叶计数/2}，父叶计数上限 15 → {@code R ≤ 8}，
	 * 所以 16 格足够从树冠最外圈看到圆心；取 16 而不是 8 是为了给手工摆出来的大半径
	 * （例如直接 {@code /setblock} 一片 {@code count=15} 的 suppressive 叶）留余量。
	 */
	private static final int TRUNK_SEARCH_RADIUS = 16;

	/**
	 * 底部裸露的树干高度（格）：向上生长时，只有<b>离土壤生长方块的距离 &gt; 此值</b>才铺四周的种子叶。
	 * <p>wiki 2026-10 新增：「为了保留底部裸露的树干」—— 距离 ≤ 3 的几层不长树冠，
	 * 于是树干底下会露出 3 格原木，树看起来才立得起来（否则树冠从地面开始，像一坨蘑菇）。
	 */
	private static final int BARE_TRUNK_HEIGHT = 3;

	/**
	 * 从叶往下找土壤生长方块时的最大扫描格数。
	 * <p>扫描只「穿过松木原木」，碰到别的方块立刻停（见 {@link #distanceToSoilBase}），
	 * 所以实际代价是树干高度；上限只是防呆（例如有人把树干接到基岩下）。
	 */
	private static final int SOIL_SCAN_LIMIT = 64;

	// ── 层 2 ｜ 方块声明 ──

	/** 松木树叶本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_PINE_LEAVES;

	// ── 层 3 ｜ 状态属性字段 ──

	// 2026-10：原 {@code PERSISTENT} 布尔属性已并入 {@link HormoneType} 的 {@code PERSISTENT} 取值
	//（状态数 192 → 128），理由与语义见该枚举的注释。

	/**
	 * 含水状态（wiki「机制与原版相同」）。
	 * <p><b>26.2 必须自己声明这一个常量</b>：{@code SimpleWaterloggedBlock} 只给了四个默认实现，
	 * 接口里没有同名字段（见「关键机制」第 3 条）。原版 {@code LeavesBlock} / {@code SlabBlock} 同样各自声明。
	 */
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	/** BHR 激素类型（层 3）：NONE / DOMINANT / SUPPRESSIVE。序列化名即枚举名小写，不可改。 */
	public static final EnumProperty<HormoneType> BHR_HORMONE_TYPE = EnumProperty.create("bhr_hormone_type", HormoneType.class);

	/** BHR 激素计数（层 3）：范围 [0, 15]（wiki）。 */
	public static final IntegerProperty BHR_HORMONE_COUNT = IntegerProperty.create("bhr_hormone_count", 0, 15);

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/** 松木树叶的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制橡木树叶。 */
	public static BlockBehaviour.Properties pineLeavesProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
				// 冗余但故意留着：橡木树叶本身就带 randomTicks()（原版叶子靠随机刻凋零），
				// 而本类的「生长」也挂在 randomTick 上 —— 写出来才不会被后来的人顺手删掉。
				.randomTicks();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>默认状态刻意保持 {@code PERSISTENT=false}（与自然生成一致；人为放置走下面的
	 * {@link #getStateForPlacement}），{@code WATERLOGGED=false}、激素类型 NONE、计数 0。
	 * <p>{@code registerDefaultState} 只能写在 {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 */
	public BHR_TreeLeavesPine(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(WATERLOGGED, false)
				.setValue(BHR_HORMONE_TYPE, HormoneType.NONE)
				.setValue(BHR_HORMONE_COUNT, 0));
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/** 状态模式（层 8）：声明本方块的四个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, BHR_HORMONE_TYPE, BHR_HORMONE_COUNT);
	}

	/**
	 * 放置逻辑（层 8）：人为放置 → 持久；含水状态取点击位置的水；两个 BHR 状态回到默认值（wiki）。
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return defaultBlockState()
				.setValue(WATERLOGGED, fluidState.is(Fluids.WATER))
				.setValue(BHR_HORMONE_TYPE, HormoneType.PERSISTENT)
				.setValue(BHR_HORMONE_COUNT, 0);
	}

	/**
	 * 流体状态（层 8）：含水时这一格<b>就是水</b>。
	 * <p>不覆写这一条，{@code WATERLOGGED} 就只是个装饰状态（水不显示、不参与流体判定）。
	 * 写法照抄原版 {@code LeavesBlock} / {@code SlabBlock}。
	 */
	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	/**
	 * 邻居更新（层 8）：<b>只</b>保留「含水时把水刻重新排上」这一段（原版 {@code LeavesBlock.updateShape}
	 * 的第一段），否则那一格的水在邻居变化后不会继续按水的规则走。
	 * <p>wiki 备注里的「消失流程」需要邻居 PP 更新 + 自定义消失判定，属 #todo；将来补的时候往这个方法里
	 * 追加，别把水那一段删掉。
	 */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (state.getValue(WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	// ── 层 8 ｜ 消失流程（wiki「事件」第 1 条）──

	/**
	 * 邻居更新（层 8）：<b>消失流程的触发器</b>。
	 * <p>wiki：检测到方块更新时，若周围一格（xyz±1）内没有任何松木树叶 / 松木原木，且自身
	 * {@code persistent=false}，则进入消失流程。
	 * <p>这里刻意<b>只排计划刻、不当场删方块</b>：删一格会引发周围 6 格的邻居更新，而它们又会各自删除……
	 * 在 {@code neighborChanged} 里直接删就是<b>递归</b>，一片大树叶云足以把栈吃穿。排成
	 * {@code scheduleTick} 之后，每次消失都是独立的一刻，连锁反应照样发生（删掉一片会更新到相邻的叶子），
	 * 但栈深度恒为 1。
	 */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			@Nullable Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if (!level.isClientSide() && shouldVanish(state, level, pos)) {
			level.scheduleTick(pos, this, VANISH_DELAY_TICKS);
		}
	}

	/**
	 * 计划刻（层 8）：<b>消失流程的执行点</b>。
	 * <p>1 tick 之后再查一遍条件（这中间可能已经长出了新的松木树叶），然后按存档游戏模式分流：
	 * <ul>
	 *   <li>原版极限 / BHR 规则 {@code bhr_decaying_leaves_no_drop} → {@code removeBlock}（直接消失，不掉落）；</li>
	 *   <li>其余 → <b>1/5 几率</b> {@code destroyBlock(pos, true)}（带掉落物破坏），
	 *       其余 4/5 也是 {@code removeBlock}（wiki 2026-10：减少粒子、鼓励上手挖树叶）。</li>
	 * </ul>
	 * <p>{@code removeBlock} / {@code destroyBlock} 都用 flag 3（邻居更新 + 同步客户端），
	 * 所以 wiki 的「触发周围方块更新」由它们自带，不需要再补一次 {@code updateNeighborsAt}。
	 * <p>⚠️ 战利品表还没做（「获取方式」阶段），所以那 1/5 的实际产物目前也是空 ——
	 * 等战利品表落地，这条分支才会真的有东西掉。
	 */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!shouldVanish(state, level, pos)) {
			return;
		}

		if (isVanishWithoutDrops(level) || random.nextInt(VANISH_DROP_CHANCE_DENOMINATOR) != 0) {
			level.removeBlock(pos, false);
		} else {
			level.destroyBlock(pos, true);
		}
	}

	/**
	 * 消失条件（层 8 的判定助手）：自身非持久，且按类型走各自的存活判定。
	 * <ul>
	 *   <li>{@code suppressive} <b>和 {@code none}</b>：见 {@link #hasOutwardSupport}（wiki 2026-10 的规则，
	 *       水平邻域要有原木或更高的激素，等价于「还连在树上」）——
	 *       {@code none} 也走这套是 2026-10 的修正：退休成 {@code none} 的外圈原本互相支撑，
	 *       砍树后会留下大片不凋亡的树叶；</li>
	 *   <li>{@code dominant}：见 {@link #hasSupport}（周围一格内含松木树叶，或全部可种植基底 ——
	 *       后者让松果种在土壤上的叶子活得过随机刻）。</li>
	 * </ul>
	 * <p>{@code PERSISTENT=true} 一律不凋零：wiki「方块状态」一栏写着它是「人为放置」的标记，
	 * 原版手放树叶也不凋零，所以这条前提在两种判定里都保留（若只想让 suppressive 无条件凋零，
	 * 把这一个 if 去掉即可）。
	 */
	private static boolean shouldVanish(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(BHR_HORMONE_TYPE) == HormoneType.PERSISTENT) {
			return false;
		}

		// suppressive 与 none 都走「连通性」判定；只有正在长树干的 dominant 用宽松的「支撑」判定。
		// 2026-10 修正：none 原先走宽松判定，于是树冠外围「退休」成 none 的那一圈互相支撑，
		// 砍掉树干后整片不凋亡（实测症状：砍完树留下大片树叶，且留下的全是 none）。
		if (state.getValue(BHR_HORMONE_TYPE) != HormoneType.DOMINANT) {
			return !hasOutwardSupport(state, level, pos);
		}

		return !hasSupport(level, pos);
	}

	/**
	 * {@code suppressive} 叶的存活判定（wiki 2026-10）：<b>水平</b>方向周围 xz±1 内只要存在
	 * ① 松木原木，或 ② 一片激素水平<b>严格大于</b>本格的松木树叶，就算活着；两条都没有 → 消失。
	 * <p>实现委托给 {@link #wouldSurvive} —— 生长侧下笔前会用同一个判据做一次「存活预演」，
	 * 两边共用一份逻辑才不会出现「生长放下去、判定又抹掉」的抖动。
	 * <p>找不到圆心不参与判定：本判定只看邻居。整层树冠与树干断开后，由
	 * {@link #randomTick} 的兜底与 {@code hasSupport} 那套接手。
	 */
	private static boolean hasOutwardSupport(BlockState state, LevelReader level, BlockPos pos) {
		return wouldSurvive(level, pos, state.getValue(BHR_HORMONE_COUNT));
	}

	/**
	 * 存活预演（层 8 的助手）：假设 {@code cell} 处有一片计数为 {@code count} 的 {@code suppressive} 叶，
	 * 用<b>现有</b>邻居判断它能不能活 —— 判据就是存活判定本身（旁边有原木，或有一片计数更高的松木树叶）。
	 *
	 * <p>生长只在预演通过时才下笔，于是「生长刚放下、随机刻又抹掉」的抖动从构造上不可能发生
	 * （2026-10 实测过那个抖动：生长原先只看 {@code round(d) ≤ R}，会往圆内的洞里补叶，
	 * 而补进洞里的叶内侧往往也是洞 → 找不到更高的邻居 → 被判死 → 生长再补回来）。
	 *
	 * <p>用 <b>8 邻域</b>（与存活判定同口径）：只查 4 邻域会漏掉斜向的内侧邻居 ——
	 * 圆盘里每个格子恰恰是靠斜向内侧那一片（差两圈）拿到更高的计数的。
	 */
	private static boolean wouldSurvive(LevelReader level, BlockPos cell, int count) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}

				BlockState neighbourState = level.getBlockState(cell.offset(dx, 0, dz));
				if (neighbourState.is(BHR_TreeLogPine.BHR_PINE_LOG)) {
					return true;
				}

				if (neighbourState.is(BHR_PINE_LEAVES)
						&& neighbourState.getValue(BHR_HORMONE_COUNT) > count) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 周围一格（xyz±1，共 26 格；不含自身）内是否有支撑。
	 * <p>wiki「事件 · 消失流程」的清单：松木树叶、松木原木，以及全部<b>可种植基底</b>
	 * （红壤 / 红壤矿土 / 泥土 / 草方块 …）。后一组正好就是 {@link #isPlantableBase} 那份清单，
	 * 所以这里直接复用 —— 语义是「长在土壤上的叶子 = 树苗，不凋零」，同时也让松果
	 * （放 {@code persistent=false} 的叶）种在土壤上时活得过随机刻、有时间长起来。
	 */
	private static boolean hasSupport(LevelReader level, BlockPos pos) {
		for (BlockPos neighbour : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (neighbour.equals(pos)) {
				continue;
			}

			BlockState neighbourState = level.getBlockState(neighbour);
			if (neighbourState.is(BHR_PINE_LEAVES) || isPlantableBase(neighbourState)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否属于「直接消失、不掉落」的模式：原版极限模式，或 BHR 的
	 * {@code richards-whimsy-mod:bhr_decaying_leaves_no_drop} 规则。
	 *
	 * <p>⚠️ 这里读的是<b>细调条目本身</b>，而不是 {@code bhr_expert_mode} 开关。
	 * 这是刻意的：专家模式只是个「一键写预设」的调节工具，玩家完全可以在开过专家模式之后
	 * 再单独把这一条调回普通挡位 —— 那条路必须立刻生效。
	 */
	private static boolean isVanishWithoutDrops(ServerLevel level) {
		return level.getLevelData().isHardcore()
				|| BHR_ExpertMode.leavesNoDrop(level.getServer());
	}

	// ── 层 8 ｜ 生长（wiki「事件」第 2 条）──

	/**
	 * 随机刻（层 8）：<b>生长</b>的入口 —— 先过共享前置，再按激素类型分派方向。
	 * <p><b>wiki 2026-10 笔误更正</b>：两条方向的先决条件原文写作 {@code !=}，实为 {@code =} ——
	 * <b>向上生长 = {@code dominant}</b>，<b>向外侧生长 = {@code suppressive}</b>（见 wiki「事件」小节）。
	 * <p>共享前置（wiki）：{@code persistent=false}、{@code bhr_hormone_type != none}、
	 * {@code bhr_hormone_count != 0}。
	 * <p>两边的「随机性」不一样（wiki 2026-10）：{@code dominant} 不再掷骰子 —— 过了门槛就做；
	 * {@code suppressive} 每次随机刻有 1/3 几率生长（由 1/2 降下来），触发时把相邻空格统一判定一遍。
	 * <p><b>随机刻先做一次「存在性兜底」</b>（wiki 2026-10）：见方法体第一段。
	 */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		// 存在性兜底（wiki 2026-10）：随机刻时再判一次「我此刻该不该存在」，不该就直接消失、<b>不掉落</b>。
		// 为什么需要它：方块更新那条路依赖邻居真的更新过，密集种植时长在远端、激素又高的叶子可能
		// 一次更新都收不到，于是靠随机刻兜底。正常长出来的树走不到这里 —— 侧路叶子与树干同时生成，
		// 判定必然通过（与 {#hasOutwardSupport} 的说法一致）。
		if (shouldVanish(state, level, pos)) {
			level.removeBlock(pos, false);
			return;
		}

		if (!canGrow(state)) {
			// 退休（wiki 2026-10 新增，允许 suppressive → none）：侧向叶把计数用光（= 本次半径已铺到边缘）
			// 之后在随机刻里转成 none。两层意义：
			// ① 不再充当半径来源（maxRadiusAround 只认 suppressive），树冠不会被它继续往外带；
			// ② 存活判定换成更宽松的那套（周围有松木树叶就算活着），边缘格不会再被判死-重生。
			if (state.getValue(BHR_HORMONE_TYPE) == HormoneType.SUPPRESSIVE
					&& state.getValue(BHR_HORMONE_COUNT) == 0) {
				level.setBlockAndUpdate(pos, state.setValue(BHR_HORMONE_TYPE, HormoneType.NONE));
			}

			return;
		}

		switch (state.getValue(BHR_HORMONE_TYPE)) {
			case DOMINANT -> growUpward(state, level, pos);
			case SUPPRESSIVE -> growSideways(state, level, pos, random);
			// none（自然生成/退休）与 persistent（人为放置）都不长，这里只是把枚举写全
			case NONE, PERSISTENT -> {
			}
		}
	}

	/** 生长的共享前置（wiki）：类型是会长的那两种（{@code dominant} / {@code suppressive}）+ 计数非 0。 */
	private static boolean canGrow(BlockState state) {
		HormoneType hormoneType = state.getValue(BHR_HORMONE_TYPE);

		return (hormoneType == HormoneType.DOMINANT || hormoneType == HormoneType.SUPPRESSIVE)
				&& state.getValue(BHR_HORMONE_COUNT) > 0;
	}

	/**
	 * 向上生长（层 8，wiki 生长第 1 条，先决条件 {@code type = dominant}）。
	 * <p>额外两道门槛（wiki 写在这一条下面）：<b>下方是可种植基底</b>（{@link #isPlantableBase}）、
	 * <b>上方是空气或松木树叶</b> —— 后半个条件来自 2026-10 的「允许顶穿」：两棵树挨着种时，
	 * 先长起来那棵的树冠会盖在后一棵的上方，只认「空气」的话后一棵就永远长不起来（#3）。
	 * 是松木树叶就把那一片顶掉（静默替换，不额外掉粒子），非树叶仍然挡住。
	 * <p><b>没有骰子</b>（wiki 2026-10：{@code dominant} 不再走 50% 判定）——
	 * 过了门槛就做，所以树干长起来是稳定的「每个随机刻一节」。
	 * <p><b>2026-10 更正：wiki 那三条是「同时做」，不是三选一</b> ——
	 * ①上方放一片（计数 -1）；②东南西北各放一片种子叶（类型转 {@code suppressive}、计数取半）；
	 * ③自身变松木原木。三条一起完成，长出来的就是「一节原木 + 上方一片继续长 + 四向铺一圈树冠」。
	 * <p>三条自洽：③ 把自己变成原木后，①放到上方的那片叶下方正好是原木 → 它下次还能继续长
	 * （原木 → 叶 → 原木…）—— 这正是 wiki 把「松木原木」也列进基底清单的原因。
	 * <p>三条一起做还顺手解决了另一个问题：③ 让圆心处那片 {@code dominant} 叶退场，
	 * 于是树冠那一层唯一的「半径来源」只剩 ② 铺出去的种子叶（R = 1 + 计数/2）——
	 * 侧向生长的距离场因此是自洽的（否则圆心那片 R = 计数的叶会把半径撑大到计数本身）。
	 */
	private static void growUpward(BlockState state, ServerLevel level, BlockPos pos) {
		BlockState aboveState = level.getBlockState(pos.above());
		if ((!aboveState.isAir() && !aboveState.is(BHR_PINE_LEAVES))
				|| !isPlantableBase(level.getBlockState(pos.below()))) {
			return;
		}

		// 三条同时做；顺序无关紧要（三条都从传入的 state 取值，不回头读 pos 上的新方块）
		placeUpward(state, level, pos);
		placeSideSeeds(state, level, pos);
		becomeLog(level, pos);
	}

	/**
	 * 向上动作 ①：在上方放一片叶，激素计数 -1（其余状态照抄，见 {@link #childState}）。
	 * <p>上方原本可能是松木树叶（2026-10 的「允许顶穿」），这里<b>静默替换</b>：
	 * 不调用 {@code destroyBlock}，因此既不额外掉粒子、也不走进消失流程。
	 */
	private static void placeUpward(BlockState state, ServerLevel level, BlockPos pos) {
		level.setBlockAndUpdate(pos.above(), childState(state, state.getValue(BHR_HORMONE_COUNT) - 1));
	}

	/**
	 * 向上动作 ②：东南西北各放一片种子叶；目标格必须是空气。
	 * <p>计数 = 当前计数的 <b>1/3（向下取整）</b>（wiki 2026-10：先由 1/2 改成 1/3，取整方向定为向下）。
	 * 用整数除法而不是 {@code Math.ceilDiv} / 手写取整：这里要的就是「不够一整份就舍掉」。
	 * <p><b>只在离土壤生长方块的距离 &gt; {@link #BARE_TRUNK_HEIGHT} 时才铺</b>（wiki 2026-10：
	 * 为了保留底部裸露的树干）—— 距离由 {@link #distanceToSoilBase} 沿树干往下数得到，
	 * 所以树干最下面 3 格只有原木、没有树冠。
	 */
	private static void placeSideSeeds(BlockState state, ServerLevel level, BlockPos pos) {
		if (distanceToSoilBase(level, pos) <= BARE_TRUNK_HEIGHT) {
			return;
		}

		BlockState child = childState(state, state.getValue(BHR_HORMONE_COUNT) / 3)
				.setValue(BHR_HORMONE_TYPE, HormoneType.SUPPRESSIVE);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos target = pos.relative(direction);
			if (level.getBlockState(target).isAir()) {
				level.setBlockAndUpdate(target, child);
			}
		}
	}

	/**
	 * 从这片叶往下数，到最近的<b>土壤生长方块</b>有几格（红壤系列 / 原版泥土系列，<b>不含松木原木</b>）。
	 * <p>扫描只穿过松木原木：碰到第一个既不是原木也不是土壤的方块就返回 {@code -1}
	 * （说明这一列不是树干，继续往下找没意义）；超过 {@link #SOIL_SCAN_LIMIT} 也返回 {@code -1}。
	 * <p>不含原木是刻意的：wiki 说的「生长方块」是<b>种树的那块土</b>，
	 * 「距离」是这片叶离地面多高 —— 树干本身不算。
	 */
	private static int distanceToSoilBase(LevelReader level, BlockPos pos) {
		for (int dy = 1; dy <= SOIL_SCAN_LIMIT; dy++) {
			BlockState belowState = level.getBlockState(pos.below(dy));

			if (isSoilBase(belowState)) {
				return dy;
			}

			if (!belowState.is(BHR_TreeLogPine.BHR_PINE_LOG)) {
				return -1;
			}
		}

		return -1;
	}

	/** 是不是「土壤生长方块」：可种植基底里去掉松木原木（wiki 的「红壤系列 / 原版泥土系列」）。 */
	private static boolean isSoilBase(BlockState state) {
		return !state.is(BHR_TreeLogPine.BHR_PINE_LOG) && isPlantableBase(state);
	}

	/** 向上动作 ③：自身变成松木原木（树干取竖直轴向）。 */
	private static void becomeLog(ServerLevel level, BlockPos pos) {
		level.setBlockAndUpdate(pos, BHR_TreeLogPine.BHR_PINE_LOG.defaultBlockState()
				.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
	}

	/**
	 * 向外侧生长（层 8，先决条件 {@code type = suppressive}）—— <b>距离场方案</b>
	 * （2026-10 与另一个 AI 讨论后定下，替换掉本类先前的「盖章铺格」提案）。
	 *
	 * <p><b>计数 = 到树干中心的剩余半径</b>：每个格子的计数满足
	 * {@code H ≈ 半径 R − 到树干中心的水平欧氏距离 d}；反过来，任一片叶子的
	 * {@code R = d + H}，所以「这一层要长到多大」不需要额外的状态，从场本身就能反推。
	 *
	 * <p>规则（按空位统一决策，而不是按源头逐个执行）：
	 * <ol>
	 *   <li>对本叶四周每个空格，取该空格相邻各叶里 {@code R = round(d) + H} 的<b>最大值</b>
	 *       （多源竞争：并列时结果本来就一样，因为目标值只由 R 和 d 决定）；</li>
	 *   <li>若 {@code round(d(空格)) ≤ R}，该空格长出一片松木树叶，计数为 {@code R − round(d(空格))}；</li>
	 *   <li><b>只往圆外长</b>（2026-10）：目标格到圆心的平方距离必须<b>严格大于</b>本格，
	 *       也就是「向外侧」的字面含义 —— 斜向步进天然满足，朝圆心走的一步被挡掉。
	 *       它与 {@link #hasOutwardSupport} 的几何判定互为补集，两边一起杜绝「反复出现和消失」；</li>
	 *   <li>一个空格只长一次（先判空气，再统一写入），不会重复生成。</li>
	 * </ol>
	 *
	 * <p><b>2026-10 修正：距离必须取整，否则场会漂移成菱形</b>（实测踩到过）。原因：
	 * 计数是整数，若按 {@code H = floor(R − d)} 存、再按 {@code R = d + H} 反推，
	 * 反推出来的 {@code R} 永远比真值小一截（亏掉的正是 {@code d} 的小数部分）；
	 * 轴向格子的 {@code d} 是整数、亏 0，而斜向格子每走一步都亏掉一部分且<b>沿路径累积</b> ——
	 * 于是轴向照常长、斜向越长越短，最终等值线退化成菱形。
	 * <p>把两边都换成 {@code round(d)} 之后，{@code round(d) + H == R} 对每个格子<b>恰好成立</b>，
	 * 场是整数精确的、不再累积误差，等值线就是干净的离散圆
	 * （{@code round(d) ≤ R} 即 {@code d ≤ R + 0.5}，是一圈均匀的圆环）。
	 *
	 * <p><b>为什么这样能得到圆</b>：计数相同的点连起来就是同心圆 ——
	 * 四邻生长铺出的是离散圆，边缘有锯齿，整体近似圆。反过来，「每次固定 -1」的步进规则代价与方向无关，
	 * 等值线必然是曼哈顿距离 {@code |x| + |z|}，形状是菱形（八邻域则是方形），永远不圆。
	 *
	 * <p><b>圆心从哪来</b>：状态里只有一个标量，方块自己推不出绝对位置，所以去<b>找树干</b> ——
	 * 见 {@link #findTrunk}。每一层树冠的圆心处一定有原木，因为向上生长的三条动作是同时做的
	 * （其中一条就是「自身变松木原木」），长出树冠的那片叶已经退场成原木了 ——
	 * 这同时保证了树冠那一层的半径来源只剩种子叶（{@code R = 1 + 计数/2}），距离场自洽。
	 *
	 * <p>找不到圆心时本次不铺（宁可不长也不按错误圆心乱铺）；铺出去的目标格只限<b>空气</b>，不覆盖地形。
	 * <p>触发频率：wiki 2026-10「生长几率由 1/2 降为 1/3」——每次随机刻掷一次
	 * {@link #SIDE_GROW_CHANCE_DENOMINATOR} 面的骰子，中了才铺（铺的时候不是只铺一格，
	 * 而是把四周的空格统一判定一遍，见上）。
	 */
	private static void growSideways(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(SIDE_GROW_CHANCE_DENOMINATOR) != 0) {
			return;
		}

		BlockPos trunk = findTrunk(level, pos);
		if (trunk == null) {
			return;
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos target = pos.relative(direction);
			if (!level.getBlockState(target).isAir()) {
				continue;
			}

			// R 卡上限（2026-10）：挡住「两棵树激素场互相传递、R 被越抬越高」造成的异常长条。
			int radius = Math.min(maxRadiusAround(level, trunk, target), MAX_CANOPY_RADIUS);
			if (radius <= 0) {
				continue;   // 四周没有可用的半径来源（兜底：正常不会发生，发起生长的那片叶就在旁边）
			}

			int targetCount = radius - roundedDistanceToTrunk(trunk, target);
			if (targetCount < 0) {
				continue;   // 超出本次半径 → 不在圆盘里
			}

			// 2026-10：只在「新叶放下去就能活」时才下笔 —— 抖动（生长放下 / 随机刻抹掉）的对症解法。
			if (!wouldSurvive(level, target, targetCount)) {
				continue;
			}

			level.setBlockAndUpdate(target, childState(state, targetCount)
					.setValue(BHR_HORMONE_TYPE, HormoneType.SUPPRESSIVE));
		}
	}

	/**
	 * 在同一层（同一个 y）里找树干：最近的松木原木 —— 它就是这一层树冠的圆心。
	 * <p>为什么圆心一定在原木上：向上生长的三条动作是同时做的，其中「自身变松木原木」把长出树冠的那片叶
	 * 换成了原木，所以每层树冠的圆心处一定是一根竖直原木。找到它，方块才能算出「我离圆心多远」——
	 * 只靠自己和邻居的计数推不出绝对位置。
	 * <p>扫描半径 {@link #TRUNK_SEARCH_RADIUS}：种子叶的 {@code R = 1 + 父叶计数/2 ≤ 8}（计数上限 15），
	 * 16 格足够覆盖整层。扫不到（树被砍了、树冠被搬到别层去了）就返回 {@code null}，本次不铺。
	 * <p>先找到近的会把远的直接跳掉（{@code distance >= nearestDistance} 时连方块都不读），
	 * 所以常见情况下（圆心就在旁边）代价远小于扫描盒的面积。同一层若同时有两棵树，
	 * 会取更近的那根原木 —— 半径场可能会被邻居的树带偏，属已知局限。
	 */
	private static @Nullable BlockPos findTrunk(LevelReader level, BlockPos pos) {
		BlockPos nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (int dx = -TRUNK_SEARCH_RADIUS; dx <= TRUNK_SEARCH_RADIUS; dx++) {
			for (int dz = -TRUNK_SEARCH_RADIUS; dz <= TRUNK_SEARCH_RADIUS; dz++) {
				double distance = Math.sqrt(dx * dx + dz * dz);
				if (distance >= nearestDistance) {
					continue;
				}

				BlockPos candidate = pos.offset(dx, 0, dz);
				if (level.getBlockState(candidate).is(BHR_TreeLogPine.BHR_PINE_LOG)) {
					nearest = candidate;
					nearestDistance = distance;
				}
			}
		}

		return nearest;
	}

	/**
	 * 两格之间的水平欧氏距离<b>取整</b>（树冠生长只在同一层，所以不看 y）。
	 * <p>取整口径必须与计数一致：计数存的是 {@code R − round(d)}，反推 R 时也用 {@code round(d)}，
	 * 两边一致 R 才是整数、场才不漂移（见 {@code growSideways} 的注释）。
	 */
	private static int roundedDistanceToTrunk(BlockPos trunk, BlockPos cell) {
		double dx = trunk.getX() - cell.getX();
		double dz = trunk.getZ() - cell.getZ();
		return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
	}

	/**
	 * 空格相邻各叶里「{@code R = round(d) + 计数}」的最大值；没有相邻叶时返回 0（调用点会跳过）。
	 */
	private static int maxRadiusAround(LevelReader level, BlockPos trunk, BlockPos cell) {
		int maxRadius = 0;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos neighbour = cell.relative(direction);
			BlockState neighbourState = level.getBlockState(neighbour);
			// 2026-10 修正：半径来源**只看 suppressive 叶**。原先不筛类型，相邻那棵松树顶端的
			// dominant 叶（计数可到 15，位置又恰好很近）会被当成 R 的来源，树冠被撑成半径十几的巨圆
			// （实测远端的激素值一度到 7，而正常侧边叶的计数不会超过 1 + 父叶/3 ≈ 6）。
			if (!neighbourState.is(BHR_PINE_LEAVES)
					|| neighbourState.getValue(BHR_HORMONE_TYPE) != HormoneType.SUPPRESSIVE) {
				continue;
			}

			maxRadius = Math.max(maxRadius,
					roundedDistanceToTrunk(trunk, neighbour) + neighbourState.getValue(BHR_HORMONE_COUNT));
		}

		return maxRadius;
	}

	/**
	 * 生长出来的子叶状态（层 8 的助手）：照 wiki「其余方块状态保持不变」，但把 {@code WATERLOGGED}
	 * 强制为 {@code false}。
	 * <p><b>这是对 wiki 的一处刻意偏离</b>：新叶长在空气里，而 {@code WATERLOGGED=true} 会让那一格被当成
	 * 水源（{@link #getFluidState} 会返回水，水还会往外扩散）—— 显然不是想要的。
	 * <p>计数用 {@code Math.max(…, 0)} 兜底：wiki 的 {@code -1} / {@code /2} 在 0 附近会算成负数，
	 * 直接 {@code setValue} 会抛异常。
	 */
	private static BlockState childState(BlockState parent, int hormoneCount) {
		return parent.setValue(WATERLOGGED, false)
				.setValue(BHR_HORMONE_COUNT, Math.max(hormoneCount, 0));
	}

	/**
	 * 可种植基底（wiki 清单，含本模组方块）—— <b>松木树叶的「生长」与松果物品的「交互」共用这一份</b>
	 * （wiki 两页列的就是同一批方块，收在一处才不会将来改了一边忘了另一边）。
	 * <p><b>懒加载</b>：清单里有本模组的方块，而它们的静态字段要等各自的 {@code OnInit()} 才赋值 ——
	 * 写死在静态初始化里只会存下一堆 {@code null}。调用只发生在服务端线程，无并发问题。
	 */
	private static Set<Block> plantableBases;

	public static boolean isPlantableBase(BlockState state) {
		if (plantableBases == null) {
			plantableBases = Set.of(
					// bhr
					BHR_TreeLogPine.BHR_PINE_LOG,
					BHR_Soil.BHR_SOIL,
					BHR_OreSoilFlint.BHR_SOIL_FLINT_ORE,
					BHR_OreSoilClay.BHR_SOIL_CLAY_ORE,
					// vanilla
					Blocks.DIRT,
					Blocks.ROOTED_DIRT,
					Blocks.GRASS_BLOCK,
					Blocks.PODZOL,
					Blocks.COARSE_DIRT,
					Blocks.MYCELIUM,
					Blocks.FARMLAND,
					Blocks.MOSS_BLOCK,
					Blocks.PALE_MOSS_BLOCK,
					Blocks.MUD,
					Blocks.MUDDY_MANGROVE_ROOTS);
		}

		return plantableBases.contains(state.getBlock());
	}

	// ── 层 10 ｜ 形状与外观 ──

	/** 环境光遮蔽（层 10）：wiki「和树叶一样」→ 原版叶子的取值 0.2F（见「关键机制」最后两条）。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 0.2F;
	}

	/** 天光透过（层 10）：wiki「返回透明方块状态」—— 默认实现会返回 false，必须显式覆写。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	// ── 层 11 ｜ 未定扩展点（注册前预留区）──
	// TODO(预留)：wiki 备注的「消失流程」落地时，若需要本类之外的新钩子（例如 BHR 专家模式的判定入口），
	//              先在这里占位；一旦确定归属就搬到对应层，别让它烂在这里。

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）：注册方块与方块物品；可燃性沿用橡木树叶属性，暂无需额外 {@code FlammableBlockRegistry} 登记。 */
	public static void OnInit() {
		BHR_PINE_LEAVES = ModRegister.registerBlockWithItem("bhr_pine_leaves",
				new BHR_TreeLeavesPine(pineLeavesProperties()
						.setId(ModRegister.blockKey("bhr_pine_leaves"))));
	}

	/**
	 * BHR 激素类型（层 3 的枚举值）。
	 *
	 * <p>序列化名 {@code none / dominant / suppressive / persistent} 一旦写进 blockstate 或存档即不可更改。
	 * <p>2026-10 <b>合并</b>：原先独立的 {@code PERSISTENT} 布尔属性并到了这里的第 4 个取值上，
	 * 方块状态数从 {@code 2×2×3×16 = 192} 降到 {@code 2×4×16 = 128}。语义按「这块叶子是什么状态」归类：
	 * <ul>
	 *   <li>{@code NONE}：自然生成或树冠退休后的叶子 —— 不长，按连通性判定凋亡；</li>
	 *   <li>{@code DOMINANT} / {@code SUPPRESSIVE}：生长中的叶子（向上 / 向外）；</li>
	 *   <li>{@code PERSISTENT}：<b>人为放置</b>的叶子 —— 不生长、也永不凋亡（原 {@code persistent=true}）。</li>
	 * </ul>
	 * wiki 备注里 {@code DOMINANT / SUPPRESSIVE} 目前共用同一张外纹理，纹理差异走资源层，与本枚举无关。
	 */
	public enum HormoneType implements StringRepresentable {
		NONE("none"),
		DOMINANT("dominant"),
		SUPPRESSIVE("suppressive"),
		PERSISTENT("persistent");

		private final String name;

		HormoneType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
