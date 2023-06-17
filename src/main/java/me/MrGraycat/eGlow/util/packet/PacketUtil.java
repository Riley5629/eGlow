package me.MrGraycat.eGlow.util.packet;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.addon.tab.TabAddon;
import me.MrGraycat.eGlow.addon.vault.VaultAddon;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.util.Common;
import me.MrGraycat.eGlow.util.packet.chat.EnumChatFormat;
import me.MrGraycat.eGlow.util.packet.chat.IChatBaseComponent;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.packet.outbound.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@UtilityClass
public class PacketUtil {

	private final Predicate<IEGlowPlayer> MINOR_VERSION_ABOVE_OR_EQUAL_TO_8 = (glowPlayer) ->
			glowPlayer.getVersion().getMinorVersion() >= 8;

	private boolean sendPackets = true;

	public void updatePlayer(IEGlowPlayer ePlayer) {
		ProtocolVersion pVersion = ePlayer.getVersion();

		VaultAddon vaultAddon = EGlow.getInstance().getVaultAddon();
		TabAddon tabAddon = EGlow.getInstance().getTabAddon();

		boolean vaultEnabled = vaultAddon != null;
		boolean tabEnabled = tabAddon != null;

		DataManager.getGlowPlayers().stream()
				.filter(glowPlayer -> glowPlayer.getEntity() instanceof Player || glowPlayer.equals(ePlayer))
				.forEach(glowPlayer -> {
					if (sendPackets && EGlowMainConfig.MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean() && tabEnabled && !tabAddon.blockEGlowPackets()) {
						sendPacketNms(ePlayer, new PacketPlayOutScoreboardTeam(glowPlayer.getTeamName(),
								(vaultEnabled ? vaultAddon.getPlayerTagPrefix(glowPlayer) : "")
										+ glowPlayer.getActiveColor(),
								vaultEnabled ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(glowPlayer) : "",

								EGlowMainConfig.MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never",
								EGlowMainConfig.MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never",

								Sets.newHashSet(glowPlayer.getDisplayName()), 21)
								.setColor(EnumChatFormat.valueOf(glowPlayer.getActiveColor().name()))
						);
					}

					if (!ePlayer.getGlowVisibility().equals(Common.GlowVisibility.UNSUPPORTEDCLIENT)) {
						if (!glowPlayer.getGlowStatus() && !glowPlayer.getFakeGlowStatus()) {
							return;
						}

						Object glowingEntity = glowPlayer.getEntity();
						int glowingEntityID = glowPlayer.getPlayer().getEntityId();

						PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

						try {
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID,
									NMSHook.setGlowFlag(glowingEntity, true));
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						boolean targetAll = glowPlayer.getGlowTarget().equals(Common.GlowTargetMode.ALL);
						boolean custom = glowPlayer.getGlowTarget().equals(Common.GlowTargetMode.CUSTOM) &&
								glowPlayer.getCustomTargetList().contains(ePlayer.getPlayer());
						boolean glowForAllOrOther = ePlayer.getGlowVisibility().equals(Common.GlowVisibility.ALL) ||
								ePlayer.getGlowVisibility().equals(Common.GlowVisibility.OTHER);

						if (targetAll || custom) {
							if (!glowForAllOrOther) {
								return;
							}

							sendPacketNms(ePlayer, Objects.requireNonNull(packetPlayOutEntityMetadata));
						}
					}
				});
	}

