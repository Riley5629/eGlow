package me.MrGraycat.eGlow.GUI.Menus.Custom;

import me.MrGraycat.eGlow.Config.Custom.EGlowCustomGuiConfig;
import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.GUI.Menus.Custom.Command.ClickCommands;
import me.MrGraycat.eGlow.GUI.Menus.Custom.Command.CommandClickType;
import me.MrGraycat.eGlow.GUI.Menus.Custom.Item.ItemBuilder;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.MrGraycat.eGlow.Util.Text.ItemPlaceholders;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class EGlowCustomMenu extends Menu {

    private final Player player;

    public EGlowCustomMenu(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public String getMenuName() {
        return ChatUtil.convertedColoredText(player, EGlowCustomGuiConfig.CustomGui.GET_TITLE.getTitle());
    }

    @Override
    public int getSlots() {
        return EGlowCustomGuiConfig.CustomGui.GET_SIZE.getSize();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        Player player = (Player) e.getWhoClicked();
        IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);
        ClickType clickType = e.getClick();
        ItemStack itemStack = e.getCurrentItem();

        if (itemStack == null) return;
        if (itemStack.getType() == Material.AIR) return;
        if (itemStack.getItemMeta() == null) return;

        ItemBuilder itemBuilder = new ItemBuilder(itemStack);
        ConfigurationSection section = EGlowCustomGuiConfig.CustomGui.GET_ITEMS.getSection();

        if (itemBuilder.getNBT().hasKey(getInstance(), "eglow")) {
            ConfigurationSection itemSection = section.getConfigurationSection(itemBuilder.getNBT().getString(getInstance(), "eglow"));
            if (itemSection == null) return;
            IEGlowEffect color;
            if (itemSection.getName().contains("rainbow")) color = DataManager.getEGlowEffect("rainbowslow");
            else color = DataManager.getEGlowEffect(itemSection.getName());
            if (DataManager.getEGlowEffects().contains(color)) {
                if (color == null){
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Color is null", false);
                    return;
                }
                if (itemBuilder.getNBT().hasKey(getInstance(), "eglow-state")) {
                    String state = itemBuilder.getNBT().getString(getInstance(), "eglow-state");
                    switch (state) {
                        case "Permission":
                            getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                            break;
                        case "Enabled":
                            if (getBlink(clickType, itemSection.getConfigurationSection(state)) && !color.getName().contains("rainbow")) {
                                IEGlowEffect blinkColor;
                                if (usePermissison(clickType, itemSection.getConfigurationSection(state))) {
                                    if (getSpeed(clickType, itemSection.getConfigurationSection(state)))
                                        blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "fast");
                                    else blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "slow");
                                    if (player.hasPermission(blinkColor.getPermission())) {
                                        if (new ItemPlaceholders().checkColor(eGlowPlayer, blinkColor)){
                                            getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                            eGlowPlayer.disableGlow(true);
                                        }
                                        getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                        eGlowPlayer.activateGlow(blinkColor);
                                        break;
                                    }
                                    else getNoPermissionCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                    break;
                                }
                                getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                eGlowPlayer.disableGlow(true);
                                break;
                            }
                            if (handleRainbow(player, eGlowPlayer, clickType, itemSection.getConfigurationSection(state), color)) break;
                            eGlowPlayer.disableGlow(true);
                            getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                            break;
                        case "Disabled":
                            if (getBlink(clickType, itemSection.getConfigurationSection(state)) && !color.getName().contains("rainbow")) {
                                IEGlowEffect blinkColor;
                                if (usePermissison(clickType, itemSection.getConfigurationSection(state))) {
                                    if (getSpeed(clickType, itemSection.getConfigurationSection(state)))
                                        blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "fast");
                                    else blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "slow");
                                    if (player.hasPermission(blinkColor.getPermission())) {
                                        getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                        eGlowPlayer.activateGlow(blinkColor);
                                        break;
                                    }
                                    else getNoPermissionCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                }
                                else {
                                    if (getSpeed(clickType, itemSection.getConfigurationSection(state)))
                                        blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "fast");
                                    else blinkColor = DataManager.getEGlowEffect("blink" + color.getName() + "slow");
                                    getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                                    eGlowPlayer.activateGlow(blinkColor);

                                }
                                break;
                            }
                            if (handleRainbow(player, eGlowPlayer, clickType, itemSection.getConfigurationSection(state), color)) break;
                            eGlowPlayer.activateGlow(color);
                            getClickCommands(player, itemSection.getConfigurationSection(state), clickType, eGlowPlayer, color);
                            break;
                    }
                } else {
                    getClickCommands(player, itemSection, clickType, eGlowPlayer, color);
                    if (color.getName().contains("rainbow"))
                        enableGlow(player, clickType, "rainbow");
                    else
                        enableGlow(player, clickType, color.getName());
                }
                refreshMenu();
            }

        }

        if (itemBuilder.getNBT().hasKey(getInstance(), "eglow-options")) {
            ConfigurationSection optionItemSection = section.getConfigurationSection(itemBuilder.getNBT().getString(getInstance(), "eglow-options"));
            String option = optionItemSection.getName();
            if (option.equalsIgnoreCase("data")) {
                if (eGlowPlayer.getSaveData())
                    eGlowPlayer.setSaveData(true);
                eGlowPlayer.setGlowOnJoin(!eGlowPlayer.getGlowOnJoin());
                getClickCommands(player, optionItemSection, clickType, eGlowPlayer, null);
            }
            if (option.equalsIgnoreCase("speed")) {
                if (hasEffect(eGlowPlayer))
                    updateSpeed(eGlowPlayer);
                getClickCommands(player, optionItemSection, clickType, eGlowPlayer, null);
            }
            if (option.equalsIgnoreCase("toggle")) {
                eGlowPlayer.toggleGlow();
                getClickCommands(player, optionItemSection, clickType, eGlowPlayer, null);
            }
            refreshMenu();
        }
        if (itemBuilder.getNBT().hasKey(getInstance(), "eglow-item")){
            ConfigurationSection itemSection = section.getConfigurationSection(itemBuilder.getNBT().getString(getInstance(), "eglow-item"));
            if (itemSection == null) return;
            getClickCommands(player, itemSection, clickType, eGlowPlayer, null);
            refreshMenu();
        }

    }

    private boolean handleRainbow(Player player, IEGlowPlayer eGlowPlayer, ClickType clickType, ConfigurationSection itemSection, IEGlowEffect color) {
        if (color.getName().contains("rainbow")) {
            if (getSpeed(clickType, itemSection)) {
                IEGlowEffect rainbowfast = DataManager.getEGlowEffect("rainbowfast");
                if (usePermissison(clickType, itemSection)) {
                    eGlowPlayer.activateGlow(rainbowfast);
                    getClickCommands(player, itemSection, clickType, eGlowPlayer, rainbowfast);
                    return true;
                }
                eGlowPlayer.activateGlow(rainbowfast);
                getNoPermissionCommands(player, itemSection, clickType, eGlowPlayer, rainbowfast);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setMenuItems() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getItems();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        menuMetadata.getOwner().openInventory(inventory);
                    }
                }.runTaskLater(getInstance(), 1L);
            }

        }.runTaskAsynchronously(getInstance());
    }

    public void refreshMenu(){
        new BukkitRunnable(){
            @Override
            public void run() {
                getItems();
            }
        }.runTask(getInstance());
    }

    private void getItems() {
        IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);
        for (String string : EGlowCustomGuiConfig.CustomGui.GET_ITEMS.get()) {
            ConfigurationSection section = EGlowCustomGuiConfig.CustomGui.GET_ITEM.getItem(string);
            if (section == null) continue;
            if (section.getInt("Slot") < 0) continue;
            if (section.getInt("Slot") > getSlots()) continue;
            if (section.getName().equalsIgnoreCase("data")) {
                inventory.setItem(section.getInt("Slot"),
                        ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                                .setString(getInstance(), "eglow-options", section.getName()).build());
                continue;
            }
            if (section.getName().equalsIgnoreCase("speed")) {
                inventory.setItem(section.getInt("Slot"),
                        ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                                .setString(getInstance(), "eglow-options", section.getName()).build());
                continue;
            }
            if (section.getName().equalsIgnoreCase("toggle")) {
                inventory.setItem(section.getInt("Slot"),
                        ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                                .setString(getInstance(), "eglow-options", section.getName()).build());
                continue;
            }
            IEGlowEffect effect;
            if (section.getName().toLowerCase().contains("rainbow"))
                effect = DataManager.getEGlowEffect("rainbowslow");
            else
                effect = DataManager.getEGlowEffect(section.getName());
            if (effect == null) {
                if (section.getString("Slots") != null){
                    String[] slots = section.getString("Slots").split("-");
                    if (slots.length != 2) continue;
                    int min = Integer.parseInt(section.getString("Slots").split("-")[0]);
                    int max = Integer.parseInt(section.getString("Slots").split("-")[1]);
                    for (int i = min; i <= max; i++) {
                        inventory.setItem(i, ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                                .setString(getInstance(), "eglow-item", section.getName())
                                .build());
                    }
                    continue;
                }
                if (section.getStringList("Slots").isEmpty()) {
                    inventory.setItem(section.getInt("Slot"), ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                            .setString(getInstance(), "eglow-item", section.getName())
                            .build());
                    continue;
                }
                for (String slot : section.getStringList("Slots")) {
                    inventory.setItem(Integer.parseInt(slot), ItemBuilder.getConfigItem(player, section, null, eGlowPlayer)
                            .setString(getInstance(), "eglow-item", section.getName())
                            .build());
                }
                continue;
            }
            if (!(player.hasPermission(effect.getPermission()) || Objects.requireNonNull(player.getPlayer(), "Unable to retrieve player").hasPermission("eglow.effect.*"))) {
                if (section.get("Permission") != null) {
                    inventory.setItem(section.getInt("Slot"), ItemBuilder.getConfigItem(player, section.getConfigurationSection("Permission"), effect, eGlowPlayer)
                            .setString(getInstance(), "eglow", section.getName())
                            .setString(getInstance(), "eglow-state", "Permission").build());
                    continue;
                }
            }
            if (new ItemPlaceholders().checkColor(eGlowPlayer, effect)) {
                if (section.get("Enabled") != null) {
                    inventory.setItem(section.getInt("Slot"), ItemBuilder.getConfigItem(player, section.getConfigurationSection("Enabled"), effect, eGlowPlayer)
                            .setString(getInstance(), "eglow", section.getName())
                            .setString(getInstance(), "eglow-state", "Enabled").build());
                    continue;
                }
            }
            if (section.get("Disabled") != null) {
                inventory.setItem(section.getInt("Slot"), ItemBuilder.getConfigItem(player, section.getConfigurationSection("Disabled"), effect, eGlowPlayer)
                        .setString(getInstance(), "eglow", section.getName())
                        .setString(getInstance(), "eglow-state", "Disabled").build());
                continue;
            }

            inventory.setItem(section.getInt("Slot"), ItemBuilder.getConfigItem(player, section, effect, eGlowPlayer).setString(getInstance(), "eglow", section.getName()).build());
        }
    }

    private boolean getBlink(ClickType type, ConfigurationSection section){
        CommandClickType commandClickType = CommandClickType.findByClickType(type) == null ? null : CommandClickType.findByClickType(type);
        if (commandClickType == null) return false;
        return new ClickCommands(getInstance(), player, section, commandClickType).getBlink();
    }

    private boolean getSpeed(ClickType type, ConfigurationSection section){
        CommandClickType commandClickType = CommandClickType.findByClickType(type) == null ? null : CommandClickType.findByClickType(type);
        if (commandClickType == null) return false;
        return new ClickCommands(getInstance(), player, section, commandClickType).getSpeed();
    }

    private boolean usePermissison(ClickType type, ConfigurationSection section){
        CommandClickType commandClickType = CommandClickType.findByClickType(type) == null ? null : CommandClickType.findByClickType(type);
        if (commandClickType == null) return false;
        return new ClickCommands(getInstance(), player, section, commandClickType).usePermission();
    }

    private void getClickCommands(Player player, ConfigurationSection section, ClickType type, IEGlowPlayer eGlowPlayer, IEGlowEffect effect) {
        CommandClickType commandClickType = CommandClickType.findByClickType(type) == null ? null : CommandClickType.findByClickType(type);
        if (commandClickType == null) return;
        new ClickCommands(getInstance(), player, section, commandClickType).runCommands(eGlowPlayer, effect);
    }

    private void getNoPermissionCommands(Player player, ConfigurationSection section, ClickType type, IEGlowPlayer eGlowPlayer, IEGlowEffect effect) {
        CommandClickType commandClickType = CommandClickType.findByClickType(type) == null ? null : CommandClickType.findByClickType(type);
        if (commandClickType == null) return;
        new ClickCommands(getInstance(), player, section, commandClickType).noPermissionCommands(eGlowPlayer, effect);
    }

}
