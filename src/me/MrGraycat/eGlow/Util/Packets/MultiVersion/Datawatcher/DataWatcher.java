package me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Packets.NMSStorage;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;

public class DataWatcher {

	//datawatcher data
	private Map<Integer, DataWatcherItem> dataValues = new HashMap<Integer, DataWatcherItem>();

	/**
	 * Sets value into data values
	 * @param type - type of value
	 * @param value - value
	 */
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.position, new DataWatcherItem(type, value));
	}

	/**
	 * Removes value by position
	 * @param position - position of value to remove
	 */
	public void removeValue(int position) {
		dataValues.remove(position);
	}

	/**
	 * Returns item with given position
	 * @param position - position of item
	 * @return item or null if not set
	 */
	public DataWatcherItem getItem(int position) {
		return dataValues.get(position);
	}

	/**
	 * Converts the class into an instance of NMS.DataWatcher
	 * @return an instance of NMS.DataWatcher with same data
	 * @throws Exception - if something fails
	 */
	public Object toNMS() throws Exception {
		NMSStorage nms = EGlow.getInstance().getNMSHook().nms;
		Object nmsWatcher = nms.newDataWatcher.newInstance(new Object[] {null});
		
		if (nms.newDataWatcher.getParameterCount() == 1) {
			nmsWatcher = nms.newDataWatcher.newInstance(new Object[] { null });
		} else {
			nmsWatcher = nms.newDataWatcher.newInstance(new Object[0]);
		}
		for (DataWatcherItem item : this.dataValues.values()) {
			Object position;
			if (nms.minorVersion >= 9) {
				position = nms.newDataWatcherObject.newInstance(new Object[] { Integer.valueOf(item.type.position), item.type.classType });
			} else {
				position = Integer.valueOf(item.type.position);
			}
			nms.DataWatcher_REGISTER.invoke(nmsWatcher, new Object[] { position, item.value });
		}
		return nmsWatcher;
		/*for (DataWatcherItem item : dataValues.values()) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				nms.DataWatcher_REGISTER.invoke(nmsWatcher, nms.newDataWatcherObject.newInstance(item.type.position, item.type.classType), item.value);
			} else {
				nms.DataWatcher_REGISTER.invoke(nmsWatcher, item.type.position, item.value);
			}
		}
		return nmsWatcher;*/
	}
	
	/**
	 * Reads NMS data watcher and returns and instance of this class with same data
	 * @param nmsWatcher - NMS datawatcher to read
	 * @return an instance of this class with same values
	 * @throws Exception - if something fails
	 */
	@SuppressWarnings("unchecked")
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception{
		DataWatcher watcher = new DataWatcher();
		List<Object> items = null;

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 17) {
			items = (List<Object>)nmsWatcher.getClass().getMethod("getAll", new Class[0]).invoke(nmsWatcher, new Object[0]);
		} else {
			items = (List<Object>)nmsWatcher.getClass().getMethod("c", new Class[0]).invoke(nmsWatcher, new Object[0]);
		}
		 
		if (items != null) {
			for (Object watchableObject : items) {
				DataWatcherItem w = DataWatcherItem.fromNMS(watchableObject);
				watcher.setValue(w.type, w.value);
			}
		}
		return watcher;
	}
}
