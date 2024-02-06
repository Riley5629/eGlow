package me.mrgraycat.eglow.addon.tab;

import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.AbstractAddonBase;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.enums.Dependency;
import me.mrgraycat.eglow.util.text.ChatUtil;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TABAddon extends AbstractAddonBase {
	private final boolean versionSupported;
	private boolean settingNametagPrefixSuffixEnabled = false;
	private boolean settingTeamPacketBlockingEnabled = false;
	private ConfigurationFile tabConfig;

	public TABAddon(EGlow eGlowInstance) {
		super(eGlowInstance);

		Plugin tabPlugin = Dependency.TAB.getPlugin();

		if (tabPlugin == null) {
			this.versionSupported = false;

			if (DebugUtil.onBungee() || DebugUtil.onVelocity())
				getEGlowInstance().getServer().getPluginManager().registerEvents(new TABAddonEvents(), getEGlowInstance());
			return;
		}

		if (!tabPlugin.getClass().getName().startsWith("me.neznamy.tab")) {
			this.versionSupported = false;
			ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires a higher TAB version&f!", true);
			return;
		}

		int tabVersion = Integer.parseInt(tabPlugin.getDescription().getVersion().replaceAll("[^\\d]", ""));

		this.versionSupported = tabVersion >= 400;

		if (!isVersionSupported()) {
			ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires a higher TAB version&f!", true);
			return;
		}

		loadTABSettings();

		getEGlowInstance().getServer().getPluginManager().registerEvents(new TABAddonEvents(), getEGlowInstance());
		Objects.requireNonNull(TabAPI.getInstance().getEventBus()).register(TabLoadEvent.class, event -> new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon tabAddon = EGlow.getInstance().getTabAddon();
					tabAddon.loadTABSettings();

					if (tabAddon.blockEGlowPackets()) {
						for (EGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
							if (ePlayer.isGlowing()) {
								tabAddon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
							}
						}
					} else {
						cancel();
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}.runTaskAsynchronously(getEGlowInstance()));
	}

	public void loadTABSettings() {
		this.tabConfig = TAB.getInstance().getConfiguration().getConfig();
		//this.tabConfig = TAB.getInstance().getConfig();

		this.settingNametagPrefixSuffixEnabled = getTabConfig().getBoolean("scoreboard-teams.enabled", false);
		this.settingTeamPacketBlockingEnabled = getTabConfig().getBoolean("scoreboard-teams.anti-override", false);

		if (!MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean())
			return;

		if (!getTabConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
			getTabConfig().set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
			getTabConfig().save();

			ChatUtil.sendToConsole("&6Enabled unlimited-nametag-mode in TAB&f!", true);
			ChatUtil.sendToConsole("&6TAB reload triggered by eGlow&f!", true);

			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload");
				}
			}.runTaskLater(EGlow.getInstance(), 10L);
		}
	}

	public void updateTABPlayer(EGlowPlayer eGlowPlayer, ChatColor glowColor) {
		if (!isVersionSupported() || !blockEGlowPackets() || MainConfig.ADVANCED_FORCE_DISABLE_TAB_INTEGRATION.getBoolean())
			return;

		TabPlayer tabPlayer = getTABPlayer(eGlowPlayer.getUuid());

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

	public void requestTABPlayerUpdate(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon tabAddon = getEGlowInstance().getTabAddon();
					EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

					if (eGlowPlayer == null)
						return;

					if (tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
						tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
					} else if (DebugUtil.onBungee() || DebugUtil.onVelocity()) {
						DataManager.TABProxyUpdateRequest(player, String.valueOf(eGlowPlayer.getActiveColor()));
					}
				} catch (ConcurrentModificationException ignored) {
					//Caused by updating to fast
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance());
	}

	private TabPlayer getTABPlayer(UUID uuid) {
		return TabAPI.getInstance().getPlayer(uuid);
	}

	public boolean blockEGlowPackets() {
		return (isSettingNametagPrefixSuffixEnabled() && isSettingTeamPacketBlockingEnabled());
	}
}