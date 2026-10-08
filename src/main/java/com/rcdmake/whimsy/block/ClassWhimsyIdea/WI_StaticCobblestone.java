package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 固石（{@code static_cobblestone} → {@code wi_static_cobblestone}）—— 由模板类 {@code ModBlocks} 迁入 WI 模块。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_StaticCobblestone.md}）：
 * <ul>
 *   <li>父级：石头（属性表逐行都是「父级」）</li>
 *   <li>渲染：常规；改写方法：无；事件：无；方块状态：无（单状态）</li>
 *   <li>获取方式：留待「迁移后统一设定」（本阶段不产出战利品表 / 挖掘标签）</li>
 * </ul>
 *
 * <h2>迁移要点</h2>
 * <ul>
 *   <li><b>注册名补模块前缀</b>：{@code static_cobblestone} → {@code wi_static_cobblestone}
 *       （wiki 备注：命名参照同模块的 {@code wi_static_liquid_water}）。</li>
 *   <li>「父级 = 石头」= 26.2 的 {@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}
 *       （旧 Yarn 写法 {@code FabricBlockSettings.copyOf(Blocks.STONE)}）。</li>
 *   <li>方块物品的注册留在本类内（项目约定），不另建物品类。</li>
 *   <li>迁入后需同步 WI 创造模式物品栏的包含列表（已在本阶段一并更新）。</li>
 * </ul>
 *
 * <p>本类没有事件、没有状态、没有改写方法，因此除了属性复制与注册之外没有别的代码 —— 这是刻意的：
 * 模板遗留方块的价值就是「跟石头一模一样」。
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.STONE)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}</td><td>「父级 = 石头」</td></tr>
 *   <tr><td>{@code Registry.register(Registries.BLOCK, new Identifier(...), block)} + 手写 {@code BlockItem}</td>
 *       <td>{@code ModRegister.registerBlockWithItem("wi_static_cobblestone", block)}</td>
 *       <td>项目自建的注册入口，方块与方块物品一次完成（26.2 还要求 {@code Properties.setId(key)}）</td></tr>
 * </table>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code wi_static_cobblestone}　<b>模块</b> WI（由模板类 {@code ModBlocks} 迁入）　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：无（只有强制的层 2 / 4 / 5 / 12；属性靠层 4 的 {@code ofFullCopy} 一次拿全）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 9 交互与判定、层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级「继承 + 声明」—— {@code extends Block} 白拿基础机制，
 *       属性直接整体复制父级，一行覆写都没有</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code WI_LangEntries}　⑤☑ {@code WI_CreativeTabs} 方块页
 *       　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：留待「迁移后统一设定」）</li>
 * </ul>
 */
public class WI_StaticCobblestone extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_STATIC_COBBLESTONE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 固石的属性：完整复制石头。 */
	public static BlockBehaviour.Properties staticCobblestoneProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #staticCobblestoneProperties()} —— 它是本构造器实参的来源。
	 */
	public WI_StaticCobblestone(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	public static void OnInit() {
		WI_STATIC_COBBLESTONE = ModRegister.registerBlockWithItem("wi_static_cobblestone",
				new WI_StaticCobblestone(staticCobblestoneProperties()
						.setId(ModRegister.blockKey("wi_static_cobblestone"))));
	}
}
