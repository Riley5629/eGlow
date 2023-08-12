package me.mrgraycat.eglow.gui.manager;

import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
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

public class MenuItemManager extends MenuManager {
	public String GLASS_PANE = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "STAINED_GLASS_PANE" : "CYAN_STAINED_GLASS_PANE";
	private final String GUNPOWDER = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SULPHUR" : "GUNPOWDER";
	private final String PLAYER_HEAD = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "SKULL_ITEM" : "PLAYER_HEAD";
	public String CLOCK = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "WATCH" : "CLOCK";
	public String ENDER_EYE = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) ? "EYE_OF_ENDER" : "ENDER_EYE";

	/**
	 * Create an itemstack
	 *
	 * @param material item material
	 * @param name     item name
	 * @param numb     item meta (only for 1.12.2 and below)
	 * @param lores    item lores
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material material, String name, int numb, String... lores) {
		ArrayList<String> lore = new ArrayList<>();

		for (String text : lores) {
			if (!text.isEmpty())
				lore.add(text);
		}

		return createItem(material, name, numb, lore, -1);
	}

	/**
	 * Create an itemstack
	 *
	 * @param material item material
	 * @param name     item name
	 * @param numb     item meta (only for 1.12.2 and below)
	 * @param lores    item lores
	 * @param model    item model id
	 * @return Item as Itemstack
	 */
	public ItemStack createItem(Material material, String name, int numb, List<String> lores, int model) {
		ItemStack item = (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && numb != 0) ? createLegacyItemStack(material, (short) numb) : new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		Objects.requireNonNull(meta, "Unable to set item name because ItemMeta is null").setDisplayName(ChatUtil.translateColors(name));

		if (model > 0)
			meta.setCustomModelData(model);

		if (!lores.isEmpty())
			meta.setLore(lores);

		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Create the skull of the player
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @return Playerskull of the player
	 */
	public ItemStack createPlayerSkull(EGlowPlayer eGlowPlayer) {
		ItemStack item = createItem(Material.valueOf(PLAYER_HEAD), Message.GUI_SETTINGS_NAME.get(), 3, createInfoLore(eGlowPlayer));

		if (!MainConfig.SETTINGS_GUI_RENDER_SKULLS.getBoolean())
			return item;

		try {
			SkullMeta meta = (SkullMeta) item.getItemMeta();

			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) {
				NMSHook.setOwningPlayer(meta, eGlowPlayer.getDisplayName());
			} else {
				Objects.requireNonNull(meta, "Unable to set skull owner because ItemMeta is null").setOwningPlayer(eGlowPlayer.getPlayer());
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
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @param color       name of the effect
	 * @param red         r value of rgb
	 * @param green       g value of rgb
	 * @param blue        b value of rgb
	 * @return colored leather chestplate
	 */
	public ItemStack createLeatherColor(EGlowPlayer eGlowPlayer, String color, int red, int green, int blue) {
		ItemStack item = createItem(Material.LEATHER_CHESTPLATE, Message.GUI_COLOR.get(color), 0, createColorLore(eGlowPlayer, color));
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
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @return glowstonedust/gunpowder
	 */
	public ItemStack createGlowingStatus(EGlowPlayer eGlowPlayer) {
		List<String> prelores = new ArrayList<>();

		prelores.add(Message.GUI_GLOWING.get() + ((eGlowPlayer.isGlowing()) ? Message.GUI_YES.get() : Message.GUI_NO.get()));
		prelores.add(Message.GUI_LAST_GLOW.get() + ((eGlowPlayer.getGlowEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : eGlowPlayer.getGlowEffect().getDisplayName()));
		prelores.add(Message.GUI_CLICK_TO_TOGGLE.get());

		String[] lores = new String[prelores.size()];
		return (eGlowPlayer.isGlowing()) ? createItem(Material.GLOWSTONE_DUST, Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores)) : createItem(Material.valueOf(GUNPOWDER), Message.GUI_GLOW_ITEM_NAME.get(), 0, prelores.toArray(lores));
	}

	public ItemStack createGlowVisibility(EGlowPlayer eGlowPlayer) {
		List<String> prelores = new ArrayList<>();
		GlowVisibility glowVisibility = eGlowPlayer.getGlowVisibility();

		prelores.add(ChatUtil.translateColors("&f") + Message.VISIBILITY_ALL.get());
		prelores.add(ChatUtil.translateColors("&f") + Message.VISIBILITY_OTHER.get());
		prelores.add(ChatUtil.translateColors("&f") + Message.VISIBILITY_OWN.get());
		prelores.add(ChatUtil.translateColors("&f") + Message.VISIBILITY_NONE.get());
		prelores.add(Message.GUI_CLICK_TO_CYCLE.get());

		switch (glowVisibility) {
			case ALL:
				prelores.set(0, Message.GLOW_VISIBILITY_INDICATOR.get() + prelores.get(0));
				break;
			case OTHER:
				prelores.set(1, Message.GLOW_VISIBILITY_INDICATOR.get() + prelores.get(1));
				break;
			case OWN:
				prelores.set(2, Message.GLOW_VISIBILITY_INDICATOR.get() + prelores.get(2));
				break;
			case NONE:
				prelores.set(3, Message.GLOW_VISIBILITY_INDICATOR.get() + prelores.get(3));
				break;
		}

		return createItem(Material.valueOf(ENDER_EYE), Message.GLOW_VISIBILITY_ITEM_NAME.get(), 0, prelores, 0);
	}

	/**
	 * Add enchanment effect to an item
	 *
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
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @param color       that this is for
	 * @return color lore
	 */
	private String[] createColorLore(EGlowPlayer eGlowPlayer, String color) {
		List<String> prelores = new ArrayList<>();
		EGlowEffect eglowColor = DataManager.getEGlowEffect(color.replace("-", ""));
		EGlowEffect eglowEffect = DataManager.getEGlowEffect("blink" + color.replace("-", "") + "slow");

		prelores.add(Message.GUI_LEFT_CLICK.get() + Message.COLOR.get(color));
		prelores.add(Message.GUI_COLOR_PERMISSION.get() + hasPermission(eGlowPlayer, Objects.requireNonNull(eglowColor, "Unable to retrieve permission from effect").getPermissionNode()));
		prelores.add(Message.GUI_RIGHT_CLICK.get() + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get(color));
		prelores.add(Message.GUI_BLINK_PERMISSION.get() + hasPermission(eGlowPlayer, Objects.requireNonNull(eglowEffect, "Unable to retrieve permission from effect").getPermissionNode()));

		String[] lores = new String[prelores.size()];
		return prelores.toArray(lores);
	}

	/**
	 * Create player info lore
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @return info lore
	 */
	private String[] createInfoLore(EGlowPlayer eGlowPlayer) {
		List<String> prelores = new ArrayList<>();

		prelores.add(Message.GUI_LAST_GLOW.get() + ChatUtil.getEffectChatName(eGlowPlayer));
		prelores.add(Message.GUI_GLOW_ON_JOIN.get() + ((eGlowPlayer.isGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get()));
		prelores.add(Message.GUI_CLICK_TO_TOGGLE.get());

		String[] lores = new String[prelores.size()];
		return prelores.toArray(lores);
	}

	/**
	 * Create speed lore
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @return speed lore
	 */
	public String[] createSpeedLore(EGlowPlayer eGlowPlayer) {
		List<String> prelores = new ArrayList<>();

		if (eGlowPlayer.getGlowEffect() != null) {
			String effect = eGlowPlayer.getGlowEffect().getName();

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
	 *
	 * @param eGlowPlayer which is looking at the gui
	 * @return true if the player has an effect & has it enabled, false if not
	 */
	public boolean hasEffect(EGlowPlayer eGlowPlayer) {
		return eGlowPlayer.getGlowEffect() != null && (eGlowPlayer.isGlowing()) && (eGlowPlayer.getGlowEffect().getName().contains("slow") || eGlowPlayer.getGlowEffect().getName().contains("fast"));
	}

	/**
	 * Check if the player has a specific permission
	 *
	 * @param eGlowPlayer to check this for
	 * @param permission  to check for
	 * @return yes if the player has permission, no if not
	 */
	public String hasPermission(EGlowPlayer eGlowPlayer, String permission) {
		Player player = eGlowPlayer.getPlayer();
		return (player.hasPermission(permission) || player.hasPermission("eglow.effect.*") || player.isOp()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
	}

	private ItemStack createLegacyItemStack(Material material, short numb) {
		try {
			return (ItemStack) NMSHook.nms.getItemStack.newInstance(material, 1, numb);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new ItemStack(Material.AIR);
	}
}