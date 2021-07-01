package me.MrGraycat.eGlow.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;

import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class DebugUtil {
	private final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private final int minorVersion =  Integer.parseInt(version.split("_")[1]);
	private PluginManager pm = Bukkit.getPluginManager();
	private boolean protocolSupport;
	private boolean viaVersion;
	
	public void sendDebug(CommandSender sender) {
		String plugins = " ";
		
		ChatUtil.sendMsg(sender, "&eServer version&f: " + version);
		ChatUtil.sendMsg(sender, "&eeGlow version&f: " + getVersion("eGlow"));	
		
		if (pluginCheck("TAB"))
			ChatUtil.sendMsg(sender, "&eTAB version&f: " + getVersion("TAB"));	
		
		if (pluginCheck("Citizens"))
			ChatUtil.sendMsg(sender, "&eCitizens version&f: " + getVersion("Citizens"));
		
		if (pluginCheck("PlaceholderAPI"))
			ChatUtil.sendMsg(sender, "&ePlaceholderAPI&f: " + getVersion("PlaceholderAPI"));
		
		if (pluginCheck("MVdWPlaceholderAPI"))
			ChatUtil.sendMsg(sender, "&eMVdWPlaceholderAPI&f: " + getVersion("MVdWPlaceholderAPI"));

		ChatUtil.sendMsg(sender, "&r");
		
		ChatUtil.sendMsg(sender, "Plugins:");
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.isEnabled()) {
				plugins = plugins + "&a" + plugin.getDescription().getName() + "&f, ";
			} else {
				plugins = plugins + "&c" + plugin.getDescription().getName() + "&f, ";
			}
		}
		
		sender.sendMessage(ChatUtil.translateColors(plugins.substring(0, plugins.length() - 2)));
	}
	
	public String getServerVersion() {
		return version;
	}
	
	public int getMinorVersion() {
		return minorVersion;
	}
	
	public boolean isProtocolSupportInstalled() {
		return protocolSupport;
	}
	
	public boolean isViaVersionInstalled() {
		return viaVersion;
	}
	
	public void addonCheck() {
		protocolSupport = pluginCheck("ProtocolSupport");
		viaVersion = pluginCheck("ViaVersion");
	}
	
	public boolean onBungee() {
		return (SpigotConfig.bungee && !Bukkit.getServer().getOnlineMode()) ? true : false;
	}
	
	public boolean pluginCheck(String plugin) {
		return (pm.getPlugin(plugin) != null && pm.getPlugin(plugin).isEnabled()) ? true : false;
	}
	
	public Plugin getPlugin(String plugin) {
		return pm.getPlugin(plugin);
	}
	
	private String getVersion(String plugin) {
		return pm.getPlugin(plugin).getDescription().getVersion();
	}
}