package me.MrGraycat.eGlow.Util.Packets.MultiVersion;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher.DataWatcher;

public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	private int entityId;
	private DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(EGlow instance, int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return EGlow.getInstance().getNMSHook().nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}
