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
	private EGlow instance;
	private boolean sendPackets = true;
	
	public PacketUtil(EGlow instance) {
		setInstance(instance);
	}
	
	public void updatePlayerNEW(IEGlowPlayer ePlayer) {
		ProtocolVersion pVersion = ePlayer.getVersion();
		
		for (IEGlowPlayer ep : getInstance().getDataManager().getEGlowPlayers()) {
			if (!(ep.getEntity() instanceof Player) || ep.equals(ePlayer))
				continue;
			
			if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
				if (getInstance().getTABAddon() == null) {
					try {getInstance().getNMSHook().sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagPrefix(ep) : "", (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagSuffix(ep) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));} catch (Exception e) {}
				} else {
					if (!getInstance().getTABAddon().isUnlimitedNametagModeEnabled())
						try {getInstance().getNMSHook().sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagPrefix(ep) : "", (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagSuffix(ep) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));} catch (Exception e) {}
				}		
			}
			
			if (!ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT) && ep.getGlowStatus()) {
				Object glowingEntity = ep.getEntity();
				int glowingEntityID = ep.getPlayer().getEntityId();
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
				
				try {
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), glowingEntityID, getInstance().getNMSHook().setGlowFlag(glowingEntity, true));
				} catch (Exception e1) {
					packetPlayOutEntityMetadata = null;
					e1.printStackTrace();
				} 

				if (ep.getGlowTargetMode().equals(GlowTargetMode.ALL) || ep.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && ep.getGlowTargets().contains(ePlayer.getPlayer())) {
					if (ep.getGlowVisibility().equals(GlowVisibility.ALL)) {
						try {getInstance().getNMSHook().sendPacket(ePlayer, packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}
				}
			}
		}
	}
	
	public synchronized void scoreboardPacket(IEGlowPlayer to, boolean join) {
		try {
			if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
				if (to == null)
					return;
				
				if (to.getVersion().getMinorVersion() >= 8) {
					for (IEGlowPlayer players : getInstance().getDataManager().getEGlowPlayers()) {
						getInstance().getNMSHook().sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName()).toNMS(to.getVersion()));
					}
				}
				if (join) {
					if (getInstance().getTABAddon() == null) {
						for (IEGlowPlayer players : getInstance().getDataManager().getEGlowPlayers()) {
							getInstance().getNMSHook().sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagPrefix(to) : "", (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagSuffix(to) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
						}
					} else {
						if (!getInstance().getTABAddon().isUnlimitedNametagModeEnabled()) {
							for (IEGlowPlayer players : getInstance().getDataManager().getEGlowPlayers()) {
								getInstance().getNMSHook().sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagPrefix(to) : "", (getInstance().getVaultAddon() != null) ? getInstance().getVaultAddon().getPlayerTagSuffix(to) : "", (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
							}
						}
					}	
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateScoreboardTeam(IEGlowPlayer entity, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (EGlowMainConfig.OptionShowNametag() ? "always" : "never"), (EGlowMainConfig.OptionDoTeamCollision() ? "always" : "never"), 21).setColor(color);
		
		if (sendPackets && EGlowMainConfig.OptionFeatureTeamPackets()) {
			if (getInstance().getTABAddon() != null) {
				if (!getInstance().getTABAddon().isUnlimitedNametagModeEnabled())
					return;
			}		
			
			if (entity == null) 
				return;
		
			switch(entity.getGlowTargetMode()) {
			case ALL:
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (getInstance().getDataManager().getEGlowPlayer(player) != null)
						try {getInstance().getNMSHook().sendPacket(player, packet.toNMS(getInstance().getDataManager().getEGlowPlayer(player).getVersion()));} catch (Exception e) {e.printStackTrace();}
				}
				break;
			case CUSTOM:
				for (Player player : entity.getGlowTargets()) {
					if (player != null && getInstance().getDataManager().getEGlowPlayer(player) != null)
						try {getInstance().getNMSHook().sendPacket(player, packet.toNMS(getInstance().getDataManager().getEGlowPlayer(player).getVersion()));} catch (Exception e) {e.printStackTrace();}	
				}
				break;
			}
			
		}
		
	}
	
	public void updateGlowing(IEGlowPlayer entity, boolean status) {
		if (entity == null) 
			return;
		
		Object glowingEntity = entity.getEntity();
		int glowingEntityID = entity.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
		
		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), glowingEntityID, getInstance().getNMSHook().setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			packetPlayOutEntityMetadata = null;
			e1.printStackTrace();
		} 
		
		if (status) {	
			if (!getInstance().getPipelineInjector().glowingEntities.containsKey(glowingEntityID))
				getInstance().getPipelineInjector().glowingEntities.put(glowingEntityID, entity);
			
			switch(entity.getGlowTargetMode()) {
			case ALL:
				for (Player player : Bukkit.getOnlinePlayers()) {
					IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(player);
					
					if (eglowPlayer == null) 
						break;

					switch(eglowPlayer.getGlowVisibility()) {
					case ALL:
						try {getInstance().getNMSHook().sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
						break;
					case OWN:
						if (entity.getPlayer().equals(player))
							try {getInstance().getNMSHook().sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
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
					IEGlowPlayer ep = getInstance().getDataManager().getEGlowPlayer(player);
					
					if (player != null && ep != null) {
						switch(ep.getGlowVisibility()) {
						case ALL:
							try {getInstance().getNMSHook().sendPacket(player, packetPlayOutEntityMetadata.toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
							break;
						case OWN:
							if (entity.getPlayer().equals(player))
								try {getInstance().getNMSHook().sendPacket(player, packetPlayOutEntityMetadata.toNMS(ep.getVersion()));} catch (Exception e) {e.printStackTrace();}
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
			if (getInstance().getPipelineInjector().glowingEntities.containsKey(glowingEntityID))
				getInstance().getPipelineInjector().glowingEntities.remove(glowingEntityID, entity);
		
			for (Player player : Bukkit.getOnlinePlayers()) {
				IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(player);
				if (eglowPlayer != null)
					try {getInstance().getNMSHook().sendPacket(player, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
			}
		}
	}
	
	public void forceUpdateGlow(IEGlowPlayer ePlayer) {
		switch(ePlayer.getGlowTargetMode()) {
		case ALL:
			for (Player p : Bukkit.getOnlinePlayers()) {
				IEGlowPlayer ep = getInstance().getDataManager().getEGlowPlayer(p);
				boolean isGlowing = (ep.getGlowStatus() || ep.getFakeGlowStatus()) ? true : false;
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;
				
				switch(ePlayer.getGlowVisibility()) {
				case ALL:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, isGlowing));
					try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					break;
				case OWN:
					if (p.equals(ep.getPlayer())) {
						packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, isGlowing));
						try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					} else {
						packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, false));
						try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}	
					break;
				case NONE:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, false));
					try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					break;
				case UNSUPPORTEDCLIENT:
					break;
				}
			}
			break;
		case CUSTOM:
			for (Player p : ePlayer.getGlowTargets()) {
				IEGlowPlayer ep = getInstance().getDataManager().getEGlowPlayer(p);
				boolean isGlowing = (ep.getGlowStatus() || ep.getFakeGlowStatus()) ? true : false;
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;
				
				switch(ePlayer.getGlowVisibility()) {
				case ALL:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, isGlowing));
					try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					break;
				case OWN:
					if (p.equals(ep.getPlayer())) {
						packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, isGlowing));
						try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					} else {
						packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(),getInstance().getNMSHook().setGlowFlag(p, false));
						try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
					}	
					break;
				case NONE:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), p.getEntityId(), getInstance().getNMSHook().setGlowFlag(p, false));
					try {getInstance().getNMSHook().sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));} catch (Exception e) {e.printStackTrace();}
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
	
	public void setSendTeamPackets(boolean status) {
		sendPackets = status;
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
