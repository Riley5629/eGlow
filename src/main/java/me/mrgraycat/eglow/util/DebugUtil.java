package me.mrgraycat.eglow.util;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.Dependency;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import static me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig.ADVANCED_VELOCITY_MESSAGING;

public class DebugUtil {
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final int minorVersion = Integer.parseInt(version.split("_")[1]);
	private static final PluginManager pm = Bukkit.getPluginManager();

	public static void sendDebug(CommandSender sender, EGlowPlayer eGlowPlayer) {
		StringBuilder plugins = new StringBuilder(" ");

		if (eGlowPlayer != null) {
			ChatUtil.sendPlainMsg(sender, "&fPlayer info (&e" + eGlowPlayer.getDisplayName() + "&f)", false);
			ChatUtil.sendPlainMsg(sender, "  &fTeamname: &e" + eGlowPlayer.getTeamName(), false);
			ChatUtil.sendPlainMsg(sender, "  &fClient version: &e" + eGlowPlayer.getVersion().getFriendlyName(), false);
			ChatUtil.sendPlainMsg(sender, "  &f", false);
			ChatUtil.sendPlainMsg(sender, "  &fLast gloweffect: " + eGlowPlayer.getLastGlowName(), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow visibility: &e" + eGlowPlayer.getGlowVisibility().name(), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow on join: " + ((eGlowPlayer.isGlowOnJoin()) ? "&aTrue" : "&cFalse"), false);
			ChatUtil.sendPlainMsg(sender, "  &fForced glow: " + ((eGlowPlayer.getForcedEffect() == null) ? "&eNone" : eGlowPlayer.getForcedEffect().getName()), false);
			ChatUtil.sendPlainMsg(sender, "  &fGlow blocked reason: &e" + eGlowPlayer.getGlowDisableReason(), false);
		}

		ChatUtil.sendPlainMsg(sender, "&f&m                                                                               ", false);
		ChatUtil.sendPlainMsg(sender, "&fServer version: &e" + version, false);
		ChatUtil.sendPlainMsg(sender, "Plugins:", false);
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			String pluginName = plugin.getDescription().getName();

			if (plugin.isEnabled()) {
				String pluginText = (pluginName.equalsIgnoreCase("eGlow") || pluginName.equalsIgnoreCase("TAB")) ? "&6" + pluginName + " &f(" + plugin.getDescription().getVersion() + "), " : "&a" + pluginName + "&f, ";

				plugins.append(pluginText);
			} else {
				plugins.append("&c").append(pluginName).append("&f, ");
			}
		}


		ChatUtil.sendPlainMsg(sender, ChatUtil.translateColors(plugins.substring(0, plugins.length() - 2)), false);

		if (EGlow.getInstance().getTabAddon() != null && !EGlow.getInstance().getTabAddon().isVersionSupported() && Dependency.TAB.isLoaded())
			ChatUtil.sendPlainMsg(sender, ChatUtil.translateColors("&cYour TAB version seems incompatible with this eGlow version&f!"), false);
	}

	public static String getServerVersion() {
		return version;
	}

	public static int getMinorVersion() {
		return minorVersion;
	}

	public static boolean onBungee() {
		return !Bukkit.getServer().getOnlineMode() && NMSHook.isBungee();
	}

	public static boolean onVelocity(){
		return ADVANCED_VELOCITY_MESSAGING.getBoolean();
	}
}