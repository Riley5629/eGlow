package me.MrGraycat.eGlow.Util.Packets.MultiVersion;

import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher.DataWatcher;

public class PacketPlayOutEntityMetadata extends PacketPlayOut {	
	private int entityId;
	private DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return NMSHook.nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}
