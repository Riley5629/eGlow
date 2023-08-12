package me.mrgraycat.eglow.util.packets.outgoing;

import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.NMSStorage;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent;

import java.util.UUID;

public class PacketPlayOutChat extends PacketPlayOut {
	/**
	 * Message to be sent
	 */
	private final IChatBaseComponent message;

	/**
	 * Message position
	 */
	private final ChatMessageType type;

	/**
	 * Constructs new instance with given parameters
	 *
	 * @param message Chat message to be sent
	 * @param type    Message position
	 */
	public PacketPlayOutChat(IChatBaseComponent message, ChatMessageType type) {
		this.message = message;
		this.type = type;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		NMSStorage nms = NMSHook.nms;
		Object component = NMSHook.stringToComponent(message.toString(clientVersion));

		if (nms.minorVersion >= 19) {
			return nms.newPacketPlayOutChat.newInstance(component, this.type.ordinal());
		}

		if (nms.minorVersion >= 16) {
			return nms.newPacketPlayOutChat.newInstance(component, nms.ChatMessageType_values[this.type.ordinal()], UUID.randomUUID());
		}
		if (nms.minorVersion >= 12) {
			return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf((Class<Enum>) nms.ChatMessageType, this.type.toString()));
		}
		if (nms.minorVersion >= 8) {
			return nms.newPacketPlayOutChat.newInstance(component, (byte) this.type.ordinal());
		}
		return null;
	}

	/**
	 * An enum representing positions of a chat message
	 * Calling ordinal() will return type's network ID.
	 */
	public enum ChatMessageType {
		CHAT,
		SYSTEM,
		GAME_INFO
	}
}