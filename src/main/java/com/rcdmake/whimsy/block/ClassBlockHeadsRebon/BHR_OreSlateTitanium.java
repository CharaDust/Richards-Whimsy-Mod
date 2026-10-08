package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 切面板岩钛矿石（{@code bhr_slate_titanium_ore}）—— 由旧项目 {@code BHR_Rocks} 拆分迁来
 * （旧注册名 {@code rock_titanium_ore}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_OreSlateTitanium.md}）：
 * <ul>
 *   <li>父级：石头；「构造」表逐行都是「父级」</li>
 *   <li>渲染：常规渲染；改写方法 / 事件 / 方块状态：无</li>
 *   <li>命名法：{@code 模块代号_承载方块_矿物质_ore}</li>
 *   <li>备注：钛为方块愚者新引入的矿物，非原版矿物</li>
 *   <li>获取方式：战利品表待「获取方式」阶段统一设定</li>
 * </ul>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_slate_titanium_ore}　<b>模块</b> BHR　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：无（只有强制的层 2 / 4 / 5 / 12）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、
 *       层 8 / 9 / 10（一个覆写点都没有）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 1 级「声明」—— 完整复制石头即可，连覆写点都不需要</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/rock_titanium_ore.png} / 模型 {@code models/block/bhr_slate_titanium_ore.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_OreSlateTitanium extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_SLATE_TITANIUM_ORE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 切面板岩钛矿石的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制石头。 */
	public static BlockBehaviour.Properties slateTitaniumOreProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #slateTitaniumOreProperties()} —— 它是本构造器实参的来源。
	 */
	public BHR_OreSlateTitanium(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_SLATE_TITANIUM_ORE = ModRegister.registerBlockWithItem("bhr_slate_titanium_ore",
				new BHR_OreSlateTitanium(slateTitaniumOreProperties()
						.setId(ModRegister.blockKey("bhr_slate_titanium_ore"))));
	}
}
