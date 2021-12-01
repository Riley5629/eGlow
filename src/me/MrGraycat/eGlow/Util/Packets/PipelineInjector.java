package me.MrGraycat.eGlow.Util.Packets;

import java.util.*;

import io.netty.channel.*;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOut;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOutEntityMetadata;

public class PipelineInjector{
	public PipelineInjector() {}
	
	private final String DECODER_NAME = "eGlowReader";
	public HashMap<Integer, IEGlowPlayer> glowingEntities = new HashMap<Integer, IEGlowPlayer>();
	
	public void inject(IEGlowPlayer eglowPlayer) {		
		Channel channel = (Channel) EGlow.getInstance().getNMSHook().getChannel(eglowPlayer.getPlayer());
		
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
					if (EGlow.getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						if (EGlow.getInstance().getTABAddon() != null && EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
							super.write(context, packet, channelPromise);
							return;
						}
						
						modifyPlayers(packet);
						super.write(context, packet, channelPromise);
						return;
					}

					if (EGlow.getInstance().getNMSHook().nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
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

							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(EGlow.getInstance(), entityID, EGlow.getInstance().getNMSHook().setGlowFlag(glowingTarget.getEntity(), true)/*eglowEntity*/);
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
		Channel channel = (Channel) EGlow.getInstance().getNMSHook().getChannel(eglowPlayer.getPlayer());
		if (channel.pipeline().names().contains(DECODER_NAME)) channel.pipeline().remove(DECODER_NAME);
		
		if (glowingEntities.containsValue(eglowPlayer))
			glowingEntities.remove(eglowPlayer.getPlayer().getEntityId());
	}
	
	@SuppressWarnings("unchecked")
	private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (!blockPackets || !EGlowMainConfig.OptionFeaturePacketBlocker())
			return;
		
		String teamName = EGlow.getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam).toString();
		Collection<String> players = (Collection<String>) EGlow.getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
		if (players == null) return;
		Collection<String> newList = new ArrayList<>();
		
		for (String entry : players) {
			IEGlowPlayer ePlayer = EGlow.getInstance().getDataManager().getEGlowPlayer(entry);
			
			if (ePlayer == null) {
				newList.add(entry);
				continue;
			}
			
			if (!ePlayer.getTeamName().equals(teamName))
				continue;

			newList.add(entry);
		}	
		
		EGlow.getInstance().getNMSHook().nms.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
	}

	private boolean blockPackets = true;
	
	public boolean blockPackets() {
		return this.blockPackets;
	}

	public void setBlockPackets(boolean blockPackets) {
		this.blockPackets = blockPackets;
	}
}
