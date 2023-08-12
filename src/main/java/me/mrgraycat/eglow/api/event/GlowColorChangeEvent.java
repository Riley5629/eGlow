package me.mrgraycat.eglow.api.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class GlowColorChangeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final UUID UUID;
	private final ChatColor color;
	private final boolean isGlowing;

	public GlowColorChangeEvent(Player player, UUID uuid, ChatColor color, boolean isGlowing) {
		this.player = player;
		this.UUID = uuid;
		this.color = color;
		this.isGlowing = isGlowing;
	}

	/**
	 * Get the player
	 *
	 * @return Player
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Get the player's UUID
	 *
	 * @return UUID
	 */
	public UUID getPlayerUUID() {
		return this.UUID;
	}

	/**
	 * Get the glow color
	 *
	 * @return glow color as String (invisible)
	 */
	public String getColor() {
		return (this.color == null || this.color.equals(ChatColor.RESET) || !this.isGlowing) ? "" : String.valueOf(this.color);
	}

	/**
	 * Get the char of the glow color
	 *
	 * @return glow color char as String which would go after the & or §
	 */
	public String getColorChar() {
		return (this.color == null || this.color.equals(ChatColor.RESET) || !this.isGlowing) ? "" : String.valueOf(this.color.getChar());
	}

	/**
	 * Get the glow color as ChatColor
	 *
	 * @return glow color as ChatColor
	 */
	public ChatColor getChatColor() {
		return (this.color == null) ? ChatColor.RESET : this.color;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}