package me.mrgraycat.eglow.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.util.dependency.Dependency;
import me.mrgraycat.eglow.util.packet.NMSHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

@UtilityClass
public class ServerUtil {

	@Getter private final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	@Getter private final int minorVersion = Integer.parseInt(version.split("_")[1]);

	private final PluginManager pluginManager = Bukkit.getPluginManager();

	public static boolean isBridgeEnabled() {
		return EGlow.getInstance().getTabAddon() == null && Dependency.TAB_BRIDGE.isLoaded();
	}

	public boolean onBungee() {
		return !Bukkit.getServer().getOnlineMode() && NMSHook.isBungee();
	}

	public boolean isLoaded(String plugin) {
		return pluginManager.getPlugin(plugin) != null &&
				Objects.requireNonNull(pluginManager.getPlugin(plugin)).isEnabled();
	}

	public static boolean isPAPIInstalled() {
		return isLoaded("PlaceholderAPI");
	}
}