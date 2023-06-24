package me.mrgraycat.eglow.api;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.enums.EGlowBlink;
import me.mrgraycat.eglow.api.enums.EGlowColor;
import me.mrgraycat.eglow.api.enums.EGlowEffect;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.util.packet.PacketUtil;
import me.mrgraycat.eglow.util.packet.PipelineInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class EGlowAPI {
	/**
	 * Get the IEGlowEntity from eGlow
	 *
	 * @param player player to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the player
	 */
	public IEGlowPlayer getEGlowPlayer(Player player) {
		return DataManager.getEGlowPlayer(player);
	}

	/**
	 * Get the IEGlowEntity from eGlow
	 *
	 * @param uuid uuid to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the uuid
	 */
	public IEGlowPlayer getEGlowPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null)
			return DataManager.getEGlowPlayer(player);
		return null;
	}

	/**
	 * Get the IEGlowEffect from eGlow
	 *
	 * @param effectName name for the effect
	 * @return IEGlowEffect is found, null if not
	 */
	public IEGlowEffect getEGlowEffect(String effectName) {
		IEGlowEffect eGlowEffect = DataManager.getEGlowEffect(effectName);

		if (eGlowEffect == null)
			ChatUtil.sendToConsole("(API) Unable to find effect for name: " + effectName, true);
		return eGlowEffect;
	}

	/**
	 * Get the glow color from a player
	 *
	 * @param eGlowPlayer player to get the glow color from
	 * @return Glow color as String (invisible)
	 */
	public String getGlowColor(IEGlowPlayer eGlowPlayer) {
		return (eGlowPlayer != null && eGlowPlayer.isGlowing()) ? String.valueOf(eGlowPlayer.getActiveColor()) : "";
	}

	/**
	 * Enable a specific effect for a player
	 *
	 * @param eGlowPlayer to activate the effect for
	 * @param eGlowEffect to enable
	 */
	public void enableGlow(IEGlowPlayer eGlowPlayer, IEGlowEffect eGlowEffect) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (eGlowPlayer == null)
					return;

				eGlowPlayer.activateGlow(eGlowEffect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}

	/**
	 * Enable a solid glow color for a player
	 *
	 * @param eGlowPlayer to activate the glow for
	 * @param color       to enable
	 */
	public void enableGlow(IEGlowPlayer eGlowPlayer, EGlowColor color) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (eGlowPlayer == null)
					return;

				IEGlowEffect eGlowEffect = DataManager.getEGlowEffect(color.toString());
				eGlowPlayer.activateGlow(eGlowEffect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}

	/**
	 * Enable a blink effect for a player
	 *
	 * @param eGlowPlayer to activate the blink for
	 * @param blink       to enable
	 */
	public void enableGlow(IEGlowPlayer eGlowPlayer, EGlowBlink blink) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (eGlowPlayer == null)
					return;

				IEGlowEffect eGlowEffect = DataManager.getEGlowEffect(blink.toString());
				eGlowPlayer.activateGlow(eGlowEffect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}

	/**
	 * Enable an effect for a player
	 *
	 * @param eGlowPlayer to activate the effect for
	 * @param effect      to enable
	 */
	public void enableGlow(IEGlowPlayer eGlowPlayer, EGlowEffect effect) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (eGlowPlayer == null)
					return;

				IEGlowEffect eGlowEffect = DataManager.getEGlowEffect(effect.toString());
				eGlowPlayer.activateGlow(eGlowEffect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}

	/**
	 * Disable the glow for a player
	 *
	 * @param eGlowPlayer to disable the glow for
	 */
	public void disableGlow(IEGlowPlayer eGlowPlayer) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (eGlowPlayer == null)
					return;

				eGlowPlayer.disableGlow(true);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}

	/**
	 * add custom receiver for a player
	 *
	 * @param eGlowPlayerSender player to add the custom receiver for
	 * @param playerReceiver    player that the sender will be able to see glowing
	 */
	public void addCustomGlowReceiver(IEGlowPlayer eGlowPlayerSender, Player playerReceiver) {
		if (eGlowPlayerSender == null)
			return;

		eGlowPlayerSender.addGlowTarget(playerReceiver);
		PacketUtil.forceUpdateGlow(eGlowPlayerSender);
	}

	/**
	 * remove custom receiver for a player
	 *
	 * @param eGlowPlayerSender player to remove the custom receiver for
	 * @param playerReceiver    player that the sender will no longer be able to see glowing
	 */
	public void removeCustomGlowReceiver(IEGlowPlayer eGlowPlayerSender, Player playerReceiver) {
		if (eGlowPlayerSender == null)
			return;

		eGlowPlayerSender.removeGlowTarget(playerReceiver);
		PacketUtil.forceUpdateGlow(eGlowPlayerSender);
	}

	/**
	 * set custom receivers for a player
	 *
	 * @param eGlowPlayerSender  player to set the custom receivers for
	 * @param playerReceiverList players that the sender will be able to see glowing
	 */
	public void setCustomGlowReceivers(IEGlowPlayer eGlowPlayerSender, List<Player> playerReceiverList) {
		if (eGlowPlayerSender == null)
			return;

		eGlowPlayerSender.setGlowTargets(playerReceiverList);
		PacketUtil.forceUpdateGlow(eGlowPlayerSender);
	}

	/**
	 * reset custom receivers for a player
	 *
	 * @param eGlowPlayer player to reset the custom receivers for
	 */
	public void resetCustomGlowReceivers(IEGlowPlayer eGlowPlayer) {
		if (eGlowPlayer == null)
			return;

		eGlowPlayer.resetGlowTargets();
		PacketUtil.forceUpdateGlow(eGlowPlayer);
	}

	/**
	 * Enable/Disable eGlow from sending team packets
	 *
	 * @param status true to send packets, false for nothing
	 */
	public void setSendTeamPackets(boolean status) {
		PacketUtil.setSendTeamPackets(status);
	}

	/**
	 * Enable/Disable eGlow from blocking packets that could overwrite the glow color
	 *
	 * @param status true for packet blocking, false for nothing
	 */
	public void setPacketBlockerStatus(boolean status) {
		PipelineInjector.setBlockPackets(status);
	}
}