package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 红壤粘土矿土（{@code bhr_soil_clay_ore}）—— 由旧项目 {@code BHR_Soils} 拆分迁来
 * （旧注册名 {@code mineral_soil_clay_ore}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_OreSoilClay.md}）：
 * <ul>
 *   <li>父级：<b>红壤</b>；硬度 <b>0.7</b>；爆炸抗性及其余行「父级」</li>
 *   <li>渲染：常规渲染；改写方法 / 事件：无</li>
 *   <li>方块状态：{@code GRASS_COVER}（BOOLEAN）—— <b>继承自红壤</b>，默认 FALSE</li>
 *   <li>命名法：{@code 模块代号_承载方块_矿物质_ore}</li>
 * </ul>
 *
 * <p>本类与 {@link BHR_OreSoilFlint} 是同一套结构的换贴图版本（两者 wiki 规格逐行相同，
 * 只有硬度 0.7 与贴图不同）：
 * <ul>
 *   <li>层 6 家族：{@code extends BHR_Soil} → {@code grass_cover} 的状态定义与默认值白拿；
 *       继承的推导与「为什么用父类静态工厂而不是 {@code ofFullCopy}」写在 {@code BHR_OreSoilFlint} 的类注释里；</li>
 *   <li>与旧代码的差异：旧 {@code strength(0.7f)} 会连爆炸抗性一起设成 0.7，wiki 写「父级」→
 *       本类用 {@code destroyTime(0.7F)} 只改硬度；</li>
 *   <li>多状态 → blockstate 手写：{@code blockstates/bhr_soil_clay_ore.json} false →
 *       {@code block/bhr_soil_clay_ore}，true → {@code block/bhr_soil_clay_ore_grass}。</li>
 * </ul>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_soil_clay_ore}　<b>模块</b> BHR　<b>父级</b> 红壤（{@code extends BHR_Soil}）</li>
 *   <li><b>启用的可选层</b>：层 6 家族（继承 {@code BHR_Soil}）；层 3 / 层 8 由父类提供</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3（不在本类声明）、层 7 codec、层 8 生命周期、层 9 / 10、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0 级「继承」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（排在 {@code BHR_Soil.OnInit()} <b>之后</b>）
 *       　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明（多状态，手写）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/mineral_soil_clay_ore*.png} / 模型 {@code models/block/bhr_soil_clay_ore.json} + {@code _grass.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_OreSoilClay extends BHR_Soil {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_SOIL_CLAY_ORE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 红壤粘土矿土的属性（层 4）：父级红壤 → 复用 {@link BHR_Soil#soilProperties()}，硬度改成 0.7。
	 */
	public static BlockBehaviour.Properties soilClayOreProperties() {
		return BHR_Soil.soilProperties()
				// 硬度 0.7（爆炸抗性保持父级红壤的值）
				.destroyTime(0.7F);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配。
	 * <p>父类构造器已经把 {@code grass_cover} 的默认值写好。
	 */
	public BHR_OreSoilClay(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 6 ｜ 家族/变体 ──
	// 家族关系由类声明表达：extends BHR_Soil（GRASS_COVER 状态定义 + 默认值全部白拿）。

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_SOIL_CLAY_ORE = ModRegister.registerBlockWithItem("bhr_soil_clay_ore",
				new BHR_OreSoilClay(soilClayOreProperties()
						.setId(ModRegister.blockKey("bhr_soil_clay_ore"))));
	}
}
