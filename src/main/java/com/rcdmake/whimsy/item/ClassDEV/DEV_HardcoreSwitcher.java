package com.rcdmake.whimsy.item.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.access.DEV_HardcoreModeAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;

/**
 * 极限模式切换器（{@code dev_hardcore_switcher}）—— DEV 的测试工具：
 * 手持右键，把当前存档的 <b>hardcore（极限模式）</b>取反，方便反复验依赖它的行为
 * （眼下是 {@code bhr_pine_leaves} 的消失流程；将来是 BHR 专家模式）。
 *
 * <p>wiki 规格：{@code doc/wiki/Items/ClassDEV/DEV_HardcoreSwitcher.md}（本物品是临时起意，
 * 页面由实现同批补写）：
 * <ul>
 *   <li>普通物品（无特殊构造器，功能由「组件」定义；{@code max_stack_size} 用默认 64）；</li>
 *   <li><b>事件</b>：手持右键 → 切换 hardcore 的 TRUE/FALSE；</li>
 *   <li><b>只切换这一个属性</b>：不动 {@code allowCommands}（作弊开关）、不动 {@code gameType}
 *       （创造/生存）、也不动难度。</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类用到的部分）</h2>
 * <table border="1">
 *   <caption>改名 / 语义变化</caption>
 *   <tr><th>Yarn（旧）/ 旧版</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code player.sendMessage(Text, boolean)}</td>
 *       <td>{@code player.sendSystemMessage(Component)}</td>
 *       <td>26.2 里没有 {@code displayClientMessage} 了（全树搜不到）：
 *       走聊天框用 {@code sendSystemMessage(Component)}，走物品栏上方那行用
 *       {@code ServerPlayer.sendSystemMessage(Component, true)}。本类用聊天框那一种</td></tr>
 *   <tr><td>（无）</td><td>{@code server.getWorldData()}</td>
 *       <td>拿真正可写的世界数据：{@code ServerLevel.getLevelData()} 给的是只读转发的
 *       {@code DerivedLevelData}，改不动 hardcore</td></tr>
 * </table>
 *
 * <h2>为什么这里非要用 Mixin</h2>
 * hardcore 存在 {@code PrimaryLevelData} 的私有 {@code settings}（{@code LevelSettings} record）里，
 * 而原版只给了读（{@code isHardcore()}），<b>一个写入口都没有</b> —— 没有 {@code withHardcore}、
 * 没有 {@code setHardcore}、没有 {@code setLevelSettings}。所以只能补一个写入口：
 * {@link DEV_HardcoreModeAccess}（duck 接口）+ {@link com.rcdmake.whimsy.mixin.ClassDEV.DEV_HardcoreModeMixin}。
 * 这是范式能力阶梯里最后一级的正当用法：前四级（继承 / 覆写 / 数据驱动 / 事件）都拿不到这个属性。
 *
 * <h2>已知边界</h2>
 * <ul>
 *   <li><b>客户端的心形显示要重进世界才刷新</b>：原版只有登录包（{@code ClientboundLoginPacket}）
 *       携带 hardcore，之后没有同步包；<b>服务端判定是即时的</b>（{@code MinecraftServer.isHardcore()}
 *       直接转给 {@code worldData.isHardcore()}），所以本模组的消失流程立刻能看到变化。</li>
 *   <li><b>刻意不做权限校验</b>：它要能在「单人 + 未开作弊」的存档里用 —— 而这正是它不碰
 *       {@code allowCommands} 的原因。多人服上请自行斟酌（这是个 DEV 测试物品）。</li>
 *   <li><b>不动难度</b>：原版里极限模式的难度会被强制为「困难」，本工具按 wiki「只切换这一个属性」
 *       的要求不代劳；要困难自己切。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_hardcore_switcher}　<b>模块</b> DEV　<b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 9 交互（{@code useOn} + {@code use}，两条都接才「怎么右键都生效」）；
 *       写入口落在层 M（Mixin）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性、层 4 属性工厂（{@code registerItem} 代劳）、
 *       层 6 家族 / 层 7 codec、层 8 生命周期、层 10 形状与外观、层 11 预留区、层 C 客户端</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（物品区）　②☑ 手写
 *       {@code models/item/dev_hardcore_switcher.json} + datagen 自动生成
 *       {@code items/dev_hardcore_switcher.json}　③☑ 中英 {@code DEV_LangEntries}
 *       （物品名 1 条 + 切换提示 2 条）　④☑ {@code DEV_CreativeTabs}（排在「一般物品」之后）
 *       　⑤☑ 贴图 {@code textures/item/dev/dev_hardcore_switcher.png}（既有资源）
 *       　⑥☑ {@code richards-whimsy-mod.mixins.json} 登记新 Mixin
 *       　⑦☐ 配方 / 战利品表 —— DEV 测试物品，按现状不做</li>
 * </ul>
 */
