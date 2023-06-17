package me.mrgraycat.eglow.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MenuManager {

	private final Map<Player, MenuMetadata> playerMenuMetadata = new HashMap<>();
	
	/**
	 * Get menumetadata of a player
	 * @param player player to get the menumetadata for
	 * @return menumetadata of the player
	 */
	public MenuMetadata getMenuMetadata(Player player) {
		return playerMenuMetadata.computeIfAbsent(player, MenuMetadata::new);
	}

	@Getter @Setter
	public static class MenuMetadata {

		private Player owner;
		
		public MenuMetadata(Player owner) {
			this.owner = owner;
		}

	}
}