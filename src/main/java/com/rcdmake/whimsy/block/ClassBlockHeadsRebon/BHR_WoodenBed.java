package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_BedTier;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 木床（{@code bhr_wooden_bed}）—— 愚者模式的第四档床（The Blockheads 口径的最低一档）。
 *
 * <p>wiki 规格：父级 = <b>白色床</b>（{@code Blocks.WHITE_BED}），
 * 放置 / 双格联动 / 入睡 / 起床 / 重生点 / 村民占床 / 爆炸规则<b>全部</b>由父类
 * {@link BedBlock} 提供 —— 本类<b>一行业务覆写都没有</b>。
 *
 * <h2>它和原版床唯一的不同：愚者模式的睡觉差异</h2>
 * <p>原版睡觉只有「睡着 → 等醒」这一种速度；愚者模式把「床有多好」做成了四档
 * （{@code FLOOR} / {@code WOODEN} / {@code SOFT} / {@code GOLD}，见 {@link BHR_BedTier}），
 * 每档决定<b>精力补充速率</b>、<b>睡满奖励的黄条点数</b>与<b>睡眠时 HUD 上画几个箭头</b>。
 * 木床对应第二档 {@code WOODEN}：
 *
 * <table border="1">
 *   <caption>木床与原版床（{@code SOFT} 档）在愚者模式下的差异</caption>
 *   <tr><th>项目</th><th>木床（{@code WOODEN}）</th><th>原版床（{@code SOFT}）</th></tr>
 *   <tr><td>精力补充（每世界 tick）</td><td>1.5 点</td><td>2.0 点</td></tr>
 *   <tr><td>睡满奖励（黄条）</td><td>2400（= 120 s）</td><td>6000（= 300 s）</td></tr>
 *   <tr><td>睡眠时精力条箭头数</td><td>3 个</td><td>4 个</td></tr>
 * </table>
 * <p><b>差异不在本类里</b>：档位由<b>方块标签</b>
 * {@code richards-whimsy-mod:fool_bed_wooden} 判定（{@link BHR_BedTier#of} 每 tick 读一次方块状态），
 * 所以木床类本身必须保持「与床一样」。想调数值改 {@link BHR_BedTier#WOODEN}，
 * 想加床（金床）就再打一个 {@code fool_bed_gold} 标签，<b>不要</b>改这个类。
 *
 * <h2>两个「必须知道」的点</h2>
 * <ol>
 *   <li><b>构造器要多一个 {@link DyeColor}</b>：26.2 的 {@code BedBlock(DyeColor, Properties)}
 *       把颜色存进被子类读的 {@code color} 字段（原版 {@code white_bed} 传 {@code DyeColor.WHITE}）。
 *       木床没有染色概念，这里给 {@link DyeColor#BROWN} 作木质底色：外观全部来自模型，
 *       这个值不出现在渲染路径上。</li>
 *   <li><b>方块物品必须是 {@link BedItem}，不能用 {@code BlockItem}</b>：
 *       {@code BedItem#placeBlock} 用的是 {@code setBlock(pos, state, 26)}（带
 *       {@code UPDATE_KNOWN_SHAPE}），先落的「床脚」才不会在 {@code setPlacedBy} 补「床头」之前
 *       被自己的 {@code updateShape} 判成「另一半是空气」直接删掉。
 *       用普通 {@code BlockItem}（flags = 3）放床，床脚会当场消失、只留下床头。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_wooden_bed}　<b>模块</b> BHR　<b>父级</b> 白色床
 *       （<b>层 6 家族</b>：继承 {@link BedBlock}）</li>
 *   <li><b>启用的可选层</b>：层 1 常量（{@code COLOR}）、层 12 额外登记（方块标签走数据文件，不在代码里）</li>
 *   <li><b>跳过的可选层</b>：层 3 / 8 / 9 / 10（父类自带 PART / OCCUPIED / FACING 与全部行为）、
 *       层 7 codec（父类已实现，本类无额外构造参数——颜色是常量，见层 1）、层 11、层 C、层 M</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级（继承对的父类 + 声明属性）+ 阶梯第 3 级（标签）</li>
 *   <li><b>登记点</b>：①☑ 主类　②☒ 不进 {@code BlockStateGenerator}（多状态：facing × part，blockstate 手写）
 *       　③☑ {@code ItemModelGenerator} 的<b>复合模型</b>表（物品图标 = 床头 + 床脚，照抄原版
 *       {@code items/white_bed.json}）　④☑ 中英 lang　⑤☑ 创造栏　⑥☑ 贴图
 *       {@code bhr/wooden_bed_{head,foot}_{end,side,up}.png} + 床头 / 床脚两份模型
 *       　⑦◐ 标签 {@code richards-whimsy-mod:fool_bed_wooden}（愚者模式档位）☑；
 *       战利品表 ☑（照抄原版床的「只有 part=head 掉一件」）；配方暂缓（「获取方式」阶段统一做）</li>
 * </ul>
 */
public class BHR_WoodenBed extends BedBlock {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 床的「颜色」。
	 *
	 * <p>{@link BedBlock} 的构造器强制要一个 {@link DyeColor}（原版 16 张床各传一个），
	 * 木床没有染色概念，取木质感的 {@code BROWN}。它只被 {@code BedBlock#getColor()} 读
	 * （原版拿它写数据包 codec），模型的贴图与渲染都不经过它。
	 */
	private static final DyeColor COLOR = DyeColor.BROWN;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_WOODEN_BED;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 木床的属性：完整复制原版<b>棕色床</b>（{@code Blocks.BED.pick(DyeColor.BROWN)}）。
	 *
	 * <p>⚠️ 26.2 的 16 张床不再是一堆独立的 {@code Blocks.WHITE_BED} 常量，而是收进
	 * {@code Blocks.BED} 这个 {@code ColorCollection<Block>}；取其中一张要用
	 * {@code ColorCollection#pick}，没有 {@code Blocks.WHITE_BED} 这个字段（编译期就会报错）。
	 *
	 * <p>复制棕色床而不是白色床，是为了让 {@code mapColor} 那条
	 * 「床脚按染色取色、床头按羊毛取色」的函数捕获到木质色（纯地图颜色，不影响模型）。
	 */
	public static BlockBehaviour.Properties woodenBedProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.BED.pick(DyeColor.BROWN));
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器。
	 *
	 * @param color      床的颜色（只存进父类字段，见 {@link #COLOR}）
	 * @param properties 方块属性
	 */
	public BHR_WoodenBed(DyeColor color, BlockBehaviour.Properties properties) {
		super(color, properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品。
	 *
	 * <p><b>方块物品手工建 {@link BedItem}</b>，不走 {@code ModRegister.registerBlockWithItem}
	 * （那条路给的是 {@code BlockItem}，放床会掉床脚，见类注释第 2 条）。
	 * 堆叠上限 1 与原版床一致（{@code Items} 里 {@code Blocks.BED} 走的是
	 * {@code new Properties().stacksTo(1)}）。
	 */
	public static void OnInit() {
		// 顺序：先方块、后物品（方块物品要引用方块实例）
		BHR_WOODEN_BED = ModRegister.registerBlock("bhr_wooden_bed",
				new BHR_WoodenBed(COLOR,
						woodenBedProperties().setId(ModRegister.blockKey("bhr_wooden_bed"))));

		ModRegister.register("bhr_wooden_bed",
				new BedItem(BHR_WOODEN_BED,
						new Item.Properties().setId(ModRegister.itemKey("bhr_wooden_bed")).stacksTo(1)));
	}
}
