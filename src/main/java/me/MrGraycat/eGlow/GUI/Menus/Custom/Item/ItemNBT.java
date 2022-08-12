package me.MrGraycat.eGlow.GUI.Menus.Custom.Item;

import me.doublenico.hypeapi.itembuilder.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ItemNBT {

    private final ItemBuilder itemStack;

    public ItemNBT(ItemBuilder itemBuilder) {
        this.itemStack = itemBuilder;
    }

    public ItemBuilder getItemStack() {
        return itemStack;
    }

    public ItemMeta getItemMeta() {
        return itemStack.getItemMeta();
    }

    private String getString(String key){
        return getNBTWrapper().getString(key);
    }

    public String getString(Plugin plugin, String key){
        if (VersionUtils.lowerThan112()) return getString(key);
        if (VersionUtils.is113()) return itemStack.getItemStack().getItemMeta().getCustomTagContainer().getCustomTag(new NamespacedKey(plugin, key), ItemTagType.STRING);
        return itemStack.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }
    private boolean hasKey(String key){
        return getNBTWrapper().hasKey(key);
    }

    public boolean hasKey(Plugin plugin, String key){
        if (VersionUtils.lowerThan112()) return hasKey(key);
        if (VersionUtils.is113()) return itemStack.getItemStack().getItemMeta().getCustomTagContainer().hasCustomTag(new NamespacedKey(plugin, key), ItemTagType.STRING);
        return itemStack.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING);

    }

    public void setString(Plugin plugin, String key, String value){
        ItemMeta meta = itemStack.getItemStack().getItemMeta();
        if (VersionUtils.lowerThan112()) getNBTWrapper().setString(key, value);
        else if (VersionUtils.is113()) meta.getCustomTagContainer().setCustomTag(new NamespacedKey(plugin, key), ItemTagType.STRING, value);
        else meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        itemStack.getItemStack().setItemMeta(meta);
    }

    private NBTWrapper getNBTWrapper() {
        if (itemStack == null) return null;
        switch (VersionUtils.getNMSVersion()) {
            case "v1_9_R1": return new NBTItemStack_v1_9_R1(itemStack.getItemStack());
            case "v1_9_R2": return new NBTItemStack_v1_9_R2(itemStack.getItemStack());
            case "v1_10_R1": return new NBTItemStack_v1_10_R1(itemStack.getItemStack());
            case "v1_11_R1": return new NBTItemStack_v1_11_R1(itemStack.getItemStack());
            case "v1_12_R1": return new NBTItemStack_v1_12_R1(itemStack.getItemStack());
        }
        return null;
    }
}
