package me.MrGraycat.eGlow.Addon.TAB;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Addon.TAB.Util.TABLegacyUtil;
import me.MrGraycat.eGlow.Addon.TAB.Util.TABUtil;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.shared.TabConstants;

public class TABAddon {
	private Object TAB_Util;
	
	private boolean TAB_Supported;
	private boolean TAB_Legacy_Version;
	private boolean TAB_NametagPrefixSuffixEnabled;
	private boolean TAB_TeamPacketBlockingEnabled;
	
	public TABAddon(Plugin TAB_Plugin) {
		int TAB_Version = (TAB_Plugin != null) ? Integer.valueOf(TAB_Plugin.getDescription().getVersion().replaceAll("[^\\d]", "")) : 0;
		
		if (TAB_Version < 302) {
			ChatUtil.sendToConsoleWithPrefix("&cWarning&f! &cThis version of eGlow requires TAB 3.1.0 or higher!");
			return;
		}

		setTABLegacyVersion((TAB_Version >= 310) ? false : true);
		setTABUtil((getTABLegacyVersion()) ? new TABLegacyUtil() : new TABUtil());
		loadTABSettings();
		
		TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, event -> {
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
						TAB_Addon.loadTABSettings();
						
						if (TAB_Addon.blockEGlowPackets()) {
							for (IEGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
								if (ePlayer.getFakeGlowStatus() || ePlayer.getGlowStatus())
									TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
								continue;
							}
						} else {
							cancel();
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(EGlow.getInstance());
		});
		
		setTABSupported(true);
	}
	
	public void loadTABSettings() {
		ConfigurationFile TAB_Config = TabAPI.getInstance().getConfig();
		
		setTABNametagPrefixSuffixEnabled(TAB_Config.getBoolean("scoreboard-teams.enabled", false));
		setTABTeamPacketBlockingEnabled(TAB_Config.getBoolean("scoreboard-teams.anti-override", false));
		
		if (EGlowMainConfig.OptionAdvancedTABIntegration()) {
			if (!TAB_Config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
				TAB_Config.set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
				ChatUtil.sendToConsoleWithPrefix("&6Enabling unlimited-nametag-mode in TAB since the advancedTABIntegration setting is enabled&f!");
				
				if (TabAPI.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.NAME_TAGS)) {
					ChatUtil.sendToConsoleWithPrefix("&cDisabling normal TAB nametags&f...");
					TabAPI.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.NAME_TAGS);
				}
				
				if (!TabAPI.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS)) {
					ChatUtil.sendToConsoleWithPrefix("&aEnabling custom TAB nametags&f...");
					if (getTABLegacyVersion()) {
						((TABLegacyUtil) getTABUtil()).registerFeature();
					} else {
						((TABUtil) getTABUtil()).registerFeature();
					}
				}
				
				ChatUtil.sendToConsoleWithPrefix("&aAdvanced-TAB-Integration has been setup&f!");
			}
		}
	}
	
	public void updateTABPlayer(IEGlowPlayer ePlayer, ChatColor glowColor) {
		TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());
		
		if (tabPlayer == null)
			return;
		
		String tagPrefix = "";
		String color = (glowColor.equals(ChatColor.RESET)) ? "" : glowColor + "";
		
		if (TabAPI.getInstance().getTeamManager() == null)
			return;
		
		try {
			tagPrefix = TabAPI.getInstance().getTeamManager().getOriginalPrefix(tabPlayer);
		}  catch(Exception ex) {
			tagPrefix = "";
		}
		
		try {
			if (!EGlowMainConfig.OptionAdvancedTABIntegration()) {
				TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, (!tagPrefix.isEmpty()) ? tagPrefix + color : color);
			} else {
				Property propertyCustomTagName = tabPlayer.getProperty(TabConstants.Property.CUSTOMTAGNAME);
				
				if (propertyCustomTagName == null) {
					TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, (!tagPrefix.isEmpty()) ? tagPrefix + color : color);
				} else {
					String originalTagName = propertyCustomTagName.getOriginalRawValue();
					
					if (!propertyCustomTagName.getCurrentRawValue().equals(tagPrefix + originalTagName))
						propertyCustomTagName.setTemporaryValue(tagPrefix + originalTagName);
						
					TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, color);
				}
			}
		} catch (IllegalStateException e) {
			//Ignored :I
		}
	}
	
	public TabPlayer getTABPlayer(UUID uuid) {
		return TabAPI.getInstance().getPlayer(uuid);
	}
	
	public boolean blockEGlowPackets() {
		return (getTABNametagPrefixSuffixEnabled() && getTABTeamPacketBlockingEnabled()) ? true : false;
	}
	
	//Getter
	public Object getTABUtil() {
		return this.TAB_Util;
	}
	
	public boolean getTABSupported() {
		return this.TAB_Supported;
	}
	
	public boolean getTABLegacyVersion() {
		return this.TAB_Legacy_Version;
	}
	
	public boolean getTABNametagPrefixSuffixEnabled() {
		return this.TAB_NametagPrefixSuffixEnabled;
	}
	
	public boolean getTABTeamPacketBlockingEnabled() {
		return this.TAB_TeamPacketBlockingEnabled;
	}
	
	//Setter
	private void setTABUtil(Object util) {
		this.TAB_Util = util;
	}
	
	private void setTABSupported(boolean status) {
		this.TAB_Supported = status;
	}
	
	private void setTABLegacyVersion(boolean status) {
		this.TAB_Legacy_Version = status;
	}
	
	private void setTABNametagPrefixSuffixEnabled(boolean status) {
		this.TAB_NametagPrefixSuffixEnabled = status;
	}
	
	private void setTABTeamPacketBlockingEnabled(boolean status) {
		this.TAB_TeamPacketBlockingEnabled = status;
	}
}