public class DEV_HardcoreSwitcher extends Item {

	// ── 层 1 ｜ 规格常量 ──

	/** 切到极限模式时的提示语言键（正文写在 lang 文件里）。 */
	public static final String MESSAGE_KEY_ON = "message.richards-whimsy-mod.dev_hardcore_switcher.on";

	/** 切回普通模式时的提示语言键。 */
	public static final String MESSAGE_KEY_OFF = "message.richards-whimsy-mod.dev_hardcore_switcher.off";

	// ── 层 2 ｜ 物品声明 ──

	/** 极限模式切换器本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Item DEV_HARDCORE_SWITCHER;

	// ── 层 5 ｜ 构造器 ──

	/** 构造器（层 5）：只做装配；属性由 {@link ModRegister#registerItem} 统一构造。 */
	public DEV_HardcoreSwitcher(Item.Properties properties) {
		super(properties);
	}

	// ── 层 9 ｜ 交互 ──

	/**
	 * 对着方块右键（层 9）：切换后再返回 {@code SUCCESS}，于是这次右键不会被方块交互接走
	 * （否则对着箱子之类右键会打开容器、切换不生效）。
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return toggle(context.getLevel(), context.getPlayer());
	}

	/** 对着空气右键（层 9）：{@code useOn} 返回 PASS 时才会走到这里，两条一起接才「怎么右键都生效」。 */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		return toggle(level, player);
	}

	/**
	 * 切换 hardcore（层 9 的实现，两条交互入口共用）。
	 * <p>客户端只返回 {@code SUCCESS}（挥手动画），真正的写入发生在服务端；
	 * 写入走 {@link DEV_HardcoreModeAccess}，且只换 hardcore 一项，其余设置原样复制。
	 * <p>找不到写入口时返回 {@code FAIL}：那说明 Mixin 没生效（被别的模组挤掉或配置没加载），
	 * 这时宁可什么都不做，也不要假装切换成功。
	 */
	private static InteractionResult toggle(Level level, @Nullable Player player) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		MinecraftServer server = level.getServer();
		if (server == null) {
			return InteractionResult.PASS;
		}

		WorldData worldData = server.getWorldData();
		if (!(worldData instanceof DEV_HardcoreModeAccess access)) {
			// 不静默失败：这是排查「右键没反应」的第一现场（Mixin 没生效 / 被别的模组挤掉）
			RichardsWhimsyMod.LOGGER.warn("[{}] 极限模式切换器：{} 上没有 DEV_HardcoreModeAccess（Mixin 未生效？）",
					RichardsWhimsyMod.MOD_ID, worldData.getClass().getName());
			return InteractionResult.FAIL;
		}

		boolean hardcore = !worldData.isHardcore();
		access.setHardcoreMode(hardcore);

		if (player != null) {
			player.sendSystemMessage(Component.translatable(hardcore ? MESSAGE_KEY_ON : MESSAGE_KEY_OFF));
		}

		return InteractionResult.SUCCESS;
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_HARDCORE_SWITCHER = ModRegister.registerItem("dev_hardcore_switcher", DEV_HardcoreSwitcher::new);
	}
}
