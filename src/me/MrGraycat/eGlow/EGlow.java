package me.MrGraycat.eGlow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.Addon.VaultAddon;
import me.MrGraycat.eGlow.Addon.Disguises.*;
import me.MrGraycat.eGlow.Addon.NPCs.Citizens.CitizensAddon;
import me.MrGraycat.eGlow.Addon.Placeholders.*;
import me.MrGraycat.eGlow.Addon.TAB.EGlowTAB;
import me.MrGraycat.eGlow.Addon.TAB.EGlowTABEvents;
import me.MrGraycat.eGlow.Command.EGlowCommand;
import me.MrGraycat.eGlow.Config.*;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.Event.*;
import me.MrGraycat.eGlow.Manager.*;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlow extends JavaPlugin {
	private static EGlow INSTANCE;
	private static EGlowAPI API;
	private boolean UP_TO_DATE = true;
	
	//Configs
	private static EGlowMainConfig mainConfig; 
	private static EGlowMessageConfig messageConfig;
	private static EGlowPlayerdataManager playerdataConfig;
	private static EGlowCustomEffectsConfig effectsConfig;
	
	//Managers
	private static DataManager dataManager;
	
	//Utils
	
	//chat/text
	private static DebugUtil debugUtil;
	
	//Addons
	private static CitizensAddon citizensAddon;
	private static IDisguiseAddon iDisguiseAddon;
	private static LibDisguiseAddon libDisguiseAddon;
	private static EGlowTAB tabAddon;
	private static VaultAddon vaultAddon;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		API = new EGlowAPI();
		debugUtil = new DebugUtil();
		
		if (versionIsCompactible()) {
			ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
			
			mainConfig = new EGlowMainConfig();
			messageConfig = new EGlowMessageConfig();
			effectsConfig = new EGlowCustomEffectsConfig();
			playerdataConfig = new EGlowPlayerdataManager();
			dataManager = new DataManager();
			NMSHook.onEnable();
			
			registerEventsAndCommands();
			checkForUpdates();
			runAddonHooks();
			runPlayerCheckOnEnable();
		} else {
			ChatUtil.sendToConsole("Disabling eGlow! Your server version is not compactible! (" + getDebugUtil().getServerVersion() + ")");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public void onDisable() {
		runPlayerCheckOnDisable();
		getServer().getServicesManager().unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
		API = null;
		INSTANCE = null;
	}
	
	private boolean versionIsCompactible() {
		if (getDebugUtil().getServerVersion().equals("v_1_9_R1") || getDebugUtil().getMinorVersion() < 9 || getDebugUtil().getMinorVersion() > 17)
			return false;
		return true;
	}
	
	private void registerEventsAndCommands() {
		getCommand("eglow").setExecutor(new EGlowCommand());
		getServer().getPluginManager().registerEvents(new EGlowEventListener(), this);
		if (getDebugUtil().getMinorVersion() >= 13)
			getServer().getPluginManager().registerEvents(new EGlowEventListener113AndAbove(), this);
	}
	
	private void runAddonHooks() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (getDebugUtil().pluginCheck("Vault"))
					vaultAddon = new VaultAddon();
				if (getDebugUtil().pluginCheck("PlaceholderAPI"))
					new EGlowPlaceholderAPI();
				if (getDebugUtil().pluginCheck("Citizens") && citizensAddon == null)
					citizensAddon = new CitizensAddon();
				if (getDebugUtil().pluginCheck("iDisguise"))
					iDisguiseAddon = new IDisguiseAddon();
				if (getDebugUtil().pluginCheck("LibsDisguises"))
					libDisguiseAddon = new LibDisguiseAddon();
				if (getDebugUtil().pluginCheck("TAB") && tabAddon == null) {
					tabAddon = new EGlowTAB();
				} else {
					getServer().getPluginManager().registerEvents(new EGlowTABEvents(), getInstance());
				}
					
				getDebugUtil().addonCheck();
			}
		}.runTask(this);
	}
	
	private void runPlayerCheckOnEnable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (EGlow.getDataManager().getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerConnect(player, player.getUniqueId());
			}
		}
	}
	
	private void runPlayerCheckOnDisable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (EGlow.getDataManager().getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerDisconnect(player);
			}
		}
	}
	
	public static EGlow getInstance() {
		return INSTANCE;
	}
	
	public static EGlowAPI getAPI() {
		return API;
	}
	
	public boolean isUpToDate() {
		return UP_TO_DATE;
	}
	
	public static EGlowMainConfig getMainConfig() {
		return mainConfig;
	}
	
	public static EGlowMessageConfig getMessageConfig() {
		return messageConfig;
	}
	
	public static EGlowCustomEffectsConfig getCustomEffectConfig() {
		return effectsConfig;
	}
	
	public static EGlowPlayerdataManager getPlayerdataManager() {
		return playerdataConfig;
	}
	
	public static DataManager getDataManager() {
		return dataManager;
	}
	
	public static DebugUtil getDebugUtil() {
		return debugUtil;
	}
	
	public static CitizensAddon getCitizensAddon() {
		return citizensAddon;
	}
	
	public static IDisguiseAddon getIDisguiseAddon() {
		return iDisguiseAddon;
	}
	
	public static LibDisguiseAddon getLibDisguiseAddon() {
		return libDisguiseAddon;
	}
	
	public static EGlowTAB getTABAddon() {
		return tabAddon;
	}
	
	public static VaultAddon getVaultAddon() {
		return vaultAddon;
	}
	
	private void checkForUpdates() {
		  try { 			
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=63295");
			String currentVersion = INSTANCE.getDescription().getVersion();
			String latestVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();
			
			if (currentVersion.contains("PRE")) {
				String betaVersion = currentVersion.split("-")[0];
				UP_TO_DATE =  (betaVersion.equals(latestVersion)) ? false : true;
			} else {
				if (!latestVersion.contains(currentVersion)) {
					UP_TO_DATE = false;
				}
			}		
	    } catch (Exception e) {}
	}
}