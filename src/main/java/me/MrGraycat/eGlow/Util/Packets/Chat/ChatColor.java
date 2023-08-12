package me.mrgraycat.eglow.util.packets.chat;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ChatColor {
	/*
	 * Copy of class net.md_5.bungee.api.ChatColor
	 * but heavily modified
	 */

	public static final char COLOR_CHAR = '\u00A7';
	public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");

	private final String toString;

	private ChatColor(String toString) {
		this.toString = toString;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.toString);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		final ChatColor other = (ChatColor) obj;
		return Objects.equals(this.toString, other.toString);
	}

	@Override
	public String toString() {
		return toString;
	}

	public static String stripColor(final String input) {
		if (input == null)
			return null;
		return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
	}

	public static ChatColor of(Color color) {
		return of("#" + String.format("%08x", color.getRGB()).substring(2));
	}

	public static ChatColor of(String string) {
		Preconditions.checkNotNull(string, "ChatColor text");

		if (string.startsWith("#") && string.length() == 7) {
			try {
				Integer.parseInt(string.substring(1), 16);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Illegal hex string " + string);
			}

			StringBuilder magic = new StringBuilder(COLOR_CHAR + "x");
			for (char c : string.substring(1).toCharArray()) {
				magic.append(COLOR_CHAR).append(c);
			}
			return new ChatColor(magic.toString());
		}

		throw new IllegalArgumentException("Could not parse ChatColor " + string);
	}
}