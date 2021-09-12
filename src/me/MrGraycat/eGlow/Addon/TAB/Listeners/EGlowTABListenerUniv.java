package me.MrGraycat.eGlow.Addon.TAB.Listeners;

import java.util.ConcurrentModificationException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
		try {
			if (getInstance().getTABAddon() != null && getInstance().getTABAddon().getTABOnBukkit()) {
				if (!getInstance().getTABAddon().getTABTeamPacketBlockingEnabled() && !getInstance().getTABAddon().getTABNametagPrefixSuffixEnabled()) {
					return;
				}				
				
				if (event.getPlayer() != null && getInstance().getTABAddon().getTABPlayer(event.getPlayer().getUniqueId()) != null) {
					IEGlowPlayer ePlayer = getInstance().getDataManager().getEGlowPlayer(event.getPlayer());
					
					getInstance().getTABAddon().updateTABPlayer(ePlayer, event.getChatColor());
				}
			} else if (getInstance().getDebugUtil().onBungee()) {
				getInstance().getDataManager().TABProxyUpdateRequest(event.getPlayer(), event.getColor());	
			}
		} catch (ConcurrentModificationException ex2) {
			//Ignore caused by updating to fast
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
