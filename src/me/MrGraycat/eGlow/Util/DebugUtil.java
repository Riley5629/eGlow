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
	private final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private final int minorVersion =  Integer.parseInt(version.split("_")[1]);
	private PluginManager pm = Bukkit.getPluginManager();
	private boolean protocolSupport;
	private boolean viaVersion;
	
	public void sendDebug(CommandSender sender, IEGlowPlayer ePlayer) {
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

		if (EGlow.getInstance().getTABAddon() != null && !EGlow.getInstance().getTABAddon().getTABNewVersion())
			sender.sendMessage(ChatUtil.translateColors("&cThis eGlow version requires a minimum TAB version of 3.0.0&f!"));
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
}