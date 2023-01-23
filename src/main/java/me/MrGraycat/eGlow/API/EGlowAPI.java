package me.MrGraycat.eGlow.API;

import me.MrGraycat.eGlow.API.Enum.EGlowBlink;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.API.Enum.EGlowEffect;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.PipelineInjector;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class EGlowAPI {
	/**
	 * Get the IEGlowEntity from eGlow
	 * @param player player to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the player
	 */
	public IEGlowPlayer getEGlowPlayer(Player player) {
		return DataManager.getEGlowPlayer(player);
	}
	
	/**
	 * Get the IEGlowEntity from eGlow
	 * @param uuid uuid to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the uuid
	 */
	public IEGlowPlayer getEGlowPlayer(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			return DataManager.getEGlowPlayer(p);
		return null;
	}
	
	/**
	 * Get the IEGlowEffect from eGlow
	 * @param name name for the effect
	 * @return IEGlowEffect is found, null if not
	 */
	public IEGlowEffect getEGlowEffect(String name) {
		IEGlowEffect effect = DataManager.getEGlowEffect(name);
		
		if (effect == null)
			ChatUtil.sendToConsole("(API) Unable to find effect for name: " + name, true);
		return effect;
	}
	
	/**
	 * Get the glow color from a player
	 * @param player player to get the glow color from
	 * @return Glow color as String (invisible)
	 */
	public String getGlowColor(IEGlowPlayer player) {
	    if (player == null)
	      return ""; 
	    
	    return (player.isGlowing()) ? player.getActiveColor() + "" : "";
	}
	
	/**
	 * Enable a specific effect for a player
	 * @param player to activate the effect for
	 * @param effect to enable
	 */
	public void enableGlow(IEGlowPlayer player, IEGlowEffect effect) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}
	
	/**
	 * Enable a solid glow color for a player
	 * @param player to activate the glow for
	 * @param color to enable
	 */
	public void enableGlow(IEGlowPlayer player, EGlowColor color) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				IEGlowEffect effect = DataManager.getEGlowEffect(color.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}
	
	/**
	 * Enable a blink effect for a player
	 * @param player to activate the blink for
	 * @param blink to enable
	 */
	public void enableGlow(IEGlowPlayer player, EGlowBlink blink) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				IEGlowEffect effect = DataManager.getEGlowEffect(blink.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}
	
	/**
	 * Enable an effect for a player
	 * @param player to activate the effect for
	 * @param effects to enable
	 */
	public void enableGlow(IEGlowPlayer player, EGlowEffect effects) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				IEGlowEffect effect = DataManager.getEGlowEffect(effects.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);
	}
	
	/**
	 * Disable the glow for a player
	 * @param player to disable the glow for
	 */
	public void disableGlow(IEGlowPlayer player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				player.disableGlow(true);
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 1);	
	}
	
	/**
	 * add custom receiver for a player
	 * @param sender player to add the custom receiver for
	 * @param receiver player that the sender will be able to see glowing
	 */
	public void addCustomGlowReceiver(IEGlowPlayer sender, Player receiver) {
		if (sender == null)
			return;
		
		sender.addGlowTarget(receiver);

		PacketUtil.forceUpdateGlow(sender);	
	}
	
	/**
	 * remove custom receiver for a player
	 * @param sender player to remove the custom receiver for
	 * @param receiver player that the sender will no longer be able to see glowing
	 */
	public void removeCustomGlowReceiver(IEGlowPlayer sender, Player receiver) {
		if (sender == null)
			return;
		
		sender.removeGlowTarget(receiver);
		PacketUtil.forceUpdateGlow(sender);	
	}
	
	/**
	 * set custom receivers for a player
	 * @param sender player to set the custom receivers for
	 * @param receivers players that the sender will be able to see glowing
	 */
	public void setCustomGlowReceivers(IEGlowPlayer sender, List<Player> receivers) {
		if (sender == null)
			return;
		
		sender.setGlowTargets(receivers);
		PacketUtil.forceUpdateGlow(sender);	
	}
	
	/**
	 * reset custom receivers for a player
	 * @param sender player to reset the custom receivers for
	 */
	public void resetCustomGlowReceivers(IEGlowPlayer sender) {
		if (sender == null)
			return;
		
		sender.resetGlowTargets();
		PacketUtil.forceUpdateGlow(sender);	
	}
	
	/**
	 * Enable/Disable eGlow from sending team packets
	 * @param status true to send packets, false for nothing
	 */
	public void setSendTeamPackets(boolean status) {
		PacketUtil.setSendTeamPackets(status);
	}
	
	/**
	 * Enable/Disable eGlow from blocking packets that could overwrite the glow color
	 * @param status true for packet blocking, false for nothing
	 */
	public void setPacketBlockerStatus(boolean status) {
		PipelineInjector.setBlockPackets(status);	
	}
}