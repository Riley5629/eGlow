package me.mrgraycat.eglow.util.packets.outgoing;

import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.NMSStorage;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent;

public class PacketPlayOutActionBar extends PacketPlayOut {

	/**
	 * Message to be sent
	 */
	private final IChatBaseComponent message;

	public PacketPlayOutActionBar(IChatBaseComponent message) {
		this.message = message;
	}

	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		NMSStorage nms = NMSHook.nms;
		Object component = NMSHook.stringToComponent(message.toString(clientVersion));

		return nms.newPlayOutPacketActionBar.newInstance(component);
	}
}