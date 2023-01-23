package me.MrGraycat.eGlow.Addon.Disguises;

import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * Making sure to disable the player glow when disguised to prevent errors.
 * Plugin: LibsDisguise
 * Versions: 1.12-latest
 */
public class LibDisguiseAddon implements Listener {
	/**
	 * Register LibDisguise disguise events
	 */
	public LibDisguiseAddon() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}

	
	/**
	 * Check to see if player is disguised
	 * @param player Player to check
	 * @return true is disguised, false if not
	 */
	public boolean isDisguised(Player player) {
		return DisguiseAPI.isDisguised(player);
	}
	
	@EventHandler
	public void onDisguise(DisguiseEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Entity entity = event.getEntity();

					if (entity instanceof Player) {
						IEGlowPlayer player = DataManager.getEGlowPlayer((Player) entity);

						if (player != null && player.isGlowing()) {
							player.setGlowDisableReason(GlowDisableReason.DISGUISE);
							player.toggleGlow();
							ChatUtil.sendMsg(player.getPlayer(), Message.DISGUISE_BLOCKED.get(), true);
						}
					}
				} catch (NoSuchMethodError ex) {
					ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance());
	}
	
	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Entity entity = event.getDisguised();

					if (entity instanceof Player) {
						IEGlowPlayer player = DataManager.getEGlowPlayer((Player) entity);

						if (player != null && player.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
							if (player.isInBlockedWorld()) {
								player.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
								return;
							}

							if (player.isInvisible()) {
								player.setGlowDisableReason(GlowDisableReason.INVISIBLE);
								return;
							}

							player.toggleGlow();
							player.setGlowDisableReason(GlowDisableReason.NONE);
							ChatUtil.sendMsg(player.getPlayer(), Message.DISGUISE_ALLOWED.get(), true);
						}
					}
				} catch (NoSuchMethodError ex) {
					ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
					ex.printStackTrace();
				} catch (NullPointerException e) {
					//Caused by disconnecting while in disguise when server performance is low (rare error)
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance());
	}
}