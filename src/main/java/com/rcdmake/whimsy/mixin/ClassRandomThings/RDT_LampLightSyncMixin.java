package com.rcdmake.whimsy.mixin.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * 两盏灯的「引擎场与本侧发光等级对齐」—— 在光照数据换上去之后按本侧规则补齐/抹掉。
 *
 * <h2>为什么两侧都需要这一步</h2>
 * <b>客户端</b>：客户端的光照数据**完全由服务端光照包喂**
 * （{@code ClientPacketListener.applyLightData → readSectionList → queueSectionData}），
 * 而包里的数值是<b>服务端引擎</b>算的 —— 那边青金石灯是 0、石英灯是 15，正好与客户端相反：
 * <ul>
 *   <li>青金石灯：客户端本地的 15 级光会被包覆盖掉 ⇒ 要<b>补回来</b>；</li>
 *   <li>石英灯：包会把它 15 级的光塞进来 ⇒ 要<b>抹掉</b>（{@code checkBlock} 让引擎按本侧 0 走衰减传播）。</li>
 * </ul>
 * 参考模组（1.12.2 Random Things）也遇到了同一件事，它的解法是客户端
 * {@code randomDisplayTick} 里判 {@code world.getLight(pos) == 0} 就 {@code checkLightFor(BLOCK, pos)} ——
 * 这里换成**事件驱动**：数据一变就修，确定且无延迟（不需要每 Tick 抽样，灯多时也不额外开销）。
 *
 * <p><b>服务端</b>：两盏灯的状态发光值都是 0，而 {@code BlockLightEngine.propagateLightSources}
 * 是直接读 {@code state.getLightEmission()} 登记光源的 ⇒ 它<b>不会</b>把灯当光源。
 * 于是「区块第一次点亮」这条路上，石英灯在服务端的 15 级光需要我们自己补
 * （放在地上的那一刻由原版 {@code checkBlock} 触发、没问题；但<b>从盘上载入的区块</b>、
 * 以及升级前就存在的旧存档里的区块，需要这一手）。青金石灯在服务端本来就是 0，
 * 这里的 {@code checkBlock} 对它是空操作。
 *
 * <h2>两个注入点，顺序是有讲究的</h2>
 * <ol>
 *   <li>{@code queueSectionData}（HEAD）：记录「哪个区块段收到了 BLOCK 层数据」。
 *       两侧都记（服务端记的是从盘上载入的区块），但脏集合是 {@code @Unique} 字段，
 *       挂在各自的引擎实例上，不会互相串。</li>
 *   <li>{@code runLightUpdates}（RETURN）：**必须等这一轮跑完**。新数据不是在
 *       {@code queueSectionData} 时上位的，而是被放进 {@code LayerLightSectionStorage.queuedSections}，
 *       在这一轮的末尾 {@code swapSectionMap()} 才真正换上去 —— 早一步补光会被这次换图抹掉。
 *       换完之后对「够得着这些区块段的灯」各调一次 {@code checkBlock}：引擎自己的增长/衰减传播
 *       会把该补的补上、该抹的抹掉（遮挡、最大值场、与其它光源共存都由引擎负责，不做任何近似）。</li>
 * </ol>
 *
 * <h2>性能</h2>
 * <ul>
 *   <li>没有脏区块时，{@code runLightUpdates} 的钩子只判一次 {@code isEmpty()}（约 1ns，不影响每 Tick 的光照更新）；</li>
 *   <li>有数据到达时：每个脏区块段查 3×3 区块的灯位缓存（没灯就是几次哈希查找）＋对够得着的灯调
 *       {@code checkBlock}。{@code checkBlock} 只是把一个节点排进待检集合，真正的传播是**增量**的
 *       （{@code propagateIncrease} 只在相邻格确实更暗时才继续），所以「光被改坏的那一小片」花多少、
 *       没被改的地方几乎不花；</li>
 *   <li>补完立刻再跑一轮 {@code runLightUpdates}，让它落在**同一个 Tick**里（否则会闪一帧暗）。
 *       脏集合在递归前已清空，不会无限递归。</li>
 * </ul>
 *
 * <p>{@code require = 0}：不命中只是退回「客户端收到光照包后两盏灯都不发光」，不会崩。
 */
@Mixin(LevelLightEngine.class)
public abstract class RDT_LampLightSyncMixin {

	/** 本引擎所属的 Level（{@code LevelLightEngine} 构造器里 {@code chunkSource.getLevel()} 存下来的）。 */
	@Shadow
	protected LevelHeightAccessor levelHeightAccessor;

	@Shadow
	public abstract int runLightUpdates();

	@Shadow
	public abstract LayerLightEventListener getLayerListener(LightLayer layer);

	/** 收到过 BLOCK 层光照数据的区块段（{@code SectionPos.asLong()}），每个引擎实例各一份。 */
	@Unique
	private Set<Long> whimsy$dirtySections = new HashSet<>();

	@Inject(method = "queueSectionData", at = @At("HEAD"), require = 0)
	private void whimsy$markLampLightDirty(LightLayer layer, SectionPos pos, DataLayer data, CallbackInfo ci) {
		if (layer != LightLayer.BLOCK) {
			return;
		}
		this.whimsy$dirtySections.add(pos.asLong());
	}

	@Inject(method = "runLightUpdates", at = @At("RETURN"), require = 0)
	private void whimsy$repairLampLight(CallbackInfoReturnable<Integer> cir) {
		if (this.whimsy$dirtySections.isEmpty()) {
			return;
		}
		if (!(this.levelHeightAccessor instanceof Level level)) {
			this.whimsy$dirtySections.clear();
			return;
		}

		// 先复制再清空：下面可能递归调用 runLightUpdates，那时不能再看到这些脏标记
		long[] sections = new long[this.whimsy$dirtySections.size()];
		int index = 0;
		for (long sectionNode : this.whimsy$dirtySections) {
			sections[index++] = sectionNode;
		}
		this.whimsy$dirtySections.clear();

		LayerLightEventListener blockLayer = this.getLayerListener(LightLayer.BLOCK);
		boolean queued = false;
		for (long sectionNode : sections) {
			for (BlockPos lamp : RDT_LampIndex.lampsReaching(level, SectionPos.of(sectionNode))) {
				blockLayer.checkBlock(lamp);
				queued = true;
			}
		}

		if (queued) {
			this.runLightUpdates();
		}
	}
}
