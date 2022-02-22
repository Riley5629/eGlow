
package me.MrGraycat.eGlow.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.yaml.snakeyaml.error.YAMLException;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowWorldAction;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;

public class EGlowMainConfig {

	private static YamlConfiguration config;
	private static File configFile;
	
	public static void initialize() {
		configFile = new File(EGlow.getInstance().getDataFolder(), "Config.yml");
		
		try {
			if (!EGlow.getInstance().getDataFolder().exists()) {
				EGlow.getInstance().getDataFolder().mkdirs();
			}
			
			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4Config.yml not found&f! &eCreating&f...");
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("Config.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading main config&f.");
			}
			
			config = new YamlConfiguration();
			config.load(configFile);
			
			registerCustomPermissions();
			
			if (!config.isConfigurationSection("Command-alias")) {
				File oldFile = new File(EGlow.getInstance().getDataFolder(), "OLDConfig.yml");
				
				if (oldFile.exists())
					oldFile.delete();
				
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &cDetected old main config&f! &eRenamed it to OLDConfig&f! &eReconfiguring might be required&f!");
				configFile.renameTo(oldFile);
				initialize();
			}
			
			configCheck();
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
			
			configFile = new File(EGlow.getInstance().getDataFolder(), "Config.yml");
			config = new YamlConfiguration();
			config.load(configFile);
			
			registerCustomPermissions();
			
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
	
	private static void configCheck() {
		addIfMissing("Tabname.Enable", false);
		addIfMissing("Tabname.tabPrefix", "%prefix%");
		addIfMissing("Tabname.tabName", "&r%name%");
		addIfMissing("Tabname.tabSuffix", "%suffix%");
		
		addIfMissing("Tagname.Enable", false);
		addIfMissing("Tagname.tagPrefix", "%prefix%");
		addIfMissing("Tagname.tagSuffix", "%suffix%");
		addIfMissing("Force-glow.Bypass-blocked-worlds", false);
		addIfMissing("Options.Send-invisibility-notification", true);
		addIfMissing("Options.Advanced-TAB-integration", false);
		addIfMissing("Options.Disable-glow-when-invisible", true);
		addIfMissing("Options.Mention-glow-state-on-join", false);
	}
	
	private static void addIfMissing(String path, Object value) {
		try {
			if (!config.contains(path)) {
				config.set(path, value);
				config.save(configFile);
			}	
		} catch (Exception e) {
			ChatUtil.reportError(e);
		}
	}
	
	public static boolean getWorldCheckEnabled() {
		return config.getBoolean("World.Enable");
	}
	
	public static GlowWorldAction getWorldAction() {
		try {
			return GlowWorldAction.valueOf(config.getString("World.Action").toUpperCase() + "ED");
		} catch (IllegalArgumentException e) {
			return GlowWorldAction.UNKNOWN;
		}
	}
	
	public static List<String> getWorlds() {
		List<String> worlds = new ArrayList<String>();
		
		for (String world : config.getStringList("World.Worlds")) {
			worlds.add(world.toLowerCase());
		}		
		return worlds;
	}
	
	public static boolean getForceGlowEnabled() {
		return config.getBoolean("Force-glow.Enable");
	}
	
	public static boolean getForceGlowBypassBlockedWorlds() {
		return config.getBoolean("Force-glow.Bypass-blocked-worlds");
	}
	
	public static Set<String> getForceGlowList() {
		return config.getConfigurationSection("Force-glow.Glows").getKeys(false);
	}
	
	public static String getForceGlowEffect(String name) {
		return config.getString("Force-glow.Glows." + name);
	}
	
	public static int getPlayerSlowDelay() {
		return (int) (config.getDouble("Delays.Player.Slow") * 20);
	}
	
	public static int getPlayerFastDelay() {
		return (int) (config.getDouble("Delays.Player.Fast") * 20);
	}
	
	public static int getNPCSlowDelay() {
		return (int) (config.getDouble("Delays.NPC.Slow") * 20);
	}
	
	public static int getNPCFastDelay() {
		return (int) (config.getDouble("Delays.NPC.Fast") * 20);
	}
	
	public static boolean OptionEnableCommandAlias() {
		return config.getBoolean("Command-alias.Enable");
	}
	
	public static String OptionCommandAlias() {
		return config.getString("Command-alias.Alias");
	}
	
	public static boolean OptionDisableGlowWhenInvisible() {
		return config.getBoolean("Options.Disable-glow-when-invisible");
	}
	
	public static boolean OptionSendInvisibilityNotification() {
		return config.getBoolean("Options.Send-invisibility-notification");
	}
	
	public static boolean OptionRenderPlayerSkulls() {
		return config.getBoolean("Options.Render-player-skulls");
	}
	
	public static boolean OptionDefaultGlowOnJoinValue() {
		return config.getBoolean("Options.Default-glow-on-join-value");
	}
	
	public static boolean OptionSendTargetNotification() {
		return config.getBoolean("Options.Send-target-notification");
	}
	
	public static boolean OptionPermissionCheckonJoin() {
		return config.getBoolean("Options.PermissionCheck-on-join");
	}
	
	public static boolean OptionSendUpdateNotifications() {
		return config.getBoolean("Options.Send-update-notifications");
	}	
	
	public static boolean OptionAdvancedTABIntegration() {
		return (config.contains("Options.Advanced-TAB-integration")) ? config.getBoolean("Options.Advanced-TAB-integration") : false;
	}

	public static boolean useMySQL() {
		return config.getBoolean("MySQL.Enable");
	}
	
	public static String getMySQLHost() {
		return config.getString("MySQL.Host");
	}
	
	public static int getMySQLPort() {
		return config.getInt("MySQL.Port");
	}
	
	public static String getMySQLDBName() {
		return config.getString("MySQL.DBName");
	}
	
	public static String getMySQLUsername() {
		return config.getString("MySQL.Username");
	}
	
	public static String getMySQLPassword( ) {
		return config.getString("MySQL.Password");
	}
	
	public static boolean getMySQLUseSSL() {
		return(config.contains("MySQL.useSSL")) ? config.getBoolean("MySQL.useSSL") : true;
	}
	
	public static boolean setTabnameFormat() {
		return config.getBoolean("Tabname.Enable");
	}
	
	public static String getTabPrefix() {
		return config.getString("Tabname.tabPrefix");
	}
	
	public static String getTabName() {
		return config.getString("Tabname.tabName");
	}
	
	public static String getTabSuffix() {
		return config.getString("Tabname.tabSuffix");
	}
	
	public static boolean setTagnameFormat() {
		return config.getBoolean("Tagname.Enable");
	}
	
	public static String getTagPrefix() {
		return config.getString("Tagname.tagPrefix");
	}
	
	public static String getTagSuffix() {
		return config.getString("Tagname.tagSuffix");
	}
	
	//Hidden
	public static boolean OptionDoTeamCollision() {
		return (config.contains("Options.Collision")) ? config.getBoolean("Options.Collision") : true;
	}
	public static boolean OptionShowNametag() {
		return (config.contains("Options.Nametag")) ? config.getBoolean("Options.Nametag") : true;
	}
	
	public static boolean OptionUseGUIColorAsChatColor() {
		return (config.contains("Options.Use-GUI-color-as-chat-color")) ? config.getBoolean("Options.Use-GUI-color-as-chat-color") : false;
	}
	
	public static boolean OptionAddGlassToInv() {
		return (config.contains("Options.Inventory-add-glass")) ? config.getBoolean("Options.Inventory-add-glass") : true;
	}
	
	public static boolean OptionRemoveScoreboardOnJoin() {
		return (config.contains("Options.Remove-scoreboard-on-join")) ? config.getBoolean("Options.Remove-scoreboard-on-join") : true;
	}
	
	public static boolean OptionFeaturePacketBlocker() {
		return (config.contains("Options.Feature-packet-blocker")) ? config.getBoolean("Options.Feature-packet-blocker") : true;
	}
	
	public static boolean OptionFeatureTeamPackets() {
		return (config.contains("Options.Feature-team-packets")) ? config.getBoolean("Options.Feature-team-packets") : true;
	}
	
	public static boolean OptionDisablePrefixInGUI() {
		return (config.contains("Options.Disable-prefix-in-GUI")) ? config.getBoolean("Options.Disable-prefix-in-GUI") : false;
	}
	
	public static boolean OptionMentionGlowState() {
		return (config.contains("Options.Mention-glow-state-on-join")) ? config.getBoolean("Options.Mention-glow-state-on-join") : false;
	}
	
	private static void registerCustomPermissions() {
		if (!getForceGlowEnabled())
			return;
		
		for (String name : getForceGlowList()) {
			try {
				EGlow.getInstance().getServer().getPluginManager().addPermission(new Permission("eglow.force." + name.toLowerCase()));
			} catch (Exception ignored) {}
		}
	}
}