package me.MrGraycat.eGlow.GUI.Menus;

import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig.Effect;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.GUI.PaginatedMenu;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EGlowEffectMenu extends PaginatedMenu {
	private ConcurrentHashMap<Integer, String> effects = new ConcurrentHashMap<>();	
	
	public EGlowEffectMenu(Player player) {
		super(player);
	}

	@Override
	public String getMenuName() {
		return ChatUtil.translateColors(((MainConfig.SETTINGS_GUI_ADD_PREFIX.getBoolean()) ? Message.GUI_TITLE.get() : Message.PREFIX.get() + Message.GUI_TITLE.get()));
	}

	@Override
	public int getSlots() {
		return 36;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);
		ClickType clickType = e.getClick();
		int clickedSlot = e.getSlot();
		
		switch(clickedSlot) {
		case(28):
			if (eGlowPlayer.getSaveData())
				eGlowPlayer.setSaveData(true);
			
			eGlowPlayer.setGlowOnJoin(!eGlowPlayer.getGlowOnJoin());
		break;
		case(29):
			if (eGlowPlayer.getPlayer().hasPermission("eglow.command.toggle")) {
				if (eGlowPlayer.isGlowing()) {
					eGlowPlayer.disableGlow(false);
					ChatUtil.sendMsgFromGUI(player, Message.DISABLE_GLOW.get());
				} else {
					if (eGlowPlayer.getEffect() == null || eGlowPlayer.getEffect().getName().equals("none")) {
						ChatUtil.sendMsgFromGUI(player, Message.NO_LAST_GLOW.get());
						return;
					} else {
						if (eGlowPlayer.getPlayer().hasPermission(eGlowPlayer.getEffect().getPermission())) {
							eGlowPlayer.activateGlow();
						} else {
							ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
							return;
						}
						ChatUtil.sendMsgFromGUI(player, Message.NEW_GLOW.get(eGlowPlayer.getLastGlowName()));
					}
				}
			} else {
				ChatUtil.sendMsgFromGUI(player, Message.NO_PERMISSION.get());
			}
		break;
		case(33):
			if (page == 1) {
				new EGlowMainMenu(eGlowPlayer.getPlayer()).openInventory();
			} else {
				page = page - 1;
				super.openInventory();
			}
			break;
		case(34):
			if (hasNextPage()) {
				page = page + 1;
				super.openInventory();
			}
			break;
		default:
			if (effects.containsKey(clickedSlot)) {
				String effect = effects.get(clickedSlot);
				enableGlow(eGlowPlayer.getPlayer(), clickType, effect);	
			}
			break;
		}
		
		UpdateMainEffectsNavigationBar(eGlowPlayer);
	}

	@Override
	public void setMenuItems() {
		Player player = menuMetadata.getOwner();
		IEGlowPlayer p = DataManager.getEGlowPlayer(player);
		effects = new ConcurrentHashMap<>();
		UpdateMainEffectsNavigationBar(p);
		setHasNextPage(false);

		int slot = 0;

		int currentEffect = 0;
		int nextEffect = (26 * (page - 1)) + ((page > 1) ? 1 : 0);
		
		for (String effect : Effect.GET_ALL_EFFECTS.get()) {
			IEGlowEffect Eeffect = DataManager.getEGlowEffect(effect.toLowerCase());
			if (Eeffect == null)
				continue;

			if (player.hasPermission(Eeffect.getPermission()) || player.hasPermission("eglow.effect.*")) {
				if (currentEffect != nextEffect) {
					currentEffect++;
					continue;
				}

				if (slot > getMaxItemsPerPage()) {
					setHasNextPage(true);
					UpdateMainEffectsNavigationBar(p);
					return;
				}


				Material material = getMaterial(effect);
				String name = getName(effect);
				int meta = getMeta(effect);
				int model = getModelID(effect);
				ArrayList<String> lores = new ArrayList<>();

				for (String lore : Effect.GET_LORES.getList(effect)) {
					lore = ChatUtil.translateColors(lore.replace("%effect_name%", Eeffect.getDisplayName()).replace("%effect_has_permission%", hasPermission(p, Eeffect.getPermission())));
					lores.add(lore);
				}

				if (model < 0) {
					inventory.setItem(slot, createItem(material, name, meta, lores));
				} else {
					inventory.setItem(slot, createItem(material, name, meta, lores, model));
				}

				if (!effects.containsKey(slot))
					effects.put(slot , Eeffect.getName());

				slot++;
			}
		}
	}
	
	private Material getMaterial(String effect) {
		String mat = Effect.GET_MATERIAL.getString(effect).toUpperCase();
		try {
			if (mat.equals("SAPLING") && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) mat = "SPRUCE_SAPLING";
			if (mat.equals("PUMPKIN") && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) mat = "CARVED_PUMPKIN";
			return Material.valueOf(mat);
		} catch (IllegalArgumentException | NullPointerException e) {
			ChatUtil.sendToConsole("Material: " + mat + " for effect " + effect + "is not valid.", true);
			return Material.valueOf("DIRT");
		}
	}
	
	private String getName(String effect) {
		return Effect.GET_NAME.getString(effect);
	}
	
	private int getMeta(String effect) {
		return Effect.GET_META.getInt(effect);
	}
	
	private int getModelID(String effect) {
		return (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) ? Effect.GET_MODEL_ID.getInt(effect) : -1;
	}
}