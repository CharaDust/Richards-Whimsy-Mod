package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 果木楼梯（{@code bhr_fruit_stairs}）—— 果木（{@link BHR_TreePlanksFruit}）的楼梯形态。
 *
 * <p>wiki 规格：父级 = <b>橡木楼梯</b>（{@code Blocks.OAK_STAIRS}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类 {@link StairBlock} 提供
 * （{@code FACING} × {@code HALF} × {@code SHAPE} × {@code WATERLOGGED}）——
 * <b>多状态</b>，blockstate 与三套模型手写（照抄原版 {@code oak_stairs}）。
 *
 * <h2>为什么构造器要多一个参数</h2>
 * 26.2 的 {@code StairBlock(BlockState baseState, Properties)} 需要「基方块状态」：
 * 它被 codec 序列化进方块类型（{@code base_state} 字段），也是楼梯外观的来源。照抄原版给的是
 * {@code Blocks.OAK_PLANKS.defaultBlockState()}。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_stairs}　<b>模块</b> BHR　<b>父级</b> 橡木楼梯
 *       （<b>层 6 家族</b>：继承 {@link StairBlock}，「朝向 / 上下半 / 内外转角」全部白拿）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3（父类自带）、层 7 codec（父类已实现）、
 *       层 8 / 9 / 10（父类自带）、层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☒ 物品模型无例外　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 模型三件套（{@code _inner} / {@code _outer}）
 *       　⑦◐ 标签 {@code #minecraft:wooden_stairs}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeStairsFruit extends StairBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_STAIRS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木楼梯的属性：完整复制橡木楼梯。 */
	public static BlockBehaviour.Properties fruitStairsProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器。
	 *
	 * @param baseState 基方块状态（原版给的是对应木板的状态，决定楼梯的「基础方块」语义与 codec 字段）
	 */
	public BHR_TreeStairsFruit(net.minecraft.world.level.block.state.BlockState baseState,
			BlockBehaviour.Properties properties) {
		super(baseState, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_STAIRS = ModRegister.registerBlockWithItem("bhr_fruit_stairs",
				new BHR_TreeStairsFruit(Blocks.OAK_PLANKS.defaultBlockState(),
						fruitStairsProperties().setId(ModRegister.blockKey("bhr_fruit_stairs"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_STAIRS, IGNITE_ODDS, BURN_ODDS);
	}
}
