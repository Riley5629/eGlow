package me.MrGraycat.eGlow.GUI.Menus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowCustomMainMenu extends Menu {
	private int availableSlots;
	private OriginalMenu originalMenu;
	
	public EGlowCustomMainMenu(Player player, OriginalMenu originalMenu) {
		super(player);
		setOriginalMenu(originalMenu);
	}

	@Override
	public String getMenuName() {
		return getOriginalMenu().getMenuName();
	}

	@Override
	public int getSlots() {
		return getOriginalMenu().getSlots();
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		IEGlowPlayer ePlayer = getInstance().getDataManager().getEGlowPlayer(player);
		int clickedSlot = e.getSlot();
		//TODO check for page change
		
		String commands = getOriginalMenu().getAnyClickCommands().get(clickedSlot);
		
		if (commands == null || commands.isEmpty()) {
			switch(e.getClick()) {
			case LEFT:
				commands = getOriginalMenu().getLeftClickCommands().get(clickedSlot);
				break;
			case RIGHT:
				commands = getOriginalMenu().getRightClickCommands().get(clickedSlot);
				break;
			default:
				break;
			}
		}

		if (commands == null) {
			return;
		}
		
		Pattern test = Pattern.compile("\\[(.*?)\\]");
		
		try {
			for (String command : commands.split("/n")) {
				String commandType = "";
				Matcher match = test.matcher(command);

				while(match.find()) {
					commandType = command.substring(match.start(), match.end());
					break;
				}
				
				command = command.replace(commandType, "");
				
				if (command.startsWith(" "))
					command = command.substring(1, command.length());
				
				switch(commandType.toLowerCase()) {
				case("[glow]"):
					IEGlowEffect effect = getInstance().getDataManager().getEGlowEffect(command.replace(" ", ""));
				
					if (effect != null)
						ePlayer.activateGlow(effect);
					break;
				case("[toggleglowonjoin]"):
					ePlayer.setGlowOnJoin(!ePlayer.getGlowOnJoin());
					break;
				case("[toggleglow]"):
					ePlayer.toggleGlow();
					break;
				case("[togglespeed]"):
					if (hasEffect(ePlayer))
						updateSpeed(ePlayer);
					break;
				case("[player]"):
					getInstance().getServer().dispatchCommand(player, command);
					break;
				case("[console]"):
					getInstance().getServer().dispatchCommand(getInstance().getServer().getConsoleSender(), command);
					break;
				case("[message]"):
					ChatUtil.sendMsgWithPrefix(player, command);
					break;
				case("[openguimenu]"):
					break;
				case("[close]"):
					player.closeInventory();
					break;
				default:
					break;
				}
			}

			updateMenuItems(player);
		} catch(IllegalArgumentException ex) {
		}
	}

	@Override
	public void setMenuItems() {
		YamlConfiguration config = getOriginalMenu().getConfig();
		
		for (String itemName : getOriginalMenu().getConfig().getConfigurationSection("items").getKeys(false)) {
			if (itemName.contains("_locked"))
				continue;
			
			ItemStack item = createItem(itemName, getMenuMetdata(), config);
			
			for (String slot : getItemSlots(itemName, config)) {
				try {
					getInventory().setItem(Integer.valueOf(slot), item);

				} catch (ArrayIndexOutOfBoundsException ex) {
					break;
				}
			}
		}
		/*for (String item : EGlow.getCustomGUIConfig().getItems()) {
			String slots = EGlow.getCustomGUIConfig().getItemInfo(item, ItemKey.SLOTS);
			Material material = Material.valueOf(EGlow.getCustomGUIConfig().getItemInfo(item, ItemKey.MATERIAL));
		}*/
	}
	
	public void updateMenuItems(Player player) {
		YamlConfiguration config = getOriginalMenu().getConfig();
		
		for (String item : getOriginalMenu().getItemsToUpdate()) {
			ItemStack itemStack = createItem(item, getMenuMetdata(), config);
			
			for (String slot : getItemSlots(item, config)) {
				try {
					getInventory().setItem(Integer.valueOf(slot), itemStack);
				} catch (ArrayIndexOutOfBoundsException ex) {
					break;
				}
			}
		}
		
		player.updateInventory();
	}
	
	//Setter
	private void setOriginalMenu(OriginalMenu originalMenu) {
		this.originalMenu = originalMenu;
	}
	
	public void setAvailableSlots(int availableSlots) {
		this.availableSlots = availableSlots;
	}

	public void setCurrentPage(int page) {
		this.page = page;
	}
	
	//Getter
	private OriginalMenu getOriginalMenu() {
		return this.originalMenu;
	}
	public int getAvailableSlots() {
		return this.availableSlots;
	}
	
	public int getCurrentPage() {
		return this.page;
	}
}
