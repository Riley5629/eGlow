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
import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowEventListener implements Listener {
	private EGlow instance;
	
	public EGlowEventListener(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
		
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			new EGlowEventListener113AndAbove(getInstance());
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
		IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(p);
		
		if (eglowPlayer != null && EGlowMainConfig.getWorldCheckEnabled()) {	
			if (eglowPlayer.isInBlockedWorld() && (eglowPlayer.getGlowStatus() || eglowPlayer.getFakeGlowStatus())) {
				eglowPlayer.toggleGlow();
				eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
				ChatUtil.sendMsgWithPrefix(p, Message.WORLD_BLOCKED.get());
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
	public void PlayerConnect(Player p, UUID uuid) {
		if (p.isGlowing())
			p.setGlowing(false);
		
		IEGlowPlayer eglowPlayer = getInstance().getDataManager().addEGlowPlayer(p, uuid.toString());
		getInstance().getPipelineInjector().inject(eglowPlayer);
		getInstance().getPacketUtil().scoreboardPacket(eglowPlayer, true);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				instance.getPlayerdataManager().loadPlayerdata(eglowPlayer);
				
				if (!getInstance().isUpToDate() && EGlowMainConfig.OptionSendUpdateNotifications() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendMsgWithPrefix(p, "&aA new update is available&f!");
				
				instance.getPacketUtil().updatePlayerNEW(eglowPlayer);
				
				if (instance.getVaultAddon() != null)
					instance.getVaultAddon().updatePlayerTabname(eglowPlayer);
				
				IEGlowEffect effect = eglowPlayer.getForceGlow();

				if (effect != null) {
					if (instance.getLibDisguiseAddon() != null && instance.getLibDisguiseAddon().isDisguised(p) || instance.getIDisguiseAddon() != null && instance.getIDisguiseAddon().isDisguised(p)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsgWithPrefix(p, Message.DISGUISE_BLOCKED.get());
					} else if (eglowPlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY) && !eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.INVISIBLE);
						ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.INVISIBILITY_DISABLED.get());
					} else {
						eglowPlayer.activateGlow(effect);
					}
					return;
				}
				
				if (eglowPlayer.getActiveOnQuit()) {
					if (eglowPlayer.getEffect() == null || !eglowPlayer.getGlowOnJoin() || !p.hasPermission("eglow.option.glowonjoin") || EGlowMainConfig.OptionPermissionCheckonJoin() && !p.hasPermission(eglowPlayer.getEffect().getPermission()))
						return;
					
					if (EGlowMainConfig.getWorldCheckEnabled() && eglowPlayer.isInBlockedWorld() && eglowPlayer.getGlowStatus() || eglowPlayer.getFakeGlowStatus()) {
						eglowPlayer.toggleGlow();
						eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
						ChatUtil.sendMsgWithPrefix(eglowPlayer.getPlayer(), Message.WORLD_BLOCKED.get());
					}
					
					if (instance.getLibDisguiseAddon() != null && instance.getLibDisguiseAddon().isDisguised(p) || instance.getIDisguiseAddon() != null && instance.getIDisguiseAddon().isDisguised(p)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsgWithPrefix(p, Message.DISGUISE_BLOCKED.get());
					} else {
						eglowPlayer.activateGlow();
					}
				}
			}
		}.runTaskLaterAsynchronously(getInstance(), 2L);
	}
	
	/**
	 * Code to unload the player from eGlow
	 * @param p player to unload
	 */
	public void PlayerDisconnect(Player p, boolean shutdown) {
		IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(p);
		getInstance().getPacketUtil().scoreboardPacket(eglowPlayer, false);
		
		if (!shutdown) {
			new BukkitRunnable() {
				@Override
				public void run() {
					PlayerDisconnectNext(eglowPlayer);
				}
			}.runTaskAsynchronously(getInstance());
		} else {
			PlayerDisconnectNext(eglowPlayer);
		}
	}
	
	private void PlayerDisconnectNext(IEGlowPlayer eglowPlayer) {
		if (eglowPlayer != null) {
			eglowPlayer.setActiveOnQuit((eglowPlayer.getFakeGlowStatus() || eglowPlayer.getGlowStatus()) ? true : false);
			getInstance().getPlayerdataManager().savePlayerdata(eglowPlayer);
			//eglowPlayer.disableGlow(true);
			
			getInstance().getPipelineInjector().uninject(eglowPlayer);
			getInstance().getDataManager().removeEGlowPlayer(eglowPlayer.getPlayer());
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
