package me.MrGraycat.eGlow.Util.Packets;

import java.util.*;

import io.netty.channel.*;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOut;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.PacketPlayOutEntityMetadata;

public class PipelineInjector{
	private static final String DECODER_NAME = "eGlowReader";
	public static HashMap<Integer, IEGlowPlayer> glowingEntities = new HashMap<Integer, IEGlowPlayer>();
	
	public static void inject(IEGlowPlayer eglowPlayer) {		
		Channel channel = (Channel) NMSHook.getChannel(eglowPlayer.getPlayer());
		
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
					if (NMSHook.nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						if (DebugUtil.TABInstalled()) {
							if (EGlow.getInstance().getTABAddon() == null) {
								super.write(context, packet, channelPromise);
								return;
							} else {
								if (!EGlow.getInstance().getTABAddon().getTABSupported() || EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
									super.write(context, packet, channelPromise);
									return;
								}
							}
						}
						
						modifyPlayers(packet);
						super.write(context, packet, channelPromise);
						return;
					}

					if (NMSHook.nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
						Integer entityID = (Integer) PacketPlayOut.getField(packet, "a");
					
						if (glowingEntities.containsKey(entityID)) {
							PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;
							IEGlowPlayer glowingTarget = glowingEntities.get(entityID);
							
							if (glowingTarget == null) {
								if (glowingEntities.containsKey(entityID))
									glowingEntities.remove(entityID);
								super.write(context, channel, channelPromise);
								return;
							}
							
							GlowVisibility gv = eglowPlayer.getGlowVisibility();
							
							if (gv.equals(GlowVisibility.UNSUPPORTEDCLIENT) || gv.equals(GlowVisibility.NONE) || //Player can't see the glow & has it enabled
								glowingTarget.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && !glowingTarget.getGlowTargets().contains(eglowPlayer.getPlayer()) || //API method check
								gv.equals(GlowVisibility.OWN) && !glowingTarget.getPlayer().equals(eglowPlayer.getPlayer())) { //if glow is set to personal
								super.write(context, packet, channelPromise);
								return;
							}

							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(EGlow.getInstance(), entityID, NMSHook.setGlowFlag(glowingTarget.getEntity(), true)/*eglowEntity*/);
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

	public static void uninject(IEGlowPlayer eglowPlayer) {
		if (glowingEntities.containsValue(eglowPlayer))
			glowingEntities.remove(eglowPlayer.getPlayer().getEntityId());
		
		try {
			Channel channel = (Channel) NMSHook.getChannel(eglowPlayer.getPlayer());
			if (channel.pipeline().names().contains(DECODER_NAME)) channel.pipeline().remove(DECODER_NAME);
		} catch (NoSuchElementException e) {
			//for whatever reason this rarely throws
            //java.util.NoSuchElementException: eGlowReader
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (!blockPackets || !EGlowMainConfig.OptionFeaturePacketBlocker())
			return;
		
		String teamName = NMSHook.nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam).toString();
		Collection<String> players = (Collection<String>) NMSHook.nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
		if (players == null) return;
		Collection<String> newList = new ArrayList<>();
		
		for (String entry : players) {
			IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(entry);
			
			if (ePlayer == null) {
				newList.add(entry);
				continue;
			}
			
			if (!ePlayer.getTeamName().equals(teamName))
				continue;

			newList.add(entry);
		}	
		
		NMSHook.nms.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
	}

	private static boolean blockPackets = true;
	
	public static boolean blockPackets() {
		return blockPackets;
	}

	public static void setBlockPackets(boolean blockPacketss) {
		blockPackets = blockPacketss;
	}
}
