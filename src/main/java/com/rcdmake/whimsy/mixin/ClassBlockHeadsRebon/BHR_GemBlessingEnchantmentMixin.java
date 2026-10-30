package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessing.Blessing;
import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_GemBlessingBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 宝石祝福：把组件里的祝福折算成<b>附魔等级增量</b>（紫锂辉 / 蓝宝石 / 祖母绿 +1，红刚玉 / 宝石钻石 +2）。
 *
 * <h2>为什么挂在 {@code EnchantmentHelper} 而不是逐条效果去改</h2>
 * <p>26.2 的附魔效果是<b>数据驱动</b>的：几乎所有效果（耐久、保护、伤害、击退、属性修饰符、
 * 弹射物、经验修补、风爆…）都从同一个遍历入口 {@code runIterationOnItem} 出发，
 * 而「问一句某附魔几级」则统一走 {@code getItemEnchantmentLevel}。
 * 对 26.2 源码全量核对后，除去 {@code EnchantmentHelper} 自身，游戏里对附魔等级的读取只有 16 处调用点，
 * 全部收口在这三个方法上，所以注入三处就覆盖了整个玩法面。
 *
 * <h2>非破坏式：只换「读组件」这一次调用的来源</h2>
 * <p>两个 {@code runIterationOnItem} 都用 {@link Redirect} 拦住方法体里<b>唯一</b>的那次
 * 「读 {@code ENCHANTMENTS} 组件」调用，把结果换成本模组算好的
 * {@link BHR_GemBlessingBonus#augmented} 值：
 * <ul>
 *   <li>2 参重载读的是 {@code ItemStack.getOrDefault(ENCHANTMENTS, EMPTY)}
 *       （字节码：{@code INVOKEVIRTUAL ItemStack.getOrDefault}）；</li>
 *   <li>4 参重载读的是 {@code ItemStack.get(ENCHANTMENTS)}（{@code INVOKEVIRTUAL ItemStack.get}），
 *       并且后面跟着 {@code != null && !isEmpty()} 的两道闸 —— 所以这里<b>必须能返回非 null</b>，
 *       否则「物品压根没有附魔组件」这种最要紧的情况（没附魔的工具加祝福）会被原版直接跳过。</li>
 * </ul>
 * <p>这样原版的方法体<b>一行不改地照常执行</b>：遍历、{@code matchingSlot} 槽位过滤、
 * {@code EnchantedItemInUse} 的构造、以及其它模组注入在这个方法体里的
 * {@code @Redirect} / {@code @ModifyArg} / {@code @ModifyVariable} / 局部变量捕获，全部照常生效。
 * <p>比起「HEAD 取消 + 复刻一遍正文」，这个写法不会静默吞掉别人的注入；唯一的冲突形态变成
 * 「别的模组也重定向了同一处调用」→ Mixin 在加载期直接报冲突（响亮），符合「保留 require=1、
 * 方便后续测兼容性」的取向。
 *
 * <h2>刻意不做的事</h2>
 * <ul>
 *   <li><b>不碰 {@code DataComponents.ENCHANTMENTS}</b>：重定向只影响「这次读取」看到的视图，
 *       不写回物品。所以没附魔的工具加了祝福也不会亮附魔光效（{@code ItemStack#isFoil} →
 *       {@code isEnchanted} 只看真组件），铁砧 / 砂轮 / 附魔台也不会把祝福当成可打磨、可计价的词条；</li>
 *   <li><b>不碰附魔书</b>：只认 {@code ENCHANTMENTS}，附魔书的 {@code STORED_ENCHANTMENTS} 不受影响；</li>
 *   <li><b>不做上限封顶</b>：原等级直接 +1/+2（耐久 3→4、时运 3→5、保护 4→6 都会真的超上限生效），
 *       这是本轮确认的口径。</li>
 * </ul>
 */
@Mixin(EnchantmentHelper.class)
public class BHR_GemBlessingEnchantmentMixin {

	/**
	 * 等级查询：在返回值上加祝福增量。
	 *
	 * <p>这里读的是 {@link ItemInstance}（不是 {@code ItemStack}）：26.2 起这个方法对
	 * 「物品快照」也开放（{@code ItemStackTemplate} 之类同样实现该接口），组件读取接口是同一个。
	 * <p>这一处本来就是「读返回值」，不改控制流，因此不需要重定向。
	 */
	@Inject(
			method = "getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemInstance;)I",
			at = @At("RETURN"),
			cancellable = true)
	private static void bhr$boostEnchantmentLevel(Holder<Enchantment> enchantment, ItemInstance piece,
			CallbackInfoReturnable<Integer> cir) {
		Blessing blessing = BHR_GemBlessing.get(piece);
		if (blessing == null || blessing.isEmpty()) {
			return;
		}
		int bonus = BHR_GemBlessingBonus.bonusFor(blessing, enchantment);
		if (bonus != 0) {
			cir.setReturnValue(cir.getReturnValue() + bonus);
		}
	}

	/**
	 * 2 参 {@code runIterationOnItem}：重定向它读附魔组件的那次 {@code getOrDefault}。
	 *
	 * <p>返回值声明成 {@code Object} 是为了与泛型擦除后的描述符
	 * {@code (…)Ljava/lang/Object;} 严格对齐；原版拿到后自己 checkcast 成 {@code ItemEnchantments}。
	 */
	@Redirect(
			method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private static Object bhr$augmentEnchantmentsForIteration(ItemStack piece, DataComponentType<?> type,
			Object fallback) {
		return bhr$augment(piece, type, fallback);
	}

	/**
	 * 4 参 {@code runIterationOnItem}：重定向它读附魔组件的那次 {@code get}。
	 *
	 * <p><b>必须允许返回非 null</b>：原版拿到的 {@code null} 会让整段遍历被跳过，
	 * 而「物品上压根没有附魔组件」正是祝福要起作用的主要场景。
	 */
	@Redirect(
			method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	private static Object bhr$augmentEnchantmentsForSlotIteration(ItemStack piece, DataComponentType<?> type) {
		return bhr$augment(piece, type, null);
	}

	/**
	 * 两个重定向共用的实现。
	 *
	 * @param piece    被遍历的物品
	 * @param type     被读取的组件类型（正常只会是 {@code ENCHANTMENTS}）
	 * @param fallback 原调用的兜底值（2 参重载是 {@code ItemEnchantments.EMPTY}，4 参重载是 {@code null}）
	 * @return 没有祝福时<b>原样</b>返回兜底/真实值，保证无祝福物品零副作用
	 */
	private static Object bhr$augment(ItemStack piece, DataComponentType<?> type, Object fallback) {
		if (type != DataComponents.ENCHANTMENTS) {
			// 理论上到不了这里（两处调用都读 ENCHANTMENTS），保底按原语义走。
			return piece.get(type);
		}

		// 真实组件值统一由我们自己取：4 参重载的原调用没有兜底值，只能走这一条。
		ItemEnchantments real = (ItemEnchantments) piece.get(type);

		Blessing blessing = BHR_GemBlessing.get(piece);
		if (blessing == null || blessing.isEmpty()) {
			// 没有祝福：与原来那次调用的结果逐字一致（null 的场合交回兜底值）。
			return real != null ? real : fallback;
		}

		return BHR_GemBlessingBonus.augmented(blessing, real);
	}
}
