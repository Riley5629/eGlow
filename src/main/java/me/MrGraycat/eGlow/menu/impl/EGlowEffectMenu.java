package me.MrGraycat.eGlow.menu.impl;

import me.MrGraycat.eGlow.config.EGlowCustomEffectsConfig;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.menu.paginated.PaginatedMenu;
import me.MrGraycat.eGlow.util.Common;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EGlowEffectMenu extends PaginatedMenu {

	private final ConcurrentHashMap<Integer, String> effects = new ConcurrentHashMap<>();

	public EGlowEffectMenu(Player player) {
		super(player);
	}

	@Override
	public String getMenuName() {
		return ChatUtil.translateColors(((EGlowMainConfig.MainConfig.SETTINGS_GUI_ADD_PREFIX.getBoolean()) ? EGlowMessageConfig.Message.GUI_TITLE.get() : EGlowMessageConfig.Message.PREFIX.get() + EGlowMessageConfig.Message.GUI_TITLE.get()));
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

		switch (clickedSlot) {
			case (28):
				if (eGlowPlayer.getSaveData())
					eGlowPlayer.setSaveData(true);

				eGlowPlayer.setGlowOnJoin(!eGlowPlayer.isGlowOnJoin());
				break;
			case (29):
				if (eGlowPlayer.getPlayer().hasPermission("eglow.command.toggle")) {
					if (eGlowPlayer.isGlowing()) {
						eGlowPlayer.disableGlow(false);
						ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.DISABLE_GLOW.get());
					} else {
						if (eGlowPlayer.getGlowEffect() == null || eGlowPlayer.getGlowEffect().getName().equals("none")) {
							ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.NO_LAST_GLOW.get());
							return;
						} else {
							if (eGlowPlayer.getGlowDisableReason().equals(Common.GlowDisableReason.DISGUISE)) {
								ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.DISGUISE_BLOCKED.get());
								return;
							}

							if (eGlowPlayer.getPlayer().hasPermission(eGlowPlayer.getGlowEffect().getPermission())) {
								eGlowPlayer.activateGlow();
							} else {
								ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.NO_PERMISSION.get());
								return;
							}
							ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.NEW_GLOW.get(eGlowPlayer.getLastGlowName()));
						}
					}
				} else {
					ChatUtil.sendMenuFromMessage(player, EGlowMessageConfig.Message.NO_PERMISSION.get());
				}
				break;
			case (33):
				if (page == 1) {
					new EGlowMainMenu(eGlowPlayer.getPlayer()).openInventory();
				} else {
					page = page - 1;
					super.openInventory();
				}
				break;
			case (34):
				if (hasNextPage) {
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

		updateMainEffectsNavigationBar(eGlowPlayer);
	}

	@Override
	public void setMenuItems() {
		Player player = menuMetadata.getOwner();
		IEGlowPlayer glowPlayer = DataManager.getEGlowPlayer(player);

		this.effects.clear();

		updateMainEffectsNavigationBar(glowPlayer);
		this.hasNextPage = false;

		AtomicInteger slot = new AtomicInteger(0);
		AtomicInteger currentEffect = new AtomicInteger(0);

		int nextEffect = (26 * (page - 1)) + ((page > 1) ? 1 : 0);

		EGlowCustomEffectsConfig.Effect.GET_ALL_EFFECTS.get().stream().map(effectName -> DataManager.getEGlowEffect(effectName.toLowerCase()))
				.filter(Objects::nonNull)
				.forEach(effect -> {
					String effectName = effect.getName();

					if (player.hasPermission(effect.getPermission()) || player.hasPermission("eglow.effect.*")) {
						if (currentEffect.get() != nextEffect) {
							currentEffect.incrementAndGet();
							return;
						}

						if (slot.get() > maxItemsPerPage) {
							hasNextPage = true;
							updateMainEffectsNavigationBar(glowPlayer);
							return;
						}

						Material material = getMaterial(effectName);
						String name = getName(effectName);

						int meta = getMeta(effectName);
						int model = getModelId(effectName);

						List<String> lore = EGlowCustomEffectsConfig.Effect.GET_LORES.getList(effectName).stream()
								.map(line -> ChatUtil.translateColors(
										line.replace("%effect_name%", effect.getDisplayName())
												.replace("%effect_has_permission%", hasPermission(glowPlayer, effect.getPermission())))
								).collect(Collectors.toList());

						if (model < 0) {
							inventory.setItem(slot.get(), createItem(material, name, meta, lore));
						} else {
							inventory.setItem(slot.get(), createItem(material, name, meta, lore, model));
						}

						if (!effects.containsKey(slot.get())) {
							effects.put(slot.get(), effectName);
						}

						slot.incrementAndGet();
					}
				});
	}

	private Material getMaterial(String effect) {
		String mat = EGlowCustomEffectsConfig.Effect.GET_MATERIAL.getString(effect).toUpperCase();

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
		return EGlowCustomEffectsConfig.Effect.GET_NAME.getString(effect);
	}

	private int getMeta(String effect) {
		return EGlowCustomEffectsConfig.Effect.GET_META.getInt(effect);
	}

	private int getModelId(String effect) {
		return ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14 ? EGlowCustomEffectsConfig.Effect.GET_MODEL_ID.getInt(effect) : -1;
	}
}