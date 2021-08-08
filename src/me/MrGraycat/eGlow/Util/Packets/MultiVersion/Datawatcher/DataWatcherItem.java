package me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Packets.NMSStorage;

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
		NMSStorage nms = EGlow.getInstance().getNMSHook().nms;
		if (nms.minorVersion >= 9) {
			Object nmsObject = nms.DataWatcherItem_TYPE.get(nmsItem);
			//DataWatcherObject object = new DataWatcherObject((int) DataWatcherItem.getValue(nmsObject, "a"), DataWatcherItem.getValue(nmsObject, "b"));
			return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherObject_SLOT.getInt(nmsObject), nms.DataWatcherObject_SERIALIZER.get(nmsObject)), nms.DataWatcherItem_VALUE.get(nmsItem));
		} else {
			return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherItem_TYPE.getInt(nmsItem), null), nms.DataWatcherItem_VALUE.get(nmsItem));
			//return new DataWatcherItem(new DataWatcherObject((int) getValue(nmsItem, "b"), getValue(nmsItem, "a")), getValue(nmsItem, "c"));
		}
	}
	
	/**
	 * Returns value of a field
	 * @param obj - object to get value from
	 * @param field - name of field to get
	 * @return value of field
	 * @throws Exception - if something fails
	 */
	/*public static Object getValue(Object obj, String field) throws Exception {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		return f.get(obj);
	}*/
}
