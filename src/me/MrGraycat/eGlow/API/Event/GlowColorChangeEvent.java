package me.MrGraycat.eGlow.API.Event;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GlowColorChangeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private UUID UUID;
	private ChatColor color;
	private boolean isGlowing;
	
	public GlowColorChangeEvent(Player player, UUID uuid, ChatColor color, boolean isGlowing) {
		this.player = player;
		this.UUID = uuid;
		this.color = color;
		this.isGlowing = isGlowing;
	}
	
	/**
	 * Get the player
	 * @return Player
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Get the player's UUID
	 * @return UUID
	 */
	public UUID getPlayerUUID() {
		return UUID;
	}
	
	/**
	 * Get the glow color
	 * @return glow color as String (invisible)
	 */
	public String getColor() {
		return (color == null || color.equals(ChatColor.RESET) || !isGlowing) ? "" :  color + "";
	}
	
	/**
	 * Get the char of the glow color
	 * @return glow color char as String which would go after the & or §
	 */
	public String getColorChar() {
		return (color == null || color.equals(ChatColor.RESET) || !isGlowing) ? "" :  String.valueOf(color.getChar());
	}
	
	/**
	 * Get the glow color as ChatColor
	 * @return glow color as ChatColor
	 */
	public ChatColor getChatColor() {
		return (color == null) ? ChatColor.RESET : color;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}