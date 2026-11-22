package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * 余烬（{@code bhr_ember}）—— BHR 的「一次性点火」消耗品：手持右键点火，用完扣 1 个。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_Ember.md}）：
 * <ul>
 *   <li>特点简介：和烈焰弹类似的<b>单次点火</b>消耗品；</li>
 *   <li>与烈焰弹唯一的差别：<b>放进发射器不会发射射弹</b>，而是把余烬本身直接投出来
 *       （发射器默认行为：物品实体朝朝向弹出，带一点随机散布）；</li>
 *   <li>组件：默认组件；{@code max_stack_size} 就是 26.2 的默认值 64，代码里<b>不必</b>写
 *       （{@code stacksTo(64)} 与不写等价）；改写方法 / 获取方式：无。</li>
 * </ul>
 *
 * <h2>为什么是 {@code extends FireChargeItem}（阶梯第 0 级：继承）</h2>
 * <p>「单次点火」的全部语义原版烈焰弹都写好了，本类一行都不重写，拿到的行为是：
 * <ul>
 *   <li><b>点亮可点燃方块</b>：营火（{@code CampfireBlock.canLight}）、蜡烛（{@code CandleBlock.canLight}）、
 *       蜡烛蛋糕 —— 把 {@code lit} 置 true；</li>
 *   <li><b>否则在点击面外侧生成火</b>：目标位置通过 {@code BaseFireBlock.canBePlacedAt(...)} 后
 *       写入 {@code BaseFireBlock.getState(...)}；</li>
 *   <li><b>两处都做不到就 {@code FAIL}</b> —— 不消耗、不挥手；</li>
 *   <li><b>成功则 {@code shrink(1)}</b>：创造模式也扣 1 个（原版烈焰弹就是如此，本项目照抄不改）。</li>
 * </ul>
 * 这 4 条一起抄一遍属于第 2 级（覆写），而第 0 级就能拿全 —— 按 itemv1.md §3「能用上面一级就不许用下面一级」，
 * 所以不写 {@code useOn}。
 *
 * <h2>发射器为什么不会发射射弹（本类唯一多出来的一行）</h2>
 * <p><b>26.2 的发射器行为是「按物品查表」，不是「按接口判定」</b>（逐条反编译核过 26.2 的
 * {@code minecraft-merged-deobf-26.2.jar}）：
 * <ol>
 *   <li>{@code DispenserBlock.getDispenseMethod(level, stack)} 先查
 *       {@code DispenserBlock.DISPENSER_REGISTRY}（{@code IdentityHashMap<Item, DispenseItemBehavior>}）；</li>
 *   <li>表里没有才走 {@code getDefaultDispenseMethod(stack)} —— 它只看三件事：
 *       有 {@code DataComponents.EQUIPPABLE} → 穿装备、在 {@code minecraft:sulfur_cube_swallowable}
 *       标签里 → 硫方块吞、是刷怪蛋且有实体数据 → 生成生物；<b>三者都不是 → {@code DEFAULT_BEHAVIOR}</b>
 *       （{@code DefaultDispenseItemBehavior}：{@code split(1)} + {@code spawnItem(...)}，即把物品实体弹出去）；</li>
 *   <li>原版烈焰弹之所以发射小火球，是因为 {@code DispenseItemBehavior.bootStrap()} 里有一条
 *       {@code registerProjectileBehavior(Items.FIRE_CHARGE)} —— <b>只登记了烈焰弹这一件</b>。
 *       {@code FireChargeItem} 实现的 {@code ProjectileItem} 接口本身并不参与判定
 *       （{@code getDefaultDispenseMethod} 里没有 {@code instanceof ProjectileItem}）。</li>
 * </ol>
 * 也就是说：<b>继承 {@code FireChargeItem} 但什么都不登记，余烬在发射器里就已经是「直接投出来」了。</b>
 * 这里仍然显式登记一条 {@code DefaultDispenseItemBehavior}，理由是<b>把意图写进代码</b>：
 * 否则「发射器不发射射弹」这件事在本类里表现为一片空白，读者只能靠推断；
 * 同时它也是对未来版本的保险 —— 万一原版以后改成「凡 {@code ProjectileItem} 自动注册射弹行为」，
 * 显式登记的那条会盖在默认规则之上（查表在前），行为不变。
 *
 * <h2>它不是燃料</h2>
 * <p>早期 1.20.4 的练习版本用 {@code FuelRegistry.INSTANCE.add(BHR_EMBER, 2 * 200)} 把它注册成燃料，
 * 那是实验性质的写法。<b>本模组的余烬不是燃料</b>，26.2 版不登记 {@code cooking_fuel}、
 * 也不碰 Fabric 的 {@code FuelValueEvents} —— wiki 页里「可为熔炉提供热量」一栏已删除。
 *
 * <h2>Yarn（1.20.4）→ 26.2 官方名对照（本类用到的部分）</h2>
 * <table border="1">
 *   <tr><th>旧写法</th><th>26.2</th></tr>
 *   <tr><td>{@code new Item(new FabricItemSettings())}</td><td>{@code ModRegister.registerItem(name, factory)}（内部 {@code new Item.Properties().setId(key)}）</td></tr>
 *   <tr><td>{@code Registry.register(Registries.ITEM, new Identifier(ns, path), item)}</td><td>同上（注册入口代劳；id 由 {@code Identifier.fromNamespaceAndPath} 造）</td></tr>
 *   <tr><td>{@code FuelRegistry.INSTANCE.add(item, ticks)}</td><td>不使用（余烬不是燃料）</td></tr>
 *   <tr><td>发射器：{@code DispenserBlock.registerBehavior}</td><td>同名同签名（{@code ItemLike, DispenseItemBehavior}），26.2 仍在</td></tr>
 *   <tr><td>{@code appendTooltip(...)}</td><td>{@code appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}（本类不用）</td></tr>
 * </table>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 余烬　<b>英文名</b> Ember　<b>注册名</b> {@code bhr_ember}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> {@link FireChargeItem}（不是普通 {@code Item}：单次点火语义直接继承，见上）</li>
 *   <li><b>启用的可选层</b>：层 2 物品声明、层 6 家族（{@code FireChargeItem}）、层 12 初始化（注册 + 发射器登记）</li>
 *   <li><b>跳过的可选层及原因</b>：
 *       <ul>
 *         <li>层 1 常量 —— 没有任何魔法数值（堆叠 64 是默认值、点火语义全在父类）；</li>
 *         <li>层 3 组件 / 层 4 装配方法 —— 物品没有额外组件，{@code Properties} 原样交给父类；
 *             一旦以后要加（例如 {@code rarity}），就在 {@link #OnInit()} 里写成
 *             {@code properties -> new BHR_Ember(emberProperties(properties))} 并补回层 4；</li>
 *         <li>层 7 codec —— 物品**没有**这一层（itemv1.md §7 第 3 条）；</li>
 *         <li>层 8 生命周期、层 9 交互（{@code useOn} 由父类提供，不覆写）、层 10 外观与提示 —— 无需求；</li>
 *         <li>层 C 客户端、层 M Mixin —— 无需求。</li>
 *       </ul></li>
 *   <li><b>停在第 0 级</b>（继承）—— 点火语义一行不写；第 12 级那次 {@code registerBehavior}
 *       不是「实现功能」而是「把默认已经成立的行为显式化」，见上一节。</li>
 *   <li><b>登记点 checklist（7 处）</b>：
 *       <ol>
 *         <li>☑ 主类 {@code RichardsWhimsyMod.onInitialize()}「物品」区加一条 {@link #OnInit()}</li>
 *         <li>☑ {@code datagen/ItemModelGenerator} —— <b>普通物品不用登记</b>（自动扫 {@code models/item/<id>.json}）</li>
 *         <li>☑ 资源：贴图 {@code textures/item/bhr/ember.png} 与手写模型
 *             {@code models/item/bhr_ember.json}（{@code item/generated} + layer0）都是既有资源</li>
 *         <li>☑ {@code datagen/ClassBlockHeadsRebon/BHR_LangEntries} 一条 {@code item.…} 键（中英各一条）</li>
 *         <li>☑ {@code item/BHR_CreativeTabs} —— 「其他物品」组（余烬不是燃料，不再单开「燃料」组）</li>
 *         <li>☑ {@code items/bhr_ember.json} 由 datagen 生成（{@code gradlew syncDatagen}）</li>
 *         <li>☐ 配方 / 战利品表 / 标签 —— 全项目暂缓，「获取方式」阶段统一做（wiki 写「无」）</li>
 *       </ol></li>
 * </ul>
 */
public class BHR_Ember extends FireChargeItem {

	// ── 层 2 ｜ 物品声明 ──

	/** 余烬本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_EMBER;

	// ── 层 5 ｜ 构造器 ──

	/**
	 * 构造器（层 5）：只做装配，不写数值。
	 * <p>点火语义、堆叠上限等全部由父类 {@link FireChargeItem} 与 {@code Item.Properties} 的默认值决定。
	 */
	public BHR_Ember(Item.Properties properties) {
		super(properties);
	}

	// ── 层 12 ｜ 初始化 ──

	/**
	 * 注册（层 12）：注册物品本体，并把发射器行为钉死成「直接投出」。
	 *
	 * <p><b>为什么发射器行为必须在这里登记、且必须在物品注册之后</b>：
	 * {@link DispenserBlock#registerBehavior} 是按 {@code Item} 实例查的
	 * （{@code DISPENSER_REGISTRY} 是 {@code IdentityHashMap}），物品还没注册时 {@code BHR_EMBER} 是 {@code null}，
	 * 传进去会直接 NPE，或者（更糟）登记到一个永远不会被拿出来的键上。
	 *
	 * <p>{@link DefaultDispenseItemBehavior} 就是原版「没有专门行为的物品」的默认处理：
	 * {@code split(1)} 之后 {@code DefaultDispenseItemBehavior.spawnItem(...)} 把 1 个余烬
	 * 作为物品实体朝发射器朝向弹出（带随机散布与「噗」的一声）—— 正是 wiki 要的
	 * 「不发射射弹，而是直接投掷出来」。
	 */
	public static void OnInit() {
		BHR_EMBER = ModRegister.registerItem("bhr_ember", BHR_Ember::new);
		DispenserBlock.registerBehavior(BHR_EMBER, new DefaultDispenseItemBehavior());
	}
}
