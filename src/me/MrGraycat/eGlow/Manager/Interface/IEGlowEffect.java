package me.MrGraycat.eGlow.Manager.Interface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Addon.Citizens.EGlowCitizensTrait;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.npc.NPC;

public class IEGlowEffect {
	private EGlow instance = EGlow.getInstance();
	
	private ConcurrentHashMap<Object, Integer> activeEntities = new ConcurrentHashMap<Object, Integer>();
	private List<ChatColor> effectLoop = new ArrayList<ChatColor>();
	private boolean isActive = false;
	private int effectDelay = 0;
	
	private String effectName;
	private String displayName;
	private String permissionNode;
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		this.effectName = name;
		this.displayName = displayName;
		this.permissionNode = permissionNode;
		this.effectDelay = delay;
		
		for (ChatColor color : colors) {
			effectLoop.add(color);
		}
	}
	
	public IEGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		this.effectName = name;
		this.displayName = displayName;
		this.permissionNode = permissionNode;
		this.effectDelay = delay;
		
		for (String color : colors) {
			color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
			effectLoop.add(ChatColor.valueOf(color.toUpperCase()));
		}
	}
	
	public String getName() {
		return effectName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		if (!this.displayName.equals(displayName))
			this.displayName = displayName;
	}
	
	public String getPermission() {
		return permissionNode;
	}
	
	public int getDelay() {
		return effectDelay;
	}
	
	public void setDelay(int effectDelay) {
		if (this.effectDelay != effectDelay) {
			isActive = false;
			this.effectDelay = effectDelay;
			activateEffect();
		}		
	}
	
	public void setColors(List<String> colors) {
		List<ChatColor> chatcolors = new ArrayList<>();
		
		for (String color : colors) {
			try {
				color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
				chatcolors.add(ChatColor.valueOf(color.toUpperCase()));
			} catch (IllegalArgumentException | NullPointerException e) {
				ChatUtil.sendToConsoleWithPrefix("&cInvalid color &f'&e" + color + "&f' &cfor effect &f'&e" + getName() + "&f'");
				return;
			}
		}
		
		if (!chatcolors.equals(effectLoop)) {
			isActive = false;
			for (Object entity : activeEntities.keySet()) {
				activeEntities.replace(entity, 0);
			}
			effectLoop = chatcolors;
			activateEffect();
		}	
	}
	
	public void activateForEntity(Object entity) {
		activeEntities.put(entity, 0);
		activateEffect();
	}
	
	public void deactivateForEntity(Object entity) {
		activeEntities.remove(entity);
	}
	
	public void removeEffect() {
		for (Object entity : activeEntities.keySet()) {
			IEGlowPlayer eglowEntity = null;
			
			if (entity instanceof Player)
				eglowEntity = getInstance().getDataManager().getEGlowPlayer((Player) entity);
			
			try {
				if (instance.getCitizensAddon() != null && entity instanceof NPC)
					eglowEntity = ((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC();
			} catch(NoSuchMethodError e) {
				ChatUtil.sendToConsoleWithPrefix("&cYour Citizens version is outdated please use 2.0.27 or later");
			}
			
			if (eglowEntity != null) {
				eglowEntity.disableGlow(true);
				if (entity instanceof Player)
					ChatUtil.sendMsgWithPrefix(eglowEntity.getPlayer(), Message.GLOW_REMOVED.get());
			}
			activeEntities.remove(entity);
		}
	}
	
	private void activateEffect() {
		if (!isActive) {
			isActive = true;
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if (activeEntities.isEmpty() || !isActive) {
						isActive = false;
						cancel();
					}
					
					for (Object entity : activeEntities.keySet()) {
						int progress = activeEntities.get(entity);
						IEGlowPlayer eglowEntity = null;
						
						if (entity instanceof Player)
							eglowEntity = getInstance().getDataManager().getEGlowPlayer((Player) entity);
						
						try {
							if (instance.getCitizensAddon() != null && entity instanceof NPC)
								eglowEntity = ((NPC) entity).getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC();
						} catch(NoSuchMethodError e) {
							ChatUtil.sendToConsoleWithPrefix("&cYour Citizens version is outdated please use 2.0.27 or later");
						}
						
						if (eglowEntity == null) {
							activeEntities.remove(entity);
							continue;
						}
						
						ChatColor color = effectLoop.get(progress);
						
						if (color.equals(ChatColor.RESET)) {
							eglowEntity.setColor(color, false, true);
						} else {
							eglowEntity.setColor(color, true, false);
						}
						
						if (effectLoop.size() == 1) {
							eglowEntity.setColor(color, true, false);
							continue;
						}
							
						
						if (progress == effectLoop.size() - 1) {
							activeEntities.replace(entity, 0);
							continue;
						}
						
						activeEntities.replace(entity, progress + 1);
					}
				}
			}.runTaskTimerAsynchronously(instance, 1, effectDelay);
		}
	}
	
	private EGlow getInstance() {
		return this.instance;
	}
}
