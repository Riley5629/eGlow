package me.MrGraycat.eGlow.Util;

import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class DebugUtil {
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final int minorVersion =  Integer.parseInt(version.split("_")[1]);
	private static final PluginManager pm = Bukkit.getPluginManager();
	
	public static void sendDebug(CommandSender sender, IEGlowPlayer ePlayer) {
		StringBuilder plugins = new StringBuilder(" ");

		if (ePlayer != null) {
			ChatUtil.sendPlainMsg(sender, "&fPlayer info (&e" + ePlayer.getDisplayName() + "&f)", false);
			ChatUtil.sendPlainMsg(sender, "  &fTeamname: &e" + ePlayer.getTeamName(), false);
			ChatUtil.sendPlainMsg(sender, "  &fClient version: &e" + ePlayer.getVersion().getFriendlyName(), false);
			ChatUtil.sendPlainMsg(sender, "  &f", false);
			ChatUtil.sendPlainMsg(sender, "  &fLast gloweffect: " + ePlayer.getLastGlowName(), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow visibility: &e" + ePlayer.getGlowVisibility().name(), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow on join: " + ((ePlayer.getGlowOnJoin()) ? "&aTrue" : "&cFalse"), false);
			ChatUtil.sendPlainMsg(sender, "  &fForced glow: " + ((ePlayer.getForceGlow() == null) ? "&eNone" : ePlayer.getForceGlow().getName()), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow blocked reason: &e" + ePlayer.getGlowDisableReason(), false);
		}
		
		ChatUtil.sendPlainMsg(sender, "&f&m                                                                               ", false);
		ChatUtil.sendPlainMsg(sender, "&fServer version: &e" + version, false);
		ChatUtil.sendPlainMsg(sender, "Plugins:", false);
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			String pluginName = plugin.getDescription().getName();
			
			if (plugin.isEnabled()) {
				String pluginText = (pluginName.equalsIgnoreCase("eGlow") || pluginName.equalsIgnoreCase("TAB") || pluginName.equalsIgnoreCase("PlaceholderAPI") || pluginName.equalsIgnoreCase("Citizens")) ? "&6" + pluginName + " &f(" + plugin.getDescription().getVersion() + "), " : "&a" + pluginName + "&f, ";
				
				plugins.append(pluginText);
			} else {
				plugins.append("&c").append(pluginName).append("&f, ");
			}
		}
		
		
		ChatUtil.sendPlainMsg(sender, ChatUtil.translateColors(plugins.substring(0, plugins.length() - 2)), false);

		if (EGlow.getInstance().getTABAddon() != null && !EGlow.getInstance().getTABAddon().getTABSupported())
			ChatUtil.sendPlainMsg(sender, ChatUtil.translateColors("&cThis eGlow version requires a minimum TAB version of 3.1.0&f!"), false);
	}
	
	public static String getServerVersion() {
		return version;
	}
	
	public static int getMinorVersion() {
		return minorVersion;
	}
	
	public static boolean isProtocolSupportInstalled() {
		return pluginCheck("ProtocolSupport");
	}
	
	public static boolean isViaVersionInstalled() {
		return pluginCheck("ViaVersion");
	}

	public static boolean onBungee() {
		return !Bukkit.getServer().getOnlineMode() && NMSHook.isBungee();
	}
	
	public static boolean pluginCheck(String plugin) {
		return pm.getPlugin(plugin) != null && pm.getPlugin(plugin).isEnabled();
	}
	
	public static Plugin getPlugin(String plugin) {
		return pm.getPlugin(plugin);
	}
	
	public static boolean TABInstalled() {
		return (pluginCheck("TAB") && getPlugin("TAB").getClass().getName().startsWith("me.neznamy.tab"));
	}
	
	public static boolean isPAPIInstalled() {
		return pluginCheck("PlaceholderAPI");
	}
}