package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 果木木板（{@code bhr_fruit_planks}）—— 木料家族的<b>唯一</b>木板方块。
 *
 * <p><b>改名记录（2026-11）</b>：本块最初按「松木木板」命名（{@code bhr_pine_planks}），
 * 核对原游戏 The Blockheads 后确认<b>所有木料共用同一种木板</b>（不像原版那样每种木头各有一块），
 * 因此改名为「果木木板」：它不隶属于某一种树，而是全部树木的共同产物。
 * 松木原木 / 松木树叶（{@link BHR_TreeLogPine} / {@link BHR_TreeLeavesPine}）仍是各自的方块。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_TreePlanksFruit.md}）：
 * <ul>
 *   <li>父级：<b>橡木木板</b>；属性表逐行都是「父级」（硬度 2 / 爆炸抗性 3 / 木质音效 / 斧类乐器），
 *       常规渲染，无事件，改写方法：无</li>
 *   <li>方块状态：<b>无</b>（单状态）→ blockstate 交给 datagen 生成，不手写</li>
 *   <li>标签：<b>照搬橡木木板</b>——{@code #minecraft:planks}（方块 + 物品）
 *       + {@code #minecraft:mineable/axe}（后者由 {@code #minecraft:planks} 传递命中，
 *       所以只落地两个 planks 标签，见下）</li>
 *   <li>获取方式：合适工具是斧，破坏后掉落自身（战利品表按全项目既定决定暂缓）</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)}</td><td>「父级 = 橡木木板」</td></tr>
 *   <tr><td>{@code Registry.register(Registries.BLOCK, new Identifier(...), block)} + 手写 {@code BlockItem}</td>
 *       <td>{@code ModRegister.registerBlockWithItem("bhr_fruit_planks", block)}</td>
 *       <td>项目自建的注册入口，方块与方块物品一次完成（26.2 还要求 {@code Properties.setId(key)}）</td></tr>
 *   <tr><td>手写 blockstate JSON</td><td><b>datagen 生成</b></td>
 *       <td>单状态方块由 {@code BlockStateGenerator} 产出（范式登记点②）</td></tr>
 * </table>
 *
 * <h2>标签为什么只有两个文件</h2>
 * 26.2 原版 {@code oak_planks} 直接出现在<b>两个</b>标签里：
 * <pre>{@code
 * data/minecraft/tags/block/planks.json   → #minecraft:planks（方块）
 * data/minecraft/tags/item/planks.json    → #minecraft:planks（物品，配方里 #minecraft:planks 认的是它）
 * }</pre>
 * 挖掘工具<b>不需要</b>单独写：原版 {@code data/minecraft/tags/block/mineable/axe.json} 里
 * 写的是 {@code "#minecraft:planks"} 这一条<b>标签引用</b>（逐字核验 26.2 的 minecraft-common.jar），
 * 所以进了 {@code #minecraft:planks} 就等于进了斧可挖掘。
 * <p>本模组此前 {@code data/minecraft/tags/blocks/...}（<b>复数</b>）的几份老文件是 1.20.4 时代的路径，
 * 1.21 起目录已单数化（{@code tags/blocks → tags/block}），那些文件在 26.2 里<b>不生效</b>；
 * 本次新增的两个标签一律写在单数路径下，且用 {@code "replace": false} <b>追加</b>，不覆盖原版内容。
 *
 * <h2>为什么还要单独登记可燃性</h2>
 * {@code ofFullCopy} 只复制<b>方块属性</b>；「能不能被火点着」在原版里不在 {@code Properties} 上，
 * 而在火焰蔓延那一侧。Fabric 的入口是 {@link FlammableBlockRegistry}，
 * 值取原版木板的 (5, 20)（引燃几率 / 烧毁几率），这样它与原版木板同档可燃。
 * <p>⚠️ 本行是「照搬父级行为」的一部分；若将来 wiki 明写果木木板不可燃，删掉层 12 的那一行即可。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_fruit_planks}　<b>模块</b> BHR　<b>父级</b> 橡木木板
 *       （<b>层 6 家族</b>：直接继承 {@code Block} —— 木板是普通整块方块，没有家族类可继承）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（可燃性两个值）、层 12 额外注册表（{@code FlammableBlockRegistry}）</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（<b>单状态</b>）、层 7 codec（继承 {@code Block} 已实现）、
 *       层 8 生命周期（木板没有任何随时间的状态变化）、层 9 / 10（原版木板就是整块、无交互）、
 *       层 11 预留区、层 C 客户端（不上色、常规渲染）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0/1 级「继承 + 声明」（复制父级属性，一行覆写都没有）；
 *       唯一往外走的一步是层 12 的可燃性登记（阶梯第 3 级：登记其他注册表），
 *       因为原版没有「按方块设可燃性」的 {@code Properties} 开关</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外（原版木板这类整块方块的物品形式就是方块模型）
 *       　④☑ 中英 {@code BHR_LangEntries}（方块 + 方块物品两条）
 *       　⑤☑ {@code BHR_CreativeTabs}（「树木」组，排在松木原木之后）
 *       　⑥☑ 贴图 {@code bhr/fruit_planks.png}；模型 {@code models/block/bhr_fruit_planks.json}（{@code cube_all}）
 *       　⑦◐ 标签<b>已落地</b>（{@code #minecraft:planks} 方块 + 物品；斧可挖掘经它传递命中）；
 *       　　配方 / 战利品表按「获取方式」阶段暂缓</li>
 * </ul>
 */
public class BHR_TreePlanksFruit extends Block {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 引燃几率（Fabric {@code FlammableBlockRegistry} 的 {@code igniteOdds}）。
	 * <p>取 <b>5</b>：与<b>原版木板</b>同档（原版木板 (5, 20)、原版原木 (5, 5)、树苗 (60, 100)）。
	 */
	private static final int IGNITE_ODDS = 5;

	/** 烧毁几率（{@code burnOdds}）：取 <b>20</b>，同样与原型木板的原版值一致。 */
	private static final int BURN_ODDS = 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_FRUIT_PLANKS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 果木木板的属性（层 4）：wiki「构造」表逐行都是「父级」→ 完整复制橡木木板。 */
	public static BlockBehaviour.Properties fruitPlanksProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>单状态方块没有 {@code registerDefaultState} 可言，也没有 {@code createBlockStateDefinition} 要写。
	 */
	public BHR_TreePlanksFruit(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品，并登记可燃性（层 12）。
	 *
	 * <p>⚠️ 本类没有方块状态，所以 blockstate JSON 由 datagen 出（已在
	 * {@code BlockStateGenerator.collectAll()} 声明），不在这里写任何资源。
	 */
	public static void OnInit() {
		BHR_FRUIT_PLANKS = ModRegister.registerBlockWithItem("bhr_fruit_planks",
				new BHR_TreePlanksFruit(fruitPlanksProperties()
						.setId(ModRegister.blockKey("bhr_fruit_planks"))));

		// 引燃几率 5 / 烧毁几率 20（与原版木板同档，见类注释「为什么还要单独登记可燃性」）
		FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_PLANKS, IGNITE_ODDS, BURN_ODDS);
	}
}
