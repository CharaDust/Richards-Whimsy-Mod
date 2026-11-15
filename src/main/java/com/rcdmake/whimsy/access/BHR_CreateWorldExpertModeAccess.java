package com.rcdmake.whimsy.access;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_ExpertModeChoice;

/**
 * 「创建世界」界面持有<b>专家模式挡位</b>的 duck interface。
 *
 * <p>{@code CreateWorldScreen} 是原版类，没有地方放我们的字段，于是用一个
 * {@code @Unique} 字段挂在它身上（实现方：{@code BHR_FoolCreateWorldMixin}），
 * 两侧这样用：
 * <ul>
 *   <li>写：{@code GameTab} 里那个专家模式按钮的点击回调
 *       （{@code BHR_FoolGameTabMixin}）；</li>
 *   <li>读：世界创建前的那一次「执行一次 开启/关闭 专家模式」
 *       （同一个 Mixin 里的 {@code createWorldAndCleanup} 注入点）。</li>
 * </ul>
 *
 * <p>为什么不是静态全局变量：挡位必须<b>随界面生灭</b>。做成静态字段的话，
 * 玩家这次选了「开」、关掉界面、下次再开创建世界界面，按钮还在「开」上 —— 那是错的。
 *
 * <h2>⛔ 为什么这个接口在 {@code access} 包</h2>
 * 见 {@link DEV_HardcoreModeAccess} 的类注释：{@code mixin} 包被 mixins.json
 * 的 {@code package} 整体声明为 mixin 包，包内任何被当普通类加载的类型都会被拒绝，
 * 所以 duck 接口一律放 {@code com.rcdmake.whimsy.access}。
 */
public interface BHR_CreateWorldExpertModeAccess {

	/** 当前挡位（默认 {@link BHR_ExpertModeChoice#OFF}）。 */
	BHR_ExpertModeChoice whimsy$getExpertModeChoice();

	/** 设置挡位（世界创建界面自己的状态，不写任何游戏规则）。 */
	void whimsy$setExpertModeChoice(BHR_ExpertModeChoice choice);
}
