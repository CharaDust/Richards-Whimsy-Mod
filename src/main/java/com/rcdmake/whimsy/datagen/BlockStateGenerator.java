package com.rcdmake.whimsy.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 方块状态（blockstate）生成器 —— 为**单方块状态**的方块产出
 * {@code assets/<modid>/blockstates/<id>.json}，内容指向它自己的模型。
 *
 * <h2>为什么不用 Minecraft / Fabric 的模型生成器</h2>
 * 26.2 实测结论（逐条核验过，不是推测）：
 * <ul>
 *   <li>{@code net.minecraft.client.data.models.BlockModelGenerators} 在 <b>client</b> jar 里，
 *       而 {@code runDatagen} 跑在服务端环境。</li>
 *   <li>它只暴露 {@code run()}、{@code createTintedLeaves}、{@code createParticleOnlyBlock}
 *       —— <b>没有</b>「只生成 blockstate、不生成 model」的公共 API（{@code createSimpleBlock}
 *       是 private）。</li>
 *   <li>Fabric 侧的 {@code FabricModelProvider} 在 {@code api.client.datagen.v1.provider} 下，
 *       且 26.2 <b>没有</b>客户端 datagen 入口点（jar 里只有 {@code FabricModelProvider}
 *       和一个 mixin）。</li>
 *   <li>服务端唯一可继承的通用基类是 {@code FabricCodecDataProvider}，
 *       不适合写任意 JSON。</li>
 * </ul>
 * 所以这里自己产出 JSON —— blockstate 结构简单，而且完全不牵涉客户端类。
 *
 * <h2>只处理单方块状态的方块</h2>
 * 多状态方块（布尔/整型/枚举/朝向/轴/多面等）的 variants 写法各不相同，仍然手写；
 * 声明了但实际是多状态的方块会被跳过并打日志点名。
 *
 * <h2>幂等</h2>
 * 写入前按内容 SHA-256 比对，内容没变就不碰文件 —— 避免时间戳每次刷新，
 * 行为与 datagen 自带的缓存一致。
 */
public final class BlockStateGenerator {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private BlockStateGenerator() {
	}

	/**
	 * 生成全部已声明方块的 blockstate。
	 *
	 * @param modRoot 本模组在运行时的资源根（含 {@code assets/} 的那一级）
	 * @return 实际写入的文件数
	 */
	public static int generate(Path modRoot) throws IOException {
		Map<Block, String> declared = collectAll();
		int written = 0;
		int skipped = 0;

		for (Map.Entry<Block, String> entry : declared.entrySet()) {
			Block block = entry.getKey();

			if (!block.getStateDefinition().isSingletonState()) {
				RichardsWhimsyMod.LOGGER.warn(
						"[datagen] 跳过 {}：它不是单方块状态，blockstate 需要手写",
						BuiltInRegistries.BLOCK.getKey(block));
				skipped++;
				continue;
			}

			Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);

			// 模型 id：声明里给了就用给的，否则沿用「与方块同名的模型」这个惯例。
			String suffix = entry.getValue();
			Identifier modelId = (suffix == null || suffix.isEmpty())
					? Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath())
					: Identifier.fromNamespaceAndPath(blockId.getNamespace(), suffix);

			JsonObject variant = new JsonObject();
			variant.addProperty("model", modelId.toString());
			JsonObject variants = new JsonObject();
			variants.add("", variant);
			JsonObject root = new JsonObject();
			root.add("variants", variants);

