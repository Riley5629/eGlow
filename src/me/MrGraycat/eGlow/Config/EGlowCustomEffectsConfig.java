package me.MrGraycat.eGlow.Config;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;

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
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4CustomEffects.yml not found&f! &eCreating&f...");
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("CustomEffects.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading CustomEffects config&f.");
			}
			
			config = new YamlConfiguration();
			config.load(configFile);
		} catch(Exception e) {
			ChatUtil.reportError(e);
			if (e.getCause() instanceof YAMLException) {
				List<String> suggestions = YamlAssist.getSuggestions(configFile);
				for(String suggestion : suggestions) {
					ChatUtil.sendToConsole("&c" + suggestion);
				}
			}
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
			if (e.getCause() instanceof YAMLException) {
				List<String> suggestions = YamlAssist.getSuggestions(configFile);
				for(String suggestion : suggestions) {
					ChatUtil.sendToConsole("&c" + suggestion);
				}
			}
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
		
		private Effect effect;
		private String configPath;
		
		private Effect(String configPath) {
			this.effect = this;
			this.configPath = configPath;
		}
		
		public String getConfigPath() {
			return configPath;
		}
		
		public Set<String> get() {
			return config.getConfigurationSection(effect.getConfigPath()).getKeys(false);
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
