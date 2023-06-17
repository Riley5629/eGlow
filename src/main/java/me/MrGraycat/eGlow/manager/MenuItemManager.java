package me.MrGraycat.eGlow.manager;

import me.MrGraycat.eGlow.config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.manager.glow.IEGlowEffect;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.packet.NMSHook;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
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
import java.util.*;
import java.util.stream.Collectors;

//TODO more special item support (custom effects)

public class MenuItemManager extends MenuManager {

	protected final String GLASS_PANE = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "STAINED_GLASS_PANE" : "CYAN_STAINED_GLASS_PANE";
	protected final String GUNPOWDER = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SULPHUR" : "GUNPOWDER";
	protected final String PLAYER_HEAD = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SKULL_ITEM" : "PLAYER_HEAD";
	protected final String CLOCK = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "WATCH" : "CLOCK";

	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param loreLines item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, String... loreLines) {
		return createItem(mat, name, numb, Arrays.asList(loreLines));
	}

	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param loreLines item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, List<String> loreLines) {
		return createItem(mat, name, numb, loreLines, 0);
	}
	
	/**
	 * Create an itemstack
	 * @param mat item material
	 * @param name item name
	 * @param numb item meta (only for 1.12.2 and below)
	 * @param loreLines item lores
	 * @param model item model id
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material mat, String name, int numb, List<String> loreLines, int model) {
		ItemStack item = createItemStack(mat, numb);

		ItemMeta meta = item.getItemMeta();
		List<String> lore = loreLines.stream()
				.filter(line -> !line.isEmpty())
				.map(ChatUtil::translateColors)
				.collect(Collectors.toList());

		Objects.requireNonNull(meta, "Unable to set item name because ItemMeta is null")
				.setDisplayName(ChatUtil.translateColors(name));

		if (model != 0) {
			meta.setCustomModelData(model);
		}

		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

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
		List<String> lore = Arrays.asList(
				Message.GUI_GLOWING.get() + ((player.isGlowing()) ? Message.GUI_YES.get() : Message.GUI_NO.get()),
				Message.GUI_LAST_GLOW.get() + ((player.getGlowEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : player.getGlowEffect().getDisplayName()),
				Message.GUI_CLICK_TO_TOGGLE.get()
		);

		return player.isGlowing() ? createItem(Material.GLOWSTONE_DUST, Message.GUI_GLOW_ITEM_NAME.get(),
				0, lore) : createItem(Material.valueOf(GUNPOWDER),
				Message.GUI_GLOW_ITEM_NAME.get(), 0, lore);
	}
	
	/**
	 * Add enchanment effect to an item
	 * @param item to add this effect for
	 * @return enchanted varient of the item
	 */
	public ItemStack setItemGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		Objects.requireNonNull(meta, "Unable to set item enchantment because ItemMeta is null")
				.addEnchant(Enchantment.DURABILITY, 1, false);
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
		IEGlowEffect eglowColor = DataManager.getEGlowEffect(color.replace("-", ""));
		IEGlowEffect eglowEffect = DataManager.getEGlowEffect("blink" + color.replace("-", "") + "slow");

		return Arrays.asList(
				Message.GUI_LEFT_CLICK.get() + Message.COLOR.get(color),
				Message.GUI_COLOR_PERMISSION.get() + hasPermission(player,
						Objects.requireNonNull(eglowColor, "Unable to retrieve permission from effect").getPermission()),
				Message.GUI_RIGHT_CLICK.get() + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get(color),
				Message.GUI_BLINK_PERMISSION.get() + hasPermission(player,
						Objects.requireNonNull(eglowEffect, "Unable to retrieve permission from effect").getPermission())
		).toArray(new String[0]);
	}
	
	/**
	 * Create player info lore
	 * @param player which is looking at the gui
	 * @return info lore
	 */
	private String[] createInfoLore(IEGlowPlayer player) {
		return Arrays.asList(
				Message.GUI_LAST_GLOW.get() + ChatUtil.getEffectChatName(player),
				Message.GUI_GLOW_ON_JOIN.get() + ((player.isGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get()),
				Message.GUI_CLICK_TO_TOGGLE.get()
		).toArray(new String[0]);
	}
	
	/**
	 * Create speed lore
	 * @param player which is looking at the gui
	 * @return speed lore
	 */
	public String[] createSpeedLore(IEGlowPlayer player) {
		List<String> prelores = new ArrayList<>();
		
		if (player.getGlowEffect() != null) {
			String effect = player.getGlowEffect().getName();
				
			if (effect.contains("slow"))
				prelores.add(Message.GUI_SPEED.get() + Message.COLOR.get("slow"));
				
			if (effect.contains("fast"))
				prelores.add(Message.GUI_SPEED.get() + Message.COLOR.get("fast"));			
		}

		return prelores.toArray(new String[0]);
	}
	
	/**
	 * Check if the player has an effect
	 * @param player which is looking at the gui
	 * @return true if the player has an effect & has it enabled, false if not
	 */
	public boolean hasEffect(IEGlowPlayer player) {
		return player.getGlowEffect() != null &&
				(player.isGlowing()) && (player.getGlowEffect().getName().contains("slow") ||
				player.getGlowEffect().getName().contains("fast"));
	}
	
	/**
	 * Check if the player has a specific permission
	 * @param player to check this for
	 * @param permission to check for
	 * @return yes if the player has permission, no if not
	 */
	public String hasPermission(IEGlowPlayer player, String permission) {
		Player p = player.getPlayer();

		return p.hasPermission(permission) ||
				p.hasPermission("eglow.effect.*") ? Message.GUI_YES.get() : Message.GUI_NO.get();
	}

	public EGlow getInstance() {
		return EGlow.getInstance();
	}

	private ItemStack createLegacyItemStack(Material mat, short j) {
		try {
			return (ItemStack) NMSHook.nms.itemStackConstructor.newInstance(mat, 1, j);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new ItemStack(Material.AIR);
	}

	protected ItemStack createItemStack(Material material, int amount) {
		return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && amount != 0) ?
				createLegacyItemStack(material, (short) amount) :
				new ItemStack(material);
	}
}