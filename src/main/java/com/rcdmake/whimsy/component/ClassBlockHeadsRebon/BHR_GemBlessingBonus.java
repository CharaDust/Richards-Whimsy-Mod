package com.rcdmake.whimsy.component.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing.Blessing;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing.Gem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 宝石祝福的<b>等级计算</b>与<b>附魔注册表缓存</b>。
 *
 * <p>这个类本身不碰 mixin：它只提供两件事，供
 * {@code mixin/ClassBlockHeadsRebon/BHR_GemBlessingEnchantmentMixin} 调用：
 * <ol>
 *   <li>{@link #bonusFor} —— 单个附魔的等级增量（+0 / +1 / +2）；</li>
 *   <li>{@link #augmented} —— 「祝福生效后」的附魔组件值：真实条目加值 + 合成条目。</li>
 * </ol>
 *
 * <p><b>非破坏式</b>：mixin 只是把原版「读 {@code ENCHANTMENTS} 组件」那一次调用换个来源，
 * 原版方法体一行不改地继续跑。因此<b>其它模组注入在原版方法体里的逻辑照常生效</b>，
 * 唯一可能的冲突出现在「别的模组也 {@code @Redirect} 了同一处调用」——那会由 Mixin
 * 在加载期报冲突（响亮），而不是静默跳过。
 *
 * <h2>为什么需要注册表缓存</h2>
 * 26.2 的附魔是<b>数据包驱动的动态注册表</b>（{@code Registries.ENCHANTMENT}），
 * {@code BuiltInRegistries} 里没有它的条目，所以「合成条目」必须拿到
 * {@link Holder}，也就必须有一份 {@link HolderLookup}。它来自：
 * <ul>
 *   <li>服务端 / 单人存档的内置服务端：{@code ServerLifecycleEvents.SERVER_STARTED}
 *       → {@code server.registryAccess()}；数据包重载（{@code /reload}）后重新取一次；</li>
 *   <li>多人游戏客户端：{@code ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE}
 *       → {@code level.registryAccess()}（换维度也会重新取）。</li>
 * </ul>
 * <b>拿不到时降级而不崩</b>：{@link #augmented} 只给物品上已有的附魔加等级，
 * 不再补合成条目。这样即便缓存还没就绪，也不会因为一次空指针把整局游戏带崩。
 *
 * <h2>为什么不缓存计算结果</h2>
 * {@code EnchantmentHelper} 的遍历在属性重算、每次伤害结算时都会跑，
 * 但只有<b>带祝福的物品</b>才走到这里（mixin 里先判组件），
 * 每次最多 5 种祝福 × 3 条附魔 = 15 次 {@code Holder#is}，
 * 比维护一份以 (组件, 附魔快照) 为键的缓存更便宜也更不容易写错。
 */
public final class BHR_GemBlessingBonus {

	/** 附魔注册表视图；{@code null} 表示还没就绪（见类注释的降级说明）。 */
	private static HolderLookup.RegistryLookup<Enchantment> enchantmentLookup;

	private BHR_GemBlessingBonus() {
	}

	/**
	 * 更新附魔注册表缓存。服务端启动、数据包重载、客户端换世界时各调一次。
	 *
	 * @param provider 注册表访问器（{@code server.registryAccess()} / {@code level.registryAccess()}）
	 */
	public static void updateLookup(HolderLookup.Provider provider) {
		enchantmentLookup = provider == null ? null : provider.lookupOrThrow(Registries.ENCHANTMENT);
	}

	/** 卸下缓存（服务端停止、客户端断线时用，避免继续持有旧世界的注册表）。 */
	public static void clearLookup() {
		enchantmentLookup = null;
	}

	/** 注册表缓存是否就绪（调试用）。 */
	public static boolean hasLookup() {
		return enchantmentLookup != null;
	}

	/** 物品是否带任意一种祝福。 */
	public static boolean isBlessed(ItemInstance stack) {
		Blessing blessing = BHR_GemBlessing.get(stack);
		return blessing != null && !blessing.isEmpty();
	}

	/**
	 * 单个附魔的等级增量。
	 *
	 * <p>按<b>附魔注册键</b>判归属，与物品类型无关 —— 三叉戟上的「水下呼吸」照样被算成 1 级，
	 * 只是原版不会让它生效（那是原版自己的槽位 / 适用物品规则，wiki 备注第 48 行的例子正是这个意思）。
	 *
	 * @return {@code 0} 表示这条附魔不受本物品的祝福影响
	 */
	public static int bonusFor(Blessing blessing, Holder<Enchantment> enchantment) {
		int total = 0;

		for (Gem gem : Gem.ALL) {
			if (!blessing.has(gem)) {
				continue;
			}
			for (ResourceKey<Enchantment> key : gem.enchantments()) {
				if (enchantment.is(key)) {
					// 一条附魔只归属一种祝福，命中即跳出本祝福的比对。
					total += gem.bonus();
					break;
				}
			}
		}

		return total;
	}

	/**
	 * 生成「祝福生效后」的附魔组件值：真实条目各自加值，再补上「物品上本来没有」的合成条目。
	 *
	 * <p>这是非破坏式改写的核心：mixin 只把 {@code EnchantmentHelper} 里那一次
	 * 「读 {@code ENCHANTMENTS} 组件」的调用结果换成本方法的返回值，
	 * <b>原版方法体照常执行</b>（遍历、{@code matchingSlot} 槽位过滤、{@code EnchantedItemInUse} 构造全由原版做）。
	 *
	 * <p>合成条目只有在附魔注册表缓存就绪时才产出；缓存为空时只做「已存在的附魔加值」，
	 * 这种情况下返回值与真实组件等价（不会凭空多出附魔）。
	 *
	 * @param blessing 物品上的祝福（调用方保证非空且非 {@link Blessing#NONE}）
	 * @param base     物品真实的附魔组件；{@code null} 表示物品根本没有该组件
	 * @return 加成后的组件值；若没有任何变化则原样返回 {@code base}（保证无祝福物品零副作用）
	 */
	public static ItemEnchantments augmented(Blessing blessing, ItemEnchantments base) {
		ItemEnchantments actual = base == null ? ItemEnchantments.EMPTY : base;
		Set<ResourceKey<Enchantment>> present = null;
		ItemEnchantments.Mutable mutable = null;

		for (Object2IntMap.Entry<Holder<Enchantment>> entry : actual.entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			if (present == null) {
				present = new HashSet<>(8);
			}
			enchantment.unwrapKey().ifPresent(present::add);

			int bonus = bonusFor(blessing, enchantment);
			if (bonus != 0) {
				if (mutable == null) {
					// 首次真的需要改值时才复制，避免无谓分配。
					mutable = new ItemEnchantments.Mutable(actual);
				}
				mutable.set(enchantment, entry.getIntValue() + bonus);
			}
		}

		HolderLookup.RegistryLookup<Enchantment> lookup = enchantmentLookup;
		if (lookup != null) {
			for (Gem gem : Gem.ALL) {
				if (!blessing.has(gem)) {
					continue;
				}
				for (ResourceKey<Enchantment> key : gem.enchantments()) {
					if (present != null && present.contains(key)) {
						continue;
					}
					Optional<Holder.Reference<Enchantment>> holder = lookup.get(key);
					if (holder.isPresent()) {
						if (mutable == null) {
							mutable = new ItemEnchantments.Mutable(actual);
						}
						mutable.set(holder.get(), gem.bonus());
					}
				}
			}
		}

		return mutable == null ? actual : mutable.toImmutable();
	}
}
