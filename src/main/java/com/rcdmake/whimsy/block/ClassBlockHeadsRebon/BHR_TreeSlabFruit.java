package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 果木台阶（{@code bhr_fruit_slab}）—— 果木（{@link BHR_TreePlanksFruit}）的台阶形态。
 *
 * <p>wiki 规格：父级 = <b>橡木台阶</b>（{@code Blocks.OAK_SLAB}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。方块状态由父类 {@link SlabBlock} 提供
 * （{@code TYPE} = bottom / top / double + {@code WATERLOGGED}），属于<b>多状态</b>方块，
 * 因此 blockstate 与三个模型是<b>手写</b>的（照抄原版 {@code oak_slab} 的文件，只换模型 id 与贴图）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_slab}　<b>模块</b> BHR　<b>父级</b> 橡木台阶
 *       （<b>层 6 家族</b>：继承 {@link SlabBlock}，「上下半砖 / 双拼自动合体 / 含水」全部白拿）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）。层 3 / 8 / 9 / 10 的机制全部由
 *       {@link SlabBlock} 提供，本类一行覆写都没有</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 7 codec（父类已实现）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不</b>进 {@code BlockStateGenerator}
 *       （多状态，blockstate 手写）　③☒ 物品模型无例外（物品直接用 {@code block/bhr_fruit_slab} 模型）
 *       　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}（「树木」组）
 *       　⑥☑ 贴图沿用 {@code bhr/fruit_planks.png}；模型 {@code bhr_fruit_slab} / {@code _top}
 *       　⑦◐ 标签：{@code #minecraft:wooden_slabs}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeSlabFruit extends SlabBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_SLAB;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木台阶的属性：完整复制橡木台阶。 */
	public static BlockBehaviour.Properties fruitSlabProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/** 构造器：装配即可，状态模式与默认状态由 {@link SlabBlock} 的构造器内部完成。 */
	public BHR_TreeSlabFruit(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_SLAB = ModRegister.registerBlockWithItem("bhr_fruit_slab",
				new BHR_TreeSlabFruit(fruitSlabProperties()
						.setId(ModRegister.blockKey("bhr_fruit_slab"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_SLAB, IGNITE_ODDS, BURN_ODDS);
	}
}
