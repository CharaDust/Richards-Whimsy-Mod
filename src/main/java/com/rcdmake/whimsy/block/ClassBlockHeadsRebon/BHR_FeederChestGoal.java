package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 动物走向喂食箱子的 Goal —— 「动物自己过来，站到面朝方向的前一格，向箱子要一口吃的」。
 *
 * <h2>它继承谁、白拿了什么（阶梯 0）</h2>
 * {@link MoveToBlockGoal} 是原版「走向某个方块」的抽象基类（乌龟回家、狐狸找浆果、村民种地都在用它）：
 * <ul>
 *   <li>周围螺旋扫描（{@code findNearestBlock}，含 {@code mob.isWithinHome} 过滤）——白拿；</li>
 *   <li>寻路 / 到站判定 / 每 40 tick 重算路径 ——白拿；</li>
 *   <li>{@code nextStartTick} 的随机节流（本类覆盖成 30~60 秒）——白拿。</li>
 * </ul>
 * 本类只实现两件事：{@link #isValidTarget}（哪一格算「喂食箱前」）与
 * {@link #tick}（到站后请求一次喂食）。
 *
 * <h2>坐标约定（最容易搞错的一处）</h2>
 * {@code MoveToBlockGoal} 里的 {@code blockPos} 是<b>动物要站上去的那块方块</b>
 * （父类 {@code getMoveToTarget()} = {@code blockPos.above()}），所以：
 * <pre>
 *   pos（扫描给我们的）   = 取食位脚下那块方块（地面）
 *   standPos = pos.above() = 动物实际站立的那一格
 *   chestPos = standPos 的四邻之一，且箱子的 FACING 必须指向 standPos
 * </pre>
 * 也就是说箱子的 y 与 standPos 相同、由 {@code FACING} 决定它在哪一侧，
 * 不写死「北/南」——放置时面朝玩家，取食位永远在它的正前方。
 *
 * <h2>为什么可以完全不碰 Mixin（阶梯 4 而非 5）</h2>
 * 原版动物各自在自己的 {@code registerGoals()} 里装配目标，外部没有「再挂一个」的入口。
 * 两条路：
 * <ul>
 *   <li>Mixin {@code Mob#registerGoals}（阶梯 5，要三处登记 + {@code require = 0}）；</li>
 *   <li>本类采用：{@code ServerEntityEvents.ENTITY_LOAD} 里加一条 —— 实体每次从存档/区块
 *       重建都拿到一个新实例，此时往它自己的 {@code goalSelector} 里 {@code addGoal} 是原版支持的用法
 *       （{@code GoalSelector#addGoal} 是 public）。</li>
 * </ul>
 * 后者只多一条类调整：{@code Mob.goalSelector} 是 {@code protected final}，由
 * {@code richards-whimsy-mod.classtweaker} 放开成 public（不改行为、只放开可见性）。
 *
 * <h2>优先级与冷却</h2>
 * <ul>
 *   <li>优先级 {@value #GOAL_PRIORITY}：在「被玩家诱惑」（3）之后、「跟幼 / 乱走」（4 / 5）附近。
 *       与 {@code FollowParentGoal} 同为 4，带幼崽时可能轮流执行。</li>
 *   <li>冷却：成年动物喂完即进求偶态（600 tick = 30 秒），{@code canFallInLove()} 变 false →
 *       {@code isValidTarget} 为假 → 目标自行结束，**不需要**自己存冷却计时器。</li>
 *   <li>一次到站只吃一口：{@link #canContinueToUse} 用 {@link #fedThisRun} 立刻掐断，
 *       否则幼崽（没有求偶计时器）会站在原地每 tick 吃一个。</li>
 * </ul>
 */
public class BHR_FeederChestGoal extends MoveToBlockGoal {

	/** 优先级：在「诱惑」之后、「乱走」之前。 */
	public static final int GOAL_PRIORITY = 4;

	/** 搜索半径（格）：动物「注意得到」喂食箱的距离。 */
	private static final int SEARCH_RANGE = 8;

	/** 垂直搜索范围：与父类默认一致，只往上看一格。 */
	private static final int VERTICAL_SEARCH_RANGE = 1;

	/** 移动速度倍率：与原版 {@code TemptGoal} 的 1.25 保持同一量级，但没有玩家时略慢一点。 */
	private static final double SPEED_MODIFIER = 1.1;

	/** 两次尝试之间的最小间隔（tick）= 30 秒。 */
	private static final int MIN_DELAY_TICKS = 600;

	/** 间隔上的随机附加量（tick）= 最多再加 30 秒。 */
	private static final int RANDOM_DELAY_TICKS = 600;

	/** 本 Goal 只服务这一只动物（父类的 {@code mob} 是 {@link PathfinderMob}，这里存强类型引用）。 */
	private final Animal animal;

	/** 本次到站是否已经吃到一口（吃到了就结束，避免原地连吃）。 */
	private boolean fedThisRun;

	/** 本次扫描认定的箱子位置（{@link #isValidTarget} 里填，{@link #tick} 里用）。 */
	private BlockPos chestPos;

	public BHR_FeederChestGoal(Animal animal, double speedModifier) {
		super(animal, speedModifier, SEARCH_RANGE, VERTICAL_SEARCH_RANGE);
		this.animal = animal;
	}

	/**
	 * 把本 Goal 挂到所有「从存档 / 区块重建」的动物身上（阶梯 4）。
	 *
	 * <p>只处理 {@link Animal}：狼 / 猫 / 马这些子类也自动覆盖（它们都继承 {@code Animal}）。
	 * 非动物（僵尸、村民……）直接跳过。
	 */
	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Animal animal) {
				animal.goalSelector.addGoal(GOAL_PRIORITY, new BHR_FeederChestGoal(animal, SPEED_MODIFIER));
			}
		});
	}

	// ── 触发节奏 ──

	/** 「有时」：30~60 秒尝试一次，比父类默认的 10~20 秒安静得多。 */
	@Override
	protected int nextStartTick(PathfinderMob mob) {
		return reducedTickDelay(MIN_DELAY_TICKS + mob.getRandom().nextInt(RANDOM_DELAY_TICKS));
	}

	// ── 目标判定 ──

	/**
	 * 这一格算不算「喂食箱的正前方」。
	 *
	 * <p>性能注意：父类的 {@code findNearestBlock} 会对螺旋扫描到的**每一个**位置调用本方法，
	 * 所以这里的顺序是「先做便宜的方块判定，最后才读方块实体」：
	 * 站立格可进入 → 四邻里有我们的箱子 → 箱子的朝向正好对着站立格 → 才有一次
	 * {@code getBlockEntity}。
	 */
	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		BlockPos standPos = pos.above();

		// ① 站立格必须是空的（动物能站进去），否则这个位置不可能是取食位
		BlockState standState = level.getBlockState(standPos);
		if (!standState.getCollisionShape(level, standPos).isEmpty()) {
			return false;
		}

		// ② 四邻找「面朝站立格」的喂食箱子（不写死方向：放置时面朝玩家，四个方向都可能）
		for (Direction direction : Direction.values()) {
			if (!direction.getAxis().isHorizontal()) {
				continue;
			}
			BlockPos candidate = standPos.relative(direction);
			BlockState state = level.getBlockState(candidate);
			if (!state.is(BHR_FeederChest.BHR_FEEDER_CHEST)) {
				continue;
			}
			// 箱子的 FACING 必须指向站立格
			if (state.getValue(BHR_FeederChest.FACING) != direction.getOpposite()) {
				continue;
			}
			// ③ 最后才读方块实体：箱里得有这只动物吃的东西，并且它现在有资格吃
			if (level.getBlockEntity(candidate, BHR_FeederChestEntity.BHR_FEEDER_CHEST_ENTITY)
					.map(chest -> chest.canBeFedNow(this.animal))
					.orElse(false)) {
				this.chestPos = candidate;
				return true;
			}
		}
		return false;
	}

	// ── 生命周期 ──

	@Override
	public void start() {
		this.fedThisRun = false;
		super.start();
	}

	@Override
	public void stop() {
		this.chestPos = null;
		super.stop();
	}

	/** 吃到一口就结束本次目标（幼崽没有求偶计时器，不掐断会原地每 tick 吃一个）。 */
	@Override
	public boolean canContinueToUse() {
		return !this.fedThisRun && super.canContinueToUse();
	}

	/**
	 * 到站后请求一次喂食。
	 *
	 * <p>Goal 只在服务端执行（{@code Mob#serverAiStep} 里才 {@code goalSelector.tick()}，
	 * 反编译 {@code Mob.java:710/729}），所以这里读方块实体没有双端副作用；
	 * 仍然显式再挡一次客户端，防止以后被别处误调。
	 */
	@Override
	public void tick() {
		super.tick();

		if (this.fedThisRun || !this.isReachedTarget() || this.chestPos == null) {
			return;
		}
		Level level = this.animal.level();
		if (level.isClientSide()) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(this.chestPos);
		if (blockEntity instanceof BHR_FeederChestEntity chest && chest.tryFeed(this.animal)) {
			this.fedThisRun = true;
		}
	}
}
