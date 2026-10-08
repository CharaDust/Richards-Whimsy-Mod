package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 磨制大理石（{@code bhr_polished_marble}）—— 由旧项目 {@code BHR__NormalBlocks} 拆分迁来
 * （旧注册名 {@code bhr_polished_marble}，未变）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_MarblePolished.md}）：
 * <ul>
 *   <li>父级：石头；「构造」表逐行都是「父级」</li>
 *   <li>渲染：常规渲染；改写方法 / 事件 / 方块状态：无</li>
 *   <li>特点：一种由大理石加工的磨制石材方块</li>
 *   <li>获取方式：战利品表待「获取方式」阶段统一设定</li>
 * </ul>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_polished_marble}　<b>模块</b> BHR　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：无（只有强制的层 2 / 4 / 5 / 12）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、
 *       层 8 / 9 / 10（一个覆写点都没有）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 1 级「声明」—— 完整复制石头即可，连覆写点都不需要</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/polished_marble.png} / 模型 {@code models/block/bhr_polished_marble.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_MarblePolished extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_POLISHED_MARBLE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 磨制大理石的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制石头。 */
	public static BlockBehaviour.Properties polishedMarbleProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #polishedMarbleProperties()} —— 它是本构造器实参的来源。
	 */
	public BHR_MarblePolished(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_POLISHED_MARBLE = ModRegister.registerBlockWithItem("bhr_polished_marble",
				new BHR_MarblePolished(polishedMarbleProperties()
						.setId(ModRegister.blockKey("bhr_polished_marble"))));
	}
}
