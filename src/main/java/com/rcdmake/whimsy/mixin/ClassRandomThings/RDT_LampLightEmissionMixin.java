package com.rcdmake.whimsy.mixin.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampQuartz;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 两盏灯的「发光等级按侧别不同」—— 能力阶梯第 5 级的核心打点。
 *
 * <h2>为什么打在这里</h2>
 * 26.2 里「这个方块发多少光」只有一个入口：{@code BlockLightEngine.getEmission(long, BlockState)}
 * （{@code checkNode} 与 {@code propagateDecrease} 都问它）。原版实现是
 * <pre>{@code
 * int emission = state.getLightEmission();          // 状态里缓存的那个 int（Properties.lightLevel 求值一次）
 * return emission > 0 && this.storage.lightOnInSection(section) ? emission : 0;
 * }</pre>
 * 而两盏灯要的是「同一盏灯在两侧引擎里发不同等级」（两侧的场各由本侧引擎算出来，
 * 所以遮挡、与火把等真实光源共存、引擎数值被其它 mod 读取，全都是天然正确的）：
 * <table border="1">
 *   <caption>分侧发光等级</caption>
 *   <tr><th>方块</th><th>客户端引擎</th><th>服务端引擎</th><th>效果</th></tr>
 *   <tr><td>{@code rdt_lapis_lamp} 青金石灯</td><td><b>15</b></td><td><b>0</b></td>
 *       <td>玩家看到真的照亮周围；刷怪/融冰/作物等一切服务端逻辑里它不存在（明亮的刷怪竞技场、视觉照亮冰而不融化）</td></tr>
 *   <tr><td>{@code rdt_quartz_lamp} 石英灯</td><td><b>0</b></td><td><b>15</b></td>
 *       <td>玩家完全看不见光；服务端按有光处理（昏暗山洞景观的隐藏式照明、防刷怪）</td></tr>
 * </table>
 * 侧别不能看环境侧（单人游戏客户端与内部服务端<b>同进程同 ClassLoader</b>），必须看
 * <b>这个引擎属于哪个 Level</b>：{@code chunkSource.getLevel() instanceof Level} + {@code Level.isClientSide()}
 * —— 参考模组（1.12.2 的 Random Things）用的是它那边的环境侧判断，26.2 没有等价概念。
 *
 * <h2>侧别与 chunkSource 为什么在构造器里取（踩坑记录）</h2>
 * {@code chunkSource} / {@code storage} 都是父类 {@code LightEngine} 的 {@code protected} 字段，
 * 而 <b>{@code @Shadow} 不会去父类找字段</b>：
 * <pre>
 * InvalidMixinException: @Shadow field chunkSource was not located in the target class
 *     net.minecraft.world.level.lighting.BlockLightEngine
 * </pre>
 * （本项目实测，见 {@code doc/wiki/Blocks/ClassRandomThings/RDT_LampLapis.md} 的踩坑记录。）
 * 改用构造器捕获：{@code BlockLightEngine} 的构造器<b>自己</b>收得到 {@code LightChunkGetter}，
 * 在构造器 RETURN 处把它与「本引擎属于哪一侧」存进 {@code @Unique} 字段，
 * 之后 {@code getEmission} 只需读字段 —— 顺带比每次做 {@code instanceof} 判断更快。
 *
 * <h2>把原方法那句守卫一起复刻</h2>
 * {@code storage.lightOnInSection} 是 {@code protected}（跨包调不到），这里改用公开 API 等价复刻：
 * {@code ChunkSource.getLightEngine().lightOnInColumn(SectionPos.getZeroNode(...))}
 * （{@code LevelLightEngine.lightOnInColumn} 是 public，语义与 {@code lightOnInSection} 相同：
 * 「这一列的区块已启用光照」）。不保留这句守卫会把光传播进「光照尚未启用」的区块段。
 *
 * <h2>状态里那个发光值必须留 0</h2>
 * 两盏灯的状态发光值都写 0（{@code lightLevel(state -> 0)}）。原因是另一条登记光源的路径
 * {@code BlockLightEngine.propagateLightSources} <b>绕过 {@link #getEmission} 直接读
 * {@code state.getLightEmission()}</b>：状态写 0 ⇒ 它不会把灯当光源登记，
 * 「该不该有光」就完全由本 Mixin 一处决定（石英灯在服务端要补光的情形见
 * {@link RDT_LampLightSyncMixin} 的区块点亮补齐）。
 *
 * <p>{@code require = 0}：目标方法改名/改结构时只是「两盏灯退回都不发光」，不会崩
 * （本项目的 Mixin 配置是 {@code required: false}，应用失败只会在日志里留 WARN）。
 */
@Mixin(BlockLightEngine.class)
public abstract class RDT_LampLightEmissionMixin {

	/** 灯的最高发光等级（与红石灯点亮时同级）。 */
	@Unique
	private static final int WHIMSY_LAMP_MAX_LEVEL = 15;

	/** 本引擎属于客户端吗（构造器里算好，见类注释的踩坑记录）。 */
	@Unique
	private boolean whimsy$clientEngine;

	/** 构造器里捕获的 {@code LightChunkGetter}（用来查「这一列区块是否已启用光照」）。 */
	@Unique
	private LightChunkGetter whimsy$chunkSource;

	/** 主构造器（{@code LevelLightEngine} 用的就是这个）。 */
	@Inject(
			method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;)V",
			at = @At("RETURN"),
			require = 0)
	private void whimsy$captureSide(LightChunkGetter chunkSource, CallbackInfo ci) {
		this.whimsy$capture(chunkSource);
	}

	/** 带 storage 的构造器（{@code @VisibleForTesting}，但一样要捕获，免得哪天真被用到时判错侧别）。 */
	@Inject(
			method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;Lnet/minecraft/world/level/lighting/BlockLightSectionStorage;)V",
			at = @At("RETURN"),
			require = 0)
	private void whimsy$captureSideWithStorage(LightChunkGetter chunkSource, BlockLightSectionStorage storage,
			CallbackInfo ci) {
		this.whimsy$capture(chunkSource);
	}

	@Unique
	private void whimsy$capture(LightChunkGetter chunkSource) {
		this.whimsy$chunkSource = chunkSource;
		this.whimsy$clientEngine = chunkSource != null
				&& chunkSource.getLevel() instanceof Level level
				&& level.isClientSide();
	}

	@Inject(method = "getEmission", at = @At("HEAD"), cancellable = true, require = 0)
	private void whimsy$lampSideEmission(long blockNode, BlockState state, CallbackInfoReturnable<Integer> cir) {
		int level = this.whimsy$lampLevelForThisEngine(state);
		if (level < 0) {
			// 不是我们的灯：走原版逻辑
			return;
		}

		if (level > 0 && !this.whimsy$lightOnForSection(blockNode)) {
			level = 0;
		}
		cir.setReturnValue(level);
	}

	/**
	 * 本侧引擎里这盏灯发多少光；不是我们的灯返回 -1（交回原版）。
	 *
	 * <p>青金石灯：客户端 15（玩家看得见）/ 服务端 0（逻辑里等于不存在）。
	 * 石英灯正好相反。
	 */
	@Unique
	private int whimsy$lampLevelForThisEngine(BlockState state) {
		if (state.is(RDT_LampLapis.RDT_LAPIS_LAMP)) {
			return this.whimsy$clientEngine ? WHIMSY_LAMP_MAX_LEVEL : 0;
		}
		if (state.is(RDT_LampQuartz.RDT_QUARTZ_LAMP)) {
			return this.whimsy$clientEngine ? 0 : WHIMSY_LAMP_MAX_LEVEL;
		}
		return -1;
	}

	/** 等价复刻原方法里的 {@code storage.lightOnInSection(section)}（公开 API 版本）。 */
	@Unique
	private boolean whimsy$lightOnForSection(long blockNode) {
		if (!(this.whimsy$chunkSource instanceof ChunkSource source)) {
			return false;
		}

		LevelLightEngine engine = source.getLightEngine();
		return engine != null && engine.lightOnInColumn(SectionPos.getZeroNode(SectionPos.blockToSection(blockNode)));
	}
}
