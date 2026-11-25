package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端 → 客户端：某台工作面的<b>原版配方投影</b>（一次全量）。
 *
 * <h2>为什么一次全量、不分页</h2>
 * <p>木工台过滤后只有一百多条、几十 KB，一次发完最省事；而且「先选结果再看成本」的界面里，
 * 客户端必须先知道<b>有哪些产物</b>才能画配方栏。分页会把这件事变成额外往返，
 * 收益（省几十 KB）远小于复杂度。以后真有工作面订阅上千条配方时再加分页。
 *
 * <h2>为什么内容是「愚者配方 JSON」而不是结构化字段</h2>
 * <p>服务端投影阶段本来就要把原版配方翻成愚者配方格式（见 {@link VanillaRecipeProjector}），
 * 走字符串等于复用同一份解析器，客户端不需要第二套结构，也不会因为字段演进出现两端不一致。
 * 每条 JSON 都是单行（'{@code \n}' 用于分隔），所以整个列表用一个 {@code String} 承载。
 */
public record SurfaceRecipesPayload(String surface, String recipes) implements CustomPacketPayload {

	/** 单个字符串上限（1 MiB）。 */
	public static final int MAX_LENGTH = 1_048_576;

	public static final CustomPacketPayload.Type<SurfaceRecipesPayload> TYPE =
			new CustomPacketPayload.Type<>(ModRegister.id("surface_recipes"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SurfaceRecipesPayload> CODEC = StreamCodec.of(
			(buffer, payload) -> {
				buffer.writeUtf(payload.surface(), 256);
				buffer.writeUtf(payload.recipes(), MAX_LENGTH);
			},
			buffer -> new SurfaceRecipesPayload(buffer.readUtf(256), buffer.readUtf(MAX_LENGTH)));

	/** 把一串 JSON 拼成单个字符串（单行 + '\n' 分隔）。 */
	public static String join(List<String> jsonList) {
		return String.join("\n", jsonList);
	}

	/** 拆回 JSON 列表。 */
	public static List<String> split(String joined) {
		if (joined == null || joined.isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		for (String line : joined.split("\n")) {
			if (!line.isBlank()) {
				result.add(line);
			}
		}
		return result;
	}

	/** 这台工作面的注册名（{@link #surface()} 的 path 部分）。 */
	public Identifier surfaceId() {
		return Identifier.parse(this.surface);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
