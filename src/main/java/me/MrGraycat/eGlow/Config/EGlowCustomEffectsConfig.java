package me.MrGraycat.eGlow.Config;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
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
		configFile = new File(EGlow.getInstance().getDataFolder(), "CustomEffects.yml");
		
		try {
			if (!EGlow.getInstance().getDataFolder().exists()) {
				EGlow.getInstance().getDataFolder().mkdirs();
			}
			
			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4CustomEffects.yml not found&f! &eCreating&f...", false);
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("CustomEffects.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading CustomEffects config&f.", false);
			}
			
			config = new YamlConfiguration();
			config.load(configFile);
		} catch(Exception e) {
			ChatUtil.reportError(e);
		}
	}
	
	public static boolean reloadConfig() {
		YamlConfiguration configBackup = config;
		File configFileBackup = configFile;
		
		try {
			config = null;
			configFile = null;
			
			configFile = new File(EGlow.getInstance().getDataFolder(), "CustomEffects.yml");
			config = new YamlConfiguration();
			config.load(configFile);
			return true;
		} catch(Exception e) {
			config = configBackup;
			configFile = configFileBackup;
			
			ChatUtil.reportError(e);
			return false;
		}
	}
	
	public enum Effect {
		GET_ALL_EFFECTS("Effects"),
		GET_DISPLAYNAME("Effects.%effect%.Displayname"),
		GET_DELAY("Effects.%effect%.Delay"),
		GET_COLORS("Effects.%effect%.Colors"),
		GET_PAGE("Effects.%effect%.GUI.Page"),
		GET_SLOT("Effects.%effect%.GUI.Slot"),
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
			} catch (NullPointerException e) {
				return Collections.emptySet();
			}
		}
		
		public int getInt(String value) {
			if (effect == GET_MODEL_ID && !config.contains(effect.getConfigPath().replace("%effect%", value)))
				return -1;
			return config.getInt(effect.getConfigPath().replace("%effect%", value));
		}
		
		public double getDouble(String value) {
			if (!config.contains(effect.getConfigPath().replace("%effect%", value)))
				return 1;
			return config.getDouble(effect.getConfigPath().replace("%effect%", value));
		}
		
		public String getString(String value) {
			return config.getString(effect.getConfigPath().replace("%effect%", value));
		}
		
		public List<String> getList(String value) {
			return config.getStringList(effect.getConfigPath().replace("%effect%", value));
		}
	}
}