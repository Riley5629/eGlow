package me.mrgraycat.eglow.manager.glow;

import me.mrgraycat.eglow.addon.citizens.EGlowCitizensTrait;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IEGlowEffect {

	private BukkitTask effectRunnable;
	
	private final ConcurrentHashMap<Object, Integer> activeEntities = new ConcurrentHashMap<>();
	private final List<ChatColor> effectLoop = new ArrayList<>();
	private int effectDelay;

	private String effectName;
	private String displayName;
	private String permissionNode;
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		this.effectName = name;
		this.displayName = displayName;
		this.permissionNode = permissionNode;
		this.effectDelay = delay;

		Collections.addAll(this.effectLoop, colors);
	}
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		this.effectName = name;
		this.displayName = displayName;
		this.permissionNode = permissionNode;
		this.effectDelay = delay;

		this.effectLoop.addAll(colors.stream()
				.map(color -> ChatColor.valueOf(color.toLowerCase().replace("dark", "dark_")
						.replace("light", "_light")
						.replace("purple", "dark_purple")
						.replace("pink", "light_purple")
						.replace("none", "reset").toUpperCase()))
				.collect(Collectors.toList())
		);
	}
	
	public void activateForEntity(Object entity) {
		activeEntities.put(entity, 0);
		activateEffect();
	}
	
	public void deactivateForEntity(Object entity) {
		activeEntities.remove(entity);
	}
	
	public void reloadEffect() {
		if (this.effectRunnable != null) {
			this.effectRunnable.cancel();
			this.effectRunnable = null;
		}

		activateEffect();
	}
	
	public void removeEffect() {
		for (Object entity : activeEntities.keySet()) {
			IEGlowPlayer eglowEntity = null;
			
			if (entity instanceof Player)
				eglowEntity = DataManager.getEGlowPlayer((Player) entity);
			
			try {
				if (EGlow.getInstance().getCitizensAddon() != null && entity instanceof NPC)
					eglowEntity = ((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC();
			} catch(NoSuchMethodError e) {
				ChatUtil.sendToConsole("&cYour Citizens version is outdated please use 2.0.27 or later", true);
			}
			
			if (eglowEntity != null) {
				eglowEntity.disableGlow(true);

				if (entity instanceof Player) {
					ChatUtil.sendMessage(eglowEntity.getPlayer(), Message.GLOW_REMOVED.get(), true);
				}
			}

			this.activeEntities.remove(entity);
		}

		this.effectRunnable.cancel();
		this.effectRunnable = null;

		EGlow.getInstance().getServer().getPluginManager().removePermission("eglow.effect." + getName());
	}
	
	private void activateEffect() {
		if (this.effectRunnable == null) {
			this.effectRunnable = new BukkitRunnable() {
				@Override
				public void run() {
					if (activeEntities.isEmpty()) {
						cancel();
						effectRunnable = null;
					}

					activeEntities.forEach((entity, progress) -> {
						IEGlowPlayer eglowEntity = null;

						if (entity instanceof Player)
							eglowEntity = DataManager.getEGlowPlayer((Player) entity);

						try {
							if (EGlow.getInstance().getCitizensAddon() != null && entity instanceof NPC)
								eglowEntity = ((NPC) entity).getTraitNullable(EGlowCitizensTrait.class).getEGlowNPC();
						} catch (NoSuchMethodError e) {
							ChatUtil.sendToConsole("&cYour Citizens version is outdated please use 2.0.27 or later", true);
						} catch (NullPointerException ex) {
							Bukkit.getScheduler().runTask(EGlow.getInstance(), () -> {
								((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class);
							});
							return;
						}

						if (eglowEntity == null) {
							activeEntities.remove(entity);
							return;
						}

						ChatColor color = getColors().get(progress);

						if (color.equals(ChatColor.RESET)) {
							eglowEntity.setColor(color, false, true);
						} else {
							eglowEntity.setColor(color, true, false);
						}

						if (effectLoop.size() == 1) {
							eglowEntity.setColor(color, true, false);
							return;
						}

						if (progress == getColors().size() - 1) {
							activeEntities.replace(entity, 0);
							return;
						}

						activeEntities.replace(entity, progress + 1);
					});
				}
			}.runTaskTimerAsynchronously(EGlow.getInstance(), 1, getDelay());
		}
	}
	
	public String getName() {
		return this.effectName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public String getPermission() {
		return this.permissionNode;
	}
	
	public int getDelay() {
		return this.effectDelay;
	}
	
	public List<ChatColor> getColors() {
		return this.effectLoop;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setDelay(int effectDelay) {
		this.effectDelay = effectDelay;	
	}

	public void setColors(List<String> colors) {
		this.effectLoop.clear();
		this.effectLoop.addAll(colors.stream()
				.map(color -> {
					try {
						return ChatColor.valueOf(color.toLowerCase()
								.replace("dark", "dark_")
								.replace("light", "_light")
								.replace("purple", "dark_purple")
								.replace("pink", "light_purple")
								.replace("none", "reset")
								.toUpperCase());
					} catch (Exception exc) {
						ChatUtil.sendToConsole("&cInvalid color &f'&e" + color + "&f' &cfor effect &f'&e" + getName() + "&f'", true);
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}