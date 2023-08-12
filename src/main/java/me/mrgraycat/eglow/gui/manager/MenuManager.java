package me.mrgraycat.eglow.gui.manager;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public class MenuManager {
	private final HashMap<Player, MenuMetadata> playerMenuMetadata = new HashMap<>();

	/**
	 * Get menumetadata of a player
	 *
	 * @param player player to get the menumetadata for
	 * @return menumetadata of the player
	 */
	public MenuMetadata getMenuMetadata(Player player) {
		if (!getPlayerMenuMetadata().containsKey(player)) {
			MenuMetadata meta;

			meta = new MenuMetadata(player);
			getPlayerMenuMetadata().put(player, meta);

			return meta;
		} else {
			return getPlayerMenuMetadata().get(player);
		}
	}

	@Getter
	public static class MenuMetadata {
		private final Player owner;
		private long lastClicked;

		public MenuMetadata(Player player) {
			this.owner = player;
			this.lastClicked = System.currentTimeMillis();
		}

		public void setLastClicked(long lastClicked) {
			this.lastClicked = lastClicked;
		}

		public long getLastClicked() {
			return lastClicked;
		}
	}
}