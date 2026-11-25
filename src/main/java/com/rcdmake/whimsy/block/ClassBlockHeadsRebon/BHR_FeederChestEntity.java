package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Set;

/**
 * 喂食箱子的方块实体 —— 27 格容器 + 「给动物喂一口」的结算入口。
 *
 * <h2>它只做两件事</h2>
 * <ol>
 *   <li><b>存东西</b>：{@link Container} 的 27 个槽位，随存档保存（{@code ContainerHelper}），
 *       方块被移除时原样吐出（{@code preRemoveSideEffects}）。</li>
 *   <li><b>结算一次喂食</b>：{@link #tryFeed} 是整条功能里唯一会改动物状态的地方，
 *       而且它**只从 {@link BHR_FeederChestGoal} 被调用**（服务端、动物已经站到取食位）。</li>
 * </ol>
 *
 * <h2>为什么「喂食」不需要玩家对象</h2>
 * 原版 {@code Animal#mobInteract(Player, InteractionHand)} 虽然是 public，但它要一个
 * {@code Player}（成年分支还要求 {@code ServerPlayer}），Fabric 又没有内置假玩家。
 * 而它内部真正做的事，每一步的原版方法都是 public：
 * <ul>
 *   <li>{@code Animal#isFood(ItemStack)} / {@code canFallInLove()} / {@code setInLove(Player)}；</li>
 *   <li>{@code AgeableMob#canAgeUp()} / {@code ageUp(int, boolean)} /
 *       {@code getSpeedUpSecondsWhenFeeding(int)}。</li>
 * </ul>
 * 所以这里直接复刻 {@code mobInteract} 的三步，而不是造一个假玩家去调它。
 * 唯一拿不到的是 {@code Animal#playEatingSound()}（protected），因此改用原版通用咀嚼声
 * {@link SoundEvents#GENERIC_EAT}。
 *
 * <h2>冷却</h2>
 * 成年动物喂完就进求偶态（600 tick = <b>30 秒</b>，{@code setInLove} 里写死），
 * 而 {@link #isEligible} 会读 {@code canFallInLove()}，所以冷却天然成立，不需要自己存计时器。
 * 幼崽没有求偶计时器，重复喂食由 Goal 的「一次到站只吃一口」+ 30~60 秒的再次触发间隔限制。
 */
public class BHR_FeederChestEntity extends BlockEntity implements Container {

	/** 容器容量：27 格（三行），与箱子 / 木桶一致。 */
	public static final int CONTAINER_SIZE = 27;

	/** 方块实体类型，在 {@link #OnInit(Block)} 里赋值。 */
	public static BlockEntityType<BHR_FeederChestEntity> BHR_FEEDER_CHEST_ENTITY;

	/** 27 个槽位。 */
	private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

	public BHR_FeederChestEntity(BlockPos pos, BlockState state) {
		super(BHR_FEEDER_CHEST_ENTITY, pos, state);
	}

	/**
	 * 注册方块实体类型（层 12 的登记动作）。
	 *
	 * <p>⚠️ {@code Set.of(block)} 里的方块必须是我们自己的那个 —— 原版 {@code BARREL} / {@code CHEST}
	 * 的合法方块集合里没有它，借用原版类型会让存档读取时内容静默丢失。
	 */
	public static void OnInit(Block block) {
		BHR_FEEDER_CHEST_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ModRegister.id(BHR_FeederChest.REGISTRY_NAME),
				new BlockEntityType<>(BHR_FeederChestEntity::new, Set.of(block)));
	}

	// ── 喂食结算 ──

	/**
	 * 这一只动物现在能不能被喂：状态合格（成年没在求偶冷却 / 幼崽还能长）+ 箱里有它吃的东西。
	 *
	 * <p>Goal 的 {@code isValidTarget} 会先问这句，所以「箱子是空的 / 里面全是它不吃的」时
	 * 动物根本不会走过来 —— 这也是方案 A 比轮询省的地方。
	 */
	public boolean canBeFedNow(Animal animal) {
		return this.isEligible(animal) && this.indexOfFoodFor(animal) >= 0;
	}

	/**
	 * 结算一次喂食：从箱里扣 1 个它能吃的，然后按原版 {@code mobInteract} 的规则施加效果。
	 *
	 * <p>只允许服务端调用；调用方（{@link BHR_FeederChestGoal}）已确保动物站在取食位上。
	 *
	 * @return 真的喂了返回 {@code true}（扣了物品、动物状态已改）
	 */
	public boolean tryFeed(Animal animal) {
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return false;
		}
		if (!this.canBeFedNow(animal)) {
			return false;
		}
		int slot = this.indexOfFoodFor(animal);
		if (slot < 0) {
			return false;
		}

		// 先扣物品：removeItem 内部会 setChanged()，存档与比较器/同步都能看到这次变化
		this.removeItem(slot, 1);
		this.applyFeed(animal);
		return true;
	}

	/**
	 * 施加喂食效果 —— 逐行对照原版 {@code Animal#mobInteract}（26.2 反编译 :132-156）：
	 * <ul>
	 *   <li>成年（{@code getAge() == 0}）且不在求偶态 → {@code setInLove}，600 tick，
	 *       爱心粒子由它广播的实体事件 {@code (byte) 18} 画出；</li>
	 *   <li>幼崽（{@code canAgeUp()}）→ {@code ageUp(getSpeedUpSecondsWhenFeeding(-age), true)}，
	 *       即按「剩余成长时间的 10%」加速，与玩家喂食完全一致。</li>
	 * </ul>
	 * 传 {@code null} 玩家给 {@code setInLove} 是合法的（原版签名就是 {@code @Nullable Player}），
	 * 代价只是不记 {@code ANIMALS_BRED} 统计与繁殖进度 —— 因为确实没有玩家参与。
	 */
	private void applyFeed(Animal animal) {
		if (animal.getAge() == 0 && animal.canFallInLove()) {
			animal.setInLove(null);
		} else if (animal.canAgeUp()) {
			animal.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-animal.getAge()), true);
		}

		Level level = this.getLevel();
		if (level != null) {
			// Animal#playEatingSound() 是 protected，这里用原版通用咀嚼声替代
			level.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_EAT.value(),
					SoundSource.NEUTRAL, 0.6F, 1.0F);
		}
	}

	/** 成年：必须还没在求偶冷却里；幼崽：必须还能长（{@code canAgeUp}）。 */
	private boolean isEligible(Animal animal) {
		return animal.getAge() == 0 ? animal.canFallInLove() : animal.canAgeUp();
	}

	/** 找到第一个「这只动物吃」的槽位，没有返回 {@code -1}。 */
	private int indexOfFoodFor(Animal animal) {
		for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
			ItemStack stack = this.items.get(slot);
			if (!stack.isEmpty() && animal.isFood(stack)) {
				return slot;
			}
		}
		return -1;
	}

	// ── Container ──

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		Level level = this.getLevel();
		if (level != null) {
			level.blockEntityChanged(this.worldPosition);
		}
	}

	// ── 移除与存盘 ──

	/**
	 * 方块被移除时把 27 格原样吐出来（层 12 的「事件」一项）。
	 * <p>26.2 的钩子是 {@code preRemoveSideEffects}；{@code onRemove} 那条老路已经不通。
	 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
			ItemStack stack = this.items.get(slot);
			if (!stack.isEmpty()) {
				Block.popResource(level, pos, stack.copy());
				this.items.set(slot, ItemStack.EMPTY);
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, this.items);
	}
}
