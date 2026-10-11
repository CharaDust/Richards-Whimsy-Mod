package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.List;

/**
 * 方块愚者（BHR）的<b>游戏规则</b>与「专家模式」预设协调器。
 *
 * <p>本类只有一件事：把 BHR 的规则注册进游戏规则表，并让
 * {@code bhr_expert_mode} 这个开关成为一个<b>批量调节工具</b>。
 *
 * <h2>三条规则（全部布尔，默认 false）</h2>
 * <table border="1">
 *   <caption>BHR 游戏规则一览</caption>
 *   <tr><th>规则 id（省略命名空间 {@code richards-whimsy-mod:}）</th><th>默认</th><th>专家挡位</th><th>用途</th></tr>
 *   <tr><td>{@code bhr_expert_mode}</td><td>false</td><td>—</td>
 *       <td>调节器本身：只看它、从不写它</td></tr>
 *   <tr><td>{@code bhr_decaying_leaves_no_drop}</td><td>false</td><td><b>true</b></td>
 *       <td>枯萎树叶直接消失、不掉落（极限模式行为）</td></tr>
 *   <tr><td>{@code bhr_legacy_hunger_consumption}</td><td>false</td><td><b>true</b></td>
 *       <td>饥饿值消耗回到 1.8 水平（尚未实现，先占位）</td></tr>
 * </table>
 *
 * <h2>「专家模式」是调节器，不是真相源（本类的核心契约）</h2>
 * <ul>
 *   <li><b>只在手动改 {@code bhr_expert_mode} 时</b>才把细调条目写成预设挡位；</li>
 *   <li>关闭时<b>强制写 FALSE</b>（不记忆历史值，见 {@link #applyPreset}）；</li>
 *   <li><b>从存档读盘不重放</b>：协调器只挂在 {@link GameRuleEvents#changeCallback} 上，
 *       而世界加载是直接反序列化 {@code world/data/minecraft/game_rules.dat}，
 *       不经过任何设置点，所以不会触发；</li>
 *   <li><b>单独改细调条目不影响 {@code bhr_expert_mode}</b>：本类从不写它，
 *       它是「只读的输入」。</li>
 * </ul>
 * <p>一句话：等于「一键切预设」。玩家手动把某条细调条目调回普通模式后，
 * {@code bhr_expert_mode} 的状态不会因此改变 —— 这正是设计意图。
 *
 * <h2>为什么用 {@code rules.set(rule, value, null)} 而不是带 server 的重载</h2>
 * 26.2 的 {@code GameRules.set(GameRule, Object, MinecraftServer)} 字节码（逐字节读过）：
 * <pre>{@code
 * 27: rules.set(rule, value);          // 真正写值
 * 36: if (server == null) -> 46       // ★ 传 null 就【完全跳过】通知
 * 43: server.onGameRuleChanged(...);   // → GameRuleEvents 回调
 * }</pre>
 * 于是「写值但不触发回调」是原版 API 自带的能力，不需要自己维护
 * 「正在应用预设」的置位/复位守卫（那种写法容易漏掉复位分支）。
 * 又因为本类<b>只</b>监听 {@code bhr_expert_mode}，细调条目的回调本来也没人听，
 * 传 {@code null} 连「丢掉将来可能加的回调」这个副作用都没有。
 *
 * <p>顺带一条边界（同样读字节码确认）：{@code GameRules.set} 在规则不在表里时是
 * <b>{@code LOGGER.warn} + return</b>，不抛异常 —— 万一哪条规则没注册成功，只会日志告警。
 *
 * <h2>客户端同步的能力边界（记在这里，免得当 bug 查）</h2>
 * <ul>
 *   <li>规则值在玩家登录时随 {@code ClientboundGameRuleValuesPacket} 下发，
 *       但那条路有权限门槛：{@code ServerGamePacketListenerImpl.sendGameRuleValues()}
 *       先查 {@code Permissions.COMMANDS_GAMEMASTER}，<b>非 OP 客户端拿不到</b>；</li>
 *   <li>{@code MinecraftServer.onGameRuleChanged} 只对原版那几条规则做特殊同步
 *       （{@code LIMITED_CRAFTING} / {@code IMMEDIATE_RESPAWN} / {@code REDUCED_DEBUG_INFO} /
 *       {@code LOCATOR_BAR} / {@code SPAWN_MONSTERS} / {@code ADVANCE_TIME}），
 *       <b>自定义规则运行期改值不会推给已登录的客户端</b>。</li>
 * </ul>
 * <p>结论：服务端逻辑（掉落份数 / 树叶 / 饥饿 / 将来 BHR 自定义工作方块）不受影响；
 * 哪天需要客户端知道（界面提示之类），得自己发包。
 *
 * <h2>与「极限模式」（原版 hardcore）的区别</h2>
 * hardcore 是<b>原版属性</b>，存在存档设置里、有专门的读接口
 * （{@code MinecraftServer.isHardcore()}），本类不碰它。判定点里两者并列：
 * {@code isHardcore() || BHR_ExpertMode.leavesNoDrop(...)}。
 *
 * <h2>登记点</h2>
 * <ul>
 *   <li>①☑ 主类 {@code RichardsWhimsyMod.onInitialize()} 调一次 {@link #OnInit()}；
 *       规则注册自身不依赖其他注册表，顺序随意</li>
 *   <li>②☑ 中英 {@code BHR_LangEntries}：每条规则<b>两条键</b>
 *       （名称 {@code <ns>.<path>} + 说明 {@code gamerule.<ns>.<path>}）</li>
 *   <li>③☒ 无 Mixin、无资源文件、无 datagen 产物</li>
 * </ul>
 */
