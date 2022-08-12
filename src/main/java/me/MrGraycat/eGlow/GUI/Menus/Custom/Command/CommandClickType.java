package me.MrGraycat.eGlow.GUI.Menus.Custom.Command;

import org.bukkit.event.inventory.ClickType;

public enum CommandClickType {

    RIGHT(ClickType.RIGHT, "Right-Click"),
    LEFT(ClickType.LEFT, "Left-Click"),
    SHIFT_RIGHT(ClickType.SHIFT_RIGHT, "Shift-Right-Click"),
    SHIFT_LEFT(ClickType.SHIFT_LEFT, "Shift-Left-Click"),
    MIDDLE(ClickType.MIDDLE, "Middle-Click"),
    NO_PERMISSION(null, null);

    private final ClickType clickType;
    private final String configurationName;

    CommandClickType(ClickType clickType, String configurationName) {
        this.clickType = clickType;
        this.configurationName = configurationName;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public static CommandClickType findByClickType(ClickType clickType) {
        if (clickType == null) return null;
        for (CommandClickType commandClickType : values()) {
            if (commandClickType.getClickType() == null) continue;
            if (commandClickType.getClickType().equals(clickType)) {
                return commandClickType;
            }
        }
        return null;
    }


}
