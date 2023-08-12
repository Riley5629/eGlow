package me.mrgraycat.eglow.event;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EGlowEventListener113AndAbove implements Listener {

	public EGlowEventListener113AndAbove() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}

	@EventHandler
	public void PlayerPotionEvent(EntityPotionEffectEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer((Player) entity);

			new BukkitRunnable() {
				@Override
				public void run() {
					if (eGlowPlayer == null)
						return;

					if (!MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean()) {
						if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
							eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
						}
						return;
					}

					if (event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
						if (eGlowPlayer.isGlowing()) {
							eGlowPlayer.disableGlow(false);
							eGlowPlayer.setGlowDisableReason(GlowDisableReason.INVISIBLE);

							if (MainConfig.SETTINGS_NOTIFICATIONS_INVISIBILITY.getBoolean())
								ChatUtil.sendMsg(eGlowPlayer.getPlayer(), Message.INVISIBILITY_BLOCKED.get(), true);
							return;
						}
					}

					if (event.getNewEffect() == null && event.getOldEffect() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
						if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
							if (eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE).equals(GlowDisableReason.NONE)) {
								eGlowPlayer.activateGlow();

								if (MainConfig.SETTINGS_NOTIFICATIONS_INVISIBILITY.getBoolean())
									ChatUtil.sendMsg(eGlowPlayer.getPlayer(), Message.INVISIBILITY_ALLOWED.get(), true);
							}
						}
					}
				}
			}.runTaskLaterAsynchronously(EGlow.getInstance(), 1L);
		}
	}
}