public final class BHR_GameRules {

	// ── 层 1 ｜ 规格常量（规则本体）──

	/**
	 * 专家模式开关（{@code bhr_expert_mode}，默认 FALSE）。
	 * <p><b>本类只读它、从不写它</b>，改它只应该由玩家（{@code /gamerule} 命令或世界创建界面）来做。
	 */
	public static final GameRule<Boolean> BHR_EXPERT_MODE = GameRuleBuilder
			.forBoolean(false)
			.category(GameRuleCategory.MISC)
			.buildAndRegister(RichardsWhimsyMod.id("bhr_expert_mode"));

	/**
	 * 枯萎树叶直接消失、不掉落（{@code bhr_decaying_leaves_no_drop}，默认 FALSE；专家挡位 TRUE）。
	 * <p>语义：{@code true} = 枯萎枝直接消失、不掉落（与极限模式一致）；
	 * {@code false} = 走原来的「1/5 几率带掉落物破坏」。
	 * <p>注意它<b>不叫</b> {@code bhr_expert_mode}：玩家可以在不开专家模式的情况下单独打开它，
	 * 也可以开了专家模式之后再把它单独关掉。
	 */
	public static final GameRule<Boolean> BHR_DECAYING_LEAVES_NO_DROP = GameRuleBuilder
			.forBoolean(false)
			.category(GameRuleCategory.MISC)
			.buildAndRegister(RichardsWhimsyMod.id("bhr_decaying_leaves_no_drop"));

	/**
	 * 传统饥饿值消耗（{@code bhr_legacy_hunger_consumption}，默认 FALSE；专家挡位 TRUE）。
	 * <p>把饥饿值消耗调回 Minecraft 1.8 的水平。机制本身尚未实现（将来落在
	 * {@code FoodData.addExhaustion} 一侧），这里先把规则注册好、挡位接上，
	 * 免得以后加规则时又要动一次预设表之外的东西。
	 */
	public static final GameRule<Boolean> BHR_LEGACY_HUNGER_CONSUMPTION = GameRuleBuilder
			.forBoolean(false)
			.category(GameRuleCategory.MISC)
			.buildAndRegister(RichardsWhimsyMod.id("bhr_legacy_hunger_consumption"));

	// ── 层 1 ｜ 规格常量（预设表）──

	/**
	 * 一条细调条目的专家挡位。
	 *
	 * @param rule   细调条目
	 * @param expert 专家模式下要写的值；关闭专家模式时写它的相反值
	 */
	private record Preset(GameRule<Boolean> rule, boolean expert) {
	}

	/**
	 * 预设表：<b>以后新增细调条目只在这里加一行</b>，协调器与文件其余部分都不用改。
	 * <p>顺序即写入顺序，无依赖关系。
	 */
	private static final List<Preset> PRESETS = List.of(
			new Preset(BHR_DECAYING_LEAVES_NO_DROP, true),
			new Preset(BHR_LEGACY_HUNGER_CONSUMPTION, true));

	// ── 层 5 ｜ 构造器 ──

