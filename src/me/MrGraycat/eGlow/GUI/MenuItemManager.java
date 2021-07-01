package me.MrGraycat.eGlow.GUI;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

@SuppressWarnings("deprecation")
public class MenuItemManager {
	
	public String GLASS_PANE = (EGlow.getDebugUtil().getMinorVersion() <= 12) ? "STAINED_GLASS_PANE" : "CYAN_STAINED_GLASS_PANE";
	private String GUNPOWDER = (EGlow.getDebugUtil().getMinorVersion() <= 12) ? "SULPHUR" : "GUNPOWDER";
	private String PLAYER_HEAD = (EGlow.getDebugUtil().getMinorVersion() <= 12) ? "SKULL_ITEM" : "PLAYER_HEAD";
	public String CLOCK = (EGlow.getDebugUtil().getMinorVersion() <= 12) ? "WATCH" : "CLOCK";
	
	//When custom GUI is a thing this one will not be needed anymore
	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param lores item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, String... lores) {
		ItemStack item = (EGlow.getDebugUtil().getMinorVersion() <= 12 && numb != 0) ? new ItemStack(mat, 1, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		meta.setDisplayName(ChatUtil.translateColors(name));
		
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
		ItemStack item = (EGlow.getDebugUtil().getMinorVersion() <= 12 && numb != 0) ? new ItemStack(mat, 1, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		meta.setDisplayName(ChatUtil.translateColors(name));
		
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
		ItemStack item = (EGlow.getDebugUtil().getMinorVersion() <= 12 && numb != 0) ? new ItemStack(mat, 1, (short) numb) : new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		
		meta.setDisplayName(ChatUtil.translateColors(name));
		//meta.setCustomModelData(model); //TODO check for readding this
		
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
		
		if (!EGlowMainConfig.OptionRenderPlayerSkulls())
			return item;
		
		try {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			
			if (EGlow.getDebugUtil().getMinorVersion() <= 12) {
				meta.setOwner(player.getDisplayName());
			} else {
				meta.setOwningPlayer(player.getPlayer());
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
		
		meta.setColor(Color.fromRGB(red, green, blue));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
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
		
		prelores.add(Message.GUI_GLOWING.get() + ((player.getFakeGlowStatus() || player.getGlowStatus()) ? Message.GUI_YES.get() : Message.GUI_NO.get()));
		prelores.add(Message.GUI_LAST_GLOW.get() + ((player.getEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : player.getEffect().getDisplayName()));
		prelores.add(Message.GUI_CLICK_TO_TOGGLE.get());
		
		String[] lores = new String[prelores.size()];
		return (player.getFakeGlowStatus() || player.getGlowStatus()) ? createItem(Material.GLOWSTONE_DUST, Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores)) : createItem(Material.valueOf(GUNPOWDER), Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores));
	}
	
	/**
	 * Add enchanment effect to an item
	 * @param item to add this effect for
	 * @return enchanted varient of the item
	 */
	public ItemStack setItemGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		meta.addEnchant(Enchantment.DURABILITY, 1, false);
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
		IEGlowEffect eglowColor = EGlow.getDataManager().getEGlowEffect(color.replace("-", ""));
		IEGlowEffect eglowEffect = EGlow.getDataManager().getEGlowEffect("blink" + color.replace("-", "") + "slow");
		
		prelores.add(Message.GUI_LEFT_CLICK.get() + Message.COLOR.get(color));
		prelores.add(Message.GUI_COLOR_PERMISSION.get() + hasPermission(player, eglowColor.getPermission()));
		prelores.add(Message.GUI_RIGHT_CLICK.get() + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get(color));
		prelores.add(Message.GUI_BLINK_PERMISSION.get() + hasPermission(player, eglowEffect.getPermission()));
		
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
		List<String> prelores = new ArrayList<String>();
		
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
		return (player.getEffect() != null && (player.getGlowStatus() || player.getFakeGlowStatus()) && (player.getEffect().getName().contains("slow") || player.getEffect().getName().contains("fast"))) ? true : false;
	}
	
	/**
	 * Check if the player has a specific permission
	 * @param player to check this for
	 * @param permission to check for
	 * @return true if the player has permission, false if not
	 */
	public String hasPermission(IEGlowPlayer player, String permission) {
		return (player.getPlayer().hasPermission(permission)) ? Message.GUI_YES.get() : Message.GUI_NO.get();
	}
}