			byte[] content = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);

			Path target = modRoot
					.resolve("assets")
					.resolve(blockId.getNamespace())
					.resolve("blockstates")
					.resolve(blockId.getPath() + ".json");

			if (writeIfChanged(target, content)) {
				written++;
			}
		}

		RichardsWhimsyMod.LOGGER.info(
				"[datagen] blockstate 生成完毕：写入 {} 个，跳过 {} 个（多状态，需手写）",
				written, skipped);
		return written;
	}

	/** 内容不变就不写（保持时间戳稳定）。返回是否真的写了。 */
	private static boolean writeIfChanged(Path target, byte[] content) throws IOException {
		if (Files.exists(target)) {
			byte[] existing = Files.readAllBytes(target);
			if (MessageDigest.isEqual(sha256(existing), sha256(content))) {
				return false;
			}
		}
		if (target.getParent() != null) {
			Files.createDirectories(target.getParent());
		}
		Files.write(target, content);
		return true;
	}

	private static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 不可用", e);
		}
	}

	/**
	 * 收集所有模块的声明：方块 → 模型路径后缀（空串 = 沿用 {@code block/<方块id>}）。
	 * <p>新增模块时在这里加一条 {@code putAll(...)}。
	 */
	public static Map<Block, String> collectAll() {
		Map<Block, String> all = new LinkedHashMap<>();
		all.putAll(com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther.blockStateModels());

		// 奇思妙想（WI）：六个方块全是单方块状态（无属性），模型与方块同名，
		// 所以 blockstate 可以完全交给 datagen，不需要手写。
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticCobblestone.WI_STATIC_COBBLESTONE, "");
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_PackedPlanks.WI_PACKED_PLANKS, "");
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel.WI_AEROGEL, "");
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_SugarBlock.WI_SUGARBLOCK, "");
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_GlassSugar.WI_GLASSSUGAR, "");
		all.put(com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER, "");

		// 随意物件（RDT）：除三块「通灵树」外也都是单方块状态 —— 包括通灵木板。
		// ⚠️ 通灵树里有**三块**是**多状态**方块，blockstate JSON 一律**手写**，绝不能在这里声明：
		//   · rdt_spectre_log      —— AXIS（轴）
		//   · rdt_spectre_leaves   —— DISTANCE / PERSISTENT / WATERLOGGED（照抄原版树叶，28 个状态）
		//   · rdt_spectre_sapling  —— STAGE（照抄原版树苗，2 个状态）
		//   三份手写 JSON 都用原版那种「catch-all 变体 ""」（外观不随状态变化），
		//   路径 assets/richards-whimsy-mod/blockstates/ 下。
		// ⚠️ 而 rdt_spectre_planks（通灵木板）**没有**任何状态，属于单状态方块 → 就在下面这一串里，
		//   由本生成器产出 blockstates/rdt_spectre_planks.json（块状物的默认 6 面模型）。
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis.RDT_LAPIS_GLASS, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz.RDT_QUARTZ_GLASS, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampLapis.RDT_LAPIS_LAMP, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampQuartz.RDT_QUARTZ_LAMP, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock.RDT_STEP_BLOCK, "");
		// 通灵镜片：单状态（薄层模型是手写的 models/block/rdt_spectre_lens.json，
		// blockstate 仍由本生成器产出，指向 block/rdt_spectre_lens）
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_SpectreLens.RDT_SPECTRE_LENS, "");
		// —— 通灵木料：原木 / 树叶 / 树苗是多状态（上面注释），只有木板是单状态 ——
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreePlanksSpectre.RDT_SPECTRE_PLANKS, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN, "");
		all.put(com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE, "");

		// 方块愚者（BHR）：
		// ⚠️ 多状态方块一律**不**在这里声明（blockstate 手写），共 8 个：
		//   · 五个宝石（bhr_gem_*）—— face(ATTACH_FACE) × facing 两个属性；
		//   · 红壤（bhr_soil）与它的两个矿土子类（bhr_soil_flint_ore / bhr_soil_clay_ore）—— grass_cover 布尔状态。
		//   · 松木原木（bhr_pine_log）带 AXIS，同上。
		// 下面全是单状态方块（模型与方块同名 → block/<id>），交给生成器。
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Compost.BHR_COMPOST, "");
		// —— 切面板岩系 ——
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Slate.BHR_SLATE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_SlateCobble.BHR_COBBLESLATE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCopper.BHR_SLATE_COPPER_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateIron.BHR_SLATE_IRON_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTin.BHR_SLATE_TIN_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCoal.BHR_SLATE_COAL_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateGold.BHR_SLATE_GOLD_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlatePlatinum.BHR_SLATE_PLATINUM_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTitanium.BHR_SLATE_TITANIUM_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateUnknown.BHR_SLATE_UNKNOWN_ORE, "");
		// —— 红壤系：只有「未知矿土」是单状态（wiki 明写它没有 grass_cover 状态） ——
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilUnknown.BHR_SOIL_UNKNOWN_ORE, "");
		// —— 石灰石 / 大理石 / 海滩沙 ——
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Limestone.BHR_LIMESTONE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_LimestoneBrick.BHR_LIMESTONE_BRICK, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreLimestoneOil.BHR_LIMESTONE_OIL_ORE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Marble.BHR_MARBLE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_MarblePolished.BHR_POLISHED_MARBLE, "");
		all.put(com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_BeachSand.BHR_BEACH_SAND, "");
		return all;
	}
}
