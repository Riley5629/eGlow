package me.MrGraycat.eGlow.Event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowEventListener113AndAbove implements Listener {
	private EGlow instance;
	
	public EGlowEventListener113AndAbove(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
	}
	
	@EventHandler
	public void PlayerPotionEvent(EntityPotionEffectEvent e) {
		Entity entity = e.getEntity();
		
		if (entity instanceof Player) {
			IEGlowPlayer ep = getInstance().getDataManager().getEGlowPlayer((Player) entity);

			if (ep == null)
				return;
			
			if (EGlowMainConfig.OptionDisableGlowWhenInvisible()) {
				if (e.getNewEffect() != null && e.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
					if (ep.getGlowStatus() || ep.getFakeGlowStatus()) {
						ep.disableGlow(false);
						ep.setGlowDisableReason(GlowDisableReason.INVISIBLE);
						
						if (EGlowMainConfig.OptionSendInvisibilityNotification())
							ChatUtil.sendMsgWithPrefix(ep.getPlayer(), Message.INVISIBILITY_DISABLED.get());
					}
				}
				
				if (e.getOldEffect() != null && e.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
					if (ep.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
						ep.activateGlow();
						ep.setGlowDisableReason(GlowDisableReason.NONE);
						
						if (EGlowMainConfig.OptionSendInvisibilityNotification())
							ChatUtil.sendMsgWithPrefix(ep.getPlayer(), Message.INVISIBILITY_ENABLED.get());
					}
				}
				return;
			} else {
				if (ep != null && ep.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE))
					ep.setGlowDisableReason(GlowDisableReason.NONE);	
			}
		}
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}
}
