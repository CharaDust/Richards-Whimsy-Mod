package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * 果木栅栏门（{@code bhr_fruit_fence_gate}）—— 果木（{@link BHR_TreePlanksFruit}）的栅栏门。
 *
 * <p>wiki 规格：父级 = <b>橡木栅栏门</b>（{@code Blocks.OAK_FENCE_GATE}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类 {@link FenceGateBlock} 提供
 * （{@code FACING} / {@code OPEN} / {@code POWERED} / {@code IN_WALL}）——<b>多状态</b>，
 * blockstate 与四套模型手写（照抄原版 {@code oak_fence_gate}）。
 *
 * <h2>为什么构造器要多一个 {@link WoodType}</h2>
 * 26.2 的 {@code FenceGateBlock(WoodType, Properties)} 用木种决定<b>开关音效</b>
 * （{@code woodType.fenceGateOpen()} / {@code fenceGateClose()}），并写进 codec 的
 * {@code wood_type} 字段。这里直接用 {@link WoodType#OAK} —— 与「父级 = 橡木」一致；
 * 不注册自定义 WoodType，避免与 {@code WoodType.CODEC} 的字符串解析（未注册的名字解不出来）打架。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_fence_gate}　<b>模块</b> BHR　<b>父级</b> 橡木栅栏门
 *       （<b>层 6 家族</b>：继承 {@link FenceGateBlock}，「开关 / 红石 / 贴墙 / 碰撞」全部白拿）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 / 8 / 9 / 10（父类自带）、层 7 codec（父类已实现）、
 *       层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☒ 物品模型无例外（物品直接用 {@code block/bhr_fruit_fence_gate}）
 *       　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 模型四件（{@code _open} / {@code _wall} / {@code _wall_open}）
 *       　⑦◐ 标签 {@code #minecraft:fence_gates}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeFenceGateFruit extends FenceGateBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_FENCE_GATE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木栅栏门的属性：完整复制橡木栅栏门。 */
	public static BlockBehaviour.Properties fruitFenceGateProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器。
	 *
	 * @param type       木种（决定开关音效与 codec 的 {@code wood_type} 字段），这里用 {@link WoodType#OAK}
	 * @param properties 方块属性
	 */
	public BHR_TreeFenceGateFruit(WoodType type, BlockBehaviour.Properties properties) {
		super(type, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_FENCE_GATE = ModRegister.registerBlockWithItem("bhr_fruit_fence_gate",
				new BHR_TreeFenceGateFruit(WoodType.OAK,
						fruitFenceGateProperties().setId(ModRegister.blockKey("bhr_fruit_fence_gate"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_FENCE_GATE, IGNITE_ODDS, BURN_ODDS);
	}
}
