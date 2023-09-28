package me.mrgraycat.eglow.data;

import lombok.Getter;
import lombok.Setter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.citizens.EGlowCitizensTrait;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class EGlowEffect {
	private BukkitTask effectRunnable;
	private ConcurrentHashMap<Object, Integer> activeEntities = new ConcurrentHashMap<>();
	private List<ChatColor> effectColors = new ArrayList<>();
	private int effectDelay = 0;

	private String name;
	private String displayName;
	private String permissionNode;

	public EGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		setName(name);
		setDisplayName(displayName);
		setPermissionNode(permissionNode);
		setEffectDelay(delay);

		Collections.addAll(effectColors, colors);
	}

	public EGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		setName(name);
		setDisplayName(displayName);
		setPermissionNode(permissionNode);
		setEffectDelay(delay);

		for (String color : colors) {
			color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
			effectColors.add(ChatColor.valueOf(color.toUpperCase()));
		}
	}

	public void activateForEntity(Object entity) {
		getActiveEntities().put(entity, 0);
		activateEffect();
	}

	public void deactivateForEntity(Object entity) {
		getActiveEntities().remove(entity);
	}

	public void reloadEffect() {
		if (getEffectRunnable() != null)
			getEffectRunnable().cancel();
		setEffectRunnable(null);
		activateEffect();
	}

	public void removeEffect() {
		for (Object entity : activeEntities.keySet()) {
			EGlowPlayer eglowEntity = null;

			if (entity instanceof Player)
				eglowEntity = DataManager.getEGlowPlayer((Player) entity);

			try {
				if (EGlow.getInstance().getCitizensAddon() != null && entity instanceof NPC)
					eglowEntity = ((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC();
			} catch (NoSuchMethodError e) {
				ChatUtil.sendToConsole("&cYour Citizens version is outdated!", true);
			}

			if (eglowEntity != null) {
				eglowEntity.disableGlow(true);
				if (entity instanceof Player)
					ChatUtil.sendMsg(eglowEntity.getPlayer(), Message.GLOW_REMOVED.get(), true);
			}
			getActiveEntities().remove(entity);
		}

		getEffectRunnable().cancel();
		setEffectRunnable(null);
		EGlow.getInstance().getServer().getPluginManager().removePermission("eglow.effect." + getName());
	}

	private void activateEffect() {
		if (getEffectRunnable() == null) {
			setEffectRunnable(
					new BukkitRunnable() {
						@Override
						public void run() {
							if (getActiveEntities() == null)
								activeEntities = new ConcurrentHashMap<>();

							if (getActiveEntities().isEmpty()) {
								cancel();
								setEffectRunnable(null);
							}

							getActiveEntities().forEach((entity, progress) -> {
								EGlowPlayer eglowEntity = null;

								if (entity instanceof Player)
									eglowEntity = DataManager.getEGlowPlayer((Player) entity);

								try {
									if (EGlow.getInstance().getCitizensAddon() != null && entity instanceof NPC)
										eglowEntity = ((NPC) entity).getTraitNullable(EGlowCitizensTrait.class).getEGlowNPC();
								} catch (NoSuchMethodError e) {
									ChatUtil.sendToConsole("&cYour Citizens version is outdated!", true);
								} catch (NullPointerException ex) {
									new BukkitRunnable() {
										@Override
										public void run() {
											((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class);
										}
									}.runTask(EGlow.getInstance());
									return;
								}

								if (eglowEntity == null) {
									getActiveEntities().remove(entity);
									return;
								}

								ChatColor color = getEffectColors().get(progress);

								if (color.equals(ChatColor.RESET)) {
									eglowEntity.setColor(color, false, true);
								} else {
									eglowEntity.setColor(color, true, false);
								}

								if (getEffectColors().size() == 1) {
									eglowEntity.setColor(color, true, false);

									if (entity instanceof Player)
										PacketUtil.updateGlowing(eglowEntity, true);
									return;
								}

								if (progress == getEffectColors().size() - 1) {
									getActiveEntities().replace(entity, 0);
									return;
								}

								getActiveEntities().replace(entity, progress + 1);
							});
						}
					}.runTaskTimerAsynchronously(EGlow.getInstance(), 5, getEffectDelay()));
		}
	}

	public void setColors(List<String> colors) {
		List<ChatColor> chatcolors = new ArrayList<>();

		for (String color : colors) {
			try {
				color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
				chatcolors.add(ChatColor.valueOf(color.toUpperCase()));
			} catch (IllegalArgumentException | NullPointerException e) {
				ChatUtil.sendToConsole("&cInvalid color &f'&e" + color + "&f' &cfor effect &f'&e" + getName() + "&f'", true);
				return;
			}
		}

		if (!chatcolors.equals(getEffectColors())) {
			for (Object entity : activeEntities.keySet()) {
				activeEntities.replace(entity, 0);
			}
			this.effectColors = chatcolors;
		}
	}
}