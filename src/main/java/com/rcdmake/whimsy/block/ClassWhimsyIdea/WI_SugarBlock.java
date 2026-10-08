package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 糖块（{@code wi_sugarblock}）—— 由旧项目 {@code WI_SugarBlock} 迁来（1.20.4 Yarn → 26.2 官方名）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_SugarBlock.md}）：
 * <ul>
 *   <li>父级：石头，但声音组改 SAND；其余属性全部沿用父级</li>
 *   <li>渲染：常规（贴图不透明）</li>
 *   <li>改写方法：无</li>
 *   <li>事件：对其按下「交互」键 → 方块消失、播放吃的声音、回 1 饥饿 + 0.1 饱和度、统计「方块被使用」</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条对 26.2 minecraft-merged-deobf 核验过）</h2>
 * <table border="1">
 *   <caption>交互与本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.STONE)}</td><td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}</td><td>「父级 = 石头」</td></tr>
 *   <tr><td>{@code onUse(state, world, pos, player, hand, hit)}</td>
 *       <td>{@code useItemOn(stack, ...)} + {@code useWithoutItem(...)}</td>
 *       <td>1.21.2 起「一次 onUse」被拆成「手持物品」与「空手」两条路径，且都返回 {@code InteractionResult}（原来的
 *           {@code ActionResult} / {@code ItemInteractionResult} 在 26.2 已合并）</td></tr>
 *   <tr><td>{@code player.getHungerManager().add(1, 0.1F)}</td><td>{@code player.getFoodData().eat(1, 0.1F)}</td><td>饥饿值</td></tr>
 *   <tr><td>{@code SoundEvents.ENTITY_GENERIC_EAT}</td><td>{@code SoundEvents.GENERIC_EAT.value()}</td>
 *       <td>26.2 的 SoundEvents 常量是 {@code Holder.Reference}，要取回 {@code SoundEvent}</td></tr>
 *   <tr><td>{@code player.incrementStat(Stats.USED.getOrCreateStat(item))}</td><td>{@code player.awardStat(Stats.ITEM_USED.get(item))}</td><td>统计</td></tr>
 *   <tr><td>{@code world.breakBlock(pos, false)}</td><td>{@code level.destroyBlock(pos, false)}</td><td>不产生掉落</td></tr>
 * </table>
 *
 * <h2>为什么两条交互路径都要覆写</h2>
 * 1.20.4 的 {@code onUse} 对「空手」与「手持物品」都会触发，本类要保持同一手感：
 * 空手走 {@code useWithoutItem}，手持物品走 {@code useItemOn}（默认实现返回
 * {@code TRY_WITH_EMPTY_HAND}，会再回调空手路径）。两条都指向 {@link #eat}，
 * 于是「手里拿着什么都能啃掉这块糖」——与旧版一致。
 *
 * <p>旧代码里注释掉的 {@code player.canConsume(false)}（饱食度满时不能再吃）保持注释状态，
 * 即与 1.20.4 的实际行为一致：<b>不检查</b>饥饿值上限。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code wi_sugarblock}　<b>模块</b> WI　<b>父级</b> 石头（声音组改 SAND）</li>
 *   <li><b>启用的可选层</b>：层 9 交互与判定（{@code useWithoutItem} / {@code useItemOn} 两条路径 → 私有 {@code eat}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（无魔法数值）、层 3 状态属性（单状态）、层 6 家族、层 7 codec、
 *       层 8 生命周期、层 10 形状与外观（常规渲染）、层 11 预留区、层 C 客户端</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外（默认指向方块模型）　④☑ 中英 {@code WI_LangEntries}
 *       　⑤☑ {@code WI_CreativeTabs} 方块页　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表</li>
 * </ul>
 */
public class WI_SugarBlock extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_SUGARBLOCK;

	/** 糖块的属性：复制石头，声音组换成 SAND。 */
	public static BlockBehaviour.Properties sugarBlockProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
				.sound(SoundType.SAND);
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #sugarBlockProperties()} —— 它是本构造器实参的来源。
	 */
	public WI_SugarBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 空手交互（旧 {@code onUse} 的空手那一半）。 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return eat(level, pos, player);
	}

	/** 手持物品交互（旧 {@code onUse} 的另一半）。 */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		return eat(level, pos, player);
	}

	/**
	 * 「吃掉这块糖」：回饥饿、出声音、记统计、方块消失。
	 * <p>与旧代码相同：逻辑只在服务端跑，客户端直接返回 {@code SUCCESS}（挥手动画）。
	 */
	private static InteractionResult eat(Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			// 回饥饿值与饱和度
			player.getFoodData().eat(1, 0.1F);
			// 播放吃的声音
			level.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
			// 统计：方块被使用
			player.awardStat(Stats.ITEM_USED.get(WI_SUGARBLOCK.asItem()));
			// 吃掉了（方块消失，不产生掉落）
			level.destroyBlock(pos, false);
		}
		return InteractionResult.SUCCESS;
	}

	public static void OnInit() {
		WI_SUGARBLOCK = ModRegister.registerBlockWithItem("wi_sugarblock",
				new WI_SugarBlock(sugarBlockProperties().setId(ModRegister.blockKey("wi_sugarblock"))));
	}
}
