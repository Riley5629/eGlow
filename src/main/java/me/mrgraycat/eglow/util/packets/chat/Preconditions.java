package me.mrgraycat.eglow.util.packets.chat;

public class Preconditions {
	public static void checkNotNull(Object obj, String name) {
		if (obj == null) throw new IllegalArgumentException(name + " cannot be null");
	}

	public static void checkRange(Number number, Number min, Number max, String variable) {
		if (number.doubleValue() < min.doubleValue() || number.doubleValue() > max.doubleValue())
			throw new IllegalArgumentException(variable + " index out of range (" + min + " - " + max + ")");
	}
}