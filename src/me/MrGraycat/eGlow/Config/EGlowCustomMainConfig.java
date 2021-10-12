package me.MrGraycat.eGlow.Config;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;

public class EGlowCustomMainConfig {
	private EGlow instance;
	
	private static YamlConfiguration config;
	private static File configFile;
	
	public EGlowCustomMainConfig(EGlow instance) {
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
	
	public enum ItemKey {
		SLOTS("Slot"),
		MATERIAL("Material"),
		NAME("Name"),
		META("Meta"),
		LORES("Lores");
		
		private String itemKey;
		
		private ItemKey(String itemKey) {
			this.itemKey = itemKey;
		}
		
		public String getItemKey() {
			return this.itemKey;
		}
	}
	
	public boolean getEnabled() {
		return config.getBoolean("Enable");
	}
	
	public int getRows() {
		return config.getInt("Rows");
	}
	
	public Set<String> getItems() {
		return config.getConfigurationSection("Items").getKeys(false);
	}
 	
	public String getItemInfo(String item, ItemKey key) {
		return config.getString("Items" + item + "." + key.getItemKey());
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}
}