	private BHR_GameRules() {
	}

	// ── 层 12 ｜ 初始化 ──

	/**
	 * 注册（层 12）：接上「专家模式开关」的监听。
	 *
	 * <p>规则常量本身在类初始化时就已经注册进 {@code BuiltInRegistries.GAME_RULE} 了，
	 * 这里只挂监听，所以主类里调用它的位置不影响别的注册。
	 *
	 * <p>⚠️ 只监听 {@link #BHR_EXPERT_MODE} 一条。<b>刻意不监听细调条目</b>，
	 * 也不监听任何「世界加载」事件 —— 于是「读盘不重放」是结构上成立的，不靠额外判断。
	 *
	 * <p>顺带在 {@code SERVER_STARTED} 打一行开局诊断（只读、不写任何值）：
	 * 它是排查「三条规则各是多少」的第一现场 —— 实测已确认它打出的是
	 * 从 {@code world/data/minecraft/game_rules.dat} 读出来的存档值，
	 * 而不是默认值，这正是「读盘不重放」契约的验证点。
	 */
	public static void OnInit() {
		// 类型说明：GameRuleEvents.changeCallback 是泛型方法 <T> Event<ValueUpdate<T>>，
		// 但它的返回类型在字节码里被擦除了，而这里又要链式 .register(lambda)，
		// 所以显式写出 Event<ValueUpdate<Boolean>> 把 T 钉死，避免推断失败。
		//
		// ⚠️ ValueUpdate 的函数式方法签名是 onGameRuleUpdated(T value, MinecraftServer server)
		// —— 【两个】参数，不带 GameRule。签名以 fabric-game-rule-api-v1 的
		//    -sources.jar（GameRuleEvents.java）为准，别按反编译的擦除签名写。
		Event<GameRuleEvents.ValueUpdate<Boolean>> expertModeChanged = GameRuleEvents.changeCallback(BHR_EXPERT_MODE);
		expertModeChanged.register((value, server) -> {
			applyPreset(server.getGameRules(), value);
			RichardsWhimsyMod.LOGGER.info("[{}] 专家模式已{}，细调条目已写入对应挡位",
					RichardsWhimsyMod.MOD_ID, value ? "开启" : "关闭");
		});

		// 开局诊断：SERVER_STARTED 时所有世界（以及从 game_rules.dat 读出来的规则值）都已就绪。
		// 纯读，不改任何状态；「读盘不重放」这条契约的验证点就在这一行。
		ServerLifecycleEvents.SERVER_STARTED.register(BHR_GameRules::logRuleState);
	}

	/** 把三条规则的当前值打进日志（诊断用，只读）。 */
	private static void logRuleState(MinecraftServer server) {
		GameRules rules = server.getGameRules();
		RichardsWhimsyMod.LOGGER.info("[{}] BHR 规则状态：{}={}，{}={}，{}={}",
				RichardsWhimsyMod.MOD_ID,
				BHR_EXPERT_MODE.id(), rules.get(BHR_EXPERT_MODE),
				BHR_DECAYING_LEAVES_NO_DROP.id(), rules.get(BHR_DECAYING_LEAVES_NO_DROP),
				BHR_LEGACY_HUNGER_CONSUMPTION.id(), rules.get(BHR_LEGACY_HUNGER_CONSUMPTION));
	}

	// ── 层 8 ｜ 协调器（预设应用）──

	/**
	 * 把预设表里的每条细调条目写成「专家挡位 / 普通挡位」。
	 *
	 * <p><b>关闭时强制写 FALSE</b>，不恢复打开前的历史值 —— 这是刻意的设计选择：
	 * 关闭后的状态必须可预测（「回到普通挡位」），而不是「回到你上次手改的样子」。
	 *
	 * <p>第三个参数传 {@code null}：写值但不触发回调（原理见类注释）。这一条同时保证了
	 * 「即使将来给细调条目也加了监听，也不会回调进本方法造成递归」。
	 *
	 * @param rules  全局唯一的那份规则对象（{@code ServerLevel.getGameRules()} 内部就是转给它）
	 * @param expert {@code true} = 写专家挡位；{@code false} = 一律写 FALSE
	 */
	private static void applyPreset(GameRules rules, boolean expert) {
		for (Preset preset : PRESETS) {
			rules.set(preset.rule(), expert && preset.expert(), null);
		}
	}
}
