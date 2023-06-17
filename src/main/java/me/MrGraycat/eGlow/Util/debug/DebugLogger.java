package me.mrgraycat.eglow.util.debug;

import lombok.experimental.UtilityClass;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.ServerUtil;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

@UtilityClass
public class DebugLogger {

    public void sendDebug(CommandSender sender, IEGlowPlayer ePlayer) {
        StringBuilder plugins = new StringBuilder(" ");

        if (ePlayer != null) {
            ChatUtil.sendPlainMessage(sender, "&fPlayer info (&e" + ePlayer.getDisplayName() + "&f)", false);
            ChatUtil.sendPlainMessage(sender, "  &fTeamname: &e" + ePlayer.getTeamName(), false);
            ChatUtil.sendPlainMessage(sender, "  &fClient version: &e" + ePlayer.getVersion().getFriendlyName(), false);
            ChatUtil.sendPlainMessage(sender, "  &f", false);
            ChatUtil.sendPlainMessage(sender, "  &fLast gloweffect: " + ePlayer.getLastGlowName(), false);
            ChatUtil.sendPlainMessage(sender, "  &fGlow visibility: &e" + ePlayer.getGlowVisibility().name(), false);
            ChatUtil.sendPlainMessage(sender, "  &fGlow on join: " + ((ePlayer.isGlowOnJoin()) ? "&aTrue" : "&cFalse"), false);
            ChatUtil.sendPlainMessage(sender, "  &fForced glow: " + ((ePlayer.getForceGlow() == null) ? "&eNone" : ePlayer.getForceGlow().getName()), false);
            ChatUtil.sendPlainMessage(sender, "  &fGlow blocked reason: &e" + ePlayer.getGlowDisableReason(), false);
        }

        ChatUtil.sendPlainMessage(sender, "&f&m                                                                               ", false);
        ChatUtil.sendPlainMessage(sender, "&fServer version: &e" + ServerUtil.getVersion(), false);
        ChatUtil.sendPlainMessage(sender, "Plugins:", false);

        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(plugin -> {
            String pluginName = plugin.getDescription().getName();

            if (plugin.isEnabled()) {
                plugins.append((pluginName.equalsIgnoreCase("eGlow") || pluginName.equalsIgnoreCase("TAB"))
                        ? "&6" + pluginName + " &f(" + plugin.getDescription().getVersion() + "), " : "&a" + pluginName + "&f, ");
            } else {
                plugins.append("&c").append(pluginName).append("&f, ");
            }
        });

        ChatUtil.sendPlainMessage(sender, ChatUtil.translateColors(plugins.substring(0, plugins.length() - 2)), false);

        if (EGlow.getInstance().getTabAddon() != null && !EGlow.getInstance().getTabAddon().isVersionSupported()) {
            ChatUtil.sendPlainMessage(sender, ChatUtil.translateColors("&cThis eGlow version requires a minimum TAB version of 3.1.0&f!"), false);
        }
    }
}
