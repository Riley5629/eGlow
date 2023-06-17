package me.mrgraycat.eglow.menu.paginated;

import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.packet.ProtocolVersion;
import me.mrgraycat.eglow.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class PaginatedMenu extends Menu {

	protected int page = 1;
	protected boolean hasNextPage = false;
	protected int maxItemsPerPage = 26;
	
	public PaginatedMenu(Player player) {
		super(player);
	}

	/**
	 * Update the navigationbar in the custom effects gui
	 * @param p player to update the navigation bar for
	 */
	public void updateMainEffectsNavigationBar(IEGlowPlayer p) {
		if (MainConfig.SETTINGS_GUI_ADD_GLASS_PANES.getBoolean()) {
			inventory.setItem(27, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(30, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(31, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(32, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(35, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
		}
		
		inventory.setItem(28, createPlayerSkull(p));
		inventory.setItem(29, createGlowingStatus(p));
		inventory.setItem(33, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), Message.GUI_PREVIOUS_PAGE.get(), 0, Message.GUI_PAGE_LORE.get((page == 1) ? Message.GUI_MAIN_MENU.get() : page - 1 + "")));
		inventory.setItem(34, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), Message.GUI_NEXT_PAGE.get(), 0, Message.GUI_PAGE_LORE.get((!hasNextPage) ? Message.GUI_NOT_AVAILABLE.get() : page + 1 + "")));
	}
}