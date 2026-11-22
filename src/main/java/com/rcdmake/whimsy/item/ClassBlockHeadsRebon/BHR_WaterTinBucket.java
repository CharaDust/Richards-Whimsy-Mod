package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 锡水桶（{@code bhr_water_tin_bucket}）—— 装了水的锡桶：<b>只能倒水</b>，倒完变回空锡桶。
 *
 * <h2>为什么不是原版水桶</h2>
 * <p>需求原文：「Bucket of Water 不能用原版的水桶，因为这个桶设定上是锡做的，<b>不能装岩浆</b>」。
 * 本类因此<b>不继承</b>原版 {@code BucketItem}：
 * <ul>
 *   <li>原版 {@code BucketItem#getEmptySuccessItem} 是 {@code static}，返回的是<b>写死的原版空桶</b>
 *       {@code Items.BUCKET} —— 继承过来倒完水就变成铁桶，锡桶设定当场破功；</li>
 *   <li>同理它也不该是 {@code Items.BUCKET} / {@code WATER_BUCKET} 的替身：原版岩浆桶 / 炼药锅 /
 *       拴绳那一圈逻辑都按物品相等判定，替身只会把「能装岩浆」这件事漏回来。</li>
 * </ul>
 * 于是本类自己写 {@link #use}，但<b>倒水本身仍借原版实现</b>：
 * {@code ((BucketItem) Items.WATER_BUCKET).emptyContents(...)}（26.2 的
 * {@code BucketItem#emptyContents} 是 {@code public}）——水面放置、液体替换、音效、粒子、
 * {@code LiquidBlockContainer}（含水方块）那套全部白拿，只用原版一条已验证过的路径。
 * 唯一自己做的两件事：判定「点到的位置能不能放」，以及<b>倒完发一只空锡桶</b>
 * （{@link BHR_TinBucket#BHR_TIN_BUCKET}，而不是原版空桶）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>最大堆叠 <b>1</b>（与原版水桶一致：装了液体的桶不堆叠）；</li>
 *   <li>获取：待定（「获取方式」阶段；空锡桶舀水见 {@link BHR_TinBucket} 的 TODO）；</li>
 *   <li>用途：篝火 {@code mineral_salt.json} / {@code flatbread.json} 的原料，
 *       两条配方都用 {@code "container"} 字段返还空锡桶。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 锡水桶　<b>英文名</b> Tin Bucket of Water　<b>注册名</b> {@code bhr_water_tin_bucket}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）—— 倒水走覆写 {@code use}（第 2 级：覆写本类方法）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（堆叠数）、层 2 物品声明、层 9 交互（{@code use}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：层 3 组件 / 层 4 装配（无组件）、层 6 家族（刻意<b>不</b>继承 {@code BucketItem}，见上）、
 *       层 7 codec / 层 8 生命周期 / 层 10 外观 / 层 11 预留 / 层 C / 层 M</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_water_tin_bucket.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（材料组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/water_tin_bucket.png}　⑥☑ 篝火配方（作为原料）</li>
 * </ul>
 */
public class BHR_WaterTinBucket extends Item {

	/** 装了水的锡桶最多 1 只一堆（与原版水桶一致）。 */
	public static final int MAX_STACK_SIZE = 1;

	/** 锡水桶本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_WATER_TIN_BUCKET;

	public BHR_WaterTinBucket(Item.Properties properties) {
		super(properties);
	}

	// ── 层 9 ｜ 交互 ──

	/**
	 * 右键倒水（对应旧 Yarn 的 {@code Item#use(World, PlayerEntity, Hand)}）。
	 *
	 * <p>流程逐条对齐原版 {@code BucketItem#use} 的「装了液体」那一支：
	 * <ol>
	 *   <li>视线命中方块（{@code ClipContext.Fluid.NONE}：液体不挡视线，与原版水桶一致）；</li>
	 *   <li>点到的若是 {@code LiquidBlockContainer}（炼药锅、含水方块）就往它自己身上放，否则放到点击面外侧；</li>
	 *   <li>{@code mayInteract} + {@code mayUseItemAt} 两道权限检查（领地类模组挂在这里）；</li>
	 *   <li>交给原版 {@code BucketItem#emptyContents} 真正倒水；倒不出去就 {@code PASS}，<b>不消耗</b>；</li>
	 *   <li>倒成功：扣 1 只锡水桶，发 1 只空锡桶（背包满了就掉在脚下）。</li>
	 * </ol>
	 * 创造模式不扣（与原版桶一致）。
	 */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}

		var pos = hit.getBlockPos();
		var face = hit.getDirection();
		var placePos = level.getBlockState(pos).getBlock() instanceof LiquidBlockContainer
				? pos
				: pos.relative(face);
		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(placePos, face, stack)) {
			return InteractionResult.PASS;
		}

		// 借原版水桶的倒水实现（挖掉「倒完变原版空桶」那一段，其余全白拿）
		BucketItem vanillaWaterBucket = (BucketItem) Items.WATER_BUCKET;
		if (!vanillaWaterBucket.emptyContents(player, level, placePos, hit)) {
			return InteractionResult.PASS;
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.getAbilities().instabuild) {
			ItemStack empty = new ItemStack(BHR_TinBucket.BHR_TIN_BUCKET);
			stack.shrink(1);
			if (!player.getInventory().add(empty)) {
				player.drop(empty, false);
			}
		}
		return InteractionResult.SUCCESS;
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_WATER_TIN_BUCKET = ModRegister.registerItem("bhr_water_tin_bucket",
				properties -> new BHR_WaterTinBucket(properties.stacksTo(MAX_STACK_SIZE)));
	}
}
