package me.MrGraycat.eGlow.util.packet.watcher;

import me.MrGraycat.eGlow.util.packet.NMSStorage;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataWatcherRegistry {
	//used registry types
	public Object byteType;
	public Object integerType;
	public Object floatType;
	public Object stringType;
	public Object iChatBaseComponentType;
	public Object booleanType;

	/**
	 * Initializes required NMS classes and fields
	 */
	public DataWatcherRegistry(NMSStorage nms) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Map<String, Object> fields = getStaticFields(nms.dataWatcherRegistryClass);
			byteType = fields.get("a");
			integerType = fields.get("b");
			floatType = fields.get("c");
			stringType = fields.get("d");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				iChatBaseComponentType = fields.get("f");
				booleanType = fields.get("i");
			} else {
				booleanType = fields.get("h");
			}
		} else {
			byteType = 0;
			integerType = 2;
			floatType = 3;
			stringType = 4;
		}
	}

	/**
	 * Gets values of all static fields in a class
	 * @param clazz class to return field values from
	 * @return map of values
	 */
	private Map<String, Object> getStaticFields(Class<?> clazz){
		Map<String, Object> fields = new HashMap<>();

		if (clazz == null) {
			return fields;
		}

		Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
			field.setAccessible(true);

			if (Modifier.isStatic(field.getModifiers())) {
				try {
					fields.put(field.getName(), field.get(null));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return fields;
	}
}