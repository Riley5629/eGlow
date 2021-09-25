package me.MrGraycat.eGlow.Addon.TAB.Listeners;

import java.util.ConcurrentModificationException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;

public class EGlowTABListenerUniv implements Listener { 	
	private EGlow instance;
	
	public EGlowTABListenerUniv(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
	}
	
	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		Player player = event.getPlayer();
		ChatColor chatColor = event.getChatColor();
		String color = event.getColor();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (getInstance().getTABAddon() != null && getInstance().getTABAddon().getTABOnBukkit()) {
						if (!getInstance().getTABAddon().getTABTeamPacketBlockingEnabled() && !getInstance().getTABAddon().getTABNametagPrefixSuffixEnabled()) {
							return;
						}				
						
						if (player != null && getInstance().getTABAddon().getTABPlayer(player.getUniqueId()) != null) {
							IEGlowPlayer ePlayer = getInstance().getDataManager().getEGlowPlayer(player);
							
							getInstance().getTABAddon().updateTABPlayer(ePlayer, chatColor);
						}
					} else if (getInstance().getDebugUtil().onBungee()) {
						getInstance().getDataManager().TABProxyUpdateRequest(player, color);	
					}
				} catch (ConcurrentModificationException ex2) {
					//Ignore caused by updating to fast
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(getInstance());
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if (getInstance().getTABAddon() != null)
			getInstance().getTABAddon().getStoredGroups().remove(event.getPlayer());
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}

}
