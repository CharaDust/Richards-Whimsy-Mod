package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 堆肥块（{@code bhr_compost}）—— 由旧项目 {@code BHR_Compost} 迁来（1.20.4 Yarn → 26.2 官方名）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_Compost.md}）：
 * <ul>
 *   <li>父级：耕地；属性表逐行都是「父级」，无方块状态、无事件、常规渲染</li>
 *   <li>特点：不需要水源即可在其上种植作物；不会因为踩踏而退化；其上的作物不会枯萎</li>
 *   <li>改写方法：wiki 标 {@code #mixin}「对 {@code CropBlock} 的 {@code canPlantOnTop} 动手 ——
 *       底部是堆肥块就直接允许种植」</li>
 *   <li>获取方式：无</li>
 * </ul>
 *
 * <h2>⭐ 26.2 把这条 {@code #mixin} 变成了数据驱动（本项目第一例「wiki 过时」）</h2>
 * 逐字节扫过 26.2 的 {@code CropBlock.class} 常量池，里面同时出现：
 * <pre>
 *   SUPPORTS_CROPS / GROWS_CROPS          ← 两个方块标签常量（声明在 net.minecraft.tags.BlockTags）
 *   mayPlaceOn(BlockState, BlockGetter, BlockPos)Z
 * </pre>
 * 再读原版数据包，26.2 实际用的是标签：
 * <pre>
 *   data/minecraft/tags/block/supports_crops.json      = [ minecraft:farmland ]   ← 「能否种在其上」
 *   data/minecraft/tags/block/supports_stem_crops.json = [ #minecraft:supports_crops ]  ← 茎类作物继承它
 *   data/minecraft/tags/block/grows_crops.json         = [ minecraft:farmland ]   ← 生长速度加成
 *   data/minecraft/tags/block/maintains_farmland.json  = [...]                     ← 维持耕地不退化
 * </pre>
 * <b>结论：种植那一半不需要 Mixin，把本方块加进 {@code minecraft:supports_crops} 标签即可</b>
 * （茎类作物通过 {@code supports_stem_crops} 自动一并覆盖）。这正是范式「能力阶梯」想要的落点：
 * 从第 5 级（Mixin）降到第 3 级（数据驱动制品）。
 *
 * <h2>「不退化 / 不枯萎」是白拿的</h2>
 * 耕地的「踩踏退化、缺水变泥土」逻辑写在 {@code FarmlandBlock} 类里，<b>不在 {@code Properties} 里</b>；
 * 所以「复制耕地属性 + 继承 {@code Block}」正好等于 wiki 要的效果 —— 这是层 6 家族选择的反向用法：
 * <b>不继承那个类，就是那个特性</b>。（原版类名在 26.2 由 {@code FarmBlock} 改成了 {@code FarmlandBlock}。）
 *
 * <h2>⭐⭐ 生长速度那一半：不用 Mixin 的「恒定值」方案（本类自己发随机刻）</h2>
 *
 * <h3>旧 Mixin 干了什么</h3>
 * 1.20.4 的 {@code BHR_Compost4CropMixin} 注入了两个方法：{@code canPlantOnTop}（允许种在堆肥块上，
 * 已被上面的标签取代）和 {@code getAvailableMoisture}（<b>生长速度</b>，26.2 改名为
 * {@code CropBlock.getGrowthSpeed}）。后者在「下方是堆肥块」时直接
 * {@code cir.setReturnValue(8F)} —— 把速度钉死成常数 8，不受周围 3×3 农田 / 水分 / 作物拥挤度影响。
 *
 * <h3>为什么不用 {@code grows_crops} 标签</h3>
 * 26.2 的生长速度读的是 {@code getGrowthSpeed(作物, 世界, 位置)}：它遍历作物<b>正下方那一层的 3×3</b>，
 * 把每个「在 {@code grows_crops} 标签里」的方块记 1.0（若该方块还有 {@code moisture} 属性且 &gt; 0 则记 3.0），
 * 非中心位置的贡献再除以 4，再按周围同类作物是否拥挤除以 2。于是：
 * <ul>
 *   <li>只把堆肥块加进标签 → 单块时速度 1.0 + 8×(1/4) = <b>3.0</b>（约 1/9 每随机刻）；</li>
 *   <li>连成一片或再借 {@code FarmlandBlock.MOISTURE} 属性（3.0 一档）→ 3.0 ~ <b>9.0</b>（1/9 ~ 1/3）。</li>
 * </ul>
 * 也就是说，标签路线拿到的是「<b>随周围环境变化</b>的速度」而不是 wiki/旧代码要的恒定值；
 * 想钉死成常数，26.2 <b>没有</b>任何声明式钩子（{@code getGrowthSpeed} 是 {@code protected static}，
 * 模组无法按「土壤」维度覆写它）。
 *
 * <h3>本类的做法：让堆肥块自己发随机刻</h3>
 * 关键观察：<b>随机刻的频率对作物和土壤是一样的</b>（{@code ServerLevel.tickChunk} 每个区段每 tick 抽
 * {@code randomTickSpeed} 次，抽到谁就给谁一次 {@code randomTick}）。所以「堆肥块的随机刻里以固定概率
 * 让上方作物长一级」与「作物自己的随机刻里以固定概率长一级」是<b>同一把尺子</b>：
 * <pre>
 *   旧 Mixin（作物随机刻）：random.nextInt((int)(25.0F / 8.0F) + 1) == 0  →  1 / 4
 *   本类（堆肥随机刻）    ：random.nextInt(GROWTH_CHANCE_DENOMINATOR) == 0 →  1 / 4（本类取 4）
 * </pre>
 * 而且它<b>与周围 3×3 完全无关</b>（不看水、不看连片、不看拥挤度）—— 这就是「恒定值」，
 * 落在能力阶梯第 2 级「覆写本类方法」（层 8 的 {@code randomTick}），<b>没有动 Mixin</b>。
 *
 * <h3>与旧行为的差异（必须知道的残余）</h3>
 * 作物自己的随机刻仍然会跑一次原版判定（速度 1.0 → 1/26 ≈ 0.038/次）。所以总速率 ≈ 0.25 + 0.038 =
 * 0.288，比旧 Mixin 的 0.25 <b>快约 15%</b>。这个残差无法在不 Mixin 的前提下抹掉（它属于作物本体），
 * 只能调 {@link #GROWTH_CHANCE_DENOMINATOR} 抵消：
 * <table border="1">
 *   <caption>分母取值对照（总速率 = 1/N + 1/26）</caption>
 *   <tr><th>N</th><th>总速率</th><th>相对旧 8F（0.25）</th></tr>
 *   <tr><td>4（本类默认）</td><td>0.288</td><td>快 15%</td></tr>
 *   <tr><td>5</td><td>0.238</td><td>慢 5%</td></tr>
 *   <tr><td>6</td><td>0.205</td><td>慢 18%</td></tr>
 * </table>
 * 覆盖范围：{@link CropBlock}（小麦 / 胡萝卜 / 马铃薯 / 甜菜根，以及绝大多数继承它的模组作物）与
 * {@link StemBlock}（南瓜 / 西瓜茎）。其它带 {@code age} 的方块（例如投掷者作物、洞穴藤蔓）保持原版基础速率
 * —— 要连它们一起提速，把本方块加进 {@code minecraft:grows_crops} 即可（代价是速度回到「随环境变化」那一档）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_compost}　<b>模块</b> BHR　<b>父级</b> 耕地</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（{@link #GROWTH_CHANCE_DENOMINATOR}）、
 *       层 8 生命周期（{@code randomTick}）—— 行为的一半靠数据驱动的方块标签，不是覆写点</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（单状态）、层 6 家族（刻意<b>不</b>继承
 *       {@code FarmlandBlock}，理由见上）、层 7 codec、层 9 / 10（一个覆写点都没有）、层 11 预留区、
 *       层 C 客户端、<b>层 M Mixin（wiki 的 {@code #mixin} 在 26.2 两条都已过时 / 有更高级的写法）</b></li>
 *   <li><b>能力阶梯</b>：种植 = 第 3 级「数据驱动制品（方块标签）」；生长速度 = 第 2 级「覆写本类方法
 *       （{@code randomTick}）」—— 都比第 5 级（Mixin）高，所以没有理由再上 Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☑ 物品模型定义由 {@code ItemModelGenerator} 自动生成　④☑ 中英 {@code BHR_LangEntries}
 *       　⑤☑ {@code BHR_CreativeTabs}（本方块暂时兼作该页图标，见 {@code BHR_General}）
 *       　⑥☑ 贴图 / 模型　<b>⑦☑ 标签 {@code minecraft:supports_crops}</b>
 *       　⑧☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 *
 * <h2>标签怎么落地的（以及以后怎么改）</h2>
 * 现在是<b>手写数据包 JSON</b>（标签是跨数据包合并的，模组加自己的条目是标准做法）：
 * {@code src/main/resources/data/minecraft/tags/block/supports_crops.json} =
 * <pre>{@code {"replace": false, "values": ["richards-whimsy-mod:bhr_compost"]}}</pre>
 * <p>TODO（「获取方式」阶段）：把上面那个手写 JSON 改成走 datagen 的
 * {@code FabricTagsProvider.BlockTagsProvider}（26.2 的类名，旧的 {@code FabricTagProvider} 已改名）。
 */
public class BHR_Compost extends Block {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 一次堆肥块随机刻里让上方作物长一级的概率分母（即 1 / N）。
	 * <p>取 4 = 旧 Mixin 的 {@code 8F}：原版判定是 {@code random.nextInt((int)(25.0F / speed) + 1) == 0}，
	 * {@code (int)(25 / 8) + 1 = 4}。取值对照表与残余误差见类注释。
	 */
	private static final int GROWTH_CHANCE_DENOMINATOR = 4;

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_COMPOST;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/**
	 * 堆肥块的属性（层 4）：wiki「构造」表逐行都是「父级」，所以完整复制耕地。
	 * <p><b>注意类继承的是 {@code Block} 而不是 {@code FarmlandBlock}</b> ——
	 * 那才是「不会因为踩踏而退化」的来源（理由见类注释）。
	 * <p>{@code randomTicks()} 是「恒定生长速度」的开关：{@code randomTick} 之前要先过
	 * {@code isRandomlyTicking()} 这一问，而它读的就是属性里的这个布尔。
	 * <b>其实这行是冗余的、但故意留着</b>：{@code Blocks.FARMLAND} 本身就带 {@code randomTicks()}
	 * （26.2 的 {@code Blocks.FARMLAND} 定义里有这一条），{@code ofFullCopy} 会把它一起复制过来
	 * —— 但「本方块靠自己的随机刻生长」是行为级依赖，写出来才不会被后来的人或未来版本悄悄改掉。
	 */
	public static BlockBehaviour.Properties compostProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)
				// 让本方块参与随机刻：作物生长的恒定速率由它驱动（见类注释）
				.randomTicks();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #compostProperties()} —— 它是本构造器实参的来源。
	 */
	public BHR_Compost(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/**
	 * 随机刻（层 8）：让紧贴上方的作物以<b>与周围环境无关的固定概率</b>长一级。
	 *
	 * <p>三道门槛都照抄原版 {@code CropBlock.randomTick} / {@code StemBlock.randomTick} 的语义：
	 * <ol>
	 *   <li>光照 {@code getRawBrightness(pos, 0) >= 9} —— 不足就不长（与作物自己的判定一致）；</li>
	 *   <li>上方必须是 {@link CropBlock}（小麦等）或 {@link StemBlock}（南瓜 / 西瓜茎），否则不动；</li>
	 *   <li>已经长到最大年龄就不动。</li>
	 * </ol>
	 * 只要过了这些门槛，就用 {@link #GROWTH_CHANCE_DENOMINATOR} 这个<b>常数</b>掷骰子 ——
	 * 不看水、不看连片、不看拥挤度，这就是「恒定值」。
	 * <p>生长用 {@code setBlock(..., Block.UPDATE_CLIENTS)}（即原版的 flag 2）：与作物自己生长时
	 * 用的 flag 一模一样，不额外触发邻居更新。
	 */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockPos cropPos = pos.above();

		// ① 光照：与原版作物同一道门槛
		if (level.getRawBrightness(cropPos, 0) < 9) {
			return;
		}

		BlockState cropState = level.getBlockState(cropPos);
		BlockState grown;

		// ② 只认两类作物；③ 到顶就不动
		if (cropState.getBlock() instanceof CropBlock crop) {
			if (crop.isMaxAge(cropState)) {
				return;
			}
			// 用作物自己的 getStateForAge，行为与原版作物生长完全一致（含甜菜根的 AGE_3）
			grown = crop.getStateForAge(crop.getAge(cropState) + 1);
		} else if (cropState.getBlock() instanceof StemBlock) {
			int age = cropState.getValue(StemBlock.AGE);
			if (age >= StemBlock.MAX_AGE) {
				return;
			}
			// 茎方块的原版写法是直接在原状态上改 AGE（保留其它属性）
			grown = cropState.setValue(StemBlock.AGE, age + 1);
		} else {
			return;
		}

		// 恒定概率：1 / GROWTH_CHANCE_DENOMINATOR
		if (random.nextInt(GROWTH_CHANCE_DENOMINATOR) != 0) {
			return;
		}

		level.setBlock(cropPos, grown, Block.UPDATE_CLIENTS);
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。作物支撑靠标签、生长靠上面的随机刻（见类注释），都不在这里登记。 */
	public static void OnInit() {
		BHR_COMPOST = ModRegister.registerBlockWithItem("bhr_compost",
				new BHR_Compost(compostProperties()
						.setId(ModRegister.blockKey("bhr_compost"))));
	}
}
