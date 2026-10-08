package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Map;

/**
 * 开发方块集合（方块）—— 由旧的 {@code DEV_Blocks} 更名而来。
 *
 * <p>包含 2 个方块：
 * <ul>
 *   <li>一般方块 {@code dev_block_normal}</li>
 *   <li>固定向上方块 {@code dev_block_vector_fix_side_u}</li>
 * </ul>
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassDEV/DEV_BlocksOther.md}）：
 * 父级 = 石头，属性表其余各项均为「父级」，无改写方法、无事件、无方块状态、常规渲染。
 * 因此两个方块都用 {@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}——
 * 「父级 = 石头」就是「复制石头的属性」，对应 26.2 的 {@code ofFullCopy}
 * （旧的 Yarn 写法是 {@code FabricBlockSettings.copyOf(Blocks.STONE)}）。
 *
 * <p>「固定向上方块」是一块永远朝上的方块，朝向由模型写死
 * （模型 {@code dev_block_vector_fix_side_u} 用 {@code cube_bottom_top} 固定上下贴图），
 * 不带方向状态，所以这里仍然是普通 {@link Block}。
 *
 * <p>两个方块都是「方块 + 方块物品」一起注册，方块物品的注册在本类内完成，
 * 不另外建物品类（项目约定）。
 *
 * <p><b>本类不再维护 {@code DEV_BLOCKS} 集合变量</b>：
 * 创造模式物品栏直接引用下面的两个方块变量即可。理由与 {@code DEV_ITEMS} 相同
 * （详见 {@code DEV_CreativeTabs} 的类注释）。
 *
 * <p><b>阶段 0 起也不再维护 {@code blockItems()}</b>：那是给语言提供器用的方块物品列表，
 * 现在由 {@code LangEntrySupport.addBlockAndItem(builder, 方块, 名字)} 一个方块一次调用同时写
 * 「方块」与「方块物品」两条键，列表本身就多余了（26.2 的「方块物品要单独写一条翻译」
 * 这条事实已记进 {@code LangEntrySupport} 的类注释）。
 */
public class DEV_BlocksOther {

	/** 一般方块 {@code dev_block_normal}。 */
	public static Block DEV_BLOCK_NORMAL;
	/** 固定向上方块 {@code dev_block_vector_fix_side_u}。 */
	public static Block DEV_BLOCK_VECTOR_FIX_SIDE_U;

	/**
	 * 方块状态数据生成的声明：方块 → 模型路径后缀（空串 = 沿用 {@code block/<方块id>}）。
	 *
	 * <p>本类两个方块都是**单方块状态**（无属性），模型与方块同名，
	 * 所以 blockstate 可以完全由 datagen 生成，不需要手写。
	 * 多状态方块不要往这里加 —— 它们的 variants 写法各异，仍需手写。
	 *
	 * <p><b>为什么这里保留显式清单</b>：blockstate 的生成是「按声明」而非「按模块」
	 * —— 必须能精确控制哪些方块交给自动生成（单状态）而哪些留给手写（多状态）。
	 * 因此这个 map 不随 {@code DEV_BLOCKS} 一起退场。
	 */
	public static Map<Block, String> blockStateModels() {
		return Map.of(
				DEV_BLOCK_NORMAL, "",
				DEV_BLOCK_VECTOR_FIX_SIDE_U, "");
	}

	/**
	 * 初始化：构造并注册本类的全部方块及其方块物品。
	 * <p>主类只需要调用这一条。
	 *
	 * <p>方块在这里才构造并把 id 写进 Properties：{@code Properties.setId} 是方块
	 * 取得自己 id 的唯一途径，必须在 {@code new Block(...)} 之前调用。
	 */
	public static void OnInit() {
		DEV_BLOCK_NORMAL = ModRegister.registerBlockWithItem("dev_block_normal",
				new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_normal"))));

		DEV_BLOCK_VECTOR_FIX_SIDE_U = ModRegister.registerBlockWithItem("dev_block_vector_fix_side_u",
				new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_vector_fix_side_u"))));
	}
}
