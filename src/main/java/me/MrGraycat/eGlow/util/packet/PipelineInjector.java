package me.MrGraycat.eGlow.util.packet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.util.Common;
import me.MrGraycat.eGlow.util.ServerUtil;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.dependency.Dependency;
import me.MrGraycat.eGlow.util.packet.outbound.PacketPlayOut;
import me.MrGraycat.eGlow.util.packet.outbound.PacketPlayOutEntityMetadata;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;

import java.util.*;

@UtilityClass
public class PipelineInjector {

	private final String DECODER_NAME = "eGlowReader";
	public final HashMap<Integer, IEGlowPlayer> glowingEntities = new HashMap<>();

	@Setter
	private static boolean blockPackets = true;

	public static void inject(IEGlowPlayer glowPlayer) {
		Channel channel = (Channel) NMSHook.getChannel(glowPlayer.getPlayer());

		if (!Objects.requireNonNull(channel).pipeline().names().contains("packet_handler"))
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
					if (NMSHook.nms.packetPlayOutScoreboardTeamClas.isInstance(packet)) {
						if (Dependency.TAB.isLoaded()) {
							if (EGlow.getInstance().getTabAddon() == null) {
								super.write(context, packet, channelPromise);
								return;
							} else {
								if (!EGlow.getInstance().getTabAddon().isVersionSupported() || EGlow.getInstance().getTabAddon().blockEGlowPackets()) {
									super.write(context, packet, channelPromise);
									return;
								}
							}
						} else if (ServerUtil.isBridgeEnabled()) {
							super.write(context, packet, channelPromise);
							return;
						}

						modifyPlayers(packet);
						super.write(context, packet, channelPromise);
						return;
					}

					if (NMSHook.nms.packetPlayOutEntityMetadataClass.isInstance(packet)) {
						Integer entityId;

						if (NMSHook.nms.isIs1_19_3OrAbove()) {
							entityId = (Integer) PacketPlayOut.getField(packet, "b");
						} else {
							entityId = (Integer) PacketPlayOut.getField(packet, "a");
						}

						if (glowingEntities.containsKey(entityId)) {
							PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;
							IEGlowPlayer glowingTarget = glowingEntities.get(entityId);

							if (glowingTarget == null) {
								glowingEntities.remove(entityId);
								super.write(context, channel, channelPromise);
								return;
							}

							Common.GlowVisibility gv = glowPlayer.getGlowVisibility();

							if (gv.equals(Common.GlowVisibility.UNSUPPORTEDCLIENT)) {
								super.write(context, packet, channelPromise);
								return;
							}

							if (glowingTarget.getGlowTarget().equals(Common.GlowTargetMode.CUSTOM) && !glowingTarget.getCustomTargetList().contains(glowPlayer.getPlayer())) {
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityId, NMSHook.setGlowFlag(glowingTarget.getEntity(), false));
								super.write(context, packetPlayOutEntityMetadata.toNMS(glowPlayer.getVersion()), channelPromise);
								return;
							}

							if (gv.equals(Common.GlowVisibility.NONE) || //Player can't see the glow or set to none
									(gv.equals(Common.GlowVisibility.OTHER) && glowingTarget.getPlayer().equals(glowPlayer.getPlayer())) || //if glow is set to other
									(gv.equals(Common.GlowVisibility.OWN) && !glowingTarget.getPlayer().equals(glowPlayer.getPlayer()))) { //if glow is set to own
								packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityId, NMSHook.setGlowFlag(glowingTarget.getEntity(), false));
								super.write(context, packetPlayOutEntityMetadata.toNMS(glowPlayer.getVersion()), channelPromise);
								return;
							}

							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityId, NMSHook.setGlowFlag(glowingTarget.getEntity(), true));
							super.write(context, packetPlayOutEntityMetadata.toNMS(glowPlayer.getVersion()), channelPromise);
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
		if (!blockPackets || !EGlowMainConfig.MainConfig.ADVANCED_PACKETS_SMART_BLOCKER.getBoolean())
			return;

		int action = NMSHook.nms.packetPlayOutScoreboardTeamActionField.getInt(packetPlayOutScoreboardTeam);

		if (action > 0 && action < 5) {
			return;
		}

		String teamName = NMSHook.nms.packetPlayOutScoreboardTeamName.get(packetPlayOutScoreboardTeam).toString();
		Collection<String> players = (Collection<String>) NMSHook.nms.packetPlayOutScoreboardTeamPlayers.get(packetPlayOutScoreboardTeam);
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

		NMSHook.nms.packetPlayOutScoreboardTeamPlayers.set(packetPlayOutScoreboardTeam, newList);
	}

	public static boolean blockPackets() {
		return blockPackets;
	}
}
