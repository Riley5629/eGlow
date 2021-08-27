package me.MrGraycat.eGlow.Util.Packets;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.netty.channel.*;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOut;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOutEntityMetadata;

public class PipelineInjector{
	private EGlow instance;
	
	public PipelineInjector(EGlow instance) {
		setInstance(instance);
	}
	
	private final String DECODER_NAME = "eGlowReader";
	public HashMap<Integer, IEGlowPlayer> glowingEntities = new HashMap<Integer, IEGlowPlayer>();
	
	public void inject(IEGlowPlayer eglowPlayer) {
		Channel channel = (Channel) getInstance().getNMSHook().getChannel(eglowPlayer.getPlayer());
		
		if (!channel.pipeline().names().contains("packet_handler"))
			return;
		
		if (channel.pipeline().names().contains(DECODER_NAME)) 
			channel.pipeline().remove(DECODER_NAME);
		
		try {
			channel.pipeline().addBefore("packet_handler", DECODER_NAME, new ChannelDuplexHandler() {
				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					super.channelRead(context, packet);
					return;
				}
				
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {		
					if (getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						modifyPlayers(packet);
					}

					if (getInstance().getNMSHook().nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
						Integer entityID = (Integer) PacketPlayOut.getField(packet, "a");
					
						if (glowingEntities.containsKey(entityID)) {
							PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
							IEGlowPlayer glowingTarget = glowingEntities.get(entityID);
							
							GlowVisibility gv = eglowPlayer.getGlowVisibility();
							
							if (gv.equals(GlowVisibility.UNSUPPORTEDCLIENT) || gv.equals(GlowVisibility.NONE) || //Player can't see the glow & has it enabled
								glowingTarget.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && !glowingTarget.getGlowTargets().contains(eglowPlayer.getPlayer()) || //API method check
								gv.equals(GlowVisibility.OWN) && !glowingTarget.getPlayer().equals(eglowPlayer.getPlayer())) { //if glow is set to personal
								super.write(context, packet, channelPromise);
								return;
							}

							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getInstance(), entityID, getInstance().getNMSHook().setGlowFlag(glowingTarget.getEntity(), true)/*eglowEntity*/);
							super.write(context, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()), channelPromise);
							return;
						}
					}

					super.write(context, packet, channelPromise);
					return;
				}
			});
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
	}

	public void uninject(IEGlowPlayer eglowPlayer) {
		Channel channel = (Channel) getInstance().getNMSHook().getChannel(eglowPlayer.getPlayer());
		if (channel.pipeline().names().contains(DECODER_NAME)) channel.pipeline().remove(DECODER_NAME);
		
		if (glowingEntities.containsValue(eglowPlayer))
			glowingEntities.remove(eglowPlayer.getPlayer().getEntityId());
	}
	
	@SuppressWarnings("unchecked")
	private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (!blockPackets || !EGlowMainConfig.OptionFeaturePacketBlocker())
			return;
		
		String teamName = getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam).toString();
		Collection<String> players = (Collection<String>) getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
		if (players == null) return;
		Collection<String> newList = new ArrayList<>();
		
		for (String entry : players) {
			Player p = Bukkit.getPlayer(entry);
			IEGlowPlayer ePlayer = (p != null) ? getInstance().getDataManager().getEGlowPlayer(p) : null;
			
			if (p == null) {
				newList.add(entry);
				continue;
			}
			
			if (getInstance().getTABAddon() == null) {
				if (!ePlayer.getTeamName().equals(teamName))
					continue;
			} else {
				if (getInstance().getTABAddon().isUnlimitedNametagModeEnabled())
					continue;
			}
			
			newList.add(entry);
			getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
		}		
		return;
	}

	private boolean blockPackets = true;
	
	public boolean blockPackets() {
		return this.blockPackets;
	}

	public void setBlockPackets(boolean blockPackets) {
		this.blockPackets = blockPackets;
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
