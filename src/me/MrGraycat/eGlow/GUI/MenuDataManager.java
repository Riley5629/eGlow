package me.MrGraycat.eGlow.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.GUI.Manager.MenuItemManager.ItemInfo;

public class MenuDataManager {
	private EGlow instance;
	
	private ConcurrentHashMap<String, HashMap<ClickType, HashMap<Integer, String>>> allCommands = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, HashMap<Integer, String>> anyClickCommands = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, HashMap<Integer, String>> leftClickCommands = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, HashMap<Integer, String>> rightClickCommands = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, List<String>> itemsToUpdate = new ConcurrentHashMap<>();
	
	public MenuDataManager(EGlow instance) {
		setInstance(instance);
		setupClickCommands();
	}
	
	//name hashmap clicktype commands
	//name arraylist int
	
	//Page support
	
	private void setupClickCommands() {
		for (File file : new File(getInstance().getDataFolder() + File.separator + "GUI").listFiles()) {
			YamlConfiguration config = new YamlConfiguration();
			
			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			
			new BukkitRunnable() {
				@Override
				public void run() {
					for (String item : config.getConfigurationSection("items").getKeys(false)) {
						if (item.contains("_locked"))
							continue;
						
						HashMap<ClickType, HashMap<Integer, String>> allCmds = new HashMap<>();
						HashMap<Integer, String> anyClick = new HashMap<>();
						HashMap<Integer, String> leftClick = new HashMap<>();
						HashMap<Integer, String> rightClick = new HashMap<>();
						List<String> markedToUpdate = new ArrayList<>();

						for (String slot : getItemSlots(item, config)) {
							if (!getItemInteractionStringList(item, config, ItemInfo.ANY_CLICK).isEmpty()) {
								String commands = StringUtils.join(getItemInteractionStringList(item, config, ItemInfo.ANY_CLICK), "/n");
								
								if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
									markedToUpdate.add(item);
								
								anyClick.put(Integer.valueOf(slot), commands);
							}
							
							if (!getItemInteractionStringList(item, config, ItemInfo.LEFT_CLICK).isEmpty()) {
								String commands = StringUtils.join(getItemInteractionStringList(item, config, ItemInfo.LEFT_CLICK), "/n");
								
								if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
									markedToUpdate.add(item);
								
								leftClick.put(Integer.valueOf(slot), commands);
							}
							
							if (!getItemInteractionStringList(item, config, ItemInfo.RIGHT_CLICK).isEmpty()) {
								String commands = StringUtils.join(getItemInteractionStringList(item, config, ItemInfo.RIGHT_CLICK), "/n");
								
								if (commands.contains("[toggleglowonjoin]") || commands.contains("[toggleglow]") || commands.contains("[togglespeed]"))
									markedToUpdate.add(item);
								
								rightClick.put(Integer.valueOf(slot), commands);
							}
						}
						
						allCmds.put(ClickType.UNKNOWN, anyClick);
						
						
						allCommands.put(file.getName().replace(".yml", "").toLowerCase(), allCmds);
						
						anyClickCommands.put(file.getName().replace(".yml", "").toLowerCase(), anyClick);
						leftClickCommands.put(file.getName().replace(".yml", "").toLowerCase(), leftClick);
						rightClickCommands.put(file.getName().replace(".yml", "").toLowerCase(), rightClick);
						
						itemsToUpdate.put(file.getName().replace(".yml", "").toLowerCase(), markedToUpdate);
					}
				}
			}.runTaskAsynchronously(getInstance());
		}
	}
	
	//Setter
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}
	//Getter
	private EGlow getInstance() {
		return this.instance;
	}
	
	public List<String> getItemSlots(String item, YamlConfiguration config) {
		int slot = getItemInfoInteger(item, config, ItemInfo.SLOT);
		List<String> slots = getItemInfoStringList(item, config, ItemInfo.SLOTS);
		
		if (slot != -1) {
			slots.add(String.valueOf(slot));
		}
		
		return slots;
	}
	
	@SuppressWarnings("unused")
	private String getItemInfoString(String item, YamlConfiguration config, ItemInfo key) {
		if (config.contains("items." + item + "." + key.toString().toLowerCase())) {
			return config.getString("items." + item + "." + key.toString().toLowerCase());
		} else {
			return "";
		}
	}
	
	private int getItemInfoInteger(String item, YamlConfiguration config, ItemInfo key) {
		if (config.contains("items." + item + "." + key.toString().toLowerCase())) {
			return config.getInt("items." + item + "." + key.toString().toLowerCase());
		} else {
			return -1;
		}	
	}
	
	protected Boolean getItemInfoBoolean(String item, YamlConfiguration config, ItemInfo key) {
		try {
			return config.getBoolean("items." + item + "." + key.toString().toLowerCase());
		} catch (Exception e) {
			return false;
		}	
	}
	
	protected List<String> getItemInfoStringList(String item, YamlConfiguration config, ItemInfo key) {
		if (config.contains("items." + item + "." + key.toString().toLowerCase())) {
			return config.getStringList("items." + item + "." + key.toString().toLowerCase());
		} else {
			return new ArrayList<>();
		}
	}
	
	public List<String> getItemInteractionStringList(String item, YamlConfiguration config, ItemInfo key) {
		if (config.contains("items." + item + ".interaction." + key.toString().toLowerCase())) {
			return config.getStringList("items." + item + ".interaction." + key.toString().toLowerCase());
		} else {
			return new ArrayList<>();
		}
	}
	/*
	 * @SuppressWarnings("unused")
	private void addOriginalMenu() {
		for (File file : new File(getInstance().getDataFolder() + File.separator + "GUI").listFiles()) {
			dataOriginalMenu.put(file.getName().replace(".yml", "").toLowerCase(), new OriginalMenu(file.getName().replace(".yml", "")));
		}
	}
	 */

}