	public synchronized void scoreboardPacket(IEGlowPlayer to, boolean join) {
		if (!sendPackets || !EGlowMainConfig.MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
			return;
		}

		if (to == null || EGlow.getInstance() == null) {
			return;
		}

		TabAddon tabAddon = EGlow.getInstance().getTabAddon();
		VaultAddon vaultAddon = EGlow.getInstance().getVaultAddon();

		if (tabAddon == null || tabAddon.blockEGlowPackets()) {
			return;
		}

		if (!join) {
			DataManager.getGlowPlayers().stream()
					.filter(MINOR_VERSION_ABOVE_OR_EQUAL_TO_8)
					.forEach(glowPlayer -> {
						sendPacketNms(glowPlayer, new PacketPlayOutScoreboardTeam(to.getTeamName()));
					});
			return;
		}

		if (EGlowMainConfig.MainConfig.ADVANCED_TEAMS_REMOVE_ON_JOIN.getBoolean()) {
			DataManager.getGlowPlayers().stream().filter(MINOR_VERSION_ABOVE_OR_EQUAL_TO_8)
					.forEach(glowPlayer -> {
						try {
							NMSHook.sendPacket(glowPlayer, new PacketPlayOutScoreboardTeam(to.getTeamName()).toNMS(to.getVersion()));
						} catch (Exception exc) {
							exc.printStackTrace();
						}
					});
		}

		DataManager.getGlowPlayers().stream()
				.filter(MINOR_VERSION_ABOVE_OR_EQUAL_TO_8)
				.forEach(glowPlayer -> {
					String playerPrefix = vaultAddon != null ?
							vaultAddon.getPlayerTagPrefix(to) + to.getActiveColor() : String.valueOf(to.getActiveColor());
					String playerSuffix = vaultAddon != null ?
							EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(to) : "";
					String nameTagVisibility = EGlowMainConfig.MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never";
					String entityCollision = (EGlowMainConfig.MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never");

					sendPacketNms(glowPlayer, new PacketPlayOutScoreboardTeam(
								to.getTeamName(), playerPrefix, playerSuffix, nameTagVisibility, entityCollision,
								Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET));
				});
	}

	public void updateScoreboardTeam(IEGlowPlayer entity, String teamName, String prefix, String suffix, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (EGlowMainConfig.MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (EGlowMainConfig.MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), 21).setColor(color);

		if (!sendPackets || !EGlowMainConfig.MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
			return;
		}

		if (EGlow.getInstance() == null || entity == null) {
			return;
		}

		TabAddon tabAddon = EGlow.getInstance().getTabAddon();

		if (tabAddon != null && tabAddon.blockEGlowPackets()) {
			return;
		}

		DataManager.getGlowPlayers().forEach(glowPlayer -> {
			sendPacketNms(glowPlayer, packet);
		});
	}

	public void updateGlowing(IEGlowPlayer entity, boolean status) {
		if (entity == null || EGlow.getInstance() == null) {
			return;
		}

		Object glowingEntity = entity.getEntity();
		int glowingEntityId = entity.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityId, NMSHook.setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Necessary to be used inside the context of a lambda
		PacketPlayOutEntityMetadata packet = packetPlayOutEntityMetadata;

		if (!status) {
			if (PipelineInjector.glowingEntities.containsKey(glowingEntityId)) {
				PipelineInjector.glowingEntities.remove(glowingEntityId, entity);
			}

			DataManager.getGlowPlayers().forEach(player -> {
				sendPacketNms(player, packet);
			});
			return;
		}
		if (!PipelineInjector.glowingEntities.containsKey(glowingEntityId)) {
			PipelineInjector.glowingEntities.put(glowingEntityId, entity);
		}

		(entity.getGlowTarget().equals(Common.GlowTargetMode.ALL) ? Bukkit.getOnlinePlayers() : entity.getCustomTargetList()).stream()
				.map(DataManager::getEGlowPlayer)
				.forEach(glowPlayer -> {
					switch (entity.getGlowVisibility()) {
						case ALL:
							sendPacketNms(glowPlayer, Objects.requireNonNull(packet));
							break;
						case OTHER:
							if (entity.getPlayer().equals(glowPlayer.getPlayer())) {
								break;
							}

							sendPacketNms(glowPlayer, Objects.requireNonNull(packet));
							break;
						case OWN:
							if (!entity.getPlayer().equals(glowPlayer.getPlayer())) {
								break;
							}

							sendPacketNms(glowPlayer, packet);
							break;
						default:
							break;
					}
				});
	}

	//check glow visibility of main then continue
	public void glowTargetChange(IEGlowPlayer main, Player change, boolean type) {
		IEGlowPlayer target = DataManager.getEGlowPlayer(change);

		if (target == null) {
			return;
		}

		Object glowingEntity = main.getEntity();
		int glowingEntityID = main.getPlayer().getEntityId();

		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		if (type && !(main.getGlowStatus() || main.getFakeGlowStatus())) {
			type = false;
		}

		switch (target.getGlowVisibility()) {
			case ALL:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, type));
				break;
			case OTHER:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, (!change.equals(main.getPlayer()) && type)));
				break;
			case OWN:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, (change.equals(main.getPlayer()) && type)));
				break;
			case NONE:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, false));
				break;
			case UNSUPPORTEDCLIENT:
				return;
		}

		sendPacketNms(target, packetPlayOutEntityMetadata);
	}

	public void updateGlowTarget(IEGlowPlayer ePlayer) {
		Collection<IEGlowPlayer> players = DataManager.getGlowPlayers();
		List<Player> customTargets = ePlayer.getCustomTargetList();

		Object glowingEntity = ePlayer.getEntity();
		int glowingEntityID = ePlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID,
					NMSHook.setGlowFlag(glowingEntity, ePlayer.getGlowTarget().equals(Common.GlowTargetMode.ALL) && ePlayer.getGlowStatus()));
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Player player = ePlayer.getPlayer();
		PacketPlayOutEntityMetadata packet = packetPlayOutEntityMetadata;

		switch (ePlayer.getGlowTarget()) {
			case ALL:
				players.stream().filter(glowPlayer -> glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.ALL) ||
						(glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.OTHER) && !glowPlayer.getPlayer().equals(player) ||
								(glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.OWN) && (glowPlayer.getPlayer().equals(player)))))
						.forEach(glowPlayer -> sendPacketNms(glowPlayer, packet));
				break;
			//TODO keep in mind glow visibility
			case CUSTOM:
				players.stream()
						.filter(glowPlayer -> !customTargets.contains(glowPlayer.getPlayer()))
						.filter(glowPlayer -> glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.ALL) ||
								(glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.OTHER) && !glowPlayer.getPlayer().equals(ePlayer.getPlayer()))
								|| (glowPlayer.getPlayer().equals(ePlayer.getPlayer()) && glowPlayer.getGlowVisibility().equals(Common.GlowVisibility.OWN)))
						.forEach(glowPlayer -> {
							sendPacketNms(glowPlayer, packet);
						});
				break;
		}
	}

	public void forceUpdateGlow(IEGlowPlayer ePlayer) {
		(ePlayer.getGlowTarget().equals(Common.GlowTargetMode.ALL) ? Bukkit.getOnlinePlayers() : ePlayer.getCustomTargetList())
				.forEach(player -> {
					IEGlowPlayer ep = DataManager.getEGlowPlayer(player);
					AtomicReference<PacketPlayOutEntityMetadata> packetPlayOutEntityMetadata = new AtomicReference<>();

					boolean isGlowing = ep.getGlowStatus() || ep.getFakeGlowStatus();

					switch (ePlayer.getGlowVisibility()) {
						case ALL:
							packetPlayOutEntityMetadata.set(new PacketPlayOutEntityMetadata(
									player.getEntityId(), NMSHook.setGlowFlag(player, isGlowing)));
							break;
						case OTHER:
							packetPlayOutEntityMetadata.set(new PacketPlayOutEntityMetadata(
									player.getEntityId(), NMSHook.setGlowFlag(player, (!player.equals(ePlayer.getPlayer()) && isGlowing))));
							break;
						case OWN:
							packetPlayOutEntityMetadata.set(new PacketPlayOutEntityMetadata(
									player.getEntityId(), NMSHook.setGlowFlag(player, (player.equals(ePlayer.getPlayer()) && isGlowing))));
							break;
						case NONE:
							packetPlayOutEntityMetadata.set(new PacketPlayOutEntityMetadata(
									player.getEntityId(), NMSHook.setGlowFlag(player, false)));
							break;
						case UNSUPPORTEDCLIENT:
							return;
					}

					sendPacketNms(ep, packetPlayOutEntityMetadata.get());
				});
	}

	public static void sendActionbar(IEGlowPlayer ePlayer, String text) {
		if (text.isEmpty()) {
			return;
		}

		IChatBaseComponent formattedText = IChatBaseComponent.optimizedComponent(text);

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
			PacketPlayOutActionBar packetPlayOutActionBar = new PacketPlayOutActionBar(formattedText);

			sendPacketNms(ePlayer, packetPlayOutActionBar);
			return;
		}

		PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(formattedText, PacketPlayOutChat.ChatMessageType.GAME_INFO);

		sendPacketNms(ePlayer, packetPlayOutChat);
	}

	private void sendPacket(Player player, Object packet) {
		try {
			NMSHook.sendPacket(player, packet);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void sendPacket(IEGlowPlayer player, Object packet) {
		try {
			NMSHook.sendPacket(player, packet);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void sendPacketNms(IEGlowPlayer player, PacketPlayOut packet) {
		try {
			sendPacket(player, packet.toNMS(player.getVersion()));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void setSendTeamPackets(boolean status) {
		sendPackets = status;
	}
}