package com.rcdmake.whimsy.client.compat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 与 <b>AppleSkin</b> 的软兼容：把它在 HUD 上的所有叠加层全部关掉。
 *
 * <p>原因：本模组已经把饥饿条换成自己的 barR，AppleSkin 那些「叠在原版 10 个鸡腿图标上」的
 * 东西（饱和度金边、消耗度暗色网格、手持食物的预估、血量预估）在新的坐标系里既不贴合也不好对齐。
 * 消耗度之后由本模组自己画 {@code bhr_barr_super_*} 系列实现。
 *
 * <h2>做法</h2>
 * AppleSkin 的 HUD 叠加层全部走它自己的公开可取消事件
 * {@code squeek.appleskin.api.event.HUDOverlayEvent.*.EVENT}：
 * 监听器把 {@code event.isCanceled} 置真，它就不画对应那一层。四层都取消即可：
 * <ul>
 *   <li>{@code Saturation} —— 饱和度金边</li>
 *   <li>{@code Exhaustion} —— 消耗度暗色网格</li>
 *   <li>{@code HungerRestored} —— 手持食物的饥饿预估</li>
 *   <li>{@code HealthRestored} —— 手持食物的血量预估</li>
 * </ul>
 *
 * <h2>为什么用反射而不是直接引用</h2>
 * AppleSkin 是可选依赖（测试环境装了、别人可能没装）。直接 {@code import squeek.appleskin...}
 * 会在没有它的环境里让本类加载失败；把整个 jar 引成 compileOnly 又要把二进制放进仓库。
 * 反射 + 动态代理既零依赖、又能在 AppleSkin 版本变动时安静地失败（只打一行 warn）。
 */
public final class BHR_AppleSkinCompat {

	/** 需要整个关掉的 HUD 叠加层事件（内部类名）。 */
	private static final String[] HUD_OVERLAY_EVENTS = {
			"squeek.appleskin.api.event.HUDOverlayEvent$Saturation",
			"squeek.appleskin.api.event.HUDOverlayEvent$Exhaustion",
			"squeek.appleskin.api.event.HUDOverlayEvent$HungerRestored",
			"squeek.appleskin.api.event.HUDOverlayEvent$HealthRestored",
	};

	private static final String EVENT_HANDLER = "squeek.appleskin.api.handler.EventHandler";

	private BHR_AppleSkinCompat() {
	}

	/** 注册「取消全部 HUD 叠加层」的监听器；AppleSkin 不在或 API 变了就安静跳过。 */
	public static void register() {
		Class<?> handlerClass;
		try {
			handlerClass = Class.forName(EVENT_HANDLER);
		} catch (Throwable t) {
			warn(t);
			return;
		}

		for (String eventClassName : HUD_OVERLAY_EVENTS) {
			try {
				Class<?> eventClass = Class.forName(eventClassName);
				Object event = eventClass.getField("EVENT").get(null);
				Object handler = Proxy.newProxyInstance(
						BHR_AppleSkinCompat.class.getClassLoader(),
						new Class<?>[]{handlerClass},
						new CancelHandler());

				// ⚠️ 必须走 Fabric 的 Event **接口** 来注册：
				// 直接反射实现类（ArrayBackedEvent）的 register 会抛
				// IllegalAccessException —— 它在 fabric-api-base 的模块里、没对我们开放。
				@SuppressWarnings("rawtypes")
				net.fabricmc.fabric.api.event.Event eventBus = (net.fabricmc.fabric.api.event.Event) event;
				registerUnchecked(eventBus, handler);
			} catch (Throwable t) {
				warn(t);
			}
		}
	}

	/** raw 类型注册（监听器类型是 AppleSkin 的接口，编译期拿不到，只能这样绕）。 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerUnchecked(net.fabricmc.fabric.api.event.Event eventBus, Object handler) {
		eventBus.register(handler);
	}

	private static void warn(Throwable t) {
		com.rcdmake.whimsy.RichardsWhimsyMod.LOGGER.warn(
				"[richards-whimsy-mod] AppleSkin 兼容未完全启用（{}: {}）",
				t.getClass().getSimpleName(), t.getMessage());
	}

	/** 收到 {@code interact(event)} 就把 {@code isCanceled} 置真。 */
	private static final class CancelHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return switch (method.getName()) {
				case "interact" -> {
					if (args != null && args.length == 1 && args[0] != null) {
						cancel(args[0]);
					}
					yield null;
				}
				case "equals" -> args != null && args.length == 1 && args[0] == proxy;
				case "hashCode" -> System.identityHashCode(proxy);
				case "toString" -> "BHR_AppleSkinCompat(cancel AppleSkin HUD overlay)";
				default -> throw new UnsupportedOperationException(method.toString());
			};
		}

		private static void cancel(Object event) {
			// 只在愚者模式下屏蔽 AppleSkin：生存模式要用原版 HUD + AppleSkin 的原生表现
			if (!com.rcdmake.whimsy.client.BHR_ClientHudState.isFoolMode(
					net.minecraft.client.Minecraft.getInstance())) {
				return;
			}
			try {
				Field field = event.getClass().getField("isCanceled");
				field.setBoolean(event, true);
			} catch (Throwable ignored) {
				// 字段没了：AppleSkin 换代了，静默放弃
			}
		}
	}
}
