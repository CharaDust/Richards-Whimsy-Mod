package com.rcdmake.whimsy;

import com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateBoolean;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnum;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumAxis;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumFace;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateInt;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeLogSpectre;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_GlassSugar;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_PackedPlanks;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticCobblestone;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_SugarBlock;
import com.rcdmake.whimsy.item.ClassDEV.DEV_ItemsOther;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组主入口（双端）。
 *
 * <p>职责：把各分支类的 {@code OnInit()} 串起来。每个类自己负责注册自己那一批
 * 方块/物品（项目约定：单方块、单物品各自独立成类，主类只写一条初始化调用）。
 *
 * <p>26.2 迁移说明：
 * <ul>
 *   <li>主入口只做双端通用的事。任何引用客户端专用类（Screen / GuiGraphics 等）的逻辑
 *       必须放进 {@link RichardsWhimsyModClient}，否则专用服务端加载主类时会 ClassNotFound。</li>
 *   <li>顺序：先方块、后物品、最后创造模式物品栏——物品栏要引用前两者的产物。</li>
 * </ul>
 */
public class RichardsWhimsyMod implements ModInitializer {

	/** 模组 id，须与 fabric.mod.json 的 id 一致（必须全小写）。 */
	public static final String MOD_ID = "richards-whimsy-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[{}] 正在初始化（Minecraft 26.2 / Fabric）", MOD_ID);

		// --- 方块 ---
		DEV_BlocksOther.OnInit();
		DEV_StateBoolean.OnInit();
		DEV_StateInt.OnInit();
		DEV_StateEnum.OnInit();
		DEV_StateEnumAxis.OnInit();
		DEV_StateEnumFace.OnInit();
		DEV_GhostBlock.OnInit();

		// --- 方块（奇思妙想 WI，由 1.20.4 迁移而来） ---
		WI_Aerogel.OnInit();
		WI_SugarBlock.OnInit();
		WI_StaticLiquidWater.OnInit();
		WI_StaticCobblestone.OnInit();
		WI_PackedPlanks.OnInit();
		WI_GlassSugar.OnInit();

		// --- 方块（随意物件 RDT，由 1.20.4 迁移而来） ---
		RDT_SuperLubricentIce.OnInit();
		RDT_GlassLapis.OnInit();
		RDT_GlassQuartz.OnInit();
		RDT_StepBlock.OnInit();
		RDT_TreeLogSpectre.OnInit();
		RDT_LuminousBlocks.OnInit();

		// --- 物品 ---
		DEV_ItemsOther.OnInit();

		// --- 创造模式物品栏 ---
		// 必须最后：它直接引用上面注册好的方块与物品的静态字段，此刻那些字段才非 null；
		// 提前调用会让标签页里的内容全是 null（空页）或直接抛 NPE。
		ModCreativeTabs.OnInit();

		LOGGER.info("[{}] 初始化完成", MOD_ID);
	}

	/**
	 * 构造本模组的资源标识符。
	 *
	 * <p>26.2 的类是 {@code net.minecraft.resources.Identifier}
	 * （1.21.11 起 mojmap 把 {@code ResourceLocation} 改成了这个名），
	 * 工厂方法是 {@code fromNamespaceAndPath}——不是 1.21 时代的 {@code of}。
	 *
	 * <p>注册用的入口在 {@link ModRegister}，这里保留一份是为了给资源路径之类
	 * 不涉及注册表的地方用。
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
