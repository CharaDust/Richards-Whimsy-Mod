package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * 果木活板门（{@code bhr_fruit_trapdoor}）—— 果木（{@link BHR_TreePlanksFruit}）的活板门。
 *
 * <p>wiki 规格：父级 = <b>橡木活板门</b>（{@code Blocks.OAK_TRAPDOOR}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类 {@link TrapDoorBlock} 提供
 * （{@code FACING} / {@code HALF} / {@code OPEN} / {@code POWERED} / {@code WATERLOGGED}）——
 * <b>多状态</b>，blockstate 与三套模型手写（照抄原版 {@code oak_trapdoor}）。
 *
 * <h2>两个「必须知道」的点</h2>
 * <ol>
 *   <li><b>构造器要多一个 {@link BlockSetType}</b>：{@code TrapDoorBlock(BlockSetType, Properties)}，
 *       这里给 {@link BlockSetType#OAK}（与父级橡木活板门一致）。</li>
 *   <li><b>物品模型必须走例外表</b>：原版 {@code items/oak_trapdoor.json} 指向
 *       {@code block/oak_trapdoor_bottom}，而不是某个叫 {@code oak_trapdoor} 的方块模型
 *       （那个模型不存在 —— 活板门是三套朝向模型）。我们照抄这条，所以
 *       {@code ITEM_MODEL_OVERRIDES} 里有一条 {@code block/bhr_fruit_trapdoor_bottom}。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_trapdoor}　<b>模块</b> BHR　<b>父级</b> 橡木活板门
 *       （<b>层 6 家族</b>：继承 {@link TrapDoorBlock}）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 / 8 / 9 / 10（父类自带）、层 7 codec（父类已实现）、
 *       层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☑ {@code ITEM_MODEL_OVERRIDES}（{@code block/bhr_fruit_trapdoor_bottom}）
 *       　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 贴图 {@code bhr/fruit_trapdoor.png} + 三套模型
 *       　⑦◐ 标签 {@code #minecraft:wooden_trapdoors}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeTrapdoorFruit extends TrapDoorBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_TRAPDOOR;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木活板门的属性：完整复制橡木活板门。 */
	public static BlockBehaviour.Properties fruitTrapdoorProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器。
	 *
	 * @param type       方块组（决定开关音效与红石行为），这里用 {@link BlockSetType#OAK}
	 * @param properties 方块属性
	 */
	public BHR_TreeTrapdoorFruit(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_TRAPDOOR = ModRegister.registerBlockWithItem("bhr_fruit_trapdoor",
				new BHR_TreeTrapdoorFruit(BlockSetType.OAK,
						fruitTrapdoorProperties().setId(ModRegister.blockKey("bhr_fruit_trapdoor"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_TRAPDOOR, IGNITE_ODDS, BURN_ODDS);
	}
}
