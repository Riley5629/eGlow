package me.MrGraycat.eGlow.Addon.TAB;

import lombok.Getter;
import lombok.Setter;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TABAddon {
	@Getter
	@Setter
	private boolean versionSupported;
	@Getter
	@Setter
	private boolean settingNametagPrefixSuffixEnabled;
	@Getter
	@Setter
	private boolean settingTeamPacketBlockingEnabled;
	@Getter
	@Setter
	private YamlConfiguration tabConfig;

	public TABAddon(Plugin TAB_Plugin) {
		int TAB_Version = (TAB_Plugin != null) ? Integer.parseInt(TAB_Plugin.getDescription().getVersion().replaceAll("[^\\d]", "")) : 0;

		if (TAB_Version < 400) {
			ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires a higher TAB version&f!", true);
			return;
		}

		loadTABSettings();

		TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, event -> new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon tabAddon = EGlow.getInstance().getTABAddon();
					tabAddon.loadTABSettings();

					if (tabAddon.blockEGlowPackets()) {
						for (IEGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
							if (ePlayer.isGlowing())
								tabAddon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						}
					} else {
						cancel();
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance()));

		setVersionSupported(true);
	}

	public void loadTABSettings() {
		File configFile = new File(TAB.getInstance().getDataFolder(), "config.yml");

		if (TAB.getInstance().getDataFolder().exists() && configFile.exists()) {
			try {
				setTabConfig(new YamlConfiguration());
				getTabConfig().load(configFile);
			} catch (Exception ignored) {
			}
		}

		setSettingNametagPrefixSuffixEnabled(getTabConfig().getBoolean("scoreboard-teams.enabled", false));
		setSettingTeamPacketBlockingEnabled(getTabConfig().getBoolean("scoreboard-teams.anti-override", false));

		if (MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
			if (!getTabConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
				try {
					getTabConfig().set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
					getTabConfig().save(configFile);

					ChatUtil.sendToConsole("&6Enabled unlimited-nametag-mode in TAB&f!", true);
					ChatUtil.sendToConsole("&6TAB reload triggered by eGlow&f!", true);

					new BukkitRunnable() {
						@Override
						public void run() {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload");
						}
					}.runTaskLater(EGlow.getInstance(), 10L);
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public void updateTABPlayer(IEGlowPlayer ePlayer, ChatColor glowColor) {
		TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());

		if (tabPlayer == null || TabAPI.getInstance().getNameTagManager() == null)
			return;

		String tagPrefix;
		String color = (glowColor.equals(ChatColor.RESET)) ? "" : String.valueOf(glowColor);

		try {
			tagPrefix = TabAPI.getInstance().getNameTagManager().getOriginalPrefix(tabPlayer);
		} catch (Exception ignored) {
			tagPrefix = color;
		}

		try {
			if (!MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
				TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, tagPrefix + color);
			} else {
				UnlimitedNameTagManager unlimitedNameTagManager;

				try {
					unlimitedNameTagManager = (UnlimitedNameTagManager) TabAPI.getInstance().getNameTagManager();
				} catch (ClassCastException exception) {
					TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, tagPrefix + color);
					ChatUtil.sendToConsole("&cTAB's unlimited nametagmode feature isn't loaded properly&f!", true);
					ChatUtil.sendToConsole("&cCheck if it's enabled and restart the server&f!", true);
					return;
				}

				if (unlimitedNameTagManager == null) {
					TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, tagPrefix + color);
				} else {
					String originalTagName = unlimitedNameTagManager.getOriginalName(tabPlayer);

					unlimitedNameTagManager.setName(tabPlayer, tagPrefix + originalTagName);

					TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, color);
				}
			}
		} catch (IllegalStateException | NullPointerException ignored) {
			//Wierd NPE on first join ignoring it
		}
	}

	private TabPlayer getTABPlayer(UUID uuid) {
		return TabAPI.getInstance().getPlayer(uuid);
	}

	public boolean blockEGlowPackets() {
		return (isSettingNametagPrefixSuffixEnabled() && isSettingTeamPacketBlockingEnabled());
	}
}