package com.rcdmake.whimsy;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 模组客户端入口（仅客户端执行）。
 *
 * <p>26.2 迁移说明：这是**唯一**允许引用客户端专用类的地方
 * （{@code Screen}、{@code GuiGraphics}、{@code RenderType}、{@code BlockColorRegistry} 等）。
 * 旧版把客户端渲染接线写成 {@code *_ClientRenderInit} 静态类再由主入口调用——
 * 那种做法在专用服务端上会 ClassNotFound，必须改到本类里。
 *
 * <p>另外：26.1 起 {@code BlockRenderLayerMap} 已被删除，
 * 透明/镂空改由贴图像素自动决定，那几行是**删除**而不是改名。
 *
 * <h2>渲染层（已删除的接线）</h2>
 * wiki {@code Misc/客户端渲染接线.md} 列的表里，WI 需要半透明的两个方块是
 * <b>气凝胶</b>与<b>固化水方块</b>；26.2 不再手动注册渲染层，只要贴图 Alpha 通道正确即可：
 * <ul>
 *   <li>气凝胶：{@code wi/aerogel.png} 全图 Alpha=32 → 半透明；</li>
 *   <li>固化水方块：{@code wi/static_liquid_water.png} Alpha=180 → 半透明；</li>
 *   <li>糖玻璃：{@code wi/glass_sugar.png} 只有 Alpha 0 / 255 两档 → 镂空。</li>
 * </ul>
 *
 * <h2>颜色提供器（已改写）</h2>
 * 旧版 {@code WI_ColorProvider} 用 Fabric 的 {@code ColorProviderRegistry.BLOCK} 注册，
 * 每次渲染都去查所在生物群系的 {@code Biome#getWaterColor()}。
 * 26.2 的注册表改名为 {@link BlockColorRegistry}，且原版已提供现成的
 * {@link BlockTintSources#water()}（与流体水用的是同一套生物群系水色），
 * 所以这里直接用它 —— 这就是 wiki 说的「与『原版水』同原理的后期上色」。
 */
public class RichardsWhimsyModClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RichardsWhimsyMod.MOD_ID + "/client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("[{}] 客户端初始化（26.2）", RichardsWhimsyMod.MOD_ID);

		// 固化水方块：按所在生物群系的水色上色（方块模型各面 tintindex=0）
		BlockColorRegistry.register(List.of(BlockTintSources.water()),
				WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER);
	}
}
