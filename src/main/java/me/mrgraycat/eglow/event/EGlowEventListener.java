package me.mrgraycat.eglow.event;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.database.EGlowPlayerdataManager;
import me.mrgraycat.eglow.gui.Menu;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EGlowEventListener implements Listener {
	public EGlowEventListener() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13)
			new EGlowEventListener113AndAbove();
	}

	@EventHandler
	public void PlayerConnectEvent(PlayerJoinEvent event) {
		PlayerConnect(event.getPlayer(), event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void PlayerKickedEvent(PlayerKickEvent event) {
		PlayerDisconnect(event.getPlayer());
	}

	@EventHandler
	public void PlayerDisconnectEvent(PlayerQuitEvent event) {
		PlayerDisconnect(event.getPlayer());
	}

	@EventHandler
	public void onMenuClick(InventoryClickEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();

		if (holder == null)
			return;

		if (holder instanceof Menu) {
			event.setCancelled(true);

			if (event.getView().getBottomInventory().equals(event.getClickedInventory()) || event.getCurrentItem() == null)
				return;

			Menu menu = (Menu) holder;
			menu.handleMenu(event);
		}
	}

	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (eGlowPlayer != null) {
			if (eGlowPlayer.isInBlockedWorld()) {
				if (eGlowPlayer.isGlowing()) {
					eGlowPlayer.disableGlow(false);
					eGlowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
					ChatUtil.sendMsg(player, Message.WORLD_BLOCKED.get(), true);
				}
			} else {
				if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
					if (eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE).equals(GlowDisableReason.NONE)) {
						eGlowPlayer.activateGlow();
						ChatUtil.sendMsg(player, Message.WORLD_ALLOWED.get(), true);
					}
				}
			}
		}
	}

	/**
	 * Code to initialise the player
	 *
	 * @param player player to initialise
	 */
	public static void PlayerConnect(Player player, UUID uuid) {
		//Fixes permanent player glows from old eGlow versions/other glow plugins that use Player#setGlowing(true)
		if (player.isGlowing())
			player.setGlowing(false);

		EGlowPlayer eGlowPlayer = DataManager.addEGlowPlayer(player, uuid.toString());
		PacketUtil.handlePlayerJoin(eGlowPlayer);

		new BukkitRunnable() {
			@Override
			public void run() {
				EGlowPlayerdataManager.loadPlayerdata(eGlowPlayer);
				eGlowPlayer.updatePlayerTabname();

				if (!EGlow.getInstance().isUpToDate() && MainConfig.SETTINGS_NOTIFICATIONS_UPDATE.getBoolean() && player.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(player, "&aA new update is available&f!", true);

				if (EGlowPlayerdataManager.getMySQL_Failed() && player.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(player, "&cMySQL failed to enable properly, have a look at this asap&f.", true);

				if (!eGlowPlayer.isGlowOnJoin() || !player.hasPermission("eglow.option.glowonjoin")) {
					sendNoGlowMessage(eGlowPlayer);
					return;
				} else {
					if (!eGlowPlayer.isActiveOnQuit() && eGlowPlayer.hasNoForceGlow()) {
						sendNoGlowMessage(eGlowPlayer);
						return;
					}
				}

				if (MainConfig.SETTINGS_JOIN_CHECK_PERMISSION.getBoolean() && !player.hasPermission(eGlowPlayer.getGlowEffect().getPermissionNode())) {
					sendNoGlowMessage(eGlowPlayer);
					return;
				}

				GlowDisableReason glowDisableReason = eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE);

				switch (glowDisableReason) {
					case BLOCKEDWORLD:
						ChatUtil.sendMsg(player, Message.WORLD_BLOCKED.get(), true);
						break;
					case INVISIBLE:
						ChatUtil.sendMsg(player, Message.INVISIBILITY_BLOCKED.get(), true);
						break;
					case ANIMATION:
						ChatUtil.sendMsg(player, Message.ANIMATION_BLOCKED.get(), true);
						break;
					default:
						if (eGlowPlayer.getGlowEffect() != null) {
							eGlowPlayer.activateGlow();

							if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && player.hasPermission("eglow.option.glowstate"))
								ChatUtil.sendMsg(player, Message.GLOWING_STATE_ON_JOIN.get(eGlowPlayer.getGlowEffect().getDisplayName()), true);
						} else {
							sendNoGlowMessage(eGlowPlayer);
						}
						break;
				}
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 2L);
	}

	/**
	 * Code to unload the player from eGlow
	 *
	 * @param player player to unload
	 */
	public static void PlayerDisconnect(Player player) {
		EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (eGlowPlayer == null)
			return;

		PacketUtil.handlePlayerQuit(eGlowPlayer);

		new BukkitRunnable() {
			@Override
			public void run() {
				eGlowPlayer.setActiveOnQuit(eGlowPlayer.isGlowing());
				EGlowPlayerdataManager.savePlayerdata(eGlowPlayer);

				if (EGlow.getInstance().getAdvancedGlowVisibilityAddon() != null)
					EGlow.getInstance().getAdvancedGlowVisibilityAddon().uncachePlayer(eGlowPlayer.getUuid());
			}
		}.runTaskAsynchronously(EGlow.getInstance());

		DataManager.removeEGlowPlayer(eGlowPlayer.getPlayer());
	}

	private static void sendNoGlowMessage(EGlowPlayer eGlowPlayer) {
		if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && eGlowPlayer.hasPermission("eglow.option.glowstate"))
			ChatUtil.sendMsg(eGlowPlayer.getPlayer(), Message.NON_GLOWING_STATE_ON_JOIN.get(), true);
	}
}