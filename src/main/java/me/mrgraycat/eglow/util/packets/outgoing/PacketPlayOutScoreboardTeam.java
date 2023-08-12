package me.mrgraycat.eglow.util.packets.outgoing;

import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.NMSStorage;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent;

import java.util.Collection;
import java.util.Collections;

public class PacketPlayOutScoreboardTeam extends PacketPlayOut {
	public String name;
	public String playerPrefix;
	public String playerSuffix;
	public String nametagVisibility;
	public String collisionRule;
	public EnumChatFormat color;
	public Collection<String> players = Collections.emptyList();
	public int method;
	public int options;

	private PacketPlayOutScoreboardTeam(int method, String name) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		this.method = method;
		this.name = name;
	}

	//CREATE
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
		this(0, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.players = players;
		this.options = options;
	}

	//REMOVE
	public PacketPlayOutScoreboardTeam(String team) {
		this(1, team);
	}

	//UPDATE
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, int options) {
		this(2, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.options = options;
	}

	public PacketPlayOutScoreboardTeam setColor(EnumChatFormat color) {
		this.color = color;
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		NMSStorage nms = NMSHook.nms;

		String prefix = playerPrefix;
		String suffix = playerSuffix;
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}

		Object team = nms.newScoreboardTeam.newInstance(nms.newScoreboard.newInstance(), name);
		((Collection) nms.ScoreboardTeam_getPlayerNameSet.invoke(team, new Object[0])).addAll(players);

		if (nms.minorVersion >= 13) {
			if (prefix != null && prefix.length() > 0)
				nms.ScoreboardTeam_setPrefix.invoke(team, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(prefix).toString(clientVersion)));
			if (suffix != null && suffix.length() > 0)
				nms.ScoreboardTeam_setSuffix.invoke(team, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(suffix).toString(clientVersion)));
			EnumChatFormat format = color != null ? color : EnumChatFormat.lastColorsOf(prefix);
			nms.ScoreboardTeam_setColor.invoke(team, ((Object[]) nms.EnumChatFormat.getMethod("values").invoke(null, new Object[0]))[format.ordinal()]);
		} else {
			if (prefix != null)
				nms.ScoreboardTeam_setPrefix.invoke(team, prefix);
			if (suffix != null)
				nms.ScoreboardTeam_setSuffix.invoke(team, suffix);
		}

		if (nms.EnumNameTagVisibility != null && nametagVisibility != null)
			nms.ScoreboardTeam_setNameTagVisibility.invoke(team, nametagVisibility.equals("always") ? ((Object[]) nms.EnumNameTagVisibility.getMethod("values").invoke(null))[0] : ((Object[]) nms.EnumNameTagVisibility.getMethod("values").invoke(null))[1]);
		if (nms.EnumTeamPush != null && collisionRule != null)
			nms.ScoreboardTeam_setCollisionRule.invoke(team, collisionRule.equals("always") ? ((Object[]) nms.EnumTeamPush.getMethod("values").invoke(null))[0] : ((Object[]) nms.EnumTeamPush.getMethod("values").invoke(null))[1]);
		if (nms.minorVersion >= 17) {
			switch (method) {
				case 0:
					return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, true);
				case 1:
					return nms.PacketPlayOutScoreboardTeam_of.invoke(null, team);
				case 2:
					return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, false);
				case 3:
					return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, players.toArray(new String[0])[0], ((Object[]) nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[0]);
				case 4:
					return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, players.toArray(new String[0])[0], ((Object[]) nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[1]);
			}
			throw new IllegalArgumentException("Invalid action: " + method);
		}
		return nms.newPacketPlayOutScoreboardTeam.newInstance(team, method);
	}

	@Override
	public String toString() {
		return "PacketPlayOutScoreboardTeam{name=" + name + ",playerPrefix=" + playerPrefix + ",playerSuffix=" + playerSuffix +
				",nametagVisibility=" + nametagVisibility + ",collisionRule=" + collisionRule + ",color=" + color +
				",players=" + players + ",method=" + method + ",options=" + options + "}";
	}
}