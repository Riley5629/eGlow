package me.MrGraycat.eGlow.Util.Packets.OutGoing;

import me.MrGraycat.eGlow.Util.Packets.Chat.IChatBaseComponent;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.NMSStorage;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;

import java.util.UUID;

public class PacketPlayOutChat extends PacketPlayOut {
    /** Message to be sent */
    private final IChatBaseComponent message;

    /** Message position */
    private final ChatMessageType type;

    /**
     * Constructs new instance with given parameters
     *
     * @param   message
     *          Chat message to be sent
     * @param   type
     *          Message position
     */
    public PacketPlayOutChat(IChatBaseComponent message, ChatMessageType type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Returns {@link #message}
     *
     * @return  message
     */
    public IChatBaseComponent getMessage() {
        return message;
    }

    /**
     * Returns {@link #type}
     *
     * @return  type
     */
    public ChatMessageType getType() {
        return type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object toNMS(ProtocolVersion clientVersion) throws Exception {
        NMSStorage nms = NMSHook.nms;
        Object component = NMSHook.stringToComponent(getMessage().toString(clientVersion));

        if (nms.minorVersion >= 19) {
            return nms.newPacketPlayOutChat.newInstance(component, this.getType().ordinal());
        }

        if (nms.minorVersion >= 16) {
            return nms.newPacketPlayOutChat.newInstance(component, nms.ChatMessageType_values[this.getType().ordinal()], UUID.randomUUID());
        }
        if (nms.minorVersion >= 12) {
            return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf((Class<Enum>) nms.ChatMessageType, this.getType().toString()));
        }
        if (nms.minorVersion >= 8) {
            return nms.newPacketPlayOutChat.newInstance(component, (byte) this.getType().ordinal());
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