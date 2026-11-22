package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 果木栅栏（{@code bhr_fruit_fence}）—— 果木（{@link BHR_TreePlanksFruit}）的栅栏形态。
 *
 * <p>wiki 规格：父级 = <b>橡木栅栏</b>（{@code Blocks.OAK_FENCE}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类 {@link FenceBlock} 提供
 * （{@code NORTH} / {@code EAST} / {@code SOUTH} / {@code WEST} + {@code WATERLOGGED}）——
 * <b>多状态</b>，blockstate 是 multipart，与 {@code _post} / {@code _side} / {@code _inventory}
 * 三个模型一起手写（照抄原版 {@code oak_fence}）。
 *
 * <p>⚠️ <b>物品模型必须走例外表</b>：物品图标用的是 {@code block/bhr_fruit_fence_inventory}，
 * 而不是 {@code block/bhr_fruit_fence}（后者不存在 —— 栅栏的方块模型是 multipart 拼出来的）。
 * 所以 {@code ItemModelGenerator.ITEM_MODEL_OVERRIDES} 里有它一条。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_fence}　<b>模块</b> BHR　<b>父级</b> 橡木栅栏
 *       （<b>层 6 家族</b>：继承 {@link FenceBlock}，「自动连墙 / 拴绳 / 碰撞 / 含水」全部白拿）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 / 8 / 9 / 10（父类自带）、层 7 codec（父类已实现）、
 *       层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☑ {@code ITEM_MODEL_OVERRIDES}（{@code block/bhr_fruit_fence_inventory}）
 *       　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 模型（multipart + 三件）　⑦◐ 标签
 *       {@code #minecraft:wooden_fences}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeFenceFruit extends FenceBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_FENCE;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木栅栏的属性：完整复制橡木栅栏。 */
	public static BlockBehaviour.Properties fruitFenceProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/** 构造器：装配即可，四个朝向状态与碰撞形状由 {@link FenceBlock} 构造器内部完成。 */
	public BHR_TreeFenceFruit(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_FENCE = ModRegister.registerBlockWithItem("bhr_fruit_fence",
				new BHR_TreeFenceFruit(fruitFenceProperties()
						.setId(ModRegister.blockKey("bhr_fruit_fence"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_FENCE, IGNITE_ODDS, BURN_ODDS);
	}
}
