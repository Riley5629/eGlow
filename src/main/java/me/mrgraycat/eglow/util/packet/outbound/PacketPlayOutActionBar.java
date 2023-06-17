package me.mrgraycat.eglow.util.packet.outbound;

import me.mrgraycat.eglow.util.packet.chat.IChatBaseComponent;
import me.mrgraycat.eglow.util.packet.NMSHook;
import me.mrgraycat.eglow.util.packet.NMSStorage;
import me.mrgraycat.eglow.util.packet.ProtocolVersion;

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