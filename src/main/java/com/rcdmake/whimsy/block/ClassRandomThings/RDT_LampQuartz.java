package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 石英灯（{@code rdt_quartz_lamp}）—— <b>新增方块</b>（无 1.20.4 前身）。
 * {@link RDT_LampLapis 青金石灯} 的<b>对偶</b>：同一套机制，两个方向反过来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_LampQuartz.md}）：
 * <ul>
 *   <li>父级：红石灯，<b>只取属性</b>（硬度 0.3 / 爆炸抗性 / 玻璃声等）；<b>不接收红石信号</b> ——
 *       本体继承 {@link Block}，不继承 {@code RedstoneLampBlock}</li>
 *   <li>「玩家看起来并没有在发光」：客户端引擎里它的发光等级是 <b>0</b>，
 *       它自己和周围都跟没放灯一样（不写 {@code emissiveRendering}）</li>
 *   <li>「服务端逻辑里是一盏 15 级光源」：刷怪、融冰、作物生长等一切服务端判定里它都在发光</li>
 *   <li>用途（原话）：<b>昏暗山洞景观的隐藏式照明，放置刷怪</b> ——
 *       景观保持昏暗外观，服务端按"有光"处理（不刷怪；冰也会按有光融化、作物按有光生长）</li>
 *   <li>渲染：常规渲染（{@code block/cube_all}）；改写方法 / 事件 / 方块状态：无；获取方式：无</li>
 * </ul>
 *
 * <h2>实现：与青金石灯共用同一套三个 Mixin（能力阶梯第 5 级，层 M）</h2>
 * <ol>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampLightEmissionMixin} ——
 *       {@code BlockLightEngine.getEmission}：<b>服务端引擎 15 / 客户端引擎 0</b>
 *       （侧别看引擎所属的 Level，不看环境侧：单人游戏同进程同 ClassLoader）。</li>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampLightSyncMixin} —— 两侧对齐：
 *       客户端要把服务端包里那 15 级光<b>抹掉</b>（{@code checkBlock} 让引擎按本侧 0 走衰减传播）；
 *       服务端在区块从盘上载入（以及升级前的旧存档）时把缺的光<b>补上</b>
 *       —— 因为状态发光值是 0，原版 {@code propagateLightSources} 不把灯当光源。</li>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampIndexMaintenanceMixin} ——
 *       灯位置索引失效，供两侧对齐时定位灯。</li>
 * </ol>
 * 与青金石灯一样，引擎里存的就是<b>本侧视角的真值</b>：服务端引擎里真的有这盏灯的光，
 * 所以遮挡、与火把等真实光源共存、融冰范围、被其它 mod 读取，全都自然正确 —— 没有"区域近似"。
 *
 * <h2>逐级排除（为什么第 0~4 级都不行）</h2>
 * <ul>
 *   <li><b>1 声明</b>：{@code lightLevel} 是 {@code ToIntFunction<BlockState>}，状态构造时求值一次并缓存，
 *       两侧共用同一个 int，拿不到世界也就分不了侧；{@code emissiveRendering} 只让方块自己（1 格级）变亮。</li>
 *   <li><b>2 覆写</b>：方块类里没有任何方法能改"某一侧看到的光照场"。</li>
 *   <li><b>3 注册表 / 数据驱动</b>：26.2 的 {@code attributes} 是环境属性（维度 / 生物群系 / 时间线），
 *       没有方块级钩子；{@code SpawnPlacements.register} 对同一 {@code EntityType} 重复注册会抛异常。</li>
 *   <li><b>4 事件</b>：Fabric API 没有"光照计算"或"刷怪"事件；光照数据也不经过可取消的事件。</li>
 * </ul>
 * <p>（第一阶段这里曾放一个过渡实现：在 {@code Monster.isDarkEnoughToSpawn} 上补"幻影光"。
 * 它有两条已知偏差 —— 只覆盖敌怪刷怪、且是距离近似会穿墙 —— 第二阶段换成与青金石灯同一套引擎机制后已删除。）
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_quartz_lamp}　<b>模块</b> RDT　<b>父级</b> 红石灯（属性；额外改写
 *       {@code lightLevel} 为 0，<b>不</b>写 {@code emissiveRendering}）</li>
 *   <li><b>启用的可选层</b>：层 4 属性工厂（{@code lightLevel}）、层 M Mixin（与青金石灯共用三个）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族（刻意不继承
 *       {@code RedstoneLampBlock}）、层 7 codec、层 8 生命周期（索引失效已交给 Mixin，本类无覆写）、
 *       层 9 交互与判定、层 10 形状与外观、层 11 预留区、层 C 客户端（<b>刻意的</b>：客户端没有专属接线）</li>
 *   <li><b>能力阶梯</b>：第 5 级「Mixin」；排除理由见上</li>
 *   <li><b>硬约束的落地位置</b>：「服务端 15 / 客户端 0」只在 {@code RDT_LampLightEmissionMixin} 的
 *       {@code whimsy$lampLevelForThisEngine} 里那两个分支；两侧对齐只在 {@code RDT_LampLightSyncMixin}</li>
 *   <li><b>Mixin 三处登记</b>：①☑ 三个类本身　②☑ {@code richards-whimsy-mod.mixins.json} 的 {@code mixins}
 *       　③☑ {@code fabric.mod.json} 的 {@code mixins}（共用同一配置，无需改动）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 {@code rdt/lamp_quartz.png} + 常规 {@code cube_all} 模型
 *       　⑦☐ 配方与战利品表（参考模组的配方是「红石灯居中 + 四个石英」）</li>
 *   <li><b>验证状态</b>：服务端侧（这一格 = 15、青金石灯 = 0、紧挨青金石灯的火把不受影响）
 *       已用世界数据包无头验过（方法见本类 wiki 页「验证方法」一节）；
 *       客户端侧（"完全看不见光"）还留在游戏内清单里。三个 Mixin 都写成 {@code require = 0}</li>
 * </ul>
 */
public class RDT_LampQuartz extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_QUARTZ_LAMP;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 石英灯的属性：完整复制红石灯，然后把父级带来的 {@code litBlockEmission(15)} 改掉。
	 * <p><b>必须</b>显式覆写：父级的函数读 {@code LIT} 属性，而本方块没有 {@code LIT}，
	 * 不覆写会在状态构造时抛 {@code IllegalArgumentException}，注册即崩。
	 * <p>刻意写 0、且<b>不</b>写 {@code emissiveRendering}：前者让原版的光源登记路径
	 * （{@code propagateLightSources} 直接读状态发光值）不把它当光源，后者保证"玩家看不见"。
	 * 真正的分侧发光等级由 {@code BlockLightEngine.getEmission} 的 Mixin 按侧别给。
	 */
	public static BlockBehaviour.Properties quartzLampProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP)
				.lightLevel(state -> 0);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #quartzLampProperties()} —— 它是本构造器实参的来源。
	 */
	public RDT_LampQuartz(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	public static void OnInit() {
		RDT_QUARTZ_LAMP = ModRegister.registerBlockWithItem("rdt_quartz_lamp",
				new RDT_LampQuartz(quartzLampProperties().setId(ModRegister.blockKey("rdt_quartz_lamp"))));
	}
}
