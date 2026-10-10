package com.rcdmake.whimsy.mixin.ClassDEV;

import com.rcdmake.whimsy.access.DEV_HardcoreModeAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 给 {@code PrimaryLevelData} 挂上 hardcore 的写入口（原版只有读、没有写，
 * 理由见 {@link DEV_HardcoreModeAccess} 的注释）。
 *
 * <p>本 Mixin 只做一件事：读私有字段 {@code settings}、拼一张新的 {@link LevelSettings}、写回去。
 * {@code LevelSettings} 是 record，构造器公开；改 hardcore 只需要换掉
 * {@code difficultySettings} 那一项，其余字段（关卡名、游戏模式、作弊开关、数据包配置）原样复制 ——
 * 对应 wiki 的「只切换这一个属性」。
 *
 * <p>为什么用 {@code @Shadow} 读写字段、而不是拆两个 {@code @Accessor}：这里需要「读—改—写」连着做，
 * 写成一个方法更不容易用错（accessor 拆开后，调用方还得自己保证先读后写）。
 * 目标字段不是 final（原版自己也一直在 {@code this.settings = ...} 里换新 record），所以可写。
 */
@Mixin(PrimaryLevelData.class)
public abstract class DEV_HardcoreModeMixin implements DEV_HardcoreModeAccess {

	@Shadow
	private LevelSettings settings;

	@Override
	public void setHardcoreMode(boolean hardcore) {
		LevelSettings.DifficultySettings difficultySettings = this.settings.difficultySettings();

		this.settings = new LevelSettings(
				this.settings.levelName(),
				this.settings.gameType(),
				new LevelSettings.DifficultySettings(difficultySettings.difficulty(), hardcore, difficultySettings.locked()),
				this.settings.allowCommands(),
				this.settings.dataConfiguration());
	}
}
