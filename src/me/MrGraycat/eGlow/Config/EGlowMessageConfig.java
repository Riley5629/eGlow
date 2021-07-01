package me.MrGraycat.eGlow.Config;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;

public class EGlowMessageConfig {
	
	private static YamlConfiguration config;
	private File configFile;
	
	public EGlowMessageConfig() {
		load();
	}
	
	private void load() {
		configFile = new File(EGlow.getInstance().getDataFolder(), "Messages.yml");
		
		try {
			if (!EGlow.getInstance().getDataFolder().exists()) {
				EGlow.getInstance().getDataFolder().mkdirs();
			}
			
			if (!configFile.exists()) {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &4Messages.yml not found&f! &eCreating&f...");
				configFile.getParentFile().mkdirs();
				EGlow.getInstance().saveResource("Messages.yml", false);
			} else {
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading messages config&f.");
			}
			
			config = new YamlConfiguration();
			config.load(configFile);
			
			if (!config.isConfigurationSection("main")) {
				File oldFile = new File(EGlow.getInstance().getDataFolder(), "OLDMessages.yml");
				
				if (oldFile.exists())
					oldFile.delete();
				
				ChatUtil.sendToConsole("&f[&eeGlow&f]: &cDetected old messages config&f! &eRenamed it to OLDMessages&f! &eReconfiguring might be required&f!");
				configFile.renameTo(oldFile);
				load();
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
	
	public boolean reloadConfig() {
		YamlConfiguration configBackup = config;
		File configFileBackup = configFile;
		
		try {
			config = null;
			configFile = null;
			
			configFile = new File(EGlow.getInstance().getDataFolder(), "Messages.yml");
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
	
	public enum Message {
		PREFIX("main.prefix"),
		NO_PERMISSION("main.no-permission"),
		NO_PERMISSION_ON_JOIN("main.no-permission-join"),
		NO_PERMISSION_GLOW_ON_JOIN("main.no-permission-glowonjoin"),
		COLOR("main.color-"),
		NEW_GLOW("main.glow-new"),
		SAME_GLOW("main.glow-same"),
		ENABLE_GLOW("main.glow-enable"),
		DISABLE_GLOW("main.glow-disable"),
		GLOW_REMOVED("main.glow-removed"),
		VISIBILITY_CHANGE("main.glow-visibility-change"),
		UNSUPPORTED_GLOW("main.glow-unsupported"),
		OTHER_CONFIRM("main.other-glow-player-confirm"),
		OTHER_CONFIRM_OFF("main.other-glow-player-confirm-off"),
		OTHER_GLOW_ON_JOIN_CONFIRM("main.other-glow-on-join-confirm"),
		OTHER_PLAYER_IN_DISABLED_WORLD("main.other-glow-player-disabled-world"),
		OTHER_PLAYER_DISGUISE("main.other-glow-player-disguise"),
		OTHER_PLAYER_INVISIBLE("main.other-glow-player-invisible"),
		TARGET_NOTIFICATION_PREFIX("main.other-glow-target-notification-prefix"),
		RELOAD_SUCCESS("main.reload-success"),
		RELOAD_FAIL("main.reload-fail"),
		NO_LAST_GLOW("main.argument-no-last-glow"),
		INCORRECT_USAGE("main.argument-incorrect-usage"),
		PLAYER_NOT_FOUND("main.argument-player-not-found"),
		COMMAND_LIST("main.command-list"),
		PLAYER_ONLY("main.command-player-only"),
		INVISIBILITY_BLOCKED("main.invisibility-glow-blocked"),
		INVISIBILITY_DISABLED("main.invisibility-glow-disabled"),
		INVISIBILITY_ENABLED("main.invisibility-glow-enabled"),
		DISGUISE_BLOCKED("main.disguise-glow-blocked"),
		DISGUISE_ALLOWED("main.disguise-glow-allowed"),
		WORLD_BLOCKED("main.world-glow-blocked"),
		WORLD_BLOCKED_RELOAD("main.world-glow-blocked-reload"),
		WORLD_ALLOWED("main.world-glow-allowed"),
		CITIZENS_NOT_INSTALLED("main.citizens-not-installed"),
		CITIZENS_NPC_PREFIX("main.citizens-npc"),
		CITIZENS_NPC_NOT_SPAWNED("main.citizens-npc-not-spawned"),
		CITIZENS_NPC_NOT_FOUND("main.citizens-npc-not-found"),
		GUI_TITLE("gui.title"),
		GUI_COLOR("gui.color-"),
		GUI_YES("gui.misc-yes"),
		GUI_NO("gui.misc-no"),
		GUI_NOT_AVAILABLE("gui.misc-not-available"),
		GUI_LEFT_CLICK("gui.misc-left-click"),
		GUI_RIGHT_CLICK("gui.misc-right-click"),
		GUI_CLICK_TO_TOGGLE("gui.misc-click-to-toggle"),
		GUI_CLICK_TO_OPEN("gui.misc-click-to-open"),
		GUI_PREVIOUS_PAGE("gui.misc-previous-page"),
		GUI_NEXT_PAGE("gui.misc-next-page"),
		GUI_PAGE_LORE("gui.misc-page-lore"),
		GUI_MAIN_MENU("gui.misc-main-menu"),
		GUI_COLOR_PERMISSION("gui.color-colorpermission"),
		GUI_BLINK_PERMISSION("gui.color-blinkpermission"),
		GUI_EFFECT_PERMISSION("gui.color-effectpermission"),
		GUI_SETTINGS_NAME("gui.setting-item-name"),
		GUI_GLOW_ON_JOIN("gui.setting-glow-on-join"),
		GUI_LAST_GLOW("gui.setting-last-glow"),
		GUI_GLOW_ITEM_NAME("gui.glow-item-name"),
		GUI_GLOWING("gui.glow-glowing"),
		GUI_SPEED_ITEM_NAME("gui.speed-item-name"),
		GUI_SPEED("gui.speed-speed"),
		GUI_CUSTOM_EFFECTS_ITEM_NAME("gui.custom-effect-item-name");
		
		private Message msg;
		private String configPath;
		
		private Message(String configPath) {
			this.msg = this;
			this.configPath = configPath;
		}
		
		public String getConfigPath() {
			return configPath;
		}
		
		public String get() {
			return getColorValue(getConfigPath());
		}
		
		public String get(String value) {
			switch(msg) {
			case COLOR:
				return getColorValue(msg.getConfigPath() + value);
			case GUI_PAGE_LORE:
				return getColorValue(msg.getConfigPath(), "%page%", value);
			case GUI_COLOR:
				if (config.contains(msg.getConfigPath() + value))
					return getColorValue(msg.getConfigPath() + value);
				return get(Message.COLOR, value);
			case NEW_GLOW:
				return getColorValue(msg.getConfigPath(), "%glowname%", value);
			case VISIBILITY_CHANGE:
				return getColorValue(msg.getConfigPath(), "%value%", value);
			case INCORRECT_USAGE:
				return getColorValue(msg.getConfigPath(), "%command%", value);
			default:
				break;
			}
			return "Incorrect handled message for: " + msg.toString();
		}
		
		public String get(IEGlowPlayer target, String value) {
			switch(msg) {
			case OTHER_CONFIRM:
				return getColorValue(msg.getConfigPath(), target, "%glowname%", value);
			case OTHER_GLOW_ON_JOIN_CONFIRM:
				return getColorValue(msg.getConfigPath(), target, "%value%", value);
			default:
				break;
			}
			return "Incorrect handled message for: " + msg.toString();
		}
		
		public String get(IEGlowPlayer target) {
			return getColorValue(msg.getConfigPath(), "%target%", target.getDisplayName());
		}
		
		private String get(Message message, String value) {
			return getColorValue(message.getConfigPath() + value);
		}
		
		private String getColorValue(String path) {
			String text = config.getString(path);
			
			if (text == null)
				return "&cFailed to get text for&f: '&e" + path + "'";

			return ChatUtil.translateColors(text);
		}
		
		private String getColorValue(String path, String textToReplace, String replacement) {
			String text = config.getString(path);
			
			if (text == null)
				return "&cFailed to get text for&f: '&e" + path + "'";
			return ChatUtil.translateColors(text.replace(textToReplace, replacement));
		}
		
		private String getColorValue(String path, IEGlowPlayer ePlayer, String textToReplace, String replacement) {
			String text = config.getString(path);
			
			if (text == null)
				return "&cFailed to get text for&f: '&e" + path + "'";
			return ChatUtil.translateColors(text.replace(textToReplace, replacement).replace("%target%", ePlayer.getDisplayName()));
		}
	}
	
	private void configCheck() {
		addIfMissing("gui.custom-effect-item-name", "&eCustom effects menu");
		addIfMissing("gui.misc-click-to-open", "&9Click to open&f.");
		addIfMissing("gui.misc-previous-page", "&e< Previous page");
		addIfMissing("gui.misc-next-page", "&eNext page >");
		addIfMissing("gui.misc-page-lore", "&fPage: %page%");
		addIfMissing("gui.misc-main-menu", "&eMain menu");
		addIfMissing("main.glow-removed", "&cDisabling glow&f! &eThe effect your were using got removed.");
		addIfMissing("main.invisibility-glow-blocked", "&cGlowing is disabled while you're invisible&f.");
		addIfMissing("main.invisibility-glow-disabled", "&cDisabling glow while you're invisible&f.");
		addIfMissing("main.invisibility-glow-enabled", "&aRe-enabling you glow as you're no longer invisible&f.");
		addIfMissing("main.other-glow-player-invisible", "&e%target% &cis invisible which disables the glow&f.");
		addIfMissing("main.other-glow-player-disguise", "&e%target% &cis in disguise which disabled the glow&f.");
	}
	
	private void addIfMissing(String path, String text) {
		try {
			if (!config.contains(path)) {
				config.set(path, text);
				config.save(configFile);
			}	
		} catch (Exception e) {
			ChatUtil.reportError(e);
		}
	}
}