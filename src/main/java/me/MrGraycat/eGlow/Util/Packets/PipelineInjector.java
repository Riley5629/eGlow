package me.MrGraycat.eGlow.Util.Packets;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.EnumUtil;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOut;
import me.MrGraycat.eGlow.Util.Packets.OutGoing.PacketPlayOutEntityMetadata;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

import java.util.*;

public class PipelineInjector {
	private static final String DECODER_NAME = "eGlowReader";
	public static HashMap<Integer, IEGlowPlayer> glowingEntities = new HashMap<>();

	public static void inject(IEGlowPlayer eglowPlayer) {
		Channel channel = (Channel) NMSHook.getChannel(eglowPlayer.getPlayer());

		if (channel == null || !channel.pipeline().names().contains("packet_handler"))
			return;

		if (channel.pipeline().names().contains(DECODER_NAME))
			channel.pipeline().remove(DECODER_NAME);

		try {
			channel.pipeline().addBefore("packet_handler", DECODER_NAME, new ChannelDuplexHandler() {
				@Override
				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					super.channelRead(context, packet);
				}

				@Override
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					if (NMSHook.nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						if (DebugUtil.TABInstalled()) {
							if (EGlow.getInstance().getTABAddon() == null) {
								super.write(context, packet, channelPromise);
								return;
							} else {
								if (!EGlow.getInstance().getTABAddon().isVersionSupported() || EGlow.getInstance().getTABAddon().blockEGlowPackets()) {
									super.write(context, packet, channelPromise);
									return;
								}
							}
						} else if (DebugUtil.isTABBridgeInstalled()) {
							super.write(context, packet, channelPromise);
							return;
						}

						modifyPlayers(packet);
						super.write(context, packet, channelPromise);
						return;
					}

					if (NMSHook.nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
						Integer entityID;

						if (NMSHook.nms.isIs1_19_3OrAbove()) {
							entityID = (Integer) PacketPlayOut.getField(packet, "b");
						} else {
							entityID = (Integer) PacketPlayOut.getField(packet, "a");
						}

						if (glowingEntities.containsKey(entityID)) {
							PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;
							IEGlowPlayer glowingTarget = glowingEntities.get(entityID);

							if (glowingTarget == null) {
								glowingEntities.remove(entityID);
								super.write(context, channel, channelPromise);
								return;
							}

							GlowVisibility gv = eglowPlayer.getGlowVisibility();

							if (gv.equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
								super.write(context, packet, channelPromise);
								return;
							}

							if (glowingTarget.getGlowTargetMode().equals(EnumUtil.GlowTargetMode.CUSTOM) && !glowingTarget.getGlowTargets().contains(eglowPlayer.getPlayer())) {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityID, NMSHook.setGlowFlag(glowingTarget.getEntity(), false));
								super.write(context, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()), channelPromise);
								return;
							}

							if (gv.equals(GlowVisibility.NONE) || //Player can't see the glow or set to none
									(gv.equals(GlowVisibility.OTHER) && glowingTarget.getPlayer().equals(eglowPlayer.getPlayer())) || //if glow is set to other
									(gv.equals(GlowVisibility.OWN) && !glowingTarget.getPlayer().equals(eglowPlayer.getPlayer()))) { //if glow is set to own
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityID, NMSHook.setGlowFlag(glowingTarget.getEntity(), false));
								super.write(context, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()), channelPromise);
								return;
							}

							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityID, NMSHook.setGlowFlag(glowingTarget.getEntity(), true));
							super.write(context, packetPlayOutEntityMetadata.toNMS(eglowPlayer.getVersion()), channelPromise);
							return;
						}
					}

					super.write(context, packet, channelPromise);
				}
			});
		} catch (NoSuchElementException e) {
			//for whatever reason this rarely throws
			//java.util.NoSuchElementException: eGlowReader
		}
	}

	public static void uninject(IEGlowPlayer eglowPlayer) {
		if (glowingEntities.containsValue(eglowPlayer))
			glowingEntities.remove(eglowPlayer.getPlayer().getEntityId());

		try {
			Channel channel = (Channel) NMSHook.getChannel(eglowPlayer.getPlayer());
			if (Objects.requireNonNull(channel).pipeline().names().contains(DECODER_NAME))
				channel.pipeline().remove(DECODER_NAME);
		} catch (NoSuchElementException e) {
			//for whatever reason this rarely throws
			//java.util.NoSuchElementException: eGlowReader
		}

	}

	@SuppressWarnings("unchecked")
	private static void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (!blockPackets() || !MainConfig.ADVANCED_PACKETS_SMART_BLOCKER.getBoolean())
			return;

		int action = NMSHook.nms.PacketPlayOutScoreboardTeam_ACTION.getInt(packetPlayOutScoreboardTeam);
		if (action == 1 || action == 2 || action == 4) return;
		String teamName = NMSHook.nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam).toString();
		Collection<String> players = (Collection<String>) NMSHook.nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
		if (players == null) return;
		Collection<String> newList = new ArrayList<>();

		try {
			List<String> list = new ArrayList<>(players);

			for (String entity : list) {
				IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(entity);

				if (ePlayer == null) {
					newList.add(entity);
					continue;
				}

				if (!ePlayer.getTeamName().equals(teamName)) {
					continue;
				}

				newList.add(entity);
			}
		} catch (ConcurrentModificationException e) {
			ChatUtil.reportError(e);
		}

		NMSHook.nms.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
	}

	private static boolean blockPackets = true;

	public static boolean blockPackets() {
		return blockPackets;
	}

	public static void setBlockPackets(boolean blockPacketsStatus) {
		blockPackets = blockPacketsStatus;
	}
}
