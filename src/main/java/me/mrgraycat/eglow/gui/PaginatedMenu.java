package me.mrgraycat.eglow.gui;

import lombok.Getter;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
public abstract class PaginatedMenu extends Menu {
	protected int page = 1;
	protected boolean hasNextPage = false;
	protected int maxItemsPerPage = 26;

	public PaginatedMenu(Player player) {
		super(player);
	}

	public int getMaxItemsPerPage() {
		return this.maxItemsPerPage;
	}

	public boolean hasNextPage() {
		return this.hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	/**
	 * Update the navigationbar in the custom effects gui
	 *
	 * @param p player to update the navigation bar for
	 */
	public void UpdateMainEffectsNavigationBar(EGlowPlayer p) {
		if (MainConfig.SETTINGS_GUI_ADD_GLASS_PANES.getBoolean()) {
			getInventory().setItem(27, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(30, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(31, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(32, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(35, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
		}

		getInventory().setItem(28, createPlayerSkull(p));
		getInventory().setItem(29, createGlowingStatus(p));
		getInventory().setItem(33, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), Message.GUI_PREVIOUS_PAGE.get(), 0, Message.GUI_PAGE_LORE.get((page == 1) ? Message.GUI_MAIN_MENU.get() : String.valueOf(page - 1))));
		getInventory().setItem(34, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), Message.GUI_NEXT_PAGE.get(), 0, Message.GUI_PAGE_LORE.get((!hasNextPage()) ? Message.GUI_NOT_AVAILABLE.get() : String.valueOf(page + 1))));
	}
}