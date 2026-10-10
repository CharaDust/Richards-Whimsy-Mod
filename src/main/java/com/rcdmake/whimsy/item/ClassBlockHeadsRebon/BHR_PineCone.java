package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_TreeLeavesPine;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 松果（{@code bhr_pine_cone}）—— BHR 的「种树」物品：在可种植基底上放下一片带生长激素的松木树叶。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_PineCone.md}）：
 * <ul>
 *   <li>特点简介：一种可用于种植松木的物品；构造：普通物品（无特殊构造器，功能由「组件」定义）</li>
 *   <li>组件：默认组件；{@code max_stack_size} 写 64 —— 那是 26.2 的默认值，代码里不必显式指定
 *       （只有非 64 才需要 {@code stacksTo(...)}）</li>
 *   <li>改写方法 / 获取方式：无</li>
 *   <li><b>事件</b>（wiki 2026-10 已从「备注」移入「事件」小节，本类已实现）：手持松果交互以下方块时，
 *       在其上方放置一片松木树叶 —— {@code persistent=false}、{@code bhr_hormone_type=dominant}、
 *       {@code bhr_hormone_count=X}（X 为 10~15 的均匀随机数）：
 *       <ul>
 *         <li>bhr：松木原木、红壤、红壤燧石矿土、红壤粘土矿土</li>
 *         <li>vanilla：泥土、缠根泥土、草方块、灰化土、砂土、菌丝体、耕地、苔藓块、苍白苔藓块、泥巴、沾泥的红树根</li>
 *       </ul></li>
 * </ul>
 *
 * <h2>它与 {@code BHR_TreeLeavesPine} 的分工</h2>
 * 松果只负责「起头」：放一片 {@code dominant} 的叶（正是一次手动调好状态的 {@code /setblock}，
 * 省掉每次自己拼 {@code persistent / type / count} 三个状态）。之后的生长全在方块的 {@code randomTick} 里，
 * 包括 wiki 里那条尚未定案的「向外侧生长」。
 * <p>基底清单<b>不在这里另写一份</b>：直接复用
 * {@link BHR_TreeLeavesPine#isPlantableBase(net.minecraft.world.level.block.state.BlockState)}
 * —— wiki 两页列的就是同一批方块（松果的目标方块 = 树叶的生长基底），收在一处才不会改了一边忘了另一边。
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_pine_cone}　<b>模块</b> BHR　<b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（激素计数范围）、层 9 交互（{@code useOn}）——
 *       物品没有方块的 12 层那么多钩子，层号仍沿用范式 §2 的编号以便与本项目其余类对照</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（物品没有方块状态）、层 4 属性工厂
 *       （{@code ModRegister.registerItem} 自己建 {@code Item.Properties} 并写 id）、层 6 家族 / 层 7 codec（普通物品不需要）、
 *       层 8 生命周期、层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（物品区）
 *       　②☑ 手写 {@code models/item/bhr_pine_cone.json}（2D 图标）+ datagen 自动生成
 *       {@code items/bhr_pine_cone.json}　③☑ 中英 {@code BHR_LangEntries}（物品只有 {@code item.…} 一条键，
 *       没有方块那条）　④☑ {@code BHR_CreativeTabs}（树木组，排在松木树叶之后）
 *       　⑤☑ 贴图 {@code textures/item/bhr/pine_cone.png}（既有资源）
 *       　⑥☐ 配方 / 战利品表 ——「获取方式」阶段统一做（wiki 写「无」）</li>
 * </ul>
 */
public class BHR_PineCone extends Item {

	// ── 层 1 ｜ 规格常量 ──

	/** 激素计数下限（wiki：X 取 10~15，每个数机会均等）。 */
	public static final int HORMONE_COUNT_MIN = 10;

	/** 激素计数上限（含）。 */
	public static final int HORMONE_COUNT_MAX = 15;

	// ── 层 2 ｜ 物品声明 ──

	/** 松果本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_PINE_CONE;

	// ── 层 5 ｜ 构造器 ──

	/**
	 * 构造器（层 5）：只做装配。
	 * <p>物品的属性由 {@link ModRegister#registerItem} 统一构造（{@code new Item.Properties().setId(key)}），
	 * 所以本类没有层 4 的属性工厂 —— 这与方块类的「工厂先于构造器」不冲突，只是物品这边的注册入口代劳了。
	 */
	public BHR_PineCone(Item.Properties properties) {
		super(properties);
	}

	// ── 层 9 ｜ 交互 ──

	/**
	 * 右键方块（层 9）：在被点击方块的上方放一片「带激素的松木树叶」。
	 * <p>判定口径与方块那边保持一致：基底必须通过
	 * {@link BHR_TreeLeavesPine#isPlantableBase(net.minecraft.world.level.block.state.BlockState)}，
	 * 上方必须是<b>空气</b>（与生长逻辑的「上方是空气」同一口径，刻意不做 {@code canBeReplaced} 的放宽）。
	 * <p>音效两端都放（客户端放给自己、服务端广播给其他人），与 {@code BlockItem} 的写法一致；
	 * 放置只在服务端发生，客户端提前返回 {@code SUCCESS} 只是为了挥手动画。
	 * <p>生存模式扣 1 个，创造模式不扣。
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos leafPos = context.getClickedPos().above();

		if (!BHR_TreeLeavesPine.isPlantableBase(level.getBlockState(context.getClickedPos()))
				|| !level.getBlockState(leafPos).isAir()) {
			return InteractionResult.PASS;
		}

		BlockState leaf = BHR_TreeLeavesPine.BHR_PINE_LEAVES.defaultBlockState()
				.setValue(BHR_TreeLeavesPine.BHR_HORMONE_TYPE, BHR_TreeLeavesPine.HormoneType.DOMINANT)
				.setValue(BHR_TreeLeavesPine.BHR_HORMONE_COUNT, nextHormoneCount(level));

		level.playSound(context.getPlayer(), leafPos, leaf.getSoundType().getPlaceSound(),
				SoundSource.BLOCKS, 1.0F, 1.0F);

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		level.setBlockAndUpdate(leafPos, leaf);

		Player player = context.getPlayer();
		if (player == null || !player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.SUCCESS;
	}

	/** wiki「X 为 10~15 的随机数，每个数均等机会」：{@code nextInt(6)} 得 0~5，加下限即 10~15。 */
	private static int nextHormoneCount(Level level) {
		return HORMONE_COUNT_MIN + level.getRandom().nextInt(HORMONE_COUNT_MAX - HORMONE_COUNT_MIN + 1);
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）：{@code registerItem} 负责建 {@code Item.Properties} 并写 id 与物品栏登记。 */
	public static void OnInit() {
		BHR_PINE_CONE = ModRegister.registerItem("bhr_pine_cone", BHR_PineCone::new);
	}
}
