package me.MrGraycat.eGlow.Util.Packets;

import com.google.common.collect.Sets;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.Chat.EnumChatFormat;
import me.MrGraycat.eGlow.Util.Packets.Chat.IChatBaseComponent;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOutActionBar;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOutChat;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOutEntityMetadata;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PacketUtil {
	private static boolean sendPackets = true;
	
	public static void updatePlayer(IEGlowPlayer ePlayer) {
		ProtocolVersion pVersion = ePlayer.getVersion();
		
		for (IEGlowPlayer ep : DataManager.getEGlowPlayers()) {
			if (!(ep.getEntity() instanceof Player) || ep.equals(ePlayer))
				continue;
			
			if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
				if (EGlow.getInstance().getTABAddon() == null || !EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
					try {
						NMSHook.sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(ep) : "", (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(ep) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));} catch (Exception e) {e.printStackTrace();}
				}	
			}
			
			if (!ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
				if (!ep.getGlowStatus() && !ep.getFakeGlowStatus())
					return;
				
				Object glowingEntity = ep.getEntity();
				int glowingEntityID = ep.getPlayer().getEntityId();
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
				
				try {
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, true));
				} catch (Exception e1) {
					e1.printStackTrace();
				} 

				if (ep.getGlowTargetMode().equals(GlowTargetMode.ALL) || ep.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && ep.getGlowTargets().contains(ePlayer.getPlayer())) {
					if (ePlayer.getGlowVisibility().equals(GlowVisibility.ALL)) {
						try {NMSHook.sendPacket(ePlayer, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}
				}
			}
		}
	}
	
	public static synchronized void scoreboardPacket(IEGlowPlayer to, boolean join) {
		try {
			if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
				if (to == null || EGlow.getInstance() == null)
					return;
				
				if (to.getVersion().getMinorVersion() >= 8) {
					if (EGlow.getInstance().getTABAddon() == null || !EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
						for (IEGlowPlayer players : DataManager.getEGlowPlayers()) {
							NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName()).toNMS(to.getVersion()));
						}
					}
				}
				
				if (join) {
					if (EGlow.getInstance().getTABAddon() == null || !EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
						for (IEGlowPlayer players : DataManager.getEGlowPlayers()) {
							NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(to) : "", (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(to) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
						}
					}	
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateScoreboardTeam(IEGlowPlayer entity, String teamName, String prefix, String suffix, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), 21).setColor(color);
		
		if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
			if (EGlow.getInstance() == null)
				return;
			
			if (EGlow.getInstance().getTABAddon() != null && EGlow.getInstance().getTABAddon().blockEGlowPackets())
				return;
			
			if (entity == null) 
				return;

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (DataManager.getEGlowPlayer(player) != null)
					try {
						NMSHook.sendPacket(player, packet.toNMS(DataManager.getEGlowPlayer(player).getVersion()));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	public static void updateGlowing(IEGlowPlayer entity, boolean status) {
		if (entity == null || EGlow.getInstance() == null) 
			return;
		
		Object glowingEntity = entity.getEntity();
		int glowingEntityID = entity.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
		
		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		if (status) {
			if (!PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.put(glowingEntityID, entity);
			
			switch(entity.getGlowTargetMode()) {
			case ALL:
				for (Player player : Bukkit.getOnlinePlayers()) {
					IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
					
					if (eglowPlayer == null) 
						break;

					switch(eglowPlayer.getGlowVisibility()) {
					case ALL:
						try {NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
						break;
					case OWN:
						if (entity.getPlayer().equals(player))
							try {NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
						break;
					default:
						break;
					}
				}
				break;
			case CUSTOM:
				for (Player player : entity.getGlowTargets()) {
					IEGlowPlayer ep = DataManager.getEGlowPlayer(player);
					
					if (ep != null) {
						switch(ep.getGlowVisibility()) {
						case ALL:
							try {NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case OWN:
							if (entity.getPlayer().equals(player))
								try {NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						default:
							break;
						}
					}
				}
				break;
			}	
		} else {
			if (PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.remove(glowingEntityID, entity);
		
			for (Player player : Bukkit.getOnlinePlayers()) {
				IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
				if (eglowPlayer != null)
					try {NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
			}
		}
	}

	public static void glowTargetChange(IEGlowPlayer main, Player change, boolean type) {
		IEGlowPlayer target = DataManager.getEGlowPlayer(change);

		if (target == null)
			return;

		Object glowingEntity = main.getEntity();
		int glowingEntityID = main.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		if (type && !(main.getGlowStatus() || main.getFakeGlowStatus()))
			type = false;

		try {packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, type));} catch (Exception e1) {e1.printStackTrace();}
		try {NMSHook.sendPacket(target, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(target.getVersion()));} catch (Exception e) {e.printStackTrace();}
	}

	public static void updateGlowTarget(IEGlowPlayer ePlayer) {
		Collection<IEGlowPlayer> players = DataManager.getEGlowPlayers();
		List<Player> customTargets = ePlayer.getGlowTargets();

		Object glowingEntity = ePlayer.getEntity();
		int glowingEntityID = ePlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, ePlayer.getGlowTargetMode().equals(GlowTargetMode.ALL) && ePlayer.getGlowStatus()));} catch (Exception e1) {e1.printStackTrace();}

		switch(ePlayer.getGlowTargetMode()) {
			case ALL:
				for (IEGlowPlayer ep : players) {
					if (ep.getGlowVisibility().equals(GlowVisibility.ALL) || ep.equals(ePlayer) && ep.getGlowVisibility().equals(GlowVisibility.OWN)) {
						try {NMSHook.sendPacket(ep, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}
				}
				break;
			case CUSTOM:
				for (IEGlowPlayer ep : players) {
					if (!customTargets.contains(ep.getPlayer())) {
						try {NMSHook.sendPacket(ep, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}
				}
				break;
		}
	}

	public static void forceUpdateGlow(IEGlowPlayer ePlayer) {
		switch(ePlayer.getGlowTargetMode()) {
			case ALL:
				for (Player p : Bukkit.getOnlinePlayers()) {
					IEGlowPlayer ep = DataManager.getEGlowPlayer(p);
					boolean isGlowing = ep.getGlowStatus() || ep.getFakeGlowStatus();
					PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;

					switch(ePlayer.getGlowVisibility()) {
						case ALL:
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, isGlowing));
							try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case OWN:
							if (p.equals(ePlayer.getPlayer())) {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, isGlowing));
								try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							} else {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, false));
								try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							}
							break;
						case NONE:
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, false));
							try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case UNSUPPORTEDCLIENT:
							break;
					}
				}
				break;
			case CUSTOM:
				for (Player p : ePlayer.getGlowTargets()) {
					IEGlowPlayer ep = DataManager.getEGlowPlayer(p);
					boolean isGlowing = ep.getGlowStatus() || ep.getFakeGlowStatus();
					PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;

					switch(ePlayer.getGlowVisibility()) {
						case ALL:
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, isGlowing));
							try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case OWN:
							if (p.equals(ep.getPlayer())) {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, isGlowing));
								try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							} else {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(),NMSHook.setGlowFlag(p, false));
								try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							}
							break;
						case NONE:
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, false));
							try {NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case UNSUPPORTEDCLIENT:
							break;
					}
				}
				break;
		}
	}

	public static void sendActionbar(IEGlowPlayer ePlayer, String text) {
		if (text.isEmpty())
			return;

		IChatBaseComponent formattedText = IChatBaseComponent.optimizedComponent(text);

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
			PacketPlayOutActionBar packetPlayOutActionBar = new PacketPlayOutActionBar(formattedText);

			try {
				NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutActionBar.toNMS(ePlayer.getVersion()));
			} catch (Exception e){
				e.printStackTrace();
			}
		} else {
			PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(formattedText, PacketPlayOutChat.ChatMessageType.GAME_INFO);

			try {
				NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutChat.toNMS(ePlayer.getVersion()));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void setSendTeamPackets(boolean status) {
		sendPackets = status;
	}
}
