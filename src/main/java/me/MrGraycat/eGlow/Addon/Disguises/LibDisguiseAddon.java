package me.MrGraycat.eGlow.Addon.Disguises;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;

/*
 * Making sure to disable the player glow when disguised to prevent errors.
 * Plugin: LibsDisguise
 * Versions: 1.12-1.16
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
		try {
			Entity entity = event.getEntity();
			
			if (entity instanceof Player) {
				Player player = (Player) entity;
				IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
			
				if (eglowPlayer != null && eglowPlayer.getGlowStatus() || eglowPlayer != null && eglowPlayer.getFakeGlowStatus()) {
					eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
					eglowPlayer.toggleGlow();
					ChatUtil.sendMsg(player, Message.DISGUISE_BLOCKED.get(), true);
				}	
			}
		} catch (NoSuchMethodError ex) {
			ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		try {
			Entity entity = event.getDisguised();
			
			if (entity instanceof Player) {
				Player player = (Player) event.getDisguised();
				IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
				
				if (eglowPlayer != null && eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
					ChatUtil.sendMsg(player, Message.DISGUISE_ALLOWED.get(), true);
				}
			}
		} catch (NoSuchMethodError ex) {
			ChatUtil.sendToConsole("&cLibsDisguise isn't up to date &f!", true);
			ex.printStackTrace();
		}
	}
}
