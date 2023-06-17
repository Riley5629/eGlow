package me.MrGraycat.eGlow.menu.paginated;

import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import me.MrGraycat.eGlow.menu.Menu;
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
		if (EGlowMainConfig.MainConfig.SETTINGS_GUI_ADD_GLASS_PANES.getBoolean()) {
			inventory.setItem(27, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(30, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(31, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(32, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			inventory.setItem(35, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
		}
		
		inventory.setItem(28, createPlayerSkull(p));
		inventory.setItem(29, createGlowingStatus(p));
		inventory.setItem(33, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), EGlowMessageConfig.Message.GUI_PREVIOUS_PAGE.get(), 0, EGlowMessageConfig.Message.GUI_PAGE_LORE.get((page == 1) ? EGlowMessageConfig.Message.GUI_MAIN_MENU.get() : page - 1 + "")));
		inventory.setItem(34, createItem((ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"), EGlowMessageConfig.Message.GUI_NEXT_PAGE.get(), 0, EGlowMessageConfig.Message.GUI_PAGE_LORE.get((!hasNextPage) ? EGlowMessageConfig.Message.GUI_NOT_AVAILABLE.get() : page + 1 + "")));
	}
}