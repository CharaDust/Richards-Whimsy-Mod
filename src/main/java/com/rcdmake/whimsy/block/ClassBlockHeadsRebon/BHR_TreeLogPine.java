package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 松木原木（{@code bhr_pine_log}）—— 由旧项目 {@code BHR_TreeLogPine} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_TreeLogPine.md}）：
 * <ul>
 *   <li>父级：橡木原木；属性表逐行「父级」，常规渲染，无事件</li>
 *   <li>改写方法：放置时轴向取决于贴合面（类似原木）</li>
 *   <li>方块状态：{@code AXIS} + <b>{@code #deprecated BHR_DECAYED}</b>（BOOLEAN）</li>
 *   <li>获取方式：合适工具是斧，破坏后掉落自身（战利品表按全项目既定决定暂缓）</li>
 * </ul>
 *
 * <h2>为什么只剩 {@code AXIS}</h2>
 * wiki 的「方块状态」区把 {@code BHR_DECAYED} 标了 <b>{@code #deprecated}</b>，
 * 并注明原因：「挖掘方块时不会保留方块状态；该状态将拆分为两个不同方块 —— 松木原木、枯萎的松木原木」。
 * 按既定规则<b>不移植 {@code #deprecated} 条目</b>，所以本类只保留 {@code AXIS}，
 * 枯萎形态留给将来的 {@code #todo} 方块「枯萎的松木原木」（贴图 {@code bhr/pine_decayed_log*.png}
 * 与模型 {@code bhr_pine_decayed_log} 已随资源迁入，届时直接用）。
 * <p>连带后果：旧的手写 {@code blockstates/bhr_pine_log.json} 里有 6 个 variants
 * （{@code axis} × {@code bhr_decayed}），本阶段已改成 3 个（只留 {@code axis}）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_pine_log}　<b>模块</b> BHR　<b>父级</b> 橡木原木
 *       （<b>层 6 家族</b>：继承原版 {@code RotatedPillarBlock}）</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code AXIS}，由父类提供）、层 8 生命周期
 *       （构造器里的 {@code registerDefaultState}）—— 其余可选层都没用</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 7 codec（父类 {@code RotatedPillarBlock} 已提供）、
 *       层 9 / 10（通灵原木那套「实体穿过 / 半透明 / 贴合面剔除」本方块<b>不要</b>：wiki 写的是常规原木）、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0 级「继承」—— {@code RotatedPillarBlock} 白拿
 *       「AXIS 状态 + 放置时按贴合面 + 六个模型朝向」，一行覆写都不用写</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ <b>手写</b> {@code blockstates/bhr_pine_log.json}
 *       （{@code AXIS} 是多状态，datagen 不生成、也不在 {@code collectAll()} 里声明）
 *       　③☑ 物品模型定义由 {@code ItemModelGenerator} 自动生成　④☑ 中英 {@code BHR_LangEntries}
 *       　⑤☑ {@code BHR_CreativeTabs}　⑥☑ 贴图 / 模型（{@code models/block/bhr_pine_log.json} 等已随资源迁入）
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签（斧）——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_TreeLogPine extends RotatedPillarBlock {

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_PINE_LOG;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/** 松木原木的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制橡木原木。 */
	public static BlockBehaviour.Properties pineLogProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #pineLogProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 * <p>默认轴取 X（与旧代码一致；实际放置时会被原版 {@code RotatedPillarBlock} 的放置逻辑覆盖成贴合面的轴）。
	 */
	public BHR_TreeLogPine(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
	}

	// ── 层 6 ｜ 家族/变体 ──

	// 家族关系由类声明表达：extends RotatedPillarBlock（AXIS 状态、放置逻辑、模型朝向全部白拿）。

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_PINE_LOG = ModRegister.registerBlockWithItem("bhr_pine_log",
				new BHR_TreeLogPine(pineLogProperties()
						.setId(ModRegister.blockKey("bhr_pine_log"))));
	}
}
