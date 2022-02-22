package me.MrGraycat.eGlow.Event;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.PipelineInjector;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowEventListener implements Listener {
	public EGlowEventListener() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
		
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			new EGlowEventListener113AndAbove();
		}
	}
	
	@EventHandler
	public void PlayerConnectEvent(PlayerJoinEvent e) {
		PlayerConnect(e.getPlayer(), e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void PlayerKickedEvent(PlayerKickEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}

	@EventHandler
	public void PlayerDisconnectEvent(PlayerQuitEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}
	
	@EventHandler
	public void onMenuClick(InventoryClickEvent e) {
		InventoryHolder holder = e.getInventory().getHolder();
		
		if (holder == null)
			return;
		
		if (holder instanceof Menu) {
			e.setCancelled(true);
			
			if (e.getView().getBottomInventory().equals(e.getClickedInventory()) || e.getCurrentItem() == null) 
				return;
			
			Menu menu = (Menu) holder;
			menu.handleMenu(e);
		}
	}
	
	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);
		
		if (eglowPlayer != null && EGlowMainConfig.getWorldCheckEnabled()) {	
			if (eglowPlayer.isInBlockedWorld()) {
				if (eglowPlayer.getGlowStatus() || eglowPlayer.getFakeGlowStatus()) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
					ChatUtil.sendMsgWithPrefix(p, Message.WORLD_BLOCKED.get());
				}
			} else {
				if (eglowPlayer.getGlowDisableReason() != null && eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
					ChatUtil.sendMsgWithPrefix(p, Message.WORLD_ALLOWED.get());
				}
			}
		}
	}
	
	/**
	 * Code to initialise the player
	 * @param p player to initialise
	 */
	public static void PlayerConnect(Player p, UUID uuid) {
		if (p.isGlowing())
			p.setGlowing(false);
		
		IEGlowPlayer eglowPlayer = DataManager.addEGlowPlayer(p, uuid.toString());
		PipelineInjector.inject(eglowPlayer);
		PacketUtil.scoreboardPacket(eglowPlayer, true);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				EGlowPlayerdataManager.loadPlayerdata(eglowPlayer);
				
				if (!EGlow.getInstance().isUpToDate() && EGlowMainConfig.OptionSendUpdateNotifications() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendMsgWithPrefix(p, "&aA new update is available&f!");
				
				new BukkitRunnable() {
					@Override
					public void run() {
						PacketUtil.updatePlayerNEW(eglowPlayer);
					}
				}.runTask(EGlow.getInstance());
				
				if (EGlow.getInstance().getVaultAddon() != null)
					EGlow.getInstance().getVaultAddon().updatePlayerTabname(eglowPlayer);
				
				IEGlowEffect effect = eglowPlayer.getForceGlow();

				if (effect != null) {
					if (EGlow.getInstance().getLibDisguiseAddon() != null && EGlow.getInstance().getLibDisguiseAddon().isDisguised(p) || EGlow.getInstance().getIDisguiseAddon() != null && EGlow.getInstance().getIDisguiseAddon().isDisguised(p)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsgWithPrefix(p, Message.DISGUISE_BLOCKED.get());
					} else if (eglowPlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY) && !eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.INVISIBLE);
						ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.INVISIBILITY_DISABLED.get());
					} else {
						eglowPlayer.activateGlow(effect);
						if (EGlowMainConfig.OptionMentionGlowState() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
							ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.GLOWING_STATE_ON_JOIN.get(effect.getDisplayName()));
						return;
					}
					if (EGlowMainConfig.OptionMentionGlowState() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
						ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.NON_GLOWING_STATE_ON_JOIN.get());
					return;
				}
				
				if (eglowPlayer.getActiveOnQuit()) {
					if (eglowPlayer.getEffect() == null || !eglowPlayer.getGlowOnJoin() || !p.hasPermission("eglow.option.glowonjoin") || EGlowMainConfig.OptionPermissionCheckonJoin() && !p.hasPermission(eglowPlayer.getEffect().getPermission()))
						return;
					
					if (EGlowMainConfig.getWorldCheckEnabled() && eglowPlayer.isInBlockedWorld()) {	
						if (eglowPlayer.getGlowStatus() || eglowPlayer.getFakeGlowStatus()) {
							eglowPlayer.toggleGlow();
							eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
							ChatUtil.sendMsgWithPrefix(p, Message.WORLD_BLOCKED.get());
							return;
						}
					}

					if (EGlow.getInstance().getLibDisguiseAddon() != null && EGlow.getInstance().getLibDisguiseAddon().isDisguised(p) || EGlow.getInstance().getIDisguiseAddon() != null && EGlow.getInstance().getIDisguiseAddon().isDisguised(p)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsgWithPrefix(p, Message.DISGUISE_BLOCKED.get());
						return;
					}
					
					eglowPlayer.activateGlow();
					if (EGlowMainConfig.OptionMentionGlowState() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate") && eglowPlayer.getEffect() != null)
						ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.GLOWING_STATE_ON_JOIN.get(eglowPlayer.getEffect().getDisplayName()));
					return;
				}
				
				if (EGlowMainConfig.OptionMentionGlowState() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
					ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.NON_GLOWING_STATE_ON_JOIN.get());
				return;
			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 2L);
	}
	
	/**
	 * Code to unload the player from eGlow
	 * @param p player to unload
	 */
	public static void PlayerDisconnect(Player p, boolean shutdown) {
		IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);
		PacketUtil.scoreboardPacket(eglowPlayer, false);
		
		if (!shutdown) {
			new BukkitRunnable() {
				@Override
				public void run() {
					PlayerDisconnectNext(eglowPlayer);
				}
			}.runTaskAsynchronously(EGlow.getInstance());
		} else {
			PlayerDisconnectNext(eglowPlayer);
		}
	}
	
	private static void PlayerDisconnectNext(IEGlowPlayer eglowPlayer) {
		if (eglowPlayer != null) {
			eglowPlayer.setActiveOnQuit((eglowPlayer.getFakeGlowStatus() || eglowPlayer.getGlowStatus()) ? true : false);
			EGlowPlayerdataManager.savePlayerdata(eglowPlayer);
			//eglowPlayer.disableGlow(true);
			
			PipelineInjector.uninject(eglowPlayer);
			DataManager.removeEGlowPlayer(eglowPlayer.getPlayer());
		}
	}
}