package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/**
 * 奇思妙想（WI）状态效果用到的<b>声音</b>。
 *
 * <h2>两个音效</h2>
 * <table border="1">
 *   <caption>注册名与触发时机</caption>
 *   <tr><th>常量</th><th>注册名（省略命名空间 {@code richards-whimsy-mod:}）</th><th>何时播放</th></tr>
 *   <tr><td>{@link #WI_RADIOACTIVE_APPLIED}</td><td>{@code wi_radioactive_applied}</td>
 *       <td>身上<b>被挂上</b>电离辐射时</td></tr>
 *   <tr><td>{@link #WI_BIO_TOXICITY_APPLIED}</td><td>{@code wi_bio_toxicity_applied}</td>
 *       <td>身上<b>被挂上</b>生物毒性时</td></tr>
 * </table>
 *
 * <h2>怎么发出的：用原版 {@code withSoundOnAdded}</h2>
 * <p>26.2 的 {@code MobEffect#withSoundOnAdded(SoundEvent)} 是原版自带的钩子
 * （原版「不祥之兆」「袭击之兆」就是这么加音效的）：效果被<b>添加</b>时，
 * 在实体所在位置以实体自己的音源播放一次，音量 1.0、音高 1.0
 * （逐行读过 {@code MobEffect#onEffectAdded}）。
 * 因此不需要自己挂事件、也不需要 mixin：
 * <pre>{@code
 * new WI_RadioactiveEffect().withSoundOnAdded(WI_Sounds.WI_RADIOACTIVE_APPLIED)
 * }</pre>
 * 注意它只在「添加」时响一次；重复施加同效果（刷新时长/提高等级）走的是
 * {@code onEffectUpdated} 分支，<b>不会</b>再响——这正好符合需求「获得时播放一个声音」。
 *
 * <h2>⚠️ 你要放的文件（资源侧，Java 这边管不了）</h2>
 * <p>Java 只负责把「声音事件」注册进注册表；真正的声音文件必须由资源包提供，
 * 且路径与文件名<b>必须</b>对上，否则日志会报找不到声音、游戏里静音。
 * 请在工程里放这两个文件（<b>没有</b>就新建目录）：
 * <pre>{@code
 * src/main/resources/assets/richards-whimsy-mod/sounds.json
 * src/main/resources/assets/richards-whimsy-mod/sounds/wi_radioactive_applied.ogg
 * src/main/resources/assets/richards-whimsy-mod/sounds/wi_bio_toxicity_applied.ogg
 * }</pre>
 * 也就是：<b>目录</b> {@code assets/richards-whimsy-mod/sounds/}，<b>文件名</b>就用上面那两个
 * （{@code .ogg} 扩展名，全小写下划线）。{@code sounds.json} 的写法见本类注释末尾。
 *
 * <h2>⚠️ ogg 是否需要转换</h2>
 * <p>需要满足三条，否则要么不响、要么没声场：
 * <ol>
 *   <li><b>编码</b>：必须是 Ogg <b>Vorbis</b>（Minecraft 只认 Vorbis，不认 Ogg Opus/FLAC）。
 *       若你手上是 {@code .ogg} 但仍不响，先用工具确认容器内是 Vorbis 流。</li>
 *   <li><b>声道：建议转成单声道（mono）</b>。这是最常被忽略的一条：Minecraft 的位置音效
 *       按「单声道 → 按相对方位分配到左右声道」处理；<b>立体声文件会被当成 2D 音效直接播放</b>，
 *       于是在世界里听不出方位、也不会随距离衰减得自然。既然这是「在身上响一下」的效果音，
 *       建议转单声道。（若刻意要一个「贴在耳边」的 2D 音，可留立体声。）</li>
 *   <li><b>采样率</b>：44100 Hz 即可，无需特殊处理。</li>
 * </ol>
 * 转换命令（装了 ffmpeg 的话）：
 * <pre>{@code
 * ffmpeg -i 原始文件.ogg -ac 1 -c:a libvorbis -q:a 5 wi_radioactive_applied.ogg
 * }</pre>
 * 若你告诉我这两个文件的当前格式（声道数/编码），我可以帮你确认要不要转。
 *
 * <h2>{@code sounds.json} 要写成这样</h2>
 * <pre>{@code
 * {
 *   "wi_radioactive_applied": {
 *     "sounds": ["richards-whimsy-mod:wi_radioactive_applied"]
 *   },
 *   "wi_bio_toxicity_applied": {
 *     "sounds": ["richards-whimsy-mod:wi_bio_toxicity_applied"]
 *   }
 * }
 * }</pre>
 * 键名 = 注册名去掉命名空间（与上面注册的 path 一致）；
 * 值里的字符串 = 声音文件路径（省略 {@code .ogg}）。两者都对上才会响。
 *
 * <h2>登记点</h2>
 * <p>本类不单独登记：由 {@code WI_Radioactive.OnInit()} / {@code WI_BioToxicity.OnInit()}
 * 在拿到返回的声音事件后直接 {@code withSoundOnAdded} 挂到各自的效果上。
 */
public final class WI_Sounds {

	/** 被挂上「电离辐射」时播放。 */
	public static SoundEvent WI_RADIOACTIVE_APPLIED;

	/** 被挂上「生物毒性」时播放。 */
	public static SoundEvent WI_BIO_TOXICITY_APPLIED;

	private WI_Sounds() {
	}

	/**
	 * 注册两个声音事件。由两个效果类的 {@code OnInit()} 在任何一处调用一次即可
	 * （两个效果都需要，谁先调都行；重复调用只会把同一个 id 注册两次，原版注册表会报重复，
	 * 所以这里用一个标志位挡住重复注册）。
	 */
	public static void OnInit() {
		if (WI_RADIOACTIVE_APPLIED != null) {
			return;
		}
		WI_RADIOACTIVE_APPLIED = register("wi_radioactive_applied");
		WI_BIO_TOXICITY_APPLIED = register("wi_bio_toxicity_applied");
	}

	/**
	 * 注册一个声音事件。
	 *
	 * <p>用 {@link SoundEvent#createVariableRangeEvent}：声音的传播距离交给
	 * {@code sounds.json} 里的 {@code attenuation_distance}（不写则用默认值），
	 * 这是给普通音效用的标准做法（固定距离版本用于唱片机那类要精确衰减的场合）。
	 */
	private static SoundEvent register(String path) {
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				ModRegister.id(path),
				SoundEvent.createVariableRangeEvent(ModRegister.id(path)));
	}
}
