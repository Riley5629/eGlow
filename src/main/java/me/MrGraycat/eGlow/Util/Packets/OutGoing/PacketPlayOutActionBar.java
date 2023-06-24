package me.MrGraycat.eGlow.Util.Packets.OutGoing;

import me.MrGraycat.eGlow.Util.Packets.Chat.IChatBaseComponent;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.NMSStorage;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;

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