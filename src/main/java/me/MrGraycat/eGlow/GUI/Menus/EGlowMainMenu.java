package me.MrGraycat.eGlow.GUI.Menus;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EGlowMainMenu extends Menu {

	public EGlowMainMenu(Player player) {
		super(player);
	}

	@Override
	public String getMenuName() {
		return ChatUtil.translateColors(((MainConfig.SETTINGS_GUI_ADD_PREFIX.getBoolean()) ? Message.GUI_TITLE.get() : Message.PREFIX.get() + Message.GUI_TITLE.get()));
	}

	@Override
	public int getSlots() {
		return 36;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);
		ClickType clickType = e.getClick();
		int clickedSlot = e.getSlot();
		
		switch(clickedSlot) {
		case(0):
			enableGlow(player, clickType, "red");
		break;
		case(1):
			enableGlow(player, clickType, "darkred");
		break;
		case(2):
			enableGlow(player, clickType, "gold");
		break;
		case(3):
			enableGlow(player, clickType, "yellow");
		break;
		case(4):
			enableGlow(player, clickType, "green");
		break;
		case(5):
			enableGlow(player, clickType, "darkgreen");
		break;
		case(6):
			enableGlow(player, clickType, "aqua");
		break;
		case(7):
			enableGlow(player, clickType, "darkaqua");
		break;
		case(8):
			enableGlow(player, clickType, "blue");
		break;
		case(10):
			enableGlow(player, clickType, "darkblue");
		break;
		case(11):
			enableGlow(player, clickType, "purple");
		break;
		case(12):
			enableGlow(player, clickType, "pink");
		break;
		case(13):
			enableGlow(player, clickType, "white");
		break;
		case(14):
			enableGlow(player, clickType, "gray");
		break;
		case(15):
			enableGlow(player, clickType, "darkgray");
		break;
		case(16):
			enableGlow(player, clickType, "black");
		break;
		case(28):
			if (eGlowPlayer.getSaveData())
				eGlowPlayer.setSaveData(true);
			
			eGlowPlayer.setGlowOnJoin(!eGlowPlayer.getGlowOnJoin());
		break;
		case(30):
			if (eGlowPlayer.getPlayer().hasPermission("eglow.command.toggle")) {
				if (eGlowPlayer.isGlowing()) {
					eGlowPlayer.disableGlow(false);
					ChatUtil.sendMsgFromGUI(player, Message.DISABLE_GLOW.get());
				} else {
					if (eGlowPlayer.getEffect() == null || eGlowPlayer.getEffect().getName().equals("none")) {
						ChatUtil.sendMsgFromGUI(player, Message.NO_LAST_GLOW.get());
						return;
					} else {
						if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
							ChatUtil.sendMsgFromGUI(player, Message.INVISIBILITY_BLOCKED.get());
						}
						
						if (eGlowPlayer.getPlayer().hasPermission(eGlowPlayer.getEffect().getPermission()) || eGlowPlayer.isForcedGlow(eGlowPlayer.getEffect())) {
							eGlowPlayer.activateGlow();
						} else {
							ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
							return;
						}
						ChatUtil.sendMsgFromGUI(player, Message.NEW_GLOW.get(eGlowPlayer.getLastGlowName()));
					}
				}
			} else {
				ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
			}
		break;
		case(31):
			enableGlow(player, clickType, "rainbow");
		break;
		case(32):
			if (hasEffect(eGlowPlayer))
				updateSpeed(eGlowPlayer);
		break;
		case(34):
			if (MainConfig.SETTINGS_GUI_CUSTOM_EFFECTS.getBoolean())
			 new EGlowEffectMenu(eGlowPlayer.getPlayer()).openInventory();
		break;
		default:
			return;
		}

		UpdateMainNavigationBar(eGlowPlayer);
	}

	@Override
	public void setMenuItems() {
		new BukkitRunnable() {
			@Override
			public void run() {
				IEGlowPlayer p = DataManager.getEGlowPlayer(menuMetadata.getOwner());
				
				UpdateMainNavigationBar(p);
				
				inventory.setItem(0, createLeatherColor(p, "red", 255, 85, 85));
				inventory.setItem(1, createLeatherColor(p, "dark-red", 170, 0, 0));
				inventory.setItem(2, createLeatherColor(p, "gold", 255, 170, 0));
				inventory.setItem(3, createLeatherColor(p, "yellow", 255, 255, 85));
				inventory.setItem(4, createLeatherColor(p, "green", 85, 255, 85));
				inventory.setItem(5, createLeatherColor(p, "dark-green", 0, 170, 0));
				inventory.setItem(6, createLeatherColor(p, "aqua", 85, 255, 255));
				inventory.setItem(7, createLeatherColor(p, "dark-aqua", 0, 170, 170));
				inventory.setItem(8, createLeatherColor(p, "blue", 85, 85, 255));
				
				inventory.setItem(10, createLeatherColor(p, "dark-blue", 0, 0, 170));
				inventory.setItem(11, createLeatherColor(p, "purple", 170, 0, 170));
				inventory.setItem(12, createLeatherColor(p, "pink", 255, 85, 255));
				inventory.setItem(13, createLeatherColor(p, "white", 255, 255, 255));
				inventory.setItem(14, createLeatherColor(p, "gray", 170, 170, 170));
				inventory.setItem(15, createLeatherColor(p, "dark-gray", 85, 85, 85));
				inventory.setItem(16, createLeatherColor(p, "black", 0, 0, 0));
				
				new BukkitRunnable() {
					@Override
					public void run() {
						menuMetadata.getOwner().openInventory(inventory);
					}
				}.runTask(getInstance());
			}
		}.runTaskAsynchronously(getInstance());
	}
}