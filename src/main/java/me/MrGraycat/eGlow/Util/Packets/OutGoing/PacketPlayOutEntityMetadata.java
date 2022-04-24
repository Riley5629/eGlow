package me.MrGraycat.eGlow.Util.Packets.OutGoing;

import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.Datawatcher.DataWatcher;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;

public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	private final int entityId;
	private final DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return NMSHook.nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}
