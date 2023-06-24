package me.MrGraycat.eGlow.event;

import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.Common.GlowDisableReason;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

public class EGlowEventListener113AndAbove implements Listener {

	private final EGlow instance;

	public EGlowEventListener113AndAbove(EGlow instance) {
		this.instance = instance;

		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}

	@EventHandler
	public void PlayerPotionEvent(EntityPotionEffectEvent event) {
		Entity entity = event.getEntity();

		if (!(entity instanceof Player)) {
			return;
		}

		IEGlowPlayer glowPlayer = DataManager.getEGlowPlayer((Player) entity);

		if (glowPlayer == null)
			return;

		Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
			if (!MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean()) {
				if (glowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
					glowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false);
				}

				return;
			}

			if (MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean()) {
				if (event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
					if (glowPlayer.isGlowing()) {
						glowPlayer.disableGlow(false);
						glowPlayer.setGlowDisableReason(GlowDisableReason.INVISIBLE, false);

						if (MainConfig.SETTINGS_NOTIFICATIONS_INVISIBILITY.getBoolean())
							ChatUtil.sendMessage(glowPlayer, Message.INVISIBILITY_DISABLED.get(), true);
						return;
					}
				}

				if (event.getOldEffect() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
					if (event.getNewEffect() == null && glowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
						if (glowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false)) {
							glowPlayer.activateGlow();

							if (MainConfig.SETTINGS_NOTIFICATIONS_INVISIBILITY.getBoolean()) {
								ChatUtil.sendMessage(glowPlayer.getPlayer(), Message.INVISIBILITY_ENABLED.get(), true);
							}
						}
					}
				}
			}
		}, 1L);
	}
}