package me.MrGraycat.eGlow.Config;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;

public class EGlowCustomGUIConfig {
	private EGlow instance;
	
	private YamlConfiguration config;
	private File configFile;
	
	public EGlowCustomGUIConfig(EGlow instance) {
		setInstance(instance);
		load();
	}
	
	public void load() {
		configFile = new File(getInstance().getDataFolder(), "CustomGUI.yml");
		
		try {
			if (!getInstance().getDataFolder().exists()) {
				getInstance().getDataFolder().mkdirs();
			}
			
			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4CustomGUI.yml not found&f! &eCreating&f...");
				configFile.getParentFile().mkdirs();
				getInstance().saveResource("CustomGUI.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading CustomGUI config&f.");
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
	
	public boolean reloadConfig() {
		YamlConfiguration configBackup = config;
		File configFileBackup = configFile;
		
		try {
			config = null;
			configFile = null;
			
			configFile = new File(getInstance().getDataFolder(), "CustomGUI.yml");
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
	
	public enum ItemInfo {
		SLOT,
		MATERIAL,
		MATERIAL_ON,
		MATERIAL_OFF,
		NAME,
		META,
		META_ON,
		META_OFF,
		LORES,
		LEFT_CLICK,
		RIGHT_CLICK,
		ANY_CLICK;
	}
	
	public boolean getEnabled() {
		return getConfig().getBoolean("Enable");
	}
	
	public String getTitle() {
		return ChatUtil.translateColors(getConfig().getString("title"));
	}
	
	public int getRows() {
		return getConfig().getInt("Rows");
	}
	
	public Set<String> getItems() {
		return getConfig().getConfigurationSection("items").getKeys(false);
	}
	
	//Setters
	@SuppressWarnings("unused")
	private void setConfig(YamlConfiguration config) {
		this.config = config;
	}
	
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	public YamlConfiguration getConfig() {
		return this.config;
	}
	
	private EGlow getInstance() {
		return this.instance;
	}
}
