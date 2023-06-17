package me.mrgraycat.eglow.util.dependency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@Getter @AllArgsConstructor
public enum Dependency {

    VIA_VERSION("ViaVersion"),
    PROTOCOL_SUPPORT("ProtocolSupport"),
    TAB_BRIDGE("TAB-Bridge"),
    TAB("TAB"),
    PLACEHOLDER_API("PlaceholderAPI"),
    VAULT("Vault"),
    CITIZENS("Citizens"),
    I_DISGUISE("iDisguise"),
    LIBS_DISGUISES("LibsDisguises"),
    GSIT("GSit"),
    LUCK_PERMS("LuckPerms"),

    ;

    private final String pluginName;

    public boolean isLoaded() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        return plugin != null && plugin.isEnabled();
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }

    public static boolean isDefined(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception exc) {
            return false;
        }
    }
}
