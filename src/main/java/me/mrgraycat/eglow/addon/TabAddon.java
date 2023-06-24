package me.mrgraycat.eglow.addon;

import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.ServerUtil;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.util.dependency.Dependency;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TabAddon extends GlowAddon {

	private final boolean versionSupported;
	private final int minVersion = 400;

	private boolean tagPrefixSuffixEnabled;
	private boolean teamPacketBlocking;

	private YamlConfiguration tabConfig;

	public TabAddon(EGlow instance) {
		super(instance);

		Plugin tabPlugin = Dependency.TAB.getPlugin();

		int tabVersion = (tabPlugin != null) ?
				Integer.parseInt(tabPlugin.getDescription().getVersion().replaceAll("[^\\d]", "")) : 0;

		this.versionSupported = tabVersion >= getMinVersion();

		if (tabVersion < getMinVersion()) {
			ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires a higher TAB version&f!", true);
			return;
		}

		loadTabSettings();

		Objects.requireNonNull(TabAPI.getInstance().getEventBus()).register(TabLoadEvent.class, event -> new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TabAddon tabAddon = EGlow.getInstance().getTabAddon();
					tabAddon.loadTabSettings();

					if (tabAddon.blockEGlowPackets()) {
						for (IEGlowPlayer ePlayer : DataManager.getGlowPlayers()) {
							if (ePlayer.isGlowing())
								tabAddon.updateTabPlayer(ePlayer, ePlayer.getActiveColor());
						}
					} else {
						cancel();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance()));
	}

	public void loadTabSettings() {
		File configFile = new File(TAB.getInstance().getDataFolder(), "config.yml");

		if (TAB.getInstance().getDataFolder().exists() && configFile.exists()) {
			try {
				tabConfig = new YamlConfiguration();
				getTabConfig().load(configFile);
			} catch (IOException | InvalidConfigurationException exc) {
				exc.printStackTrace();
			}
		}

		tagPrefixSuffixEnabled = getTabConfig().getBoolean("scoreboard-teams.enabled", false);
		teamPacketBlocking = getTabConfig().getBoolean("scoreboard-teams.anti-override", false);

		if (!MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
			return;
		}

		if (!getTabConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
			try {
				getTabConfig().set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
				getTabConfig().save(configFile);

				ChatUtil.sendToConsole("&6Enabled unlimited-nametag-mode in TAB&f!", true);
				ChatUtil.sendToConsole("&6TAB reload triggered by eGlow&f!", true);

				Bukkit.getScheduler().runTaskLater(EGlow.getInstance(), () -> {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload");
				}, 10L);
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}

	public void updateTabPlayer(IEGlowPlayer ePlayer, ChatColor glowColor) {
		TabPlayer tabPlayer = getTABPlayer(ePlayer.getUuid());

		if (tabPlayer == null || TabAPI.getInstance().getNameTagManager() == null) {
			return;
		}

		String tagPrefix;
		String color = (glowColor.equals(ChatColor.RESET)) ? "" : glowColor.toString();

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

	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		requestTabPlayerUpdate(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		runTaskLaterAsynchronously(() -> {
			requestTabPlayerUpdate(player);
		}, 5L);
	}

	private void requestTabPlayerUpdate(Player player) {
		runAsync(() -> {
			try {
				TabAddon tabAddon = instance.getTabAddon();
				IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

				if (eGlowPlayer == null)
					return;

				if (tabAddon != null && tabAddon.blockEGlowPackets()) {
					tabAddon.updateTabPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
				} else if (ServerUtil.onBungee()) {
					DataManager.proxyTabUpdateRequest(player, String.valueOf(eGlowPlayer.getActiveColor()));
				}
			} catch (ConcurrentModificationException ignored) {
				//Caused by updating to fast
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});
	}

	private TabPlayer getTABPlayer(UUID uuid) {
		return TabAPI.getInstance().getPlayer(uuid);
	}

	public boolean blockEGlowPackets() {
		return isTagPrefixSuffixEnabled() && isTeamPacketBlocking();
	}
}