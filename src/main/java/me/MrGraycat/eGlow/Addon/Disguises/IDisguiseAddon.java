package me.MrGraycat.eGlow.Addon.Disguises;

import de.robingrether.idisguise.api.DisguiseAPI;
import de.robingrether.idisguise.api.DisguiseEvent;
import de.robingrether.idisguise.api.UndisguiseEvent;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

/*
 * Making sure to disable the player glow when disguised to prevent errors.
 * Plugin: iDisguise
 * Versions: 1.9-1.13 (Currently discontinued)
 */
public class IDisguiseAddon implements Listener {

	private DisguiseAPI DisguiseAPI_Addon;
	
	/**
	 * Register IDisguise disguise events & api
	 */
	public IDisguiseAddon() {
		setDisguiseAPIAddon(Objects.requireNonNull(EGlow.getInstance().getServer().getServicesManager().getRegistration(DisguiseAPI.class), "Unable to hook into IDisguise").getProvider());
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}
	
	/**
	 * Check to see if player is disguised
	 * @param player Player to check
	 * @return true is disguised, false if not
	 */
	public boolean isDisguised(Player player) {
		return getDisguiseAPIAddon().isDisguised(player);
	}
	
	@EventHandler
	public void onDisguise(DisguiseEvent event) {
		IEGlowPlayer player = DataManager.getEGlowPlayer(event.getPlayer());
		
		if (player != null && player.getGlowStatus() || player != null && player.getFakeGlowStatus()) {
			player.setGlowDisableReason(GlowDisableReason.DISGUISE);
			player.toggleGlow();
			ChatUtil.sendMsg(player.getPlayer(), Message.DISGUISE_BLOCKED.get(), true);
		}
	}
	
	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		IEGlowPlayer player = DataManager.getEGlowPlayer(event.getPlayer());
		
		if (player != null && player.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			player.toggleGlow();
			player.setGlowDisableReason(GlowDisableReason.NONE);
			ChatUtil.sendMsg(player.getPlayer(), Message.DISGUISE_ALLOWED.get(), true);
		}
	}
	
	//Getter
	public DisguiseAPI getDisguiseAPIAddon() {
		return this.DisguiseAPI_Addon;
	}
	//Setter
	private void setDisguiseAPIAddon(DisguiseAPI api) {
		this.DisguiseAPI_Addon = api;
	}
}
