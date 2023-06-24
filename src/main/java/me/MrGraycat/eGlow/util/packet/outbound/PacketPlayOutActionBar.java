package me.MrGraycat.eGlow.util.packet.outbound;

import me.MrGraycat.eGlow.util.packet.chat.IChatBaseComponent;
import me.MrGraycat.eGlow.util.packet.NMSHook;
import me.MrGraycat.eGlow.util.packet.NMSStorage;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;

public class PacketPlayOutActionBar extends PacketPlayOut {

    /** Message to be sent */
    private final IChatBaseComponent message;

    public PacketPlayOutActionBar(IChatBaseComponent message) {
        this.message = message;
    }

    /**
     * Returns {@link #message}
     *
     * @return  message
     */
    public IChatBaseComponent getMessage() {
        return message;
    }

    @Override
    public Object toNMS(ProtocolVersion clientVersion) throws Exception {
        NMSStorage nms = NMSHook.nms;
        Object component = NMSHook.stringToComponent(getMessage().toString(clientVersion));

        return nms.newPlayOutPacketActionBar.newInstance(component);
    }
}