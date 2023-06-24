package me.MrGraycat.eGlow.GUI.Manager;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

//TODO more special item support (custom effects)

public class MenuItemManager extends MenuManager {
	public String GLASS_PANE = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "STAINED_GLASS_PANE" : "CYAN_STAINED_GLASS_PANE";
	private final String GUNPOWDER = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SULPHUR" : "GUNPOWDER";
	private final String PLAYER_HEAD = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SKULL_ITEM" : "PLAYER_HEAD";
	public String CLOCK = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "WATCH" : "CLOCK";

	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param lores item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, String... lores) {
		ItemStack item = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && numb != 0) ? createLegacyItemStack(mat, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		Objects.requireNonNull(meta, "Unable to set item name because ItemMeta is null").setDisplayName(ChatUtil.translateColors(name));
		
		for (String text : lores) {
			if (!text.isEmpty())
				lore.add(text);
		}
		
		if (!lore.isEmpty())
			meta.setLore(lore);
		
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param lores item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, List<String> lores) {
		ItemStack item = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && numb != 0) ? createLegacyItemStack(mat, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		Objects.requireNonNull(meta, "Unable to set item name because ItemMeta is null").setDisplayName(ChatUtil.translateColors(name));
		
		for (String text : lores) {
			if (!text.isEmpty())
				lore.add(text);
		}
		
		if (!lore.isEmpty())
			meta.setLore(lore);
		
		item.setItemMeta(meta);
		return item;
	}
	
	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param lores item lores
	 * @param model item model id
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, List<String> lores, int model) {
		ItemStack item = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && numb != 0) ? createLegacyItemStack(mat, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		Objects.requireNonNull(meta, "Unable to set item name because ItemMeta is null").setDisplayName(ChatUtil.translateColors(name));

		if (model != 0)
			meta.setCustomModelData(model);
		
		for (String text : lores) {
			if (!text.isEmpty())
				lore.add(text);
		}
		
		if (!lore.isEmpty())
			meta.setLore(lore);
		
		item.setItemMeta(meta);
		return item;
	}
	
	/**
	 * Create the skull of the player
	 * @param player which is looking at the gui
	 * @return Playerskull of the player
	 */
	public ItemStack createPlayerSkull(IEGlowPlayer player) {
		ItemStack item = createItem(Material.valueOf(PLAYER_HEAD), Message.GUI_SETTINGS_NAME.get(), 3, createInfoLore(player));
		
		if (!MainConfig.SETTINGS_GUI_RENDER_SKULLS.getBoolean())
			return item;
		
		try {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) {
				NMSHook.setOwningPlayer(meta, player.getDisplayName());
			} else {
				Objects.requireNonNull(meta, "Unable to set skull owner because ItemMeta is null").setOwningPlayer(player.getPlayer());
			}
			
			item.setItemMeta(meta);
			return item;
		} catch (ConcurrentModificationException e) {
			//Fail-safe when the server is unable to get the skin
			return item;
		}
	}
	
	/**
	 * Create colored leather armour based on the rgb values
	 * @param player which is looking at the gui
	 * @param color name of the effect
	 * @param red r value of rgb
	 * @param green g value of rgb
	 * @param blue b value of rgb
	 * @return colored leather chestplate
	 */
	public ItemStack createLeatherColor(IEGlowPlayer player, String color, int red, int green, int blue) {
		ItemStack item = createItem(Material.LEATHER_CHESTPLATE, Message.GUI_COLOR.get(color), 0, createColorLore(player, color));
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		
		Objects.requireNonNull(meta, "Unable to set item color because ItemMeta is null").setColor(Color.fromRGB(red, green, blue));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		if (ProtocolVersion.SERVER_VERSION.getNetworkId() > 751)
			meta.addItemFlags(ItemFlag.valueOf("HIDE_DYE"));
		
		item.setItemMeta(meta);
		return item;
	}
	
	/**
	 * Create item based on the glow status
	 * @param player which is looking at the gui
	 * @return glowstonedust/gunpowder 
	 */
	public ItemStack createGlowingStatus(IEGlowPlayer player) {
		List<String> prelores = new ArrayList<>();
		
		prelores.add(Message.GUI_GLOWING.get() + ((player.isGlowing()) ? Message.GUI_YES.get() : Message.GUI_NO.get()));
		prelores.add(Message.GUI_LAST_GLOW.get() + ((player.getEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : player.getEffect().getDisplayName()));
		prelores.add(Message.GUI_CLICK_TO_TOGGLE.get());
		
		String[] lores = new String[prelores.size()];
		return (player.isGlowing()) ? createItem(Material.GLOWSTONE_DUST, Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores)) : createItem(Material.valueOf(GUNPOWDER), Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores));
	}
	
	/**
	 * Add enchanment effect to an item
	 * @param item to add this effect for
	 * @return enchanted varient of the item
	 */
	public ItemStack setItemGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		Objects.requireNonNull(meta, "Unable to set item enchantment because ItemMeta is null").addEnchant(Enchantment.DURABILITY, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		item.setItemMeta(meta);
		return item;
	}
	
	/**
	 * Create color lore
	 * @param player which is looking at the gui
	 * @param color that this is for
	 * @return color lore
	 */
	private String[] createColorLore(IEGlowPlayer player, String color) {
		List<String> prelores = new ArrayList<>();
		IEGlowEffect eglowColor = DataManager.getEGlowEffect(color.replace("-", ""));
		IEGlowEffect eglowEffect = DataManager.getEGlowEffect("blink" + color.replace("-", "") + "slow");
		
		prelores.add(Message.GUI_LEFT_CLICK.get() + Message.COLOR.get(color));
		prelores.add(Message.GUI_COLOR_PERMISSION.get() + hasPermission(player, Objects.requireNonNull(eglowColor, "Unable to retrieve permission from effect").getPermission()));
		prelores.add(Message.GUI_RIGHT_CLICK.get() + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get(color));
		prelores.add(Message.GUI_BLINK_PERMISSION.get() + hasPermission(player, Objects.requireNonNull(eglowEffect, "Unable to retrieve permission from effect").getPermission()));
		
		String[] lores = new String[prelores.size()];
		return prelores.toArray(lores);
	}
	
	/**
	 * Create player info lore
	 * @param player which is looking at the gui
	 * @return info lore
	 */
	private String[] createInfoLore(IEGlowPlayer player) {
		List<String> prelores = new ArrayList<>();
		
		prelores.add(Message.GUI_LAST_GLOW.get() + ChatUtil.getEffectChatName(player));
		prelores.add(Message.GUI_GLOW_ON_JOIN.get() + ((player.getGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get()));
		prelores.add(Message.GUI_CLICK_TO_TOGGLE.get());

		String[] lores = new String[prelores.size()];
		return prelores.toArray(lores);
	}
	
	/**
	 * Create speed lore
	 * @param player which is looking at the gui
	 * @return speed lore
	 */
	public String[] createSpeedLore(IEGlowPlayer player) {
		List<String> prelores = new ArrayList<>();
		
		if (player.getEffect() != null) {
			String effect = player.getEffect().getName();
				
			if (effect.contains("slow"))
				prelores.add(Message.GUI_SPEED.get() + Message.COLOR.get("slow"));
				
			if (effect.contains("fast"))
				prelores.add(Message.GUI_SPEED.get() + Message.COLOR.get("fast"));			
		}
		
		String[] lores = new String[prelores.size()];
		return prelores.toArray(lores);
	}
	
	/**
	 * Check if the player has an effect
	 * @param player which is looking at the gui
	 * @return true if the player has an effect & has it enabled, false if not
	 */
	public boolean hasEffect(IEGlowPlayer player) {
		return player.getEffect() != null && (player.isGlowing()) && (player.getEffect().getName().contains("slow") || player.getEffect().getName().contains("fast"));
	}
	
	/**
	 * Check if the player has a specific permission
	 * @param player to check this for
	 * @param permission to check for
	 * @return yes if the player has permission, no if not
	 */
	public String hasPermission(IEGlowPlayer player, String permission) {
		Player p = player.getPlayer();
		return (p.hasPermission(permission) || p.hasPermission("eglow.effect.*") || p.isOp()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
	}

	public EGlow getInstance() {
		return EGlow.getInstance();
	}

	private ItemStack createLegacyItemStack(Material mat, short j) {
		try {
			return (ItemStack) NMSHook.nms.getItemStack.newInstance(mat, 1, j);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new ItemStack(Material.AIR);
	}
}