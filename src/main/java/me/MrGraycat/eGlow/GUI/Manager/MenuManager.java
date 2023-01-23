package me.MrGraycat.eGlow.GUI.Manager;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class MenuManager {
	private final HashMap<Player, MenuMetadata> playerMenuMetadata = new HashMap<>();
	
	/**
	 * Get menumetadata of a player
	 * @param p player to get the menumetadata for
	 * @return menumetadata of the player
	 */
	public MenuMetadata getMenuMetadata(Player p) {
		if (!playerMenuMetadata.containsKey(p)) {
			MenuMetadata meta;
			
			meta = new MenuMetadata(p);
			playerMenuMetadata.put(p, meta);
			
			return meta;
		} else {
			return playerMenuMetadata.get(p);
		}
	}
	
	public static class MenuMetadata {
		private Player owner;
		
		public MenuMetadata(Player p) {
			setOwner(p);
		}
		
		//Setter
		private void setOwner(Player owner) {
			this.owner = owner;
		}
		
		//Getter
		public Player getOwner() {
			return this.owner;
		}
	}
}