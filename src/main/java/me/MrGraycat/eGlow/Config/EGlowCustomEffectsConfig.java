package me.mrgraycat.eglow.config;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EGlowCustomEffectsConfig {

	private static YamlConfiguration config;
	private static File configFile;

	public static void initialize() {
		File oldConfigFile = new File(EGlow.getInstance().getDataFolder(), "CustomEffects.yml");
		configFile = new File(EGlow.getInstance().getDataFolder(), "customeffects.yml");

		try {
			if (oldConfigFile.exists())
				oldConfigFile.renameTo(configFile);

			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4customeffects.yml not found&f! &eCreating&f...", false);
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("customeffects.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading customeffects config&f.", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);
		} catch (Exception exception) {
			ChatUtil.reportError(exception);
		}
	}

	public static boolean reloadConfig() {
		YamlConfiguration configBackup = config;
		File configFileBackup = configFile;

		try {
			config = null;
			configFile = null;

			configFile = new File(EGlow.getInstance().getDataFolder(), "customeffects.yml");
			config = new YamlConfiguration();
			config.load(configFile);
			return true;
		} catch (Exception exception) {
			config = configBackup;
			configFile = configFileBackup;

			ChatUtil.reportError(exception);
			return false;
		}
	}

	public enum Effect {
		GET_ALL_EFFECTS("Effects"),
		GET_DISPLAYNAME("Effects.%effect%.Displayname"),
		GET_DELAY("Effects.%effect%.Delay"),
		GET_COLORS("Effects.%effect%.Colors"),
		GET_MATERIAL("Effects.%effect%.GUI.Material"),
		GET_META("Effects.%effect%.GUI.Meta"),
		GET_MODEL_ID("Effects.%effect%.GUI.Model"),
		GET_NAME("Effects.%effect%.GUI.Name"),
		GET_LORES("Effects.%effect%.GUI.Lores");

		private final Effect effect;
		private final String configPath;

		Effect(String configPath) {
			this.effect = this;
			this.configPath = configPath;
		}

		public String getConfigPath() {
			return configPath;
		}

		public Set<String> get() {
			try {
				return Objects.requireNonNull(config.getConfigurationSection(effect.getConfigPath()), effect.getConfigPath() + " isn't a valid path").getKeys(false);
			} catch (NullPointerException ignored) {
				return Collections.emptySet();
			}
		}

		public int getInt(String value) {
			if (effect == GET_MODEL_ID && !config.contains(effect.getConfigPath().replace("%effect%", value)))
				return -1;
			return config.getInt(effect.getConfigPath().replace("%effect%", value));
		}

		public double getDouble(String value) {
			return config.getDouble(effect.getConfigPath().replace("%effect%", value), 1);
		}

		public String getString(String value) {
			return config.getString(effect.getConfigPath().replace("%effect%", value));
		}

		public List<String> getList(String value) {
			return config.getStringList(effect.getConfigPath().replace("%effect%", value));
		}
	}
}