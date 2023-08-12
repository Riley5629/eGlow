package me.mrgraycat.eglow.gui;

import lombok.Getter;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.gui.manager.MenuItemManager;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

@Getter
public abstract class Menu extends MenuItemManager implements InventoryHolder {
	protected MenuMetadata menuMetadata;
	protected Inventory inventory;

	public Menu(Player player) {
		this.menuMetadata = getMenuMetadata(player);
	}

	public abstract String getMenuName();

	public abstract int getSlots();

	public abstract void handleMenu(InventoryClickEvent e);

	public abstract void setMenuItems();

	public void openInventory() {
		inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
		this.setMenuItems();
		getMenuMetadata().getOwner().openInventory(getInventory());
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Enable glow for a player based on the clicktype
	 *
	 * @param player     to enable the glow for
	 * @param clickType  left/right click
	 * @param effectName effect to check for solid/blink/effect
	 */
	public void enableGlow(Player player, ClickType clickType, String effectName) {
		EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (clickType.equals(ClickType.LEFT)) {
			if (DataManager.getEGlowEffect(effectName) != null) {
				EGlowEffect color = DataManager.getEGlowEffect(effectName);

				if (color == null)
					return;

				if (!(player.hasPermission(color.getPermissionNode()) || DataManager.isCustomEffect(color.getName()) && Objects.requireNonNull(player.getPlayer(), "Unable to retrieve player").hasPermission("eglow.effect.*"))) {
					ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
					return;
				}

				if (eGlowPlayer.isSameGlow(color)) {
					ChatUtil.sendMsgFromGUI(player, Message.SAME_GLOW.get());
					return;
				}

				eGlowPlayer.activateGlow(color);
				ChatUtil.sendMsgFromGUI(player, Message.NEW_GLOW.get(color.getDisplayName()));
			} else if (DataManager.getEGlowEffect(effectName + "slow") != null) { //for rainbow effect 
				EGlowEffect effect = DataManager.getEGlowEffect(effectName + "slow");

				if (!player.hasPermission(Objects.requireNonNull(effect, "Unable to retrieve effect from given name").getPermissionNode())) {
					ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
					return;
				}

				if (eGlowPlayer.isSameGlow(effect)) {
					ChatUtil.sendMsgFromGUI(player, Message.SAME_GLOW.get());
					return;
				}

				eGlowPlayer.activateGlow(effect);
				ChatUtil.sendMsgFromGUI(player, Message.NEW_GLOW.get(effect.getDisplayName()));
			}

		} else if (clickType.equals(ClickType.RIGHT)) {
			EGlowEffect effect = DataManager.getEGlowEffect("blink" + effectName + "slow");

			if (effect == null)
				return;

			if (!player.hasPermission(effect.getPermissionNode())) {
				ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
				return;
			}

			if (eGlowPlayer.isSameGlow(effect)) {
				ChatUtil.sendMsgFromGUI(player, Message.SAME_GLOW.get());
				return;
			}

			eGlowPlayer.activateGlow(effect);
			ChatUtil.sendMsgFromGUI(player, Message.NEW_GLOW.get(effect.getDisplayName()));
		}
	}

	/**
	 * Update the effects speed
	 *
	 * @param eGlowPlayer to update the speed for
	 */
	public void updateSpeed(EGlowPlayer eGlowPlayer) {
		if (eGlowPlayer.getGlowEffect() != null) {
			String effect = eGlowPlayer.getGlowEffect().getName();
			EGlowEffect eGlowEffect = null;

			if (effect.contains("slow"))
				eGlowEffect = DataManager.getEGlowEffect(effect.replace("slow", "fast"));

			if (effect.contains("fast"))
				eGlowEffect = DataManager.getEGlowEffect(effect.replace("fast", "slow"));

			eGlowPlayer.activateGlow(eGlowEffect);
			ChatUtil.sendMsgFromGUI(getMenuMetadata().getOwner(), Message.NEW_GLOW.get(Objects.requireNonNull(eGlowEffect, "Unable to get displayname from effect").getDisplayName()));
		}
	}

	/**
	 * Code to update the navigationbar for the main menu
	 *
	 * @param eGlowPlayer to update the navigationbar for
	 */
	public void UpdateMainNavigationBar(EGlowPlayer eGlowPlayer) {
		if (MainConfig.SETTINGS_GUI_ADD_GLASS_PANES.getBoolean()) {
			getInventory().setItem(27, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(29, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(32, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
			getInventory().setItem(33, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));

			getInventory().setItem(34, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));

			getInventory().setItem(35, createItem(Material.valueOf(GLASS_PANE), "&f", 5, ""));
		}

		getInventory().setItem(28, createPlayerSkull(eGlowPlayer));
		getInventory().setItem(30, createGlowingStatus(eGlowPlayer));
		getInventory().setItem(31, createGlowVisibility(eGlowPlayer));

		if (hasEffect(eGlowPlayer))
			getInventory().setItem(32, createItem(Material.valueOf(CLOCK), Message.GUI_SPEED_ITEM_NAME.get(), 0, createSpeedLore(eGlowPlayer)));

		if (MainConfig.SETTINGS_GUI_CUSTOM_EFFECTS.getBoolean())
			getInventory().setItem(34, setItemGlow(createItem(Material.BOOK, Message.GUI_CUSTOM_EFFECTS_ITEM_NAME.get(), 0, Message.GUI_CLICK_TO_OPEN.get())));
	}
}