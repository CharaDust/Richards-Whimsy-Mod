package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 锡桶（{@code bhr_tin_bucket}）—— 篝火线的容器物品（<b>空</b>），Blockheads 的 Tin Bucket。
 *
 * <h2>为什么不用原版水桶</h2>
 * <p>需求原文：「Bucket of Water 不能用原版的水桶，因为这个桶设定上是锡做的，<b>不能装岩浆</b>」。
 * 所以本模组有自己的两只桶：
 * <ul>
 *   <li>{@code bhr_tin_bucket}（本类）= 空锡桶，也是「锡水桶」用完后返还的那只；</li>
 *   <li>{@link BHR_WaterTinBucket}（{@code bhr_water_tin_bucket}）= 装了水的锡水桶，
 *       只能倒水，倒完变回空锡桶 —— 与「锡桶装不了岩浆」一致（它压根不是原版的 {@code BUCKET} /
 *       {@code WATER_BUCKET}，原版的岩浆桶 / 水桶逻辑碰不到它）。</li>
 * </ul>
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>纯材料 + 容器：无组件、无右键行为；</li>
 *   <li>获取：待定（Blockheads 里由金属工作台用「锡锭 ×3」打出来，本项目锡线尚未落地）；
 *       <b>TODO（获取方式阶段）</b>：对着水源右键把空锡桶变成锡水桶（
 *       {@code BucketPickup#pickupBlock} 那条路，需要时再落地）；</li>
 *   <li>用途：篝火配方里的 {@code "container"} —— 盐与煎饼消耗锡水桶后返还<b>本物品</b>。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 锡桶　<b>英文名</b> Tin Bucket　<b>注册名</b> {@code bhr_tin_bucket}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 2 物品声明、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_Flour}（层 1 / 3 / 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求；
 *       层 9 的「舀水」留 TODO，见上）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_tin_bucket.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（材料组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/tin_bucket.png}　⑥☑ 配方 {@code "container"} 字段引用</li>
 * </ul>
 */
public class BHR_TinBucket extends Item {

	/** 空锡桶本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_TIN_BUCKET;

	public BHR_TinBucket(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_TIN_BUCKET = ModRegister.registerItem("bhr_tin_bucket", BHR_TinBucket::new);
	}
}
