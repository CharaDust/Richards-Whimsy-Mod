package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * 果木门（{@code bhr_fruit_door}）—— 果木（{@link BHR_TreePlanksFruit}）的门。
 *
 * <p>wiki 规格：父级 = <b>橡木门</b>（{@code Blocks.OAK_DOOR}），属性表逐行「父级」，
 * 常规渲染、无事件、无改写方法。状态由父类 {@link DoorBlock} 提供
 * （{@code FACING} / {@code HALF} / {@code HINGE} / {@code OPEN} / {@code POWERED}）——
 * <b>多状态</b>，blockstate 与八套模型手写（照抄原版 {@code oak_door}）。
 *
 * <p>放置/开关/红石/双半联动全部来自 {@link DoorBlock}：
 * {@code setPlacedBy} 里自动补上半，{@code useWithoutItem} 里处理开关与村民交互 —— 本类不碰。
 *
 * <h2>两个「必须知道」的点</h2>
 * <ol>
 *   <li><b>构造器要多一个 {@link BlockSetType}</b>：26.2 的 {@code DoorBlock(BlockSetType, Properties)}
 *       用方块组决定开关音效与红石行为。这里给 {@link BlockSetType#OAK}（与父级橡木门一致）。</li>
 *   <li><b>物品图标必须走例外表</b>：门在物品栏里是<b>一张 2D 平面图</b>
 *       （原版 {@code items/oak_door.json} → {@code minecraft:item/oak_door}），
 *       不是门方块的三维模型。本模组手绘图在 {@code textures/item/bhr/fruit_door.png}，
 *       所以 {@code models/item/bhr_fruit_door.json} + {@code ITEM_MODEL_OVERRIDES} 里各有一条。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_door}　<b>模块</b> BHR　<b>父级</b> 橡木门
 *       （<b>层 6 家族</b>：继承 {@link DoorBlock}）</li>
 *   <li><b>启用的可选层</b>：层 12 额外注册表（可燃性）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 / 8 / 9 / 10（父类自带）、层 7 codec（父类已实现）、
 *       层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级 + 层 12 登记（阶梯第 3 级）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态）
 *       　③☑ {@code ITEM_MODEL_OVERRIDES}（{@code item/bhr_fruit_door}）
 *       　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 贴图 {@code bhr/fruit_door_{top,bottom}.png} + 八套模型
 *       　⑦◐ 标签 {@code #minecraft:wooden_doors}（方块 + 物品）已落地；配方 / 战利品表暂缓</li>
 * </ul>
 */
public class BHR_TreeDoorFruit extends DoorBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 引燃几率：原版木质的 5。 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率：原版木质的 20。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_DOOR;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木门的属性：完整复制橡木门。 */
	public static BlockBehaviour.Properties fruitDoorProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器。
	 *
	 * @param type       方块组（决定开关音效与红石行为），这里用 {@link BlockSetType#OAK}
	 * @param properties 方块属性
	 */
	public BHR_TreeDoorFruit(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块与方块物品，并登记可燃性。 */
	public static void OnInit() {
		BHR_FRUIT_DOOR = ModRegister.registerBlockWithItem("bhr_fruit_door",
				new BHR_TreeDoorFruit(BlockSetType.OAK,
						fruitDoorProperties().setId(ModRegister.blockKey("bhr_fruit_door"))));

		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_DOOR, IGNITE_ODDS, BURN_ODDS);
	}
}
