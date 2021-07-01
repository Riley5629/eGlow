package me.MrGraycat.eGlow.Util.Packets;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.google.common.collect.Sets;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.EnumChatFormat;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOutEntityMetadata;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOutScoreboardTeam;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;

public class PacketUtil {
	private static boolean sendPackets = true;
	
	public static void updatePlayerNEW(IEGlowPlayer ePlayer) {
		ProtocolVersion pVersion = ePlayer.getVersion();
		
		for (IEGlowPlayer ep : EGlow.getDataManager().getEGlowPlayers()) {
			if (!(ep.getEntity() instanceof Player) || ep.equals(ePlayer))
				continue;
			
			if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
				if (EGlow.getTABAddon() == null) {
					try {NMSHook.sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagPrefix(ep) : "", (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagSuffix(ep) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));} catch (Exception e) {}
				} else {
					if (!EGlow.getTABAddon().isUnlimitedNametagModeEnabled())
						try {NMSHook.sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagPrefix(ep) : "", (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagSuffix(ep) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));} catch (Exception e) {}
				}		
			}
			
			if (!ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT) && ep.getGlowStatus()) {
				Object glowingEntity = ep.getEntity();
				int glowingEntityID = ep.getPlayer().getEntityId();
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
				
				try {
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, true));
				} catch (Exception e1) {
					packetPlayOutEntityMetadata = null;
					e1.printStackTrace();
				} 

				if (ep.getGlowTargetMode().equals(GlowTargetMode.ALL) || ep.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && ep.getGlowTargets().contains(ePlayer.getPlayer())) {
					if (ep.getGlowVisibility().equals(GlowVisibility.ALL)) {
						try {NMSHook.sendPacket(ePlayer, packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}
				}
			}
		}
	}
	
	public static synchronized void scoreboardPacket(IEGlowPlayer to, boolean join) {
		try {
			if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
				if (to == null)
					return;
				
				if (to.getVersion().getMinorVersion() >= 8) {
					for (IEGlowPlayer players : EGlow.getDataManager().getEGlowPlayers()) {
						NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName()).toNMS(to.getVersion()));
					}
				}
				if (join) {
					if (EGlow.getTABAddon() == null) {
						for (IEGlowPlayer players : EGlow.getDataManager().getEGlowPlayers()) {
							NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagPrefix(to) : "", (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagSuffix(to) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
						}
					} else {
						if (!EGlow.getTABAddon().isUnlimitedNametagModeEnabled()) {
							for (IEGlowPlayer players : EGlow.getDataManager().getEGlowPlayers()) {
								NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagPrefix(to) : "", (EGlow.getVaultAddon() != null) ? EGlow.getVaultAddon().getPlayerTagSuffix(to) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
							}
						}
					}	
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateScoreboardTeam(IEGlowPlayer entity, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), 21).setColor(color);
		
		if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
			if (EGlow.getTABAddon() != null) {
				if (!EGlow.getTABAddon().isUnlimitedNametagModeEnabled())
					return;
			}		
			
			if (entity == null) 
				return;
		
			switch(entity.getGlowTargetMode()) {
			case ALL:
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (EGlow.getDataManager().getEGlowPlayer(player) != null)
						try {NMSHook.sendPacket(player, packet.toNMS(EGlow.getDataManager().getEGlowPlayer(player).getVersion()));} catch (Exception e) {e.printStackTrace();}
				}
				break;
			case CUSTOM:
				for (Player player : entity.getGlowTargets()) {
					if (player != null && EGlow.getDataManager().getEGlowPlayer(player) != null)
						try {NMSHook.sendPacket(player, packet.toNMS(EGlow.getDataManager().getEGlowPlayer(player).getVersion()));} catch (Exception e) {e.printStackTrace();}	
				}
				break;
			}
			
		}
		
	}
	
	public static void updateGlowing(IEGlowPlayer entity, boolean status) {
		if (entity == null) 
			return;
		
		Object glowingEntity = entity.getEntity();
		int glowingEntityID = entity.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
		
		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			packetPlayOutEntityMetadata = null;
			e1.printStackTrace();
		} 
		
		if (status) {	
			if (!PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.put(glowingEntityID, entity);
			
			switch(entity.getGlowTargetMode()) {
			case ALL:
				for (Player player : Bukkit.getOnlinePlayers()) {
					IEGlowPlayer eglowPlayer = EGlow.getDataManager().getEGlowPlayer(player);
					
					if (eglowPlayer == null) 
						break;

					switch(eglowPlayer.getGlowVisibility()) {
					case ALL:
						try {NMSHook.sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
						break;
					case OWN:
						if (entity.getPlayer().equals(player))
							try {NMSHook.sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
						break;
					case NONE:
						break;
					case UNSUPPORTEDCLIENT:
						break;
					}
				}
				break;
			case CUSTOM:
				for (Player player : entity.getGlowTargets()) {
					IEGlowPlayer ep = EGlow.getDataManager().getEGlowPlayer(player);
					
					if (player != null && ep != null) {
						switch(ep.getGlowVisibility()) {
						case ALL:
							try {NMSHook.sendPacket(player, packetPlayOutEntityMetadata.toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case OWN:
							if (entity.getPlayer().equals(player))
								try {NMSHook.sendPacket(player, packetPlayOutEntityMetadata.toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case NONE:
							break;
						case UNSUPPORTEDCLIENT:
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
				IEGlowPlayer eglowPlayer = EGlow.getDataManager().getEGlowPlayer(player);
				if (eglowPlayer != null)
					try {NMSHook.sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
			}
		}
	}
	
	public static void forceUpdateGlow(IEGlowPlayer ePlayer) {
		switch(ePlayer.getGlowTargetMode()) {
		case ALL:
			for (Player p : Bukkit.getOnlinePlayers()) {
				IEGlowPlayer ep = EGlow.getDataManager().getEGlowPlayer(p);
				boolean isGlowing = (ep.getGlowStatus() || ep.getFakeGlowStatus()) ? true : false;
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
				IEGlowPlayer ep = EGlow.getDataManager().getEGlowPlayer(p);
				boolean isGlowing = (ep.getGlowStatus() || ep.getFakeGlowStatus()) ? true : false;
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
		}		
	}
	
	//TODO could remove player (center) itself
	public List<Player> playersInRangeOfPlayer(Player center) {
		List<Player> playersInRange = center.getWorld().getPlayers();
		
		for (Player p : playersInRange) {
			Location diff = center.getLocation().subtract(p.getLocation());

			if (diff.getX() > 50 && diff.getZ() > 50 && diff.getX() < -50 && diff.getZ() < -50)
				playersInRange.remove(p);
		}
		return playersInRange;
	}
	
	@SuppressWarnings("unused")
	private boolean inRange(Location loc1, Location loc2) {
		if (Math.abs(loc1.getX() - loc2.getX()) > 50 || 
			Math.abs(loc1.getZ() - loc2.getZ()) > 50) return false;
		return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2)) <= 50;
	}
	
	public static void setSendTeamPackets(boolean status) {
		sendPackets = status;
	}
}
