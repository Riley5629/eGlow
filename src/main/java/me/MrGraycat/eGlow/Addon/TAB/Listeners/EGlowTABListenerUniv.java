package me.MrGraycat.eGlow.Addon.TAB.Listeners;

import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Addon.TAB.TABAddon;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ConcurrentModificationException;

public class EGlowTABListenerUniv implements Listener {

	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		requestTABPlayerUpdate(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {
				requestTABPlayerUpdate(player);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 5L);
	}

	private void requestTABPlayerUpdate(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon tabAddon = EGlow.getInstance().getTABAddon();
					IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

					if (eGlowPlayer == null)
						return;

					if (tabAddon != null && tabAddon.blockEGlowPackets()) {
						tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
					} else if (DebugUtil.onBungee()) {
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
}