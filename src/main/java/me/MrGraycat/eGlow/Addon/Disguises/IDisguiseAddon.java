package me.MrGraycat.eGlow.Addon.Disguises;

import de.robingrether.idisguise.api.DisguiseAPI;
import de.robingrether.idisguise.api.DisguiseEvent;
import de.robingrether.idisguise.api.UndisguiseEvent;
import lombok.Getter;
import lombok.Setter;
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

	@Getter
	@Setter
	private DisguiseAPI disguiseAPI;

	/**
	 * Register IDisguise disguise events & api
	 */
	public IDisguiseAddon() {
		setDisguiseAPI(Objects.requireNonNull(EGlow.getInstance().getServer().getServicesManager().getRegistration(DisguiseAPI.class), "Unable to hook into IDisguise").getProvider());
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}

	/**
	 * Check to see if player is disguised
	 *
	 * @param player Player to check
	 * @return whether player is disguised
	 */
	public boolean isDisguised(Player player) {
		return getDisguiseAPI().isDisguised(player);
	}

	@EventHandler
	public void onDisguise(DisguiseEvent event) {
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(event.getPlayer());

		if (eGlowPlayer != null && eGlowPlayer.isGlowing()) {
			eGlowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE, false);
			eGlowPlayer.disableGlow(false);
			ChatUtil.sendMsg(eGlowPlayer.getPlayer(), Message.DISGUISE_BLOCKED.get(), true);
		}
	}

	@EventHandler
	public void onUnDisguise(UndisguiseEvent event) {
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(event.getPlayer());

		if (eGlowPlayer != null && eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			if (eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false)) {
				eGlowPlayer.activateGlow();
				ChatUtil.sendMsg(eGlowPlayer.getPlayer(), Message.DISGUISE_ALLOWED.get(), true);
			}
		}
	}
}
