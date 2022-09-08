package me.MrGraycat.eGlow.Manager.Interface;

import me.MrGraycat.eGlow.Addon.Citizens.EGlowCitizensTrait;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IEGlowEffect {
	private BukkitTask effectRunnable;
	
	private ConcurrentHashMap<Object, Integer> activeEntities = new ConcurrentHashMap<>();
	private List<ChatColor> effectLoop = new ArrayList<>();
	private int effectDelay = 0;
	
	private String effectName;
	private String displayName;
	private String permissionNode;
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		setName(name);
		setDisplayName(displayName);
		setPermission(permissionNode);
		setDelay(delay);

		Collections.addAll(effectLoop, colors);
	}
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		setName(name);
		setDisplayName(displayName);
		setPermission(permissionNode);
		setDelay(delay);
		
		for (String color : colors) {
			color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
			effectLoop.add(ChatColor.valueOf(color.toUpperCase()));
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
		if (getRunnable() != null)
			getRunnable().cancel();
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
				if (entity instanceof Player)
					ChatUtil.sendMsg(eglowEntity.getPlayer(), Message.GLOW_REMOVED.get(), true);
			}
			getActiveEntities().remove(entity);
		}

		getRunnable().cancel();
		setRunnable(null);
		EGlow.getInstance().getServer().getPluginManager().removePermission("eglow.effect." + getName());
	}
	
	private void activateEffect() {
		if (getRunnable() == null) {
			setRunnable(
			new BukkitRunnable() {
				@Override
				public void run() {
					if (getActiveEntities() == null)
						activeEntities = new ConcurrentHashMap<>();
					
					if (getActiveEntities().isEmpty()) {
						cancel();
						setRunnable(null);
					}

					
					getActiveEntities().forEach((entity, progress) -> {
					IEGlowPlayer eglowEntity = null;

					if (entity instanceof Player)
						eglowEntity = DataManager.getEGlowPlayer((Player) entity);

					try {
						if (EGlow.getInstance().getCitizensAddon() != null && entity instanceof NPC)
							eglowEntity = ((NPC) entity).getTraitNullable(EGlowCitizensTrait.class).getEGlowNPC();
					} catch (NoSuchMethodError e) {
						ChatUtil.sendToConsole("&cYour Citizens version is outdated please use 2.0.27 or later", true);
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

					ChatColor color = getColors().get(progress);

					if (color.equals(ChatColor.RESET)) {
						eglowEntity.setColor(color, false, true);
					} else {
						eglowEntity.setColor(color, true, false);
					}

					if (getColors().size() == 1) {
						eglowEntity.setColor(color, true, false);
						return;
					}

					if (progress == getColors().size() - 1) {
						getActiveEntities().replace(entity, 0);
						return;
					}

					getActiveEntities().replace(entity, progress + 1);
					});
				}
			}.runTaskTimerAsynchronously(EGlow.getInstance(), 1, getDelay()));
		}
	}
	
	//Getter
	private BukkitTask getRunnable() {
		return this.effectRunnable;
	}
	
	private ConcurrentHashMap<Object, Integer> getActiveEntities() {
		return this.activeEntities;
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
	
	//Setter
	private void setRunnable(BukkitTask effectRunnable) {
		this.effectRunnable = effectRunnable;
	}
	
	private void setName(String effectName) {
		this.effectName = effectName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	private void setPermission(String permissionNode) {
		this.permissionNode = permissionNode;
	}
	
	public void setDelay(int effectDelay) {
		this.effectDelay = effectDelay;	
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
		
		if (!chatcolors.equals(getColors())) {
			for (Object entity : activeEntities.keySet()) {
				activeEntities.replace(entity, 0);
			}
			this.effectLoop = chatcolors;
		}	
	}
}
