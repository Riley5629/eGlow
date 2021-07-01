package me.MrGraycat.eGlow.API.Event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;

public class eGlowPostPlayerJoinEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private IEGlowPlayer ep;
	
	public eGlowPostPlayerJoinEvent(IEGlowPlayer ep) {
		this.ep = ep;
	}
	
	/**
	 * Get the player
	 * @return Player
	 */
	public IEGlowPlayer getEGlowPlayer() {
		return ep;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
