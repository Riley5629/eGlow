package me.MrGraycat.eGlow.Addon.TAB.Listeners;

import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Addon.TAB.TABAddon;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ConcurrentModificationException;

public class EGlowTABListenerUniv implements Listener { 	

	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		Player player = event.getPlayer();
		ChatColor chatColor = event.getChatColor();
		String color = event.getColor();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
					
					if (TAB_Addon != null && TAB_Addon.blockEGlowPackets()) {
						if (player != null && TAB_Addon.getTABPlayer(player.getUniqueId()) != null) {
							IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

							if (ePlayer == null)
								return;

							TAB_Addon.updateTABPlayer(ePlayer, chatColor);
						}
					} else if (DebugUtil.onBungee()) {
						DataManager.TABProxyUpdateRequest(player, color);	
					}
				} catch (ConcurrentModificationException ex2) {
					//Ignore caused by updating to fast
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
					IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

					if (ePlayer == null)
						return;

					if (TAB_Addon != null && TAB_Addon.blockEGlowPackets()) {
						if (TAB_Addon.getTABPlayer(player.getUniqueId()) != null) {
							TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						}
					} else if (DebugUtil.onBungee()) {
						DataManager.TABProxyUpdateRequest(player, (ePlayer.getActiveColor().equals(ChatColor.RESET) || !ePlayer.isGlowing()) ? "" :  ePlayer.getActiveColor() + "");
					}
				} catch (ConcurrentModificationException ex2) {
					//Ignore caused by updating to fast
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 2L);
	}
}