package me.MrGraycat.eGlow.GUI;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class MenuManager {
	private static final HashMap<Player, MenuMetadata> playerMenuMetadata = new HashMap<>();
	
	/**
	 * Get menumetadata of a player
	 * @param p player to get the menumetadata for
	 * @return menumetadata of the player
	 */
	public static MenuMetadata getMenuMetadata(Player p) {
		if (!playerMenuMetadata.containsKey(p)) {
			MenuMetadata meta;
			
			meta = new MenuMetadata(p);
			playerMenuMetadata.put(p, meta);
			
			return meta;
		} else {
			return playerMenuMetadata.get(p);
		}
	}
}
