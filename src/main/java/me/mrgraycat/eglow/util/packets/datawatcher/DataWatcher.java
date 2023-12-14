package me.mrgraycat.eglow.util.packets.datawatcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.NMSStorage;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataWatcher {

	//datawatcher data
	private final Map<Integer, DataWatcherItem> dataValues = new HashMap<>();

	/**
	 * Sets value into data values
	 *
	 * @param type  - type of value
	 * @param value - value
	 */
	public void setValue(DataWatcherObject type, Object value) {
		dataValues.put(type.position, new DataWatcherItem(type, value));
	}

	/**
	 * Returns item with given position
	 *
	 * @param position - position of item
	 * @return item or null if not set
	 */
	public DataWatcherItem getItem(int position) {
		return dataValues.get(position);
	}

	/**
	 * Converts the class into an instance of NMS.DataWatcher
	 *
	 * @return an instance of NMS.DataWatcher with same data
	 * @throws Exception - if something fails
	 */
	public Object toNMS() throws Exception {
		NMSStorage nms = NMSHook.nms;
		Object nmsWatcher;

		if (nms.newDataWatcher.getParameterCount() == 1) {
			nmsWatcher = nms.newDataWatcher.newInstance(new Object[]{null});
		} else {
			nmsWatcher = nms.newDataWatcher.newInstance();
		}
		for (DataWatcherItem item : this.dataValues.values()) {
			Object position;
			if (nms.minorVersion >= 9) {
				position = nms.newDataWatcherObject.newInstance(item.type.position, item.type.classType);
			} else {
				position = item.type.position;
			}
			nms.DataWatcher_REGISTER.invoke(nmsWatcher, position, item.value);
		}
		return nmsWatcher;
	}

	/**
	 * Reads NMS data watcher and returns and instance of this class with same data
	 *
	 * @param nmsWatcher - NMS datawatcher to read
	 * @return an instance of this class with same values
	 * @throws Exception - if something fails
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception {
		DataWatcher watcher = new DataWatcher();
		List<Object> items;

		if (NMSHook.nms.isIs1_19_3OrAbove()) {
			items = new ArrayList<>(((Int2ObjectMap) NMSHook.nms.DataWatcherItems.get(nmsWatcher)).values());
		} else {
			items = (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 17) ? (List<Object>) nmsWatcher.getClass().getMethod("getAll").invoke(nmsWatcher, new Object[0]) : (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
		}

		for (Object watchableObject : items) {
			DataWatcherItem w = DataWatcherItem.fromNMS(watchableObject);
			watcher.setValue(w.type, w.value);
		}
		return watcher;
	}

	/**
	 * Reads NMS data watcher and returns and instance of this class with same data
	 *
	 * @param nmsPacket - NMS datawatcher to read
	 * @return an instance of this class with same values
	 * @throws Exception - if something fails
	 */
	public static DataWatcher fromNMSPacket(Object nmsPacket) throws Exception {
		DataWatcher watcher = new DataWatcher();
		List<Object> items = new ArrayList<>((List<?>) NMSHook.nms.PacketPlayOutEntityMetadata_LIST.get(nmsPacket));

		for (Object watchableObject : items) {
			DataWatcherItem w;
			if (NMSHook.nms.isIs1_19_3OrAbove()) {
				w = DataWatcherItem.fromPacketNMS(watchableObject);
			} else {
				w = DataWatcherItem.fromNMS(watchableObject);
			}
			watcher.setValue(w.type, w.value);
		}
		return watcher;
	}
}