package me.MrGraycat.eGlow.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class DebugUtil {
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final int minorVersion =  Integer.parseInt(version.split("_")[1]);
	private static PluginManager pm = Bukkit.getPluginManager();
	
	private static boolean placeholderapi;
	private static boolean protocolSupport;
	private static boolean viaVersion;
	
	public static void sendDebug(CommandSender sender, IEGlowPlayer ePlayer) {
		String plugins = " ";

		if (ePlayer != null) {
			ChatUtil.sendMsg(sender, "&fPlayer info (&e" + ePlayer.getDisplayName() + "&f)");
			ChatUtil.sendMsg(sender, "  &fTeamname: &e" + ePlayer.getTeamName());
			ChatUtil.sendMsg(sender, "  &fClient version: &e" + ePlayer.getVersion().getFriendlyName());
			ChatUtil.sendMsg(sender, "  &f");
			ChatUtil.sendMsg(sender, "  &fLast gloweffect: " + ePlayer.getLastGlowName());
			ChatUtil.sendMsg(sender, "  &fGlow visibility: &e" + ePlayer.getGlowVisibility().name());
			ChatUtil.sendMsg(sender, "  &fGlow on join: " + ((ePlayer.getGlowOnJoin()) ? "&aTrue" : "&cFalse"));
			ChatUtil.sendMsg(sender, "  &fForced glow: " + ((ePlayer.getForceGlow() == null) ? "&eNone" : ePlayer.getForceGlow().getName()));
			ChatUtil.sendMsg(sender, "  &fGlow blocked reason: &e" + ePlayer.getGlowDisableReason());
		}
		
		ChatUtil.sendMsg(sender, "&f&m                                                                               ");
		ChatUtil.sendMsg(sender, "&fServer version: &e" + version);
		ChatUtil.sendMsg(sender, "Plugins:");
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			String pluginName = plugin.getDescription().getName();
			
			if (plugin.isEnabled()) {
				String pluginText = (pluginName.equalsIgnoreCase("eGlow") || pluginName.equalsIgnoreCase("TAB") || pluginName.equalsIgnoreCase("PlaceholderAPI") || pluginName.equalsIgnoreCase("Citizens")) ? "&6" + pluginName + " &f(" + plugin.getDescription().getVersion() + "), " : "&a" + pluginName + "&f, ";
				
				plugins = plugins + pluginText;
			} else {
				plugins = plugins + "&c" + pluginName + "&f, ";
			}
		}
		
		
		sender.sendMessage(ChatUtil.translateColors(plugins.substring(0, plugins.length() - 2)));

		if (EGlow.getInstance().getTABAddon() != null && !EGlow.getInstance().getTABAddon().getTABLegacyVersion())
			sender.sendMessage(ChatUtil.translateColors("&cThis eGlow version requires a minimum TAB version of 3.1.0&f!"));
	}
	
	public static String getServerVersion() {
		return version;
	}
	
	public static int getMinorVersion() {
		return minorVersion;
	}
	
	public static boolean isProtocolSupportInstalled() {
		return protocolSupport;
	}
	
	public static boolean isViaVersionInstalled() {
		return viaVersion;
	}
	
	public static void addonCheck() {
		placeholderapi = pluginCheck("PlaceholderAPI");
		protocolSupport = pluginCheck("ProtocolSupport");
		viaVersion = pluginCheck("ViaVersion");
	}
	
	public static boolean onBungee() {
		return (SpigotConfig.bungee && !Bukkit.getServer().getOnlineMode()) ? true : false;
	}
	
	public static boolean pluginCheck(String plugin) {
		return (pm.getPlugin(plugin) != null && pm.getPlugin(plugin).isEnabled()) ? true : false;
	}
	
	public static Plugin getPlugin(String plugin) {
		return pm.getPlugin(plugin);
	}
	
	public static boolean TABInstalled() {
		return (pluginCheck("TAB") && getPlugin("TAB").getClass().getName().startsWith("me.neznamy.tab"));
	}
	
	public static boolean isPAPIInstalled() {
		return placeholderapi;
	}
}