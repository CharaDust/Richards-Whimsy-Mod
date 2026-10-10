package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 青金石灯（{@code rdt_lapis_lamp}）—— <b>新增方块</b>（无 1.20.4 前身）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_LampLapis.md}）：
 * <ul>
 *   <li>父级：红石灯，<b>只取属性</b>（硬度 0.3 / 爆炸抗性 / 玻璃声等）；<b>不接收红石信号</b> ——
 *       本体继承 {@link Block}，不继承 {@code RedstoneLampBlock}</li>
 *   <li>「玩家看起来是照明方块」：客户端看到它发出 <b>15 级光</b>，真的照亮周围
 *       —— 可以拿来做<b>明亮的刷怪竞技场</b>（看着亮、照样刷怪），
 *       也可以<b>在视觉上照亮冰雪建筑而不融化冰</b></li>
 *   <li>「服务端方块与实体逻辑里亮度 0」：刷怪、结冰/融冰、作物生长等一切服务端判定里，
 *       它<b>等于不存在</b>（不是"被忽略"，是引擎里根本没有这盏灯的光）</li>
 *   <li>渲染：常规渲染（{@code block/cube_all}）；改写方法 / 事件 / 方块状态：无；获取方式：无</li>
 * </ul>
 *
 * <h2>能力阶梯：第 5 级「Mixin」（层 M），第 0~4 级逐条排除</h2>
 * 需求是「同一盏灯在两侧的<b>光照场</b>不同」，而 26.2 的结构决定了这必须动 Mixin：
 * <table border="1">
 *   <caption>为什么每一级都不够</caption>
 *   <tr><th>级</th><th>为什么不行</th></tr>
 *   <tr><td>0 继承</td><td>原版没有"按侧别发光"的父类</td></tr>
 *   <tr><td>1 声明</td>
 *       <td>{@code lightLevel} 是 {@code ToIntFunction<BlockState>}，在<b>状态构造时求值一次</b>并缓存
 *       （{@code BlockStateBase.lightEmission}），两侧共用同一个 int，拿不到世界也就分不了侧；
 *       {@code emissiveRendering} 只让<b>方块自己</b>（以及相邻面 4 个采样里的 1 个）变亮，
 *       是 1 格级效果，做不出 15 格的场</td></tr>
 *   <tr><td>2 覆写</td><td>方块类里没有任何方法能改"某一侧看到的光照场"</td></tr>
 *   <tr><td>3 注册表 / 数据驱动</td>
 *       <td>光照不是注册表驱动；26.2 的 {@code attributes}（维度 JSON 里那套）是
 *       <b>环境属性</b>（维度 / 生物群系 / 时间线），没有方块级钩子</td></tr>
 *   <tr><td>4 事件</td><td>Fabric API 没有"光照计算"事件；光照数据也不经过任何可取消的事件</td></tr>
 *   <tr><td><b>5 Mixin</b></td><td>唯一可行，见下</td></tr>
 * </table>
 *
 * <h2>实现：引擎发光等级按侧别不同（三个 Mixin）</h2>
 * <ol>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampLightEmissionMixin} ——
 *       {@code BlockLightEngine.getEmission}：客户端引擎 15 / 服务端引擎 0。
 *       侧别看<b>引擎所属的 Level</b>（{@code chunkSource.getLevel().isClientSide()}），
 *       不能看环境侧：单人游戏里客户端与内部服务端同进程同 ClassLoader。</li>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampLightSyncMixin} —— 客户端自愈：
 *       光照包的数据在这一轮 {@code runLightUpdates} 末尾才真正上位，而它来自"服务端引擎（没有这盏灯）"，
 *       会把客户端本地的 15 级光抹掉；所以在那一轮收尾后，对够得着这些区块段的灯各调一次
 *       {@code checkBlock}，让引擎用自己的传播把光补回来（遮挡与其它光源共存都由引擎算，不做近似）。</li>
 *   <li>{@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampIndexMaintenanceMixin} ——
 *       灯位置索引的失效（{@code LevelChunk.setBlockState}），供自愈定位灯。</li>
 * </ol>
 * 服务端不需要额外动作：本方块状态发光值是 0，而登记光源的另一条路
 * {@code BlockLightEngine.propagateLightSources} 直接读 {@code state.getLightEmission()}
 * ——它本来就不把这盏灯当光源；服务端唯一问发光等级的地方（{@code getEmission}）返回 0，
 * 于是服务端引擎里真的没有它 ⇒ 引擎数值、存档、其它 mod 读到的都是"这里没有光"。
 *
 * <h2>参考实现（1.12.2 的 Random Things）与 26.2 的差别</h2>
 * 它只用一个覆写 + 一个客户端自愈就做到了同样的事：
 * <pre>{@code
 * public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
 *     return FMLCommonHandler.instance().getEffectiveSide().isClient() ? 15 : 0;   // 石英灯反过来
 * }
 * public void randomDisplayTick(...) {   // 客户端自愈
 *     if (worldIn.isRemote && worldIn.getLight(pos) == 0) worldIn.checkLightFor(EnumSkyBlock.BLOCK, pos);
 * }
 * }</pre>
 * 差别在于：①1.12.2 的 {@code getLightValue} 能拿到世界（分侧），26.2 的 {@code lightLevel} 不能
 * ⇒ 只能打在引擎的发光查询上；②1.12.2 的客户端<b>自己算光照</b>（光照只随区块包下发，之后由客户端传播），
 * 26.2 的客户端光照数据<b>完全由服务端光照包喂</b> ⇒ 自愈从"保险"变成"必需"。
 * 参考模组同样需要自愈这一点，反过来印证了这条路线是对的。
 *
 * <h2>已排除的走法（别再试）</h2>
 * <ul>
 *   <li><b>服务端查询处近似</b>（让引擎里照常发光，只在 {@code getBrightness} 出口"把灯附近的光忽略掉"）：
 *       看着省事，但光照是最大值场、减不掉某一盏灯的贡献，只能按距离猜 —— 结果就是
 *       <b>灯旁边真实光源（火把等）在服务端一起失效</b>，而且石英灯那边会<b>穿墙</b>。
 *       RDT 是原版附加模组，混用（灯 + 火把 + 两种灯）是常态，所以这条不走。</li>
 *   <li><b>把光放进 sky 层</b>：天色会缩放 sky 光（夜里灯就不亮了），而且 {@code getBrightness(SKY)}
 *       参与刷怪判定，等于没分开。</li>
 *   <li><b>用方块实体 / 每 Tick 强制重算</b>：前者把一个装饰灯变成方块实体（注册、codec、活塞行为全变），
 *       后者每 Tick 对每盏灯做一次 15 格传播（灯一多就是毫秒级开销）。自愈只在该修的时候修。</li>
 * </ul>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_lapis_lamp}　<b>模块</b> RDT　<b>父级</b> 红石灯（属性；额外改写
 *       {@code lightLevel} 为 0，<b>不</b>写 {@code emissiveRendering}）</li>
 *   <li><b>启用的可选层</b>：层 4 属性工厂（{@code lightLevel}）、层 M Mixin（三个，见上）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（发光等级是 Mixin 里的私有常量）、层 3 状态属性（单状态）、
 *       层 6 家族（刻意不继承 {@code RedstoneLampBlock}）、层 7 codec、层 8 生命周期、层 9 交互与判定、
 *       层 10 形状与外观、层 11 预留区、层 C 客户端（不需要：光由引擎给，客户端没有专属接线）</li>
 *   <li><b>能力阶梯</b>：停在第 5 级；第 0~4 级的排除理由见上面的表</li>
 *   <li><b>硬约束的落地位置</b>：「两侧发光等级不同」只在 {@code RDT_LampLightEmissionMixin} 一处；
 *       「客户端光的自愈」只在 {@code RDT_LampLightSyncMixin} 一处；位置索引在 {@link RDT_LampIndex}</li>
 *   <li><b>Mixin 三处登记</b>：①☑ 三个类本身　②☑ {@code richards-whimsy-mod.mixins.json} 的 {@code mixins}
 *       　③☑ {@code fabric.mod.json} 的 {@code mixins}（共用同一配置，无需改动）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 {@code rdt/lamp_lapis.png} + 常规 {@code cube_all} 模型
 *       　⑦☐ 配方与战利品表（wiki：获取方式「无」；参考模组的配方是「红石灯居中 + 四个青金石」）</li>
 *   <li><b>验证状态</b>：客户端侧已游戏内实测正常；服务端侧（这一格 = 0、紧挨它的火把照常生效）
 *       已用世界数据包无头验过（方法见 {@code RDT_LampQuartz} 的 wiki 页「验证方法」一节）。
 *       三个 Mixin 都写成 {@code require = 0}：不命中只是退回「客户端不发光」，不会崩</li>
 * </ul>
 *
 * <p>对偶方块：{@link RDT_LampQuartz}（服务端 15 / 客户端 0，用于「昏暗山洞景观的隐藏式照明与防刷怪」）。
 */
public class RDT_LampLapis extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_LAPIS_LAMP;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 青金石灯的属性：完整复制红石灯（硬度 0.3 / 爆炸抗性 / 玻璃声 / 地图色等），
	 * 然后把父级带来的 {@code litBlockEmission(15)} 改掉（父级的函数读 {@code LIT} 属性，本方块没有，
	 * 不覆写会在状态构造时抛异常、注册即崩）。
	 *
	 * <p><b>这里刻意写 0</b>：本方块的发光等级由 {@code BlockLightEngine.getEmission} 的 Mixin
	 * 按侧别给（客户端 15 / 服务端 0），状态里那个共用 int 保持 0，
	 * 这样"服务端路径"（{@code propagateLightSources} 直接读状态发光值）天然不会把它当光源。
	 */
	public static BlockBehaviour.Properties lapisLampProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP)
				.lightLevel(state -> 0);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #lapisLampProperties()} —— 它是本构造器实参的来源。
	 */
	public RDT_LampLapis(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	public static void OnInit() {
		RDT_LAPIS_LAMP = ModRegister.registerBlockWithItem("rdt_lapis_lamp",
				new RDT_LampLapis(lapisLampProperties().setId(ModRegister.blockKey("rdt_lapis_lamp"))));
	}
}
