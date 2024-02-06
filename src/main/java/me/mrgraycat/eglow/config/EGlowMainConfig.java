package me.mrgraycat.eglow.config;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EGlowMainConfig {
	private static YamlConfiguration config;
	private static File configFile;

	public static void initialize() {
		File oldConfigFile = new File(EGlow.getInstance().getDataFolder(), "Config.yml");
		configFile = new File(EGlow.getInstance().getDataFolder(), "config.yml");

		try {
			if (!EGlow.getInstance().getDataFolder().exists())
				EGlow.getInstance().getDataFolder().mkdirs();

			if (oldConfigFile.exists())
				oldConfigFile.renameTo(configFile);

			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4config.yml not found&f! &eCreating&f...", false);
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("config.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading main config&f.", false);
			}

			config = new YamlConfiguration();
			config.load(configFile);

			registerCustomPermissions();
			repairConfig();
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

			configFile = new File(EGlow.getInstance().getDataFolder(), "config.yml");
			config = new YamlConfiguration();
			config.load(configFile);

			registerCustomPermissions();

			return true;
		} catch (Exception exception) {
			config = configBackup;
			configFile = configFileBackup;

			ChatUtil.reportError(exception);
			return false;
		}
	}

	private static void repairConfig() {
		InputStream resource = EGlow.getInstance().getResource("config.yml");
		YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(resource)));

		for (String path : Objects.requireNonNull(tempConfig.getConfigurationSection("")).getKeys(true)) {
			if (path.contains("Settings.join.force-glows.glows")) {
				continue;
			}

			if (!config.contains(path))
				config.set(path, tempConfig.get(path));
		}

		try {
			config.save(configFile);
		} catch (Exception exception) {
			ChatUtil.reportError(exception);
		}
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
		SETTINGS_GUIS_INTERACTION_DELAY("Settings.gui.interaction-delay"),

		SETTINGS_JOIN_CHECK_PERMISSION("Settings.join.check-glow-permission"),
		SETTINGS_JOIN_DEFAULT_GLOW_ON_JOIN_VALUE("Settings.join.default-glow-on-join-value"),
		SETTINGS_JOIN_MENTION_GLOW_STATE("Settings.join.mention-glow-state"),
		SETTINGS_JOIN_FORCE_GLOWS_ENABLE("Settings.join.force-glows.enable"),
		SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS("Settings.join.force-glows.bypass-blocked-worlds"),
		SETTINGS_JOIN_FORCE_GLOWS_LIST("Settings.join.force-glows.glows"),

		SETTINGS_NOTIFICATIONS_UPDATE("Settings.notifications.plugin-update"),
		SETTINGS_NOTIFICATIONS_INVISIBILITY("Settings.notifications.invisibility-change"),
		SETTINGS_NOTIFICATIONS_TARGET_COMMAND("Settings.notifications.target-set-unset-command"),

		ADVANCED_VELOCITY_MESSAGING("Advanced.use-velocity-plugin-messaging"),
		ADVANCED_FORCE_DISABLE_PROXY_MESSAGING("Advanced.force-disable-proxy-messaging"),
		ADVANCED_FORCE_DISABLE_TAB_INTEGRATION("Advanced.force-disable-tab-integration"),
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

		public long getLong() {
			return (long) (config.getDouble(main.getConfigPath()) * 1000L);
		}

		public Boolean getBoolean() {
			return config.getBoolean(main.getConfigPath());
		}

		public Set<String> getConfigSection() {
			return Objects.requireNonNull(config.getConfigurationSection(main.getConfigPath()), main.getConfigPath() + " isn't a valid path").getKeys(false);
		}
	}

	private static void registerCustomPermissions() {
		if (!MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean())
			return;

		for (String name : MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection()) {
			try {
				EGlow.getInstance().getServer().getPluginManager().addPermission(new Permission("eglow.force." + name.toLowerCase()));
			} catch (Exception ignored) {
			}
		}
	}
}