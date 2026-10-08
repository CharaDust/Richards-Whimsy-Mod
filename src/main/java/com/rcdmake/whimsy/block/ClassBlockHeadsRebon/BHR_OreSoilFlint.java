package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 红壤燧石矿土（{@code bhr_soil_flint_ore}）—— 由旧项目 {@code BHR_Soils} 拆分迁来
 * （旧注册名 {@code mineral_soil_flint_ore}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_OreSoilFlint.md}）：
 * <ul>
 *   <li>父级：<b>红壤</b>；硬度 <b>0.7</b>；爆炸抗性及其余行「父级」</li>
 *   <li>渲染：常规渲染；改写方法 / 事件：无</li>
 *   <li>方块状态：{@code GRASS_COVER}（BOOLEAN）—— <b>继承自红壤</b>，默认 FALSE</li>
 *   <li>命名法：{@code 模块代号_承载方块_矿物质_ore}</li>
 * </ul>
 *
 * <h2>层 6 ｜ 家族：{@code extends BHR_Soil}</h2>
 * 「父级：红壤」+「GRASS_COVER 继承自红壤」这两行合起来就是范式的层 6 —— 直接继承
 * {@link BHR_Soil}，于是：
 * <ul>
 *   <li>{@code grass_cover} 的<b>状态定义</b>（{@code createBlockStateDefinition}）白拿；</li>
 *   <li>默认值 {@code grass_cover = false}（写在父类构造器里）白拿；</li>
 *   <li>本类只剩「属性差异」这一件事要写，一行状态代码都不用碰。</li>
 * </ul>
 * 这也是范式 §10.2 第 4 条那个「家族类要不要必看 codec」的问题在<b>模组内家族</b>上的一个正面例子：
 * 继承自家父类同样能白拿整套状态机制。
 *
 * <h2>与旧代码的一处刻意差异：爆炸抗性</h2>
 * 旧代码写 {@code copyOf(MINERAL_SOIL).strength(0.7f)} —— {@code strength} 会<b>同时</b>把爆炸抗性也设成 0.7；
 * wiki 这一行写的是 0.7（硬度）+「父级」（爆炸抗性），所以本类用
 * {@code BHR_Soil.soilProperties().destroyTime(0.7F)}：只改硬度，爆炸抗性跟着红壤走。
 *
 * <h2>方块状态与手写 blockstate</h2>
 * 继承来的 {@code grass_cover} 让它成为<b>多状态</b>方块 → blockstate 必须手写：
 * {@code blockstates/bhr_soil_flint_ore.json} 里 false → {@code block/bhr_soil_flint_ore}，
 * true → {@code block/bhr_soil_flint_ore_grass}（贴图沿用旧资源 {@code bhr/mineral_soil_flint_ore*.png}）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_soil_flint_ore}　<b>模块</b> BHR　<b>父级</b> 红壤（{@code extends BHR_Soil}）</li>
 *   <li><b>启用的可选层</b>：层 6 家族（继承 {@code BHR_Soil}）；层 3 状态属性与层 8 状态模式
 *       <b>由父类提供</b>，本类不重复声明</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3（不在本类声明）、层 7 codec、层 8 生命周期（无覆写）、
 *       层 9 / 10、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0 级「继承」—— 状态机制全部由 {@link BHR_Soil} 提供，本类只换硬度</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（排在 {@code BHR_Soil.OnInit()} <b>之后</b>）
 *       　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明（多状态，手写）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/mineral_soil_flint_ore*.png} / 模型 {@code models/block/bhr_soil_flint_ore.json} + {@code _grass.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_OreSoilFlint extends BHR_Soil {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_SOIL_FLINT_ORE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 红壤燧石矿土的属性（层 4）：父级红壤 → 直接复用 {@link BHR_Soil#soilProperties()}，只把硬度改成 0.7。
	 * <p>用父类的<b>静态工厂</b>而不是 {@code ofFullCopy(BHR_Soil.BHR_SOIL)}，是为了不依赖
	 * 「父类先注册好」这个运行期顺序（工厂只允许依赖静态常量，见范式 §7 硬约束 1）。
	 */
	public static BlockBehaviour.Properties soilFlintOreProperties() {
		return BHR_Soil.soilProperties()
				// 硬度 0.7（爆炸抗性保持父级红壤的值）
				.destroyTime(0.7F);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配。
	 * <p>父类构造器已经把 {@code grass_cover} 的默认值写好，这里不需要再碰状态。
	 */
	public BHR_OreSoilFlint(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 6 ｜ 家族/变体 ──
	// 家族关系由类声明表达：extends BHR_Soil（GRASS_COVER 状态定义 + 默认值全部白拿）。

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_SOIL_FLINT_ORE = ModRegister.registerBlockWithItem("bhr_soil_flint_ore",
				new BHR_OreSoilFlint(soilFlintOreProperties()
						.setId(ModRegister.blockKey("bhr_soil_flint_ore"))));
	}
}
