package me.mrgraycat.eglow.util.packets;

import com.google.common.collect.Sets;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowTargetMode;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutActionBar;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutChat;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutEntityMetadata;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PacketUtil {
	private static boolean sendPackets = true;

	public static void handlePlayerJoin(EGlowPlayer eGlowPlayer) {
		PipelineInjector.inject(eGlowPlayer);
		scoreboardPacket(eGlowPlayer, true);
		updatePlayer(eGlowPlayer);
	}

	public static void handlePlayerQuit(EGlowPlayer eGlowPlayer) {
		PacketUtil.scoreboardPacket(eGlowPlayer, false);
		PipelineInjector.uninject(eGlowPlayer);
	}

	private static void updatePlayer(EGlowPlayer eGlowPlayer) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ProtocolVersion eGlowPlayerVersion = eGlowPlayer.getVersion();

				for (EGlowPlayer eGlowTarget : DataManager.getEGlowPlayers()) {
					if (!(eGlowTarget.getEntity() instanceof Player) || eGlowTarget.equals(eGlowPlayer))
						continue;

					if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
						if (EGlow.getInstance().getTabAddon() == null || !EGlow.getInstance().getTabAddon().blockEGlowPackets()) {
							try {
								NMSHook.sendPacket(eGlowPlayer, new PacketPlayOutScoreboardTeam(eGlowTarget.getTeamName(), ((EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(eGlowTarget) : "") + eGlowTarget.getActiveColor(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(eGlowTarget) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(eGlowTarget.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(eGlowTarget.getActiveColor().name())).toNMS(eGlowPlayerVersion));
							} catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}

					if (!eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
						if (!eGlowTarget.getGlowStatus() && !eGlowTarget.isFakeGlowStatus())
							return;

						Object glowingEntity = eGlowTarget.getEntity();
						int glowingEntityID = eGlowTarget.getPlayer().getEntityId();
						PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

						try {
							packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, true));
						} catch (Exception exception) {
							exception.printStackTrace();
						}

						if (eGlowTarget.getGlowTargetMode().equals(GlowTargetMode.ALL) || eGlowTarget.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && eGlowTarget.getGlowTargets().contains(eGlowPlayer.getPlayer())) {
							if (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.ALL) || eGlowPlayer.getGlowVisibility().equals(GlowVisibility.OTHER)) {
								try {
									NMSHook.sendPacket(eGlowPlayer, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowPlayer.getVersion()));
								} catch (Exception exception) {
									exception.printStackTrace();
								}
							}
						}
					}
				}
			}
		}.runTaskLater(EGlow.getInstance(), 2L);
	}

	public static synchronized void scoreboardPacket(EGlowPlayer eGlowPlayer, boolean join) {
		try {
			if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
				if (eGlowPlayer == null || EGlow.getInstance() == null)
					return;

				if (join) {
					if (EGlow.getInstance().getTabAddon() == null || !EGlow.getInstance().getTabAddon().blockEGlowPackets()) {
						if (MainConfig.ADVANCED_TEAMS_REMOVE_ON_JOIN.getBoolean()) {
							for (EGlowPlayer players : DataManager.getEGlowPlayers()) {
								if (players.getVersion().getMinorVersion() >= 8)
									NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(eGlowPlayer.getTeamName()).toNMS(eGlowPlayer.getVersion()));
							}
						}

						for (EGlowPlayer eGlowTarget : DataManager.getEGlowPlayers()) {
							if (eGlowTarget.getVersion().getMinorVersion() >= 8)
								NMSHook.sendPacket(eGlowTarget, new PacketPlayOutScoreboardTeam(eGlowPlayer.getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(eGlowPlayer) + eGlowPlayer.getActiveColor() : String.valueOf(eGlowPlayer.getActiveColor()), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(eGlowPlayer) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(eGlowPlayer.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(eGlowPlayer.getVersion()));
						}
					}
				} else {
					if (EGlow.getInstance().getTabAddon() == null || !EGlow.getInstance().getTabAddon().blockEGlowPackets()) {
						for (EGlowPlayer eGlowTarget : DataManager.getEGlowPlayers()) {
							if (eGlowTarget.getVersion().getMinorVersion() >= 8)
								NMSHook.sendPacket(eGlowTarget, new PacketPlayOutScoreboardTeam(eGlowPlayer.getTeamName()).toNMS(eGlowPlayer.getVersion()));
						}
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void updateScoreboardTeam(EGlowPlayer eGlowPlayer, String teamName, String prefix, String suffix, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), 21).setColor(color);

		if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
			if (eGlowPlayer == null && EGlow.getInstance() == null)
				return;

			if (EGlow.getInstance().getTabAddon() != null && EGlow.getInstance().getTabAddon().blockEGlowPackets())
				return;

			for (EGlowPlayer eGlowTarget : DataManager.getEGlowPlayers()) {
				try {
					NMSHook.sendPacket(eGlowTarget.getPlayer(), packet.toNMS(eGlowTarget.getVersion()));
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public static void updateGlowing(EGlowPlayer eGlowPlayer, boolean status) {
		if (eGlowPlayer == null || EGlow.getInstance() == null)
			return;

		Object glowingEntity = eGlowPlayer.getEntity();
		int glowingEntityID = eGlowPlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (status) {
			if (!PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.put(glowingEntityID, eGlowPlayer);

			for (Player player : (eGlowPlayer.getGlowTargetMode().equals(GlowTargetMode.ALL)) ? Bukkit.getOnlinePlayers() : eGlowPlayer.getGlowTargets()) {
				EGlowPlayer eGlowTarget = DataManager.getEGlowPlayer(player);

				if (eGlowTarget == null)
					continue;

				switch (eGlowPlayer.getGlowVisibility()) {
					case ALL:
						try {
							NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
						} catch (Exception exception) {
							exception.printStackTrace();
						}
						break;
					case OTHER:
						if (!eGlowPlayer.getPlayer().equals(player))
							try {
								NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
							} catch (Exception exception) {
								exception.printStackTrace();
							}
						break;
					case OWN:
						if (eGlowPlayer.getPlayer().equals(player))
							try {
								NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
							} catch (Exception exception) {
								exception.printStackTrace();
							}
						break;
					default:
						break;
				}
			}
		} else {
			if (PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.remove(glowingEntityID, eGlowPlayer);

			for (Player player : Bukkit.getOnlinePlayers()) {
				EGlowPlayer eGlowTarget = DataManager.getEGlowPlayer(player);
				if (eGlowTarget != null)
					try {
						NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
					} catch (Exception exception) {
						exception.printStackTrace();
					}
			}
		}
	}

	//check glow visibility of main then continue
	public static void glowTargetChange(EGlowPlayer eGlowPlayer, Player change, boolean type) {
		EGlowPlayer eGlowTarget = DataManager.getEGlowPlayer(change);

		if (eGlowTarget == null)
			return;

		Object glowingEntity = eGlowPlayer.getEntity();
		int glowingEntityID = eGlowPlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		if (type && !eGlowPlayer.isGlowing())
			type = false;

		switch (eGlowTarget.getGlowVisibility()) {
			case ALL:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, type));
				break;
			case OTHER:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, (!change.equals(eGlowPlayer.getPlayer()) && type)));
				break;
			case OWN:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, (change.equals(eGlowPlayer.getPlayer()) && type)));
				break;
			case NONE:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, false));
				break;
			case UNSUPPORTEDCLIENT:
				return;
		}

		try {
			NMSHook.sendPacket(eGlowTarget, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void updateGlowTarget(EGlowPlayer eGlowPlayer) {
		Collection<EGlowPlayer> players = DataManager.getEGlowPlayers();
		List<Player> customTargets = eGlowPlayer.getGlowTargets();

		Object glowingEntity = eGlowPlayer.getEntity();
		int glowingEntityID = eGlowPlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, eGlowPlayer.getGlowTargetMode().equals(GlowTargetMode.ALL) && eGlowPlayer.getGlowStatus()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		switch (eGlowPlayer.getGlowTargetMode()) {
			case ALL:
				for (EGlowPlayer eGlowTarget : players) {
					if (eGlowTarget.getGlowVisibility().equals(GlowVisibility.ALL) || (eGlowTarget.getGlowVisibility().equals(GlowVisibility.OTHER) && !eGlowTarget.getPlayer().equals(eGlowPlayer.getPlayer())) || eGlowTarget.getGlowVisibility().equals(GlowVisibility.OWN) && (eGlowTarget.getPlayer().equals(eGlowPlayer.getPlayer()))) {
						try {
							NMSHook.sendPacket(eGlowTarget, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
						} catch (Exception exception) {
							exception.printStackTrace();
						}
					}
				}
				break;
			case CUSTOM:
				for (EGlowPlayer eGlowTarget : players) {
					if (!customTargets.contains(eGlowTarget.getPlayer())) {
						if (eGlowTarget.getGlowVisibility().equals(GlowVisibility.ALL) || (eGlowTarget.getGlowVisibility().equals(GlowVisibility.OTHER) && !eGlowTarget.getPlayer().equals(eGlowPlayer.getPlayer())) || (eGlowTarget.getPlayer().equals(eGlowPlayer.getPlayer()) && eGlowTarget.getGlowVisibility().equals(GlowVisibility.OWN))) {
							try {
								NMSHook.sendPacket(eGlowTarget, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eGlowTarget.getVersion()));
							} catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
				}
				break;
		}
	}

	public static void forceUpdateGlow(EGlowPlayer eGlowPlayer) {
		for (Player player : (eGlowPlayer.getGlowTargetMode().equals(GlowTargetMode.ALL)) ? Bukkit.getOnlinePlayers() : eGlowPlayer.getGlowTargets()) {
			EGlowPlayer eGlowTarget = DataManager.getEGlowPlayer(player);

			if (eGlowTarget == null) {
				continue;
			}

			boolean isGlowing = eGlowTarget.getGlowStatus() || eGlowTarget.isFakeGlowStatus();
			PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

			switch (eGlowPlayer.getGlowVisibility()) {
				case ALL:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(player.getEntityId(), NMSHook.setGlowFlag(player, isGlowing));
					break;
				case OTHER:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(player.getEntityId(), NMSHook.setGlowFlag(player, (!player.equals(eGlowPlayer.getPlayer()) && isGlowing)));
					break;
				case OWN:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(player.getEntityId(), NMSHook.setGlowFlag(player, (player.equals(eGlowPlayer.getPlayer()) && isGlowing)));
					break;
				case NONE:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(player.getEntityId(), NMSHook.setGlowFlag(player, false));
					break;
				case UNSUPPORTEDCLIENT:
					return;
			}

			try {
				NMSHook.sendPacket(eGlowPlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(eGlowPlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendActionbar(EGlowPlayer eGlowPlayer, String text) {
		if (text.isEmpty())
			return;

		IChatBaseComponent formattedText = IChatBaseComponent.optimizedComponent(text);

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
			PacketPlayOutActionBar packetPlayOutActionBar = new PacketPlayOutActionBar(formattedText);

			try {
				NMSHook.sendPacket(eGlowPlayer.getPlayer(), packetPlayOutActionBar.toNMS(eGlowPlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(formattedText, PacketPlayOutChat.ChatMessageType.GAME_INFO);

			try {
				NMSHook.sendPacket(eGlowPlayer.getPlayer(), packetPlayOutChat.toNMS(eGlowPlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setSendTeamPackets(boolean status) {
		sendPackets = status;
	}
}