package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * 原版配方的<b>传输层</b>（服务端侧）。
 *
 * <h2>为什么必须发包</h2>
 * <p>26.2 的客户端 {@code RecipeAccess} 只暴露 {@code propertySet} 与 {@code stonecutterRecipes()}
 * （已核验），客户端拿不到通用配方表。本模组要求双端安装，但「双端都装了模组」并不能让客户端
 * 变出配方表 —— 数据仍然只能由服务端读出来再送过去。
 *
 * <h2>时机</h2>
 * <ul>
 *   <li><b>打开工作面界面</b>：投影（首次会扫一遍数据包，之后走缓存）→ 先给自己注入
 *       （服务端列表与客户端保持一致）→ 再把同一份发给玩家；</li>
 *   <li><b>数据包重载</b>：服务端清缓存；客户端在下一次打开界面时自然被覆盖。</li>
 * </ul>
 */
public final class SurfaceNetwork {

	private SurfaceNetwork() {
	}

	/** 注册 payload 类型与数据包重载钩子（主类 {@code OnInit} 调用）。 */
	public static void OnInit() {
		PayloadTypeRegistry.clientboundPlay().register(SurfaceRecipesPayload.TYPE, SurfaceRecipesPayload.CODEC);

		// 数据包重载：投影缓存与注入的配方一起失效，避免拿旧配方做新成就
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			VanillaRecipeProjector.invalidate();
			VanillaRecipeIndex.invalidate();
			RichardsWhimsyMod.LOGGER.info("[vanilla] 数据包重载完成，原版配方缓存已清空");
		});

		// 启动自检 + 预热：把规则表里登记的工作面先投影一遍并打日志。
		// 这样「投影出几条」在服务器启动日志里就能看到，不必等玩家打开界面才发现过滤器写错。
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (String surface : VanillaRecipeProjector.subscribedSurfaces(server)) {
				RichardsWhimsyMod.LOGGER.info("[vanilla] 启动自检：{} 投影 {} 条原版配方",
						surface, VanillaRecipeProjector.forSurface(server, surface).size());
			}
		});
	}

	/**
	 * 把某台工作面的原版配方投影发给玩家（同时保证服务端自己也已注入同样的列表）。
	 *
	 * @param player    打开界面的玩家
	 * @param surfaceId 工作面注册名（如 {@code bhr_woodwork_bench}）
	 */
	public static void sendSurface(ServerPlayer player, String surfaceId) {
		var server = player.level().getServer();
		if (server == null) {
			return;
		}
		List<String> json = VanillaRecipeProjector.forSurface(server, surfaceId);
		VanillaRecipeIndex.accept(surfaceId, json);
		ServerPlayNetworking.send(player,
				new SurfaceRecipesPayload(surfaceId, SurfaceRecipesPayload.join(json)));
	}
}
