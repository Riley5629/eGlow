package me.MrGraycat.eGlow.Addon.TAB;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Addon.TAB.Listeners.EGlowTABListenerUniv;
import me.MrGraycat.eGlow.Addon.TAB.Listeners.EGlowTABNew;
import me.MrGraycat.eGlow.Addon.TAB.Listeners.EGlowTABOld;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

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
		
		if (getInstance().getDebugUtil().pluginCheck("TAB") && getInstance().getDebugUtil().getPlugin("TAB").getClass().getName().startsWith("me.neznamy.tab")) {
			setTABOnBukkit(true);
			loadConfigSettings();
			startGroupUpdateChecker();
			
			new EGlowTABListenerUniv(getInstance());
			if (getTABNewVersion()) {
				new EGlowTABNew(getInstance());
			} else {
				new EGlowTABOld(getInstance());
			}
		} else if (getInstance().getDebugUtil().onBungee()) {
			new EGlowTABListenerUniv(getInstance());
		}
	}
	
	public TabPlayer getTABPlayer(UUID uuid) {
		return (this.TAB_New) ? TabAPI.getInstance().getPlayer(uuid) : TABAPI.getPlayer(uuid);
	}
	
	public boolean blockEGlowPackets() {
		if (getTABNametagPrefixSuffixEnabled() && getTABTeamPacketBlockingEnabled())
			return true;
		return false;
	}
	
	public void loadConfigSettings() {
		String tabVersion = ((Plugin) getInstance().getDebugUtil().getPlugin("TAB")).getDescription().getVersion();
		
		if (Integer.valueOf(tabVersion.replaceAll("[^\\d]", "")) >= 300) {
			setTABNewVersion(true);
			setTABNametagPrefixSuffixEnabled(TabAPI.getInstance().getConfig().getBoolean("scoreboard-teams.enabled", false));
			setTABTeamPacketBlockingEnabled(TabAPI.getInstance().getConfig().getBoolean("scoreboard-teams.anti-override", false));
			
			if (EGlowMainConfig.OptionAdvancedTABIntegration() && TabAPI.getInstance().getFeatureManager().getFeature("nametagx") != null) {
				/*
				 * Old code: TABAPI.enableUnlimitedNameTagModePermanently();
				 * This isn't possible in TAB 3.0.0 yet.
				 */
			}
				
		} else {
			File tabConfigFile = new File("plugins/TAB/config.yml");
			YamlConfiguration tabConfig = YamlConfiguration.loadConfiguration(tabConfigFile);
			
			setTABNametagPrefixSuffixEnabled(tabConfig.getBoolean("change-nametag-prefix-suffix")); 
			setTABTeamPacketBlockingEnabled((tabConfig.contains("anti-override.scoreboard-teams")) ? tabConfig.getBoolean("anti-override.scoreboard-teams") : true);
		
			if (EGlowMainConfig.OptionAdvancedTABIntegration() && !TABAPI.isUnlimitedNameTagModeEnabled())
				TABAPI.enableUnlimitedNameTagModePermanently();
		}
	}
	
	public void updateTABPlayer(IEGlowPlayer ePlayer, ChatColor glowColor) {
		if (getTABPlayer(ePlayer.getUUID()) != null) {
			TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());
			
			String tagPrefix = "";
			String color = (glowColor.equals(ChatColor.RESET)) ? "" : glowColor + "";
			
			try {tagPrefix = tabPlayer.getOriginalValue(EnumProperty.TAGPREFIX);} catch(Exception ex) {tagPrefix = "";}
			
			try {
				if (getInstance().getTABAddon().getTABNewVersion() || !EGlowMainConfig.OptionAdvancedTABIntegration()) {
					tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, (!tagPrefix.isEmpty()) ? tagPrefix + color : color);
				} else {
					String customTagName = "";
					
					try {customTagName = tabPlayer.getOriginalValue(EnumProperty.CUSTOMTAGNAME);} catch(Exception ex) {customTagName = ePlayer.getPlayer().getName();}

					tabPlayer.setValueTemporarily(EnumProperty.CUSTOMTAGNAME, tagPrefix.replace("%eglow_glowcolor%", "") + customTagName);
					tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, color);
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
