package me.mrgraycat.eglow.addon.disguise;

import me.mrgraycat.eglow.addon.GlowAddon;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/*
 * Making sure to disable the player glow when disguised to prevent errors.
 * Plugin: LibsDisguise
 * Versions: 1.12-latest
 */
public class LibDisguiseAddon extends GlowAddon implements Listener {

	/**
	 * Register LibDisguise disguise events
	 */
	public LibDisguiseAddon(EGlow instance) {
		super(instance);
	}

	/**
	 * Check to see if player is disguised
	 *
	 * @param player Player to check
	 * @return whether player is disguised
	 */
	public boolean isDisguised(Player player) {
		return DisguiseAPI.isDisguised(player);
	}

	@EventHandler
	public void onDisguise(DisguiseEvent event) {
		runAsync(() -> {
			Entity entity = event.getEntity();

			if (!(entity instanceof Player)) {
				return;
			}

			IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer((Player) entity);

			try {
				if (eGlowPlayer != null && eGlowPlayer.isGlowing()) {
					eGlowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE, false);
					eGlowPlayer.disableGlow(false);

					ChatUtil.sendMessage(eGlowPlayer.getPlayer(), Message.DISGUISE_BLOCKED.get(), true);
				}
			} catch (NoSuchMethodError error) {
				ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
				error.printStackTrace();
			}
		});
	}

	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		runAsync(() -> {
			Entity entity = event.getDisguised();

			if (!(entity instanceof Player)) {
				return;
			}

			IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer((Player) entity);

			try {
				if (eGlowPlayer != null && eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					if (eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false)) {
						eGlowPlayer.activateGlow();
						ChatUtil.sendMessage(eGlowPlayer.getPlayer(), Message.DISGUISE_ALLOWED.get(), true);
					}
				}
			} catch (NoSuchMethodError error) {
				ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
				error.printStackTrace();
			} catch (NullPointerException ignored) {
				//Caused by disconnecting while in disguise when server performance is low (rare error)
			}
		});
	}
}
