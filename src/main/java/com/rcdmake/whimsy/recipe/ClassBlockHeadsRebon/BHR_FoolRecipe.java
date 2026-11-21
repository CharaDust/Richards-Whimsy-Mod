package com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 一条<b>愚者配方</b>：方块愚者的制作台能用的配方。
 *
 * <p>它把「工作台的无序合成」与「熔炉的耗时」合成一件事：九个需求格，
 * 每格是一类物品 × N 个，凑齐后按下开始，按 {@link #timeSeconds()} 走完进度条才出货。
 *
 * <h2>JSON（{@code data/richards-whimsy-mod/bhr_recipe/…/*.json}）</h2>
 * <pre>{@code
 * {
 *   "workbench": "bhr_workbench",     // 制作台类型（将来会有更多台子）
 *   "expert": false,                   // 是否专家模式配方（游戏规则 bhr_expert_recipes 切换）
 *   "level": 1,                        // 需要制作台达到的等级
 *   "time": 5,                         // 制作时间（秒）
 *   "ingredients": [ { "item": "minecraft:stick", "count": 1 } ],
 *   "result": { "item": "minecraft:torch", "count": 1 }
 * }
 * }</pre>
 * <p>升级配方把 {@code result} 换成 {@code "upgrade_to": 2}：不给物品，而是把制作台升到该等级。
 * {@code result} 与 {@code upgrade_to} <b>二选一</b>。
 *
 * <h2>为什么 time 用「秒」</h2>
 * <p>需求里所有时间都是秒（5 秒 / 60 秒 / 180 秒）；JSON 保持人类可读的秒，
 * 代码里用 {@link #timeTicks()} 换成 tick（×20）。游戏内的进度条按 tick 跑。
 *
 * <p>为什么不用原版 {@code Recipe} 接口：26.2 的客户端<b>拿不到</b>自定义配方的数据
 * （{@code RecipeAccess} 只剩石切机之类的专门接口），而界面要在客户端列出配方。
 * 本项目改成「模组自带数据文件 + 两端各自加载」，于是两端看到的是同一份列表，
 * 不需要为「配方列表」额外发包（进度/选择之类的运行期状态仍走菜单数据槽）。
 */
public record BHR_FoolRecipe(
		String workbench,
		boolean expert,
		int level,
		int timeSeconds,
		List<BHR_FoolIngredient> ingredients,
		Optional<Item> resultItem,
		int resultCount,
		Optional<Integer> upgradeTo) {

	/** 是否升级配方（不给物品，直接升制作台等级）。 */
	public boolean isUpgrade() {
		return this.upgradeTo.isPresent();
	}

	/** 制作耗时（tick）—— JSON 里写的是秒。 */
	public int timeTicks() {
		return this.timeSeconds * 20;
	}

	/** 产物预览（升级配方返回空）。 */
	public ItemStack resultStack() {
		return this.resultItem.map(item -> new ItemStack(item, this.resultCount)).orElse(ItemStack.EMPTY);
	}

	/**
	 * 从 JSON 解析一条配方（{@code id} 由文件名给出，不写在 JSON 里）。
	 *
	 * @throws IllegalArgumentException 字段缺失或自相矛盾
	 */
	public static BHR_FoolRecipe parse(JsonObject json) {
		String workbench = json.has("workbench") ? json.get("workbench").getAsString() : "bhr_workbench";
		boolean expert = json.has("expert") && json.get("expert").getAsBoolean();
		int level = json.has("level") ? json.get("level").getAsInt() : 1;
		int time = json.has("time") ? json.get("time").getAsInt() : 1;
		if (level < 1) {
			throw new IllegalArgumentException("等级必须 >= 1");
		}
		if (time < 1) {
			throw new IllegalArgumentException("制作时间必须 >= 1 秒");
		}

		if (!json.has("ingredients") || !json.get("ingredients").isJsonArray()) {
			throw new IllegalArgumentException("缺少 ingredients 数组");
		}
		List<BHR_FoolIngredient> ingredients = new ArrayList<>();
		for (var element : json.getAsJsonArray("ingredients")) {
			ingredients.add(BHR_FoolIngredient.parse(element.getAsJsonObject()));
		}
		if (ingredients.isEmpty()) {
			throw new IllegalArgumentException("ingredients 不能为空");
		}

		boolean hasResult = json.has("result");
		boolean hasUpgrade = json.has("upgrade_to");
		if (hasResult == hasUpgrade) {
			throw new IllegalArgumentException("result 与 upgrade_to 必须二选一");
		}

		Optional<Item> resultItem = Optional.empty();
		int resultCount = 1;
		if (hasResult) {
			JsonObject result = json.getAsJsonObject("result");
			String itemId = result.get("item").getAsString();
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
			if (item == null || item == Items.AIR) {
				throw new IllegalArgumentException("未知产物 id：" + itemId);
			}
			resultItem = Optional.of(item);
			resultCount = result.has("count") ? result.get("count").getAsInt() : 1;
			if (resultCount < 1) {
				throw new IllegalArgumentException("产物数量必须 >= 1");
			}
		}

		Optional<Integer> upgradeTo = hasUpgrade
				? Optional.of(json.get("upgrade_to").getAsInt())
				: Optional.empty();
		if (upgradeTo.isPresent() && upgradeTo.get() < 1) {
			throw new IllegalArgumentException("upgrade_to 必须 >= 1");
		}

		return new BHR_FoolRecipe(workbench, expert, level, time, List.copyOf(ingredients),
				resultItem, resultCount, upgradeTo);
	}

	/** 日志/自检用的可读形式。 */
	@Override
	public String toString() {
		String target = this.isUpgrade()
				? "升级到 Lv." + this.upgradeTo.get()
				: String.valueOf(BuiltInRegistries.ITEM.getKey(this.resultItem.get())) + " x" + this.resultCount;
		return "%s%s Lv.%d %ds %s → %s".formatted(
				this.expert ? "[专家] " : "", this.workbench, this.level, this.timeSeconds, this.ingredients, target);
	}
}
