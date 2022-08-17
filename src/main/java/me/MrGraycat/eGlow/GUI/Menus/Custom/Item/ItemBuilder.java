package me.MrGraycat.eGlow.GUI.Menus.Custom.Item;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.MrGraycat.eGlow.Util.Text.ItemPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final Material material;
    private final ItemStack itemStack;
    private int amount;
    private String name;
    private List<String> lore;
    private boolean enchanted;
    private boolean hideFlags;
    private boolean hideAttributes;
    private boolean hideEnchants;
    private HashMap<Enchantment, Integer> enchants;
    private int customModelData;


    /**
     * Creates a new ItemBuilder with the given material.
     * Material must be a valid material, or else DIRT will be used.
     *
     * @param material The material of the item.
     */
    public ItemBuilder(Material material) {
        this.material = material == null ? Material.DIRT : material;
        itemStack = new ItemStack(this.material);
    }

    /**
     * Creates a new ItemBuilder with the given material.
     * Material must be a valid material, or else DIRT will be used.
     *
     * @param material The material of the item.
     */
    public ItemBuilder(String material) {
        this.material = Material.getMaterial(material) == null ? Material.DIRT : Material.getMaterial(material);
        if (this.material == Material.DIRT) ChatUtil.sendToConsole("&f[&eeGlow&f]: &cInvalid material: " + material + " &cusing DIRT...", false);
        itemStack = new ItemStack(this.material);
    }

    public ItemBuilder(String material, int number){
        this.material = Material.getMaterial(material) == null ? Material.DIRT : Material.getMaterial(material);
        if (this.material == Material.DIRT) ChatUtil.sendToConsole("&f[&eeGlow&f]: &cInvalid material: " + material + " &cusing DIRT...", false);
        itemStack = number != 0 ? createLegacyItemStack(this.material, (short) number) : new ItemStack(this.material);
    }

    public ItemBuilder(ItemStack itemStack){
        this.material = itemStack.getType();
        this.itemStack = itemStack;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemMeta getItemMeta() {
        return itemStack.getItemMeta();
    }

    /**
     *
     * @return Build a new ItemStack with the current settings.
     */
    public ItemStack build(){
        if (amount != 0) itemStack.setAmount(amount);
        if(itemStack.getItemMeta() == null) return itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        if(name != null) meta.setDisplayName(name);
        if(lore != null) meta.setLore(lore);
        if(enchanted) meta.addEnchant(Enchantment.DURABILITY, 1, true);
        if(hideFlags) meta.addItemFlags(ItemFlag.values());
        if(hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(hideEnchants) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if(enchants != null && !enchants.isEmpty()) enchants.keySet().forEach(enchant -> meta.addEnchant(enchant, enchants.get(enchant), true));
        if(customModelData != 0) meta.setCustomModelData(customModelData);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public int getAmount() {
        return amount;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setLore(String... lore){
        this.lore = Arrays.asList(lore);
        return this;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public ItemBuilder setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
        return this;
    }

    public boolean isHideFlags() {
        return hideFlags;
    }

    public ItemBuilder setHideFlags(boolean hideFlags) {
        this.hideFlags = hideFlags;
        return this;
    }

    public boolean isHideAttributes() {
        return hideAttributes;
    }

    public ItemBuilder setHideAttributes(boolean hideAttributes) {
        this.hideAttributes = hideAttributes;
        return this;
    }

    public boolean isHideEnchants() {
        return hideEnchants;
    }

    public ItemBuilder setHideEnchants(boolean hideEnchants) {
        this.hideEnchants = hideEnchants;
        return this;
    }

    public HashMap<Enchantment, Integer> getEnchants(){
        return enchants;
    }

    public ItemBuilder setEnchants(HashMap<Enchantment, Integer> enchants){
        this.enchants = enchants;
        return this;
    }

    public void clearEnchants() {
        if (enchants != null) enchants.clear();
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level){
        if(enchants == null) enchants = new HashMap<>();
        enchants.put(enchantment, level);
        return this;
    }

    public boolean hasEnchant(Enchantment enchantment, int level){
        if(enchants == null) return false;
        return enchants.containsKey(enchantment) && enchants.get(enchantment) == level;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment){
        if(enchants == null) return this;
        enchants.remove(enchantment);
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment, int level){
        if(enchants == null) return this;
        if(!hasEnchant(enchantment, level)) return this;
        enchants.remove(enchantment);
        return this;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    /**
     * Used to get the NBT Data for the itemBuilder.
     * If you want to set the NBT Data, use the ItemBuilder methods.
     *
     * @return see {@link ItemNBT}
     */
    public ItemNBT getNBT(){
        return new ItemNBT(this);
    }

    public ItemBuilder setString(Plugin plugin, String key, String value){
        getNBT().setString(plugin, key, value);
        return this;
    }

    public String getString(Plugin plugin, String key){
        return getNBT().getString(plugin, key);
    }


    public static ItemBuilder getConfigItem(Player player, ConfigurationSection section, IEGlowEffect effect, IEGlowPlayer glowPlayer) {
        ItemBuilder item;
        if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) {
            if(section.getString("Material") != null && section.getString("Material").startsWith("PlayerHead") ||
                    section.getString("Material") != null && section.getString("Material").startsWith("BaseValue"))
                item = new ItemBuilder(checkHooks(player, createLegacyItemStack(Material.valueOf("SKULL_ITEM"), (short)3) , section));
            else
                item = new ItemBuilder(ChatUtil.convertedText(player, section.getString("Material")), section.getInt("Meta"));
        }
        else {
            if(section.getString("Material") != null && section.getString("Material").startsWith("PlayerHead") ||
                    section.getString("Material") != null && section.getString("Material").startsWith("BaseValue")) {
                item = new ItemBuilder(checkHooks(player, new ItemStack(Material.PLAYER_HEAD), section));
            }
            else {
                item = new ItemBuilder(ChatUtil.convertedText(player, section.getString("Material")));
            }
        }
        if (section.get("Displayname") != null) item.setName(ChatUtil.convertedColoredText(player, new ItemPlaceholders().getPlaceholders(glowPlayer, effect, section.getString("Displayname"))));
        if (section.get("Lore") != null) item.setLore(section.getStringList("Lore").stream().map(s -> ChatUtil.convertedColoredText(player, new ItemPlaceholders().getPlaceholders(glowPlayer, effect, s))).collect(Collectors.toList()));
        item.setAmount(Integer.parseInt(ChatUtil.convertedText(player, section.getString("Amount", "1"))));
        item.setEnchanted(section.getBoolean("Enchanted", false));
        item.setHideAttributes(section.getBoolean("Hide-Attributes", false));
        item.setHideEnchants(section.getBoolean("Hide-Enchants", false));
        item.setHideFlags(section.getBoolean("Hide-Flags", false));
        item.setCustomModelData(section.getInt("CustomModelData", 0));
        if (section.get("Enchantments") != null) {
            for (String enchant : section.getStringList("Enchantments")) {
                String[] split = enchant.split(";");
                if (split.length == 0) continue;
                split[0] = ChatUtil.convertedText(player, split[0]);
                if (split[0] == null || Enchantment.getByName(split[0]) == null) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Enchantment &c" + enchant + " &7is not valid.", false);
                    continue;
                }
                if (split.length == 1)
                    item.addEnchant(Enchantment.getByName(split[0]), 1);
                if (split.length == 2) {
                    split[1] = ChatUtil.convertedText(player, split[1]);
                    if (split[1] == null || !isNumber(split[1])) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Enchantment level &c" + split[1] + " &7is not valid.", false);
                        continue;
                    }
                    item.addEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
                }
            }
        }
        item.getItemMeta().addItemFlags(section.getStringList("ItemFlags").stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new));
        if (section.getString("Color") != null) {
            String[] split = section.getString("Color").split(",");
            if (split.length >= 3) {
                boolean isValid = true;
                split[0] = ChatUtil.convertedText(player, split[0]);
                split[1] = ChatUtil.convertedText(player, split[1]);
                split[2] = ChatUtil.convertedText(player, split[2]);
                if (split[0] == null || !isNumber(split[0])) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Color red &c" + split[0] + " &7is not valid.", false);
                    isValid = false;
                }
                if (split[1] == null || !isNumber(split[1])) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Color green &c" + split[1] + " &7is not valid.", false);
                    isValid = false;
                }
                if (split[2] == null || !isNumber(split[2])) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Color blue &c" + split[2] + " &7is not valid.", false);
                    isValid = false;
                }
                if (item.getItemMeta() instanceof LeatherArmorMeta) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                    if (isValid)
                        meta.setColor(Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                    item.itemStack.setItemMeta(meta);
                }
                if (item.getItemMeta() instanceof PotionMeta) {
                    PotionMeta meta = (PotionMeta) item.getItemMeta();
                    if (isValid)
                        meta.setColor(Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                    item.itemStack.setItemMeta(meta);
                }
            }
        }
        if (section.get("PotionEffect") != null){
            if (item.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                String[] split = section.getString("PotionEffect").split(";");
                if (split.length == 0) return item;
                if (split[0] == null || PotionEffectType.getByName(split[0]) == null) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect &c" + section.getString("PotionEffect") + " &7is not valid.", false);
                    return item;
                }
                if (split.length == 1) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect &c" + section.getString("PotionEffect") + " &7is not valid.", false);
                    return item;
                }
                if (split.length == 2) {
                    split[1] = ChatUtil.convertedText(player, split[1]);
                    if (split[1] == null || !isNumber(split[1])) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect duration &c" + split[1] + " &7is not valid.", false);
                        return item;
                    }
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), 0), true);
                }
                if (split.length == 3) {
                    split[2] = ChatUtil.convertedText(player, split[2]);
                    if (split[1] == null || !isNumber(split[1])) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect duration &c" + split[1] + " &7is not valid.", false);
                        return item;
                    }
                    if (split[2] == null || !isNumber(split[2])) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect amplifier &c" + split[2] + " &7is not valid.", false);
                        return item;
                    }
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])), true);
                }
                item.itemStack.setItemMeta(meta);
            }
        }
        if (!section.getStringList("PotionEffects").isEmpty()){
            if (item.getItemMeta() instanceof PotionMeta) {
                for (String potion : section.getStringList("PotionEffects")) {
                    PotionMeta meta = (PotionMeta) item.getItemMeta();
                    String[] split = potion.split(";");
                    if (split.length == 0) continue;
                    split[0] = ChatUtil.convertedText(player, split[0]);
                    if (split[0] == null || PotionEffectType.getByName(split[0]) == null) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect &c" + potion + " &7is not valid.", false);
                        continue;
                    }
                    if (split.length == 1) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect &c" + potion + " &7is not valid.", false);
                        continue;
                    }
                    if (split.length == 2) {
                        split[1] = ChatUtil.convertedText(player, split[1]);
                        if (split[1] == null || !isNumber(split[1])) {
                            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect duration &c" + split[1] + " &7is not valid.", false);
                            continue;
                        }
                        meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), 0), true);
                    }
                    if (split.length == 3) {
                        split[2] = ChatUtil.convertedText(player, split[2]);
                        if (split[1] == null || !isNumber(split[1])) {
                            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect duration &c" + split[1] + " &7is not valid.", false);
                            continue;
                        }
                        if (split[2] == null || !isNumber(split[2])) {
                            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PotionEffect amplifier &c" + split[2] + " &7is not valid.", false);
                            continue;
                        }
                        meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])), true);
                    }
                    item.itemStack.setItemMeta(meta);
                }
            }
        }
        if (section.get("Patterns") != null){
            if (item.getItemMeta() instanceof BannerMeta){
                BannerMeta meta = (BannerMeta) item.getItemMeta();
                for (String pattern : section.getStringList("Patterns")) {
                    String[] split = pattern.split(";");
                    if (split.length == 0) continue;
                    split[0] = ChatUtil.convertedText(player, split[0]);
                    split[1] = ChatUtil.convertedText(player, split[1]);
                    if (split[0] == null) {
                        ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Pattern &c" + pattern + " &7is not valid.", false);
                        continue;
                    }
                    if (split.length == 2) {
                        split[1] = ChatUtil.convertedText(player, split[1]);
                        if (split[1] == null) {
                            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Pattern color &7is not valid.", false);
                            continue;
                        }
                        meta.addPattern(new Pattern(DyeColor.valueOf(split[1]), PatternType.valueOf(split[0])));
                    }
                    item.getItemStack().setItemMeta(meta);
                }
            }
        }
        return item;
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ItemStack createLegacyItemStack(Material mat, short j) {
        try {
            return (ItemStack) NMSHook.nms.getItemStack.newInstance(mat, 1, j);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return new ItemStack(Material.DIRT);
    }

    private static ItemStack checkHooks(Player player, ItemStack item, ConfigurationSection section) {
        if (section.getString("Material").startsWith("PlayerHead-")) {
            String[] split = section.getString("Material").split("-");
            if (split.length == 2) {
                if (split[1] == null) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PlayerHead is not valid.", false);
                    return item;
                }
                split[1] = ChatUtil.convertedText(player, split[1]);
                if (!(item.getItemMeta() instanceof SkullMeta)){
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7PlayerHead is not valid.", false);
                    return item;
                }
                try {
                    SkullMeta meta = (SkullMeta) item.getItemMeta();

                    if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) {
                        NMSHook.setOwningPlayer(meta, split[1]);
                        item.setItemMeta(meta);
                    } else {
                        if (meta.hasOwner())
                            return item;
                        Objects.requireNonNull(meta, "Unable to set skull owner because ItemMeta is null").setOwningPlayer(Bukkit.getOfflinePlayer(split[1]));
                        item.setItemMeta(meta);
                    }
                } catch (ConcurrentModificationException ignored) {
                }
            }
        }
        if (section.getString("Material").startsWith("BaseValue-")){
            String[] split = section.getString("Material").split("-");
            if (split.length == 2) {
                if (split[1] == null) {
                    ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7BaseValue is not valid.", false);
                    return item;
                }
                return getValueHead(item, split[1]);
            }
        }
        return item;
    }

    public static ItemStack getValueHead(ItemStack skull, String value) {
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }

}
