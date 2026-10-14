package com.rcdmake.whimsy;

import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_BeachSand;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Compost;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemAmethyst;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemDiamond;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemEmerald;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemRuby;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemSapphire;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Limestone;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_LimestoneBrick;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Marble;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_MarblePolished;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreLimestoneOil;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCoal;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCopper;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateGold;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateIron;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlatePlatinum;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTitanium;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTin;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateUnknown;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilClay;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilFlint;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilUnknown;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Slate;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_SlateCobble;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Soil;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_TreeLeavesPine;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_TreeLogPine;
import com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateBoolean;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnum;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumAxis;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumFacing;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateInt;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampQuartz;
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
import com.rcdmake.whimsy.item.ClassDEV.DEV_HardcoreSwitcher;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Cherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Coconut;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_CoffeeCherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_DodoApple;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Lime;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Mango;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_MapleSeed;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Orange;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PineCone;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PricklyPear;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Worm;
import com.rcdmake.whimsy.item.ClassRandomThings.RDT_FireHormonePotion;
import com.rcdmake.whimsy.item.ClassRandomThings.RDT_PoisonHormonePotion;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_PlaguePotion;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_RadiationPotion;
import com.rcdmake.whimsy.item.WhimsyPotionRegistration;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_GameRules;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_FireImbue;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_PoisonImbue;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_BioToxicity;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_Radioactive;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
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

		// --- 游戏规则（misc：不属于任何「可放置/可使用」的模块，按模块子包归档） ---
		// BHR 的三条布尔规则在类初始化时就注册好了，这一句只是接上「专家模式开关 → 预设挡位」的协调器。
		// 顺序随意：它不依赖上面任何方块/物品的静态字段。
		BHR_GameRules.OnInit();

		// --- 方块 ---
		DEV_BlocksOther.OnInit();
		DEV_StateBoolean.OnInit();
		DEV_StateInt.OnInit();
		DEV_StateEnum.OnInit();
		DEV_StateEnumAxis.OnInit();
		DEV_StateEnumFacing.OnInit();
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
		RDT_LampLapis.OnInit();
		RDT_LampQuartz.OnInit();
		RDT_StepBlock.OnInit();
		RDT_TreeLogSpectre.OnInit();
		RDT_LuminousBlocks.OnInit();

		// --- 方块（方块愚者 BHR，由 1.20.4 迁移而来） ---
		// 第一批：2 个 #wait4move 方块
		BHR_TreeLogPine.OnInit();
		BHR_Compost.OnInit();

		// 第二批：5 个 #deprecated 集合类拆分成 25 个独立类（注册名对照表见 doc/agent/deepseek/06）
		// 顺序约定：家族父类必须先就位（BHR_Soil 在前，BHR_OreSoilFlint / Clay 继承它），
		// 其余按 wiki 物品栏页「包含物品」的分组顺序排列，读起来与游戏里看到的顺序一致。
		// —— 树木：松木树叶（2026-10 由其它 AI 编写的类，本批补齐登记点） ——
		BHR_TreeLeavesPine.OnInit();
		// —— 方块与矿物：切面板岩系 ——
		BHR_Slate.OnInit();
		BHR_SlateCobble.OnInit();
		BHR_OreSlateCopper.OnInit();
		BHR_OreSlateIron.OnInit();
		BHR_OreSlateTin.OnInit();
		BHR_OreSlateCoal.OnInit();
		BHR_OreSlateGold.OnInit();
		BHR_OreSlatePlatinum.OnInit();
		BHR_OreSlateTitanium.OnInit();
		BHR_OreSlateUnknown.OnInit();
		// —— 方块与矿物：红壤系（父类先于两个矿土子类） ——
		BHR_Soil.OnInit();
		BHR_OreSoilFlint.OnInit();
		BHR_OreSoilClay.OnInit();
		BHR_OreSoilUnknown.OnInit();
		// —— 方块与矿物：石灰石 / 大理石 / 海滩沙 ——
		BHR_Limestone.OnInit();
		BHR_LimestoneBrick.OnInit();
		BHR_OreLimestoneOil.OnInit();
		BHR_Marble.OnInit();
		BHR_MarblePolished.OnInit();
		BHR_BeachSand.OnInit();
		// —— 宝石（五个独立类，原来的 BHR_Gems 集合类就此退休） ——
		BHR_GemAmethyst.OnInit();
		BHR_GemSapphire.OnInit();
		BHR_GemEmerald.OnInit();
		BHR_GemRuby.OnInit();
		BHR_GemDiamond.OnInit();

		// --- 物品 ---
		DEV_ItemsOther.OnInit();
		// DEV 测试工具：极限模式切换器（右键切换存档 hardcore，见其类注释与 wiki 页）
		DEV_HardcoreSwitcher.OnInit();
		// 方块愚者（BHR）的物品：松果（种树用；由它放下第一片带激素的松木树叶）
		BHR_PineCone.OnInit();
		// BHR 物品阶段第一批：食物「石灰酸橙」（1.20.5 起的食物 = 普通构造器 + food / consumable 两个组件）
		BHR_Lime.OnInit();
		// BHR 物品阶段第二批（2026-10）：wiki 里已有独立页面的 9 件食物一次补齐。
		// 数据全部取自各自的 wiki 页组件表（nutrition / saturation / can_always_eat / consume_seconds），
		// 口径与第一批的 BHR_Lime 一致。各页的「种植」条目本轮**跳过** —— 对应的树叶/仙人掌方块尚未添加。
		BHR_DodoApple.OnInit();     // 渡渡苹果（配方等价原版苹果，方案见 wiki 备注）
		BHR_Cherry.OnInit();        // 樱桃
		BHR_Coconut.OnInit();       // 椰子
		BHR_CoffeeCherry.OnInit();  // 咖啡樱桃
		BHR_Mango.OnInit();         // 芒果
		BHR_MapleSeed.OnInit();     // 枫实
		BHR_Orange.OnInit();        // 橙子
		BHR_PricklyPear.OnInit();   // 梨果仙人掌（旧语言文件里叫「赤果」）
		BHR_Worm.OnInit();          // 愚人蠕虫（本批唯一 consume_seconds = 2.6）

		// --- 状态效果 ---
		// 随意物件（RDT）的两种「灌注」：正面效果，本身不逐刻做事，
		// 实际行为在攻击结算里（mixin ClassRandomThings.RDT_ImbueHurtMixin →
		// statusEffect.ClassRandomThings.RDT_ImbueAttackHandler）。
		// 顺序随意：两者互不依赖，注册完即可被指令 / 药水引用。
		RDT_FireImbue.OnInit();
		RDT_PoisonImbue.OnInit();
		// 激素药水（饮用型，3 级 ×2 味）。
		// 顺序要求：两个效果的句柄必须先都就绪，注册时才能把「互斥同族」写进 consumable 组件
		// （注册后再补没意义：组件不可变；也不能用自定义 ConsumeEffect，会被序列化丢掉——
		//  两次踩坑详见 item/WhimsyPotionRegistration 类注释）。
		// 上面 RDT_FireImbue/RDT_PoisonImbue 的 OnInit() 已把两个效果注册好，这里直接注册药水即可。
		RDT_FireHormonePotion.OnInit();
		RDT_PoisonHormonePotion.OnInit();

		// 奇思妙想（WI）的两种「损伤类」效果：负面、逐刻扣血。
		// 电离辐射另有「生命值上限减少」，走原版属性修饰符钩子（add/removeAttributeModifiers），
		// 所以提前清除/喝牛奶也能把上限还回来；两者的传染行为在攻击结算里
		// （mixin ClassWhimsyIdea.WI_*HurtMixin → statusEffect.ClassWhimsyIdea.WI_*AttackHandler）。
		// 顺序随意：两个效果互不依赖。
		WI_Radioactive.OnInit();
		WI_BioToxicity.OnInit();
		// WI 的两族药水：瘟疫（生物毒性）与放射（电离辐射），各做最高的两级 × 三种形态
		// （饮用 / 喷溅 / 滞留）。这两族不参与激素互斥。
		WI_PlaguePotion.OnInit();
		WI_RadiationPotion.OnInit();

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
