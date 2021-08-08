package me.MrGraycat.eGlow.Addon.Disguises;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;

/*
 * Making sure to disable the player glow when disguised the prevent errors.
 * Plugin: LibsDisguise
 * Versions: 1.12-1.16
 */
public class LibDisguiseAddon implements Listener {
	private EGlow instance;
	
	/**
	 * Register LibDisguise disguise events
	 */
	public LibDisguiseAddon(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
	}

	
	/**
	 * Check to see if player is diguised
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
				IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(player);
			
				if (eglowPlayer != null && eglowPlayer.getGlowStatus() || eglowPlayer != null && eglowPlayer.getFakeGlowStatus()) {
					eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
					eglowPlayer.toggleGlow();
					ChatUtil.sendMsgWithPrefix(player, Message.DISGUISE_BLOCKED.get());
				}	
			}
		} catch (NoSuchMethodError ex) {ex.printStackTrace();}
	}
	
	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		try {
			Entity entity = event.getDisguised();
			
			if (entity instanceof Player) {
				Player player = (Player) event.getDisguised();
				IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(player);
				
				if (eglowPlayer != null && eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
					ChatUtil.sendMsgWithPrefix(player, Message.DISGUISE_ALLOWED.get());
				}
			}
		} catch (NoSuchMethodError ex) {
			ChatUtil.sendToConsoleWithPrefix("&cLibsDisguise isn't up to date &f!");
			ex.printStackTrace();
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
