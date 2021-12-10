package me.MrGraycat.eGlow.Addon.TAB;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Addon.TAB.Listeners.EGlowTABListenerBukkit;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.TabConstants;

public class TABAddon {	
	private EGlow instance;
	
	//Bukkit
	private boolean TAB_Bukkit;
	private boolean TAB_New;
	private boolean TAB_NametagPrefixSuffixEnabled;
	private boolean TAB_TeamPacketBlockingEnabled;
	private boolean groupUpdateCheckerEnabled;
	private ConcurrentHashMap<Player, String> storedGroups = new ConcurrentHashMap<>();
	
	public TABAddon(EGlow instance) {
		setInstance(instance);
		Plugin tabPlugin = ((Plugin) getInstance().getDebugUtil().getPlugin("TAB"));
		String version = (tabPlugin != null) ? tabPlugin.getDescription().getVersion() : "0";
		
		if (Integer.valueOf(version.replaceAll("[^\\d]", "")) >= 300) {
			if (getInstance().getDebugUtil().pluginCheck("TAB") && getInstance().getDebugUtil().getPlugin("TAB").getClass().getName().startsWith("me.neznamy.tab")) {
				setTABOnBukkit(true);
				loadConfigSettings();
				startGroupUpdateChecker();
				
				new EGlowTABListenerBukkit(getInstance());
			}
		} else {
			if (getInstance().getDebugUtil().pluginCheck("TAB"))
				ChatUtil.sendToConsoleWithPrefix("&cWarning&f! &cThis version of eGlow required TAB 3.0.0 or higher!");
		}
	}
	
	public TabPlayer getTABPlayer(UUID uuid) {
		return TabAPI.getInstance().getPlayer(uuid);
	}
	
	public boolean blockEGlowPackets() {
		if (getTABNametagPrefixSuffixEnabled() && getTABTeamPacketBlockingEnabled())
			return true;
		return false;
	}
	
	public void loadConfigSettings() {
		String tabVersion = ((Plugin) getInstance().getDebugUtil().getPlugin("TAB")).getDescription().getVersion();
		ConfigurationFile tabConfig = TabAPI.getInstance().getConfig();
		
		if (Integer.valueOf(tabVersion.replaceAll("[^\\d]", "")) >= 300) {
			setTABNewVersion(true);
			setTABNametagPrefixSuffixEnabled(tabConfig.getBoolean("scoreboard-teams.enabled", false));
			setTABTeamPacketBlockingEnabled(tabConfig.getBoolean("scoreboard-teams.anti-override", false));
			
			if (EGlowMainConfig.OptionAdvancedTABIntegration()) {
				if (!tabConfig.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
					tabConfig.set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
					ChatUtil.sendToConsoleWithPrefix("&6Enabling unlimited-nametag-mode in TAB since the advancedTABIntegration setting is enabled&f!");
					
					if (TabAPI.getInstance().getFeatureManager().isFeatureEnabled("nametag16")) {
						ChatUtil.sendToConsoleWithPrefix("&cDisabling normal TAB nametags&f...");
						TabAPI.getInstance().getFeatureManager().unregisterFeature("nametag16");
					}
					
					if (!TabAPI.getInstance().getFeatureManager().isFeatureEnabled("nametagx")) {
						ChatUtil.sendToConsoleWithPrefix("&aEnabling custom TAB nametags&f...");
						TabAPI.getInstance().getFeatureManager().registerFeature("nametagx", new NameTagX(EGlow.getInstance()));
					}
						
					ChatUtil.sendToConsoleWithPrefix("&aAdvanced-TAB-Integration has been setup&f!");
				}
			}	
		} else {
			ChatUtil.sendToConsoleWithPrefix("&cWarning&f! &cThis version of eGlow required TAB 3.0.0 or higher!");
		}
	}
	
	public void updateTABPlayer(IEGlowPlayer ePlayer, ChatColor glowColor) {
		if (getTABPlayer(ePlayer.getUUID()) != null) {
			TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());
			
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
	}
	
	private void startGroupUpdateChecker() {
		if (!getGroupUpdateCheckerEnabled()) {
			setGroupUpdateCheckerEnabled(true);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if (getTABNametagPrefixSuffixEnabled() && getTABTeamPacketBlockingEnabled()) {
						for (IEGlowPlayer ePlayer : getInstance().getDataManager().getEGlowPlayers()) {
							TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());
							
							if (tabPlayer == null)
								continue;
							
							String playerGroup = tabPlayer.getGroup();
							
							if (getStoredGroups().containsKey(ePlayer.getPlayer()) && playerGroup.equals(getStoredGroups().get(ePlayer.getPlayer())))
								return;
								
							if (!getStoredGroups().containsKey(ePlayer.getPlayer())) {
								getStoredGroups().put(ePlayer.getPlayer(), playerGroup);
							} else {
								getStoredGroups().replace(ePlayer.getPlayer(), tabPlayer.getGroup());
							}
							
							updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						}
					}
				}
			}.runTaskTimerAsynchronously(getInstance(), 0L, 200L);
		}
	}
	
	//Getter
	private EGlow getInstance() {
		return this.instance;
	}
	
	public boolean getTABOnBukkit() {
		return this.TAB_Bukkit;
	}
	
	public boolean getTABNewVersion() {
		return this.TAB_New;
	}
	
	public boolean getTABNametagPrefixSuffixEnabled() {
		return this.TAB_NametagPrefixSuffixEnabled;
	}
	
	public boolean getTABTeamPacketBlockingEnabled() {
		return this.TAB_TeamPacketBlockingEnabled;
	}
	
	private boolean getGroupUpdateCheckerEnabled() {
		return this.groupUpdateCheckerEnabled;
	}
	
	public ConcurrentHashMap<Player, String> getStoredGroups() {
		return this.storedGroups;
	}
	
	//Setter
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}
	
	private void setTABOnBukkit(boolean status) {
		this.TAB_Bukkit = status;
	}
	
	private void setTABNewVersion(boolean status) {
		this.TAB_New = status;
	}
	
	private void setTABNametagPrefixSuffixEnabled(boolean status) {
		this.TAB_NametagPrefixSuffixEnabled = status;
	}
	
	private void setTABTeamPacketBlockingEnabled(boolean status) {
		this.TAB_TeamPacketBlockingEnabled = status;
	}
	
	private void setGroupUpdateCheckerEnabled(boolean status) {
		this.groupUpdateCheckerEnabled = status;
	}
}
