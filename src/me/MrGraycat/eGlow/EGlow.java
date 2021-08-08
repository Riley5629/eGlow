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
import me.MrGraycat.eGlow.Addon.Citizens.CitizensAddon;
import me.MrGraycat.eGlow.Addon.Disguises.IDisguiseAddon;
import me.MrGraycat.eGlow.Addon.Disguises.LibDisguiseAddon;
import me.MrGraycat.eGlow.Addon.Placeholders.EGlowPlaceholderAPI;
import me.MrGraycat.eGlow.Addon.TAB.EGlowTAB;
import me.MrGraycat.eGlow.Addon.TAB.EGlowTABEvents;
import me.MrGraycat.eGlow.Command.EGlowCommand;
import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig;
import me.MrGraycat.eGlow.Config.EGlowCustomMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.Event.EGlowEventListener;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.PipelineInjector;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlow extends JavaPlugin {
	private static EGlow instance;
	private static EGlowAPI API;
	private boolean UP_TO_DATE = true;
	
	//Configs
	private EGlowMainConfig mainConfig;
	private EGlowMessageConfig messageConfig;
	private EGlowPlayerdataManager playerdataConfig;
	private EGlowCustomMainConfig customMainConfig;
	private EGlowCustomEffectsConfig effectsConfig;
	
	//Events
	private EGlowEventListener eventListener;
	
	//Managers
	private DataManager dataManager;
	
	//Packets
	private NMSHook nmsHook;
	private PacketUtil packetUtil;
	private PipelineInjector pipelineInjector;
	
	//chat/text
	private DebugUtil debugUtil;
	
	//Addons
	private CitizensAddon citizensAddon;
	private IDisguiseAddon iDisguiseAddon;
	private LibDisguiseAddon libDisguiseAddon;
	private EGlowTAB tabAddon;
	private VaultAddon vaultAddon;
	
	@Override
	public void onEnable() {
		setInstance(this);
		setAPI(new EGlowAPI(this));
		setDebugUtil(new DebugUtil());
		
		if (versionIsCompactible()) {
			ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
			
			setMainConfig(new EGlowMainConfig(getInstance()));
			setMessageConfig(new EGlowMessageConfig(getInstance()));
			//TODO customMainConfig = new EGlowCustomMainConfig();
			setCustomEffectConfig(new EGlowCustomEffectsConfig(getInstance()));
			
			setPlayerdataManager(new EGlowPlayerdataManager(getInstance()));
			setDataManager(new DataManager(getInstance()));
			
			setNMSHook(new NMSHook());
			setPacketUtil(new PacketUtil(getInstance()));
			setPipelineInjector(new PipelineInjector(getInstance()));
			
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
		instance = null;
	}
	
	private boolean versionIsCompactible() {
		if (getDebugUtil().getServerVersion().equals("v_1_9_R1") || getDebugUtil().getMinorVersion() < 9 || getDebugUtil().getMinorVersion() > 17)
			return false;
		return true;
	}
	
	private void registerEventsAndCommands() {
		getCommand("eglow").setExecutor(new EGlowCommand(getInstance()));
		setEventListener(new EGlowEventListener(getInstance()));
	}

	private void runAddonHooks() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (getDebugUtil().pluginCheck("Vault"))
					setVaultAddon(new VaultAddon(getInstance()));
				if (getDebugUtil().pluginCheck("PlaceholderAPI"))
					new EGlowPlaceholderAPI(getInstance());
				if (getDebugUtil().pluginCheck("Citizens") && citizensAddon == null)
					setCitizensAddon(citizensAddon = new CitizensAddon());
				if (getDebugUtil().pluginCheck("iDisguise"))
					setIDisguiseAddon(new IDisguiseAddon(getInstance()));
				if (getDebugUtil().pluginCheck("LibsDisguises"))
					setLibDisguiseAddon(new LibDisguiseAddon(getInstance()));
				if (getDebugUtil().pluginCheck("TAB") && tabAddon == null) {
					setTABAddon(new EGlowTAB(getInstance()));
				} else {
					getServer().getPluginManager().registerEvents(new EGlowTABEvents(getInstance()), getInstance());
				}
					
				getDebugUtil().addonCheck();
			}
		}.runTask(this);
	}
	
	private void runPlayerCheckOnEnable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (getDataManager().getEGlowPlayer(player) == null)
					getEventListener().PlayerConnect(player, player.getUniqueId());
			}
		}
	}
	
	private void runPlayerCheckOnDisable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (getDataManager().getEGlowPlayer(player) == null)
					getEventListener().PlayerDisconnect(player, true);
			}
		}
	}
	
	private void checkForUpdates() {
		  try { 			
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=63295");
			String currentVersion = getInstance().getDescription().getVersion();
			String latestVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();
			
			if (currentVersion.contains("PRE")) {
				String betaVersion = currentVersion.split("-")[0];
				setUpToDate((betaVersion.equals(latestVersion)) ? false : true);
			} else {
				if (!latestVersion.contains(currentVersion)) {
					setUpToDate(false);
				}
			}		
	    } catch (Exception e) {}
	}
	
	//Setter
	private static void setInstance(EGlow instance) {
		EGlow.instance = instance;
	}
	
	private void setAPI(EGlowAPI api) {
		EGlow.API = api;
	}
	
	private void setUpToDate(boolean up_to_date) {
		this.UP_TO_DATE = up_to_date;
	}
	
	private void setMainConfig(EGlowMainConfig mainConfig) {
		this.mainConfig = mainConfig;
	}
	
	private void setMessageConfig(EGlowMessageConfig messageConfig) {
		this.messageConfig = messageConfig;
	}
	
	@SuppressWarnings("unused")
	private void setCustomGUIConfig(EGlowCustomMainConfig customMainConfig) {
		this.customMainConfig = customMainConfig;
	}
	
	private void setCustomEffectConfig(EGlowCustomEffectsConfig effectsConfig) {
		this.effectsConfig = effectsConfig;
	}
	
	private void setEventListener(EGlowEventListener eventListener) {
		this.eventListener = eventListener;
	}
	
	private void setPlayerdataManager(EGlowPlayerdataManager playerdataConfig) {
		this.playerdataConfig = playerdataConfig;
	}
	
	private void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	private void setNMSHook(NMSHook nmsHook) {
		this.nmsHook = nmsHook;
	}
	
	private void setPacketUtil(PacketUtil packetUtil) {
		this.packetUtil = packetUtil;
	}
	
	private void setPipelineInjector(PipelineInjector pipelineInjector) {
		this.pipelineInjector = pipelineInjector;
	}
	
	private void setDebugUtil(DebugUtil debugUtil) {
		this.debugUtil = debugUtil;
	}
	
	private void setCitizensAddon(CitizensAddon citizensAddon) {
		this.citizensAddon = citizensAddon;
	}
	
	private void setIDisguiseAddon(IDisguiseAddon iDisguiseAddon) {
		this.iDisguiseAddon = iDisguiseAddon;
	}
	
	private void setLibDisguiseAddon(LibDisguiseAddon libDisguiseAddon) {
		this.libDisguiseAddon = libDisguiseAddon;
	}
	
	private void setTABAddon(EGlowTAB tabAddon) {
		this.tabAddon = tabAddon;
	}
	
	private void setVaultAddon(VaultAddon vaultAddon) {
		this.vaultAddon = vaultAddon;
	}
	
	//Getter
	public static  EGlow getInstance() {
		return EGlow.instance;
	}
	
	public static EGlowAPI getAPI() {
		return API;
	}
	
	public boolean isUpToDate() {
		return UP_TO_DATE;
	}
	
	public EGlowMainConfig getMainConfig() {
		return this.mainConfig;
	}
	
	public EGlowMessageConfig getMessageConfig() {
		return this.messageConfig;
	}
	
	public EGlowCustomMainConfig getCustomGUIConfig() {
		return this.customMainConfig;
	}
	
	public EGlowCustomEffectsConfig getCustomEffectConfig() {
		return this.effectsConfig;
	}
	
	public EGlowEventListener getEventListener() {
		return this.eventListener;
	}
	
	public EGlowPlayerdataManager getPlayerdataManager() {
		return this.playerdataConfig;
	}
	
	public DataManager getDataManager() {
		return this.dataManager;
	}
	
	public NMSHook getNMSHook() {
		return this.nmsHook;
	}
	
	public PacketUtil getPacketUtil() {
		return this.packetUtil;
	}
	
	public PipelineInjector getPipelineInjector() {
		return this.pipelineInjector;
	}
	
	public DebugUtil getDebugUtil() {
		return this.debugUtil;
	}
	
	public CitizensAddon getCitizensAddon() {
		return this.citizensAddon;
	}
	
	public IDisguiseAddon getIDisguiseAddon() {
		return this.iDisguiseAddon;
	}
	
	public LibDisguiseAddon getLibDisguiseAddon() {
		return this.libDisguiseAddon;
	}
	
	public EGlowTAB getTABAddon() {
		return this.tabAddon;
	}
	
	public VaultAddon getVaultAddon() {
		return this.vaultAddon;
	}
}