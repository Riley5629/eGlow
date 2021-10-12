package me.MrGraycat.eGlow.GUI.Menus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class OriginalMenu extends Menu {	
	private ConcurrentHashMap<Integer, String> anyClickCommands = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> leftClickCommands = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> rightClickCommands = new ConcurrentHashMap<>();
	private ArrayList<String> itemsToUpdate = new ArrayList<>();
	
	private YamlConfiguration yamlConfig;
	private File configFile;
	
	public OriginalMenu(String fileName) {
		super(null);
		setupConfig(getInstance().getDataFolder() + File.separator + "GUI" + File.separator + fileName + ".yml");
		setMenuItems();
	}

	@Override
	public String getMenuName() {
		return getMenuTitle();
	}

	@Override
	public int getSlots() {
		return getMenuSize();
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {}

	@Override
	public void setMenuItems() {
		//Just storing click interaction and not setting any items
		new BukkitRunnable() {
			@Override
			public void run() {
				for (String item : getItems()) {
					if (item.contains("_locked"))
						continue;
					
					List<String> anyClick = new ArrayList<>();
					List<String> leftClick = new ArrayList<>();
					List<String> rightClick = new ArrayList<>();
					
					if (!getItemInteractionStringList(item, getConfig(), ItemInfo.ANY_CLICK).isEmpty()) {
						anyClick = getItemInteractionStringList(item, getConfig(), ItemInfo.ANY_CLICK);
					}
					
					if (!getItemInteractionStringList(item, getConfig(), ItemInfo.LEFT_CLICK).isEmpty()) {
						leftClick = getItemInteractionStringList(item, getConfig(), ItemInfo.LEFT_CLICK);
					}
					
					if (!getItemInteractionStringList(item, getConfig(), ItemInfo.RIGHT_CLICK).isEmpty()) {
						rightClick = getItemInteractionStringList(item, getConfig(), ItemInfo.RIGHT_CLICK);
					}
					
					for (String slot : getItemSlots(item, getConfig())) {
						if (!anyClick.isEmpty()) {
							String commands = StringUtils.join(anyClick, "/n");
							
							if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
								getItemsToUpdate().add(item);
							
							anyClickCommands.put(Integer.valueOf(slot), commands);
						}
						
						if (!leftClick.isEmpty()) {
							String commands = StringUtils.join(leftClick, "/n");
							
							if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
								getItemsToUpdate().add(item);
							
							leftClickCommands.put(Integer.valueOf(slot), commands);
						}
						
						if (!rightClick.isEmpty()) {
							String commands = StringUtils.join(rightClick, "/n");
							
							if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
								getItemsToUpdate().add(item);
							
							rightClickCommands.put(Integer.valueOf(slot), commands);
						}
					}
				}
			}
		}.runTaskAsynchronously(getInstance());
	}
	
	private void setupConfig(String path) {
		try {
			setConfigFilePath(path);
			setConfig(new YamlConfiguration());
			getConfig().load(getConfigFilePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void reloadMenu() {
		YamlConfiguration configBackup = yamlConfig;
		
		try {
			yamlConfig = null;
			yamlConfig = new YamlConfiguration();
			yamlConfig.load(configFile);
		
			setMenuItems();
		} catch(Exception e) {
			yamlConfig = configBackup;
		}
	}
	
	//Setters
	//Config
	private void setConfig(YamlConfiguration yamlConfig) {
		this.yamlConfig = yamlConfig;
	}
	
	private void setConfigFilePath(String path) {
		this.configFile = new File(path);
	}

	//Getters
	//Menu
	private String getMenuTitle() {
		if (getConfig().contains("title")) {
			return ChatUtil.translateColors(getConfig().getString("title"));
		}
		return "";
	}
	
	private int getMenuSize() {
		if (getConfig().contains("rows")) {
			return (getConfig().getInt("rows") * 9);
		}
		return 9;
	}
	
	public ArrayList<String> getItemsToUpdate() {
		return this.itemsToUpdate;
	}
	
	public ConcurrentHashMap<Integer, String> getAnyClickCommands() {
		return this.anyClickCommands;
	}
	
	public ConcurrentHashMap<Integer, String> getLeftClickCommands() {
		return this.leftClickCommands;
	}
	
	public ConcurrentHashMap<Integer, String> getRightClickCommands() {
		return this.rightClickCommands;
	}
	
	//Config
	public YamlConfiguration getConfig() {
		return this.yamlConfig;
	}
	
	private File getConfigFilePath() {
		return this.configFile;
	}
	
	private Set<String> getItems() {
		return getConfig().getConfigurationSection("items").getKeys(false);
	}
}
