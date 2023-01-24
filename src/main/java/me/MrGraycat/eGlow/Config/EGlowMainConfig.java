
package me.MrGraycat.eGlow.Config;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.*;

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
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4Config.yml not found&f! &eCreating&f...", false);
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("Config.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading main config&f.", false);
			}
			
			config = new YamlConfiguration();
			config.load(configFile);
			
			registerCustomPermissions();

			//TODO to be removed soon
			if (!config.isConfigurationSection("Command-alias")) {
				File oldFile = new File(EGlow.getInstance().getDataFolder(), "OLDConfig.yml");
				
				if (oldFile.exists())
					oldFile.delete();
				
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &cDetected old main config&f! &eRenamed it to OLDConfig&f! &eReconfiguring might be required&f!", false);
				configFile.renameTo(oldFile);
				initialize();
			}

			configCheck();
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
			
			configFile = new File(EGlow.getInstance().getDataFolder(), "Config.yml");
			config = new YamlConfiguration();
			config.load(configFile);
			
			registerCustomPermissions();
			
			return true;
		} catch(Exception e) {
			config = configBackup;
			configFile = configFileBackup;
			
			ChatUtil.reportError(e);
			return false;
		}
	}
	
	private static void configCheck() {
		replaceOrAdd("Actionbars.Enable", "Actionbars.enable", true);
		replaceOrAdd("Actionbars.Use-in-GUI","Actionbars.use-in-GUI", false);

		replaceOrAdd("Command-alias.Enable", "Command-alias.enable", false);
		replaceOrAdd("Command-alias.Alias", "Command-alias.alias", "glow");

		replaceOrAdd("Delays.Player.Slow", "Delays.slow", 2.0);
		replaceOrAdd("Delays.Player.Fast", "Delays.fast", 1.0);

		replaceOrAdd("Tabname.Enable", "Formatting.tablist.enable", false);
		replaceOrAdd("NA", "Formatting.tablist.format", "%prefix% &r%name% %suffix%");
		replaceOrAdd("Tagname.Enable", "Formatting.tagname.enable", false);
		replaceOrAdd("Tagname.tagPrefix", "Formatting.tagname.prefix", "%prefix%");
		replaceOrAdd("Tagname.tagSuffix", "Formatting.tagname.suffix", "%suffix%");

		replaceOrAdd("MySQL.Enable", "MySQL.enable", false);
		replaceOrAdd("MySQL.Host", "MySQL.host", "localhost");
		replaceOrAdd("MySQL.Port", "MySQL.port", 3306);
		replaceOrAdd("NA", "MySQL.DBName", "");
		replaceOrAdd("MySQL.Username", "MySQL.username", "root");
		replaceOrAdd("MySQL.Password", "MySQL.password", "123");

		replaceOrAdd("World.Enable", "World.enable", false);
		replaceOrAdd("World.Action", "World.action", "BLOCK");
		replaceOrAdd("World.Worlds", "World.worlds", Arrays.asList("world1", "world2"));

		replaceOrAdd("Options.Disable-glow-when-invisible", "Settings.disable-glow-when-invisible", true);
		replaceOrAdd("Options.Advanced-TAB-integration", "Settings.smart-TAB-nametag-handler", false);

		replaceOrAdd("Options.Render-player-skulls", "Settings.gui.render-skulls", true);
		replaceOrAdd("Options.Inventory-add-glass", "Settings.gui.add-glass-panes", true);
		replaceOrAdd("Options.Disable-prefix-in-GUI", "Settings.gui.add-prefix-to-title", true); //custom-effects-in-gui
		replaceOrAdd("NA", "Settings.gui.custom-effects-in-gui", true);
		replaceOrAdd("Options.Use-GUI-color-as-chat-color", "Settings.gui.use-gui-color-for-messages", false);
		replaceOrAdd("NA", "Settings.gui.max-personal-effect-size", 10);

		replaceOrAdd("Options.PermissionCheck-on-join", "Settings.join.check-glow-permission", false);
		replaceOrAdd("Options.Default-glow-on-join-value", "Settings.join.default-glow-on-join-value", true);
		replaceOrAdd("Options.Mention-glow-state-on-join", "Settings.join.mention-glow-state", false);
		replaceOrAdd("Force-glow.Enable", "Settings.join.force-glows.enable", false);
		replaceOrAdd("Force-glow.Bypass-blocked-worlds", "Settings.join.force-glows.bypass-blocked-worlds", false);
		replaceOrAdd("Force-glow.Glows", "Settings.join.force-glows.glows", true);
		replaceOrAdd("Options.Send-update-notifications", "Settings.notifications.plugin-update", true);
		replaceOrAdd("Options.Send-invisibility-notification", "Settings.notifications.invisibility-change", true);
		replaceOrAdd("Options.Send-target-notification", "Settings.notifications.target-set-unset-command", true);

		replaceOrAdd("NA", "Advanced.glow-visibility.enable", false);
		replaceOrAdd("NA", "Advanced.glow-visibility.delay", 0.5);
		replaceOrAdd("MySQL.useSSL", "Advanced.MySQL.useSSL", true);
		replaceOrAdd("Options.Collision", "Advanced.teams.entity-collision", true);
		replaceOrAdd("Options.Nametag", "Advanced.teams.nametag-visibility", true);
		replaceOrAdd("Options.Remove-scoreboard-on-join", "Advanced.teams.remove-teams-on-join", true);
		replaceOrAdd("Options.Feature-team-packets", "Advanced.teams.send-eGlow-team-packets", true);
		replaceOrAdd("Options.Feature-packet-blocker", "Advanced.packets.smart-packet-blocker", true);
		remove( "Delays.Player", "Tabname", "Tabname.tabPrefix", "Tabname.tabName", "Tabname.tabSuffix", "Tagname", "Force-glow", "Options");
	}

	public enum MainConfig {
		ACTIONBARS_ENABLE("Actionbars.enable"),
		ACTIONBARS_IN_GUI("Actionbars.use-in-GUI"),

		COMMAND_ALIAS_ENABLE("Command-alias.enable"),
		COMMAND_ALIAS("Command-alias.alias"),

		DELAY_SLOW("Delays.slow"),
		DELAY_FAST("Delays.fast"),

		FORMATTING_TABLIST_ENABLE("Formatting.tablist.enable"),
		FORMATTING_TABLIST_FORMAT("Formatting.tablist.format"),
		FORMATTING_TAGNAME_ENABLE("Formatting.tagname.enable"),
		FORMATTING_TAGNAME_PREFIX("Formatting.tagname.prefix"),
		FORMATTING_TAGNAME_SUFFIX("Formatting.tagname.suffix"),

		MYSQL_ENABLE("MySQL.enable"),
		MYSQL_HOST("MySQL.host"),
		MYSQL_PORT("MySQL.port"),
		MYSQL_DBNAME("MySQL.DBName"),
		MYSQL_USERNAME("MySQL.username"),
		MYSQL_PASSWORD("MySQL.password"),

		WORLD_ENABLE("World.enable"),
		WORLD_ACTION("World.action"),
		WORLD_LIST("World.worlds"),

		SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE("Settings.disable-glow-when-invisible"),
		SETTINGS_SMART_TAB_NAMETAG_HANDLER("Settings.smart-TAB-nametag-handler"),

		SETTINGS_GUI_RENDER_SKULLS("Settings.gui.render-skulls"),
		SETTINGS_GUI_ADD_GLASS_PANES("Settings.gui.add-glass-panes"),
		SETTINGS_GUI_ADD_PREFIX("Settings.gui.add-prefix-to-title"),
		SETTINGS_GUI_CUSTOM_EFFECTS("Settings.gui.custom-effects-in-gui"),
		SETTINGS_GUI_COLOR_FOR_MESSAGES("Settings.gui.use-gui-color-for-messages"),
		SETTINGS_GUI_MAX_PERSONAL_GLOW_SIZE("Settings.gui.max-personal-effect-size"),

		SETTINGS_JOIN_CHECK_PERMISSION("Settings.join.check-glow-permission"),
		SETTINGS_JOIN_DEFAULT_GLOW_ON_JOIN_VALUE("Settings.join.default-glow-on-join-value"),
		SETTINGS_JOIN_MENTION_GLOW_STATE("Settings.join.mention-glow-state"),
		SETTINGS_JOIN_FORCE_GLOWS_ENABLE("Settings.join.force-glows.enable"),
		SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS("Settings.join.force-glows.bypass-blocked-worlds"),
		SETTINGS_JOIN_FORCE_GLOWS_LIST("Settings.join.force-glows.glows"),

		SETTINGS_NOTIFICATIONS_UPDATE("Settings.notifications.plugin-update"),
		SETTINGS_NOTIFICATIONS_INVISIBILITY("Settings.notifications.invisibility-change"),
		SETTINGS_NOTIFICATIONS_TARGET_COMMAND("Settings.notifications.target-set-unset-command"),

		ADVANCED_GLOW_VISIBILITY_ENABLE("Advanced.glow-visibility.enable"),
		ADVANCED_GLOW_VISIBILITY_DELAY("Advanced.glow-visibility.delay"),

		ADVANCED_MYSQL_USESSL("Advanced.MySQL.useSSL"),

		ADVANCED_TEAMS_ENTITY_COLLISION("Advanced.teams.entity-collision"),
		ADVANCED_TEAMS_NAMETAG_VISIBILITY("Advanced.teams.nametag-visibility"),
		ADVANCED_TEAMS_REMOVE_ON_JOIN("Advanced.teams.remove-teams-on-join"),
		ADVANCED_TEAMS_SEND_PACKETS("Advanced.teams.send-eGlow-team-packets"),

		ADVANCED_PACKETS_SMART_BLOCKER("Advanced.packets.smart-packet-blocker");

		private final MainConfig main;
		private final String configPath;

		MainConfig(String configPath) {
			this.main = this;
			this.configPath = configPath;
		}

		public String getConfigPath() {
			return configPath;
		}

		public String getString() {
			return config.getString(main.getConfigPath());
		}

		public String getString(String name) {
			return config.getString(main.getConfigPath() + "." + name);
		}

		public List<String> getStringList() {
			List<String> worldNames = new ArrayList<>();

			for (String worldName : config.getStringList(main.getConfigPath())) {
				worldNames.add(worldName.toLowerCase());
			}

			return worldNames;
		}

		public int getInt() {
			switch (main) {
				case DELAY_SLOW:
				case DELAY_FAST:
				case ADVANCED_GLOW_VISIBILITY_DELAY:
					return (int) (config.getDouble(main.getConfigPath()) * 20);
				default:
					return config.getInt(main.getConfigPath());
			}
		}

		public Boolean getBoolean() {
			return config.getBoolean(main.getConfigPath());
		}

		public Set<String> getConfigSection() {
			return Objects.requireNonNull(config.getConfigurationSection(main.getConfigPath()), main.getConfigPath() + " isn't a valid path").getKeys(false);
		}
	}

	private static void replaceOrAdd(String oldPath, String newPath, Object value) {
		try {
			if (config.contains(oldPath)) {
				config.set(newPath, config.get(oldPath));
				config.set(oldPath, null);
			} else if (!config.contains(newPath)) {
				config.set(newPath, value);
			}

			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void remove(String... paths) {
		try {
			for (String path : paths) {
				config.set(path, null);
			}

			config.save(configFile);
		} catch (Exception e) {
			ChatUtil.reportError(e);
		}
	}
	
	private static void registerCustomPermissions() {
		if (!MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean())
			return;
		
		for (String name : MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection()) {
			try {
				EGlow.getInstance().getServer().getPluginManager().addPermission(new Permission("eglow.force." + name.toLowerCase()));
			} catch (Exception ignored) {}
		}
	}
}