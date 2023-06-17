package me.MrGraycat.eGlow.util.packet.outbound;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.MrGraycat.eGlow.util.packet.watcher.DataWatcher;
import me.MrGraycat.eGlow.util.packet.NMSHook;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;

import java.util.ArrayList;
import java.util.List;

public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	private final int entityId;
	private final DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		if (NMSHook.nms.newPacketPlayOutEntityMetadata.getParameterCount() == 2) {
			List<Object> items = new ArrayList<>();

			for (Object object : ((Int2ObjectMap) NMSHook.nms.dataWatcherItemsField.get(dataWatcher.toNMS())).values()) {
				items.add(NMSHook.nms.dataWatcherItemToData.invoke(object));
			}

			return NMSHook.nms.newPacketPlayOutEntityMetadata.newInstance(entityId, items);
		} else {
			return NMSHook.nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
		}

	}
}