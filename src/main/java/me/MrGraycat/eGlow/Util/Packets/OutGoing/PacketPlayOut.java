package me.mrgraycat.eglow.util.packets.outgoing;

import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;

import java.lang.reflect.Field;

public abstract class PacketPlayOut {

	public abstract Object toNMS(ProtocolVersion clientVersion) throws Exception;
	
	public String cutTo(String string, int length) {
		if (string == null || string.length() <= length) return string;
		if (string.charAt(length-1) == EnumChatFormat.COLOR_CHAR) {
			return string.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
		} else {
			return string.substring(0, length);
		}
	}
	
	public static Object getField(Object packet, String field) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = packet.getClass().getDeclaredField(field);
		f.setAccessible(true);
		Object value = f.get(packet);
		f.setAccessible(false);
		return value;
	}
}