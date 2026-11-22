package com.rcdmake.whimsy;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessingBonus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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

		// 宝石祝福：客户端的附魔注册表视图。
		// 服务端那一份在主类的 ServerLifecycleEvents 里；多人游戏时客户端没有服务端，
		// 只能从当前世界拿（换维度时也会重新触发）。拿不到时自动降级：
		// 只给物品上已有的附魔加等级，不再补「物品上没有的附魔」。
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) ->
				BHR_GemBlessingBonus.updateLookup(level.registryAccess()));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			BHR_GemBlessingBonus.clearLookup();
			com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera.exit();
		});

		// 灵魂出窍：纯客户端的观察相机（本地 Marker + 原版相机实体），
		// 每 tick 推进位置，并在「醒来 / 死亡 / 换维度 / 断线」时自动归位。
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK
				.register(com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera::tick);

		// 制作台界面：把「菜单类型 → 屏幕类」接起来。
		// ⚠️ 26.2 的 MenuScreens#register 是私有的（Fabric API 没有替代入口），
		// 已在 richards-whimsy-mod.classtweaker 里放开；这条只在客户端执行，专用服务端不受影响。
		net.minecraft.client.gui.screens.MenuScreens.register(
				com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_WorkBenchMenu.BHR_WORK_BENCH_MENU,
				com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_WorkBenchScreen::new);

		// 简易篝火界面：同一套接线，底图换成 fuel_workbench.png（槽位整体右移 18 像素）。
		net.minecraft.client.gui.screens.MenuScreens.register(
				com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_CampfireMenu.BHR_CAMPFIRE_MENU,
				com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_CampfireScreen::new);

		// 濒死 BGM：每 tick 推进淡出/播放状态机（见 BHR_DownedMusic）
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK
				.register(com.rcdmake.whimsy.client.BHR_DownedMusic::tick);

		// AppleSkin 软兼容：愚者模式下屏蔽它的全部 HUD 叠加层
		// （自定义 barR 已经接管那块区域；生存模式仍然保持 AppleSkin 的原生表现）。
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("appleskin")) {
			com.rcdmake.whimsy.client.compat.BHR_AppleSkinCompat.register();
		}
	}
}
