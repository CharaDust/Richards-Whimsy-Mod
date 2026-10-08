package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 红壤未知矿土（{@code bhr_soil_unknown_ore}）—— 由旧项目 {@code BHR_Soils} 拆分迁来
 * （旧注册名 {@code mineral_soil_unknown_ore}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_OreSoilUnknown.md}）：
 * <ul>
 *   <li>父级：<b>石头</b>（不是红壤！）；「构造」表逐行都是「父级」</li>
 *   <li>渲染：常规渲染；改写方法 / 事件 / <b>方块状态：无</b></li>
 *   <li>命名法：{@code 模块代号_承载方块_矿物质_ore}</li>
 *   <li>wiki 备注特别点出这条<b>与同组其它三个红壤系方块不一致</b>的地方：
 *       「本方块与其他红壤矿土不同，<b>没有</b> GRASS_COVER 方块状态（代码中为普通方块）」</li>
 * </ul>
 *
 * <p>所以本类<b>刻意不继承 {@link BHR_Soil}</b>，也<b>不</b>复制 {@code minerial_soil} 那套地图颜色 / 声音组 ——
 * 它是「一块长得像矿土的石头」：单状态、完整复制石头，blockstate 因此可以交给 datagen 自动生成
 * （同组的燧石 / 粘土矿土是多状态，必须手写，两者的差别就写在这里）。
 * <p>旧代码里这一条也正是普通 {@code new Block(FabricBlockSettings.copyOf(Blocks.STONE))}，
 * 与 wiki 的现状描述完全吻合 —— 本类只是把「注册名补前缀 + 拆成独立类」这两件事做了。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_soil_unknown_ore}　<b>模块</b> BHR　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：无（只有强制的层 2 / 4 / 5 / 12）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族（刻意不继承 {@code BHR_Soil}，
 *       理由见上）、层 7 codec、层 8 / 9 / 10（一个覆写点都没有）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 1 级「声明」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/mineral_soil_unknown_ore.png} / 模型 {@code models/block/bhr_soil_unknown_ore.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_OreSoilUnknown extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_SOIL_UNKNOWN_ORE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 红壤未知矿土的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制石头。 */
	public static BlockBehaviour.Properties soilUnknownOreProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #soilUnknownOreProperties()} —— 它是本构造器实参的来源。
	 */
	public BHR_OreSoilUnknown(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_SOIL_UNKNOWN_ORE = ModRegister.registerBlockWithItem("bhr_soil_unknown_ore",
				new BHR_OreSoilUnknown(soilUnknownOreProperties()
						.setId(ModRegister.blockKey("bhr_soil_unknown_ore"))));
	}
}
