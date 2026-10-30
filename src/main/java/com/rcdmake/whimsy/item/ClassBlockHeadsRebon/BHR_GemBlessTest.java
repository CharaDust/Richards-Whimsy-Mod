package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing.Blessing;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing.Gem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 宝石祝福测试棒（{@code bhr_gem_bless_test}）—— 只用于开发期验证组件效果。
 *
 * <p>wiki：{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_GemBlessTest.md}
 * （旧实现是 1.20.4 的 NBT 版，右键读 {@code GemBlessing} 的整数并发到聊天框；
 * 随 1.20.5 的 NBT → 组件迁移一起重做，见 {@code BHR_GemBlessing_1} 的 DANGER 说明）。
 *
 * <h2>用法</h2>
 * <ul>
 *   <li><b>右键</b>：给<b>副手</b>物品按 紫锂辉 → 蓝宝石 → 祖母绿 → 红刚玉 → 宝石钻石 的顺序
 *       逐个加上还没加过的祝福（多次右键可以攒齐五种）；</li>
 *   <li><b>潜行 + 右键</b>：清空副手物品上的全部祝福。</li>
 * </ul>
 * 之所以作用在副手：本物品拿在主手时才谈得上「用它去点」，而祝福要给的是<b>工具</b>，
 * 主手位置已经被测试棒占着。副手空着时会明确提示，不做「就近挑一件」这种猜谜行为。
 *
 * <p>祝福本身（组件、等级计算、tooltip）在 {@link BHR_GemBlessing} 与
 * {@code BHR_GemBlessingBonus} 里，本类只负责「施加」这一个动作 ——
 * 将来若改成「右键宝石方块施加」，删掉本类不影响祝福本身。
 */
public class BHR_GemBlessTest extends Item {

	/** 注册名。 */
	public static final String NAME = "bhr_gem_bless_test";

	/** 语言键：副手没有物品。 */
	private static final String KEY_NO_TARGET = "message.richards-whimsy-mod.gem_bless_test.no_target";

	/** 语言键：已加上某种祝福（%s = 祝福名）。 */
	private static final String KEY_ADDED = "message.richards-whimsy-mod.gem_bless_test.added";

	/** 语言键：五种祝福已齐。 */
	private static final String KEY_FULL = "message.richards-whimsy-mod.gem_bless_test.full";

	/** 语言键：已清除全部祝福。 */
	private static final String KEY_CLEARED = "message.richards-whimsy-mod.gem_bless_test.cleared";

	/** 测试棒本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_GEM_BLESS_TEST;

	public BHR_GemBlessTest(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack target = player.getOffhandItem();

		if (level.isClientSide()) {
			// 客户端只做挥手动画；真正的组件写入在服务端。
			return target.isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
		}

		if (target.isEmpty()) {
			player.sendSystemMessage(Component.translatable(KEY_NO_TARGET));
			return InteractionResult.PASS;
		}

		Blessing current = BHR_GemBlessing.get(target);
		if (current == null) {
			current = Blessing.NONE;
		}

		if (player.isSecondaryUseActive()) {
			// 潜行右键：清空。
			if (current.isEmpty()) {
				player.sendSystemMessage(Component.translatable(KEY_CLEARED));
				return InteractionResult.SUCCESS;
			}
			BHR_GemBlessing.clear(target);
			player.sendSystemMessage(Component.translatable(KEY_CLEARED));
			return InteractionResult.SUCCESS;
		}

		// 普通右键：补上下一种还没加的祝福。
		Gem next = null;
		for (Gem gem : Gem.ALL) {
			if (!current.has(gem)) {
				next = gem;
				break;
			}
		}

		if (next == null) {
			player.sendSystemMessage(Component.translatable(KEY_FULL, describe(current)));
			return InteractionResult.SUCCESS;
		}

		Blessing updated = current.with(next);
		BHR_GemBlessing.set(target, updated);
		player.sendSystemMessage(Component.translatable(KEY_ADDED,
				Component.translatable(next.tooltipKey()), describe(updated)));
		return InteractionResult.SUCCESS;
	}

	/** 把当前生效的祝福拼成一行（「紫锂辉」「紫锂辉、祖母绿」…），用于播报。 */
	private static MutableComponent describe(Blessing blessing) {
		MutableComponent text = Component.empty();
		boolean first = true;

		for (Gem gem : Gem.ALL) {
			if (!blessing.has(gem)) {
				continue;
			}
			if (!first) {
				text.append(Component.literal("、"));
			}
			text.append(Component.translatable(gem.tooltipKey()));
			first = false;
		}

		return text;
	}

	/** 注册（走 {@link ModRegister#registerItem}，由它建 {@code Item.Properties} 并写 id）。 */
	public static void OnInit() {
		BHR_GEM_BLESS_TEST = ModRegister.registerItem(NAME, BHR_GemBlessTest::new);
	}
}
