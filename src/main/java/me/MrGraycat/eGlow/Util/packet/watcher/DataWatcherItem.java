package me.mrgraycat.eglow.util.packet.watcher;

import me.mrgraycat.eglow.util.packet.NMSHook;
import me.mrgraycat.eglow.util.packet.NMSStorage;

public class DataWatcherItem {
	//type of value (position + data type (1.9+))
	public DataWatcherObject type;
	
	//actual data value
	public Object value;
	
	/**
	 * Constructs new instance of the object with given parameters
	 * @param type - value type
	 * @param value - value
	 */
	public DataWatcherItem(DataWatcherObject type, Object value){
		this.type = type;
		this.value = value;
	}
	
	/**
	 * Returns and instance of this class from given NMS item
	 * @param nmsItem - NMS item
	 * @return instance of this class with same data
	 * @throws Exception - if something fails
	 */
	public static DataWatcherItem fromNMS(Object nmsItem) throws Exception {
		NMSStorage nms = NMSHook.nms;
		if (nms.minorVersion >= 9) {
			Object nmsObject = nms.dataWatcherItemTypeField.get(nmsItem);
			return new DataWatcherItem(new DataWatcherObject(nms.dataWatcherObjectSlot.getInt(nmsObject), nms.dataWatcherObjectSerializer.get(nmsObject)), nms.dataWatcherItemValue.get(nmsItem));
		} else {
			return new DataWatcherItem(new DataWatcherObject(nms.dataWatcherItemTypeField.getInt(nmsItem), null), nms.dataWatcherItemValue.get(nmsItem));
		}
	}
}