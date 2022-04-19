package me.MrGraycat.eGlow.Util.Text;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

/*
 * Copy of class net.md_5.bungee.api.ChatColor
 * but heavily modified
 */

public final class ChatColor {
    public static final char COLOR_CHAR = '\u00A7';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile( "(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap<>();
    private static final Map<String, ChatColor> BY_NAME = new HashMap<>();

    public static final ChatColor BLACK = new ChatColor('0', "black", new Color(0x000000));
    public static final ChatColor DARK_BLUE = new ChatColor('1', "dark_blue", new Color(0x0000AA));
    public static final ChatColor DARK_GREEN = new ChatColor('2', "dark_green", new Color(0x00AA00));
    public static final ChatColor DARK_AQUA = new ChatColor('3', "dark_aqua", new Color(0x00AAAA));
    public static final ChatColor DARK_RED = new ChatColor('4', "dark_red", new Color(0xAA0000));
    public static final ChatColor DARK_PURPLE = new ChatColor('5', "dark_purple", new Color(0xAA00AA));
    public static final ChatColor GOLD = new ChatColor('6', "gold", new Color( 0xFFAA00));
    public static final ChatColor GRAY = new ChatColor('7', "gray", new Color( 0xAAAAAA));
    public static final ChatColor DARK_GRAY = new ChatColor('8', "dark_gray", new Color(0x555555));
    public static final ChatColor BLUE = new ChatColor('9', "blue", new Color( 0x5555FF));
    public static final ChatColor GREEN = new ChatColor('a', "green", new Color( 0x55FF55));
    public static final ChatColor AQUA = new ChatColor('b', "aqua", new Color( 0x55FFFF));
    public static final ChatColor RED = new ChatColor('c', "red", new Color( 0xFF5555));
    public static final ChatColor LIGHT_PURPLE = new ChatColor('d', "light_purple", new Color(0xFF55FF));
    public static final ChatColor YELLOW = new ChatColor('e', "yellow", new Color(0xFFFF55));
    public static final ChatColor WHITE = new ChatColor('f', "white", new Color(0xFFFFFF));
    public static final ChatColor MAGIC = new ChatColor('k', "obfuscated");
    public static final ChatColor BOLD = new ChatColor('l', "bold");
    public static final ChatColor STRIKETHROUGH = new ChatColor('m', "strikethrough");
    public static final ChatColor UNDERLINE = new ChatColor('n', "underline");
    public static final ChatColor ITALIC = new ChatColor('o', "italic");
    public static final ChatColor RESET = new ChatColor('r', "reset");
    
    private final String toString;

    private ChatColor(char code, String name) {
        this(code, name, null);
    }

    private ChatColor(char code, String name, Color color) {
        this.toString = new String(new char[] {COLOR_CHAR, code});

        BY_CHAR.put(code, this);
        BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
    }

    private ChatColor(String name, String toString, int rgb) {
        this.toString = toString;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode( this.toString );
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final ChatColor other = (ChatColor) obj;
        return Objects.equals( this.toString, other.toString );
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

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for ( int i = 0; i < b.length - 1; i++ ) {
            if ( b[i] == altColorChar && ALL_CODES.indexOf( b[i + 1] ) > -1 ) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
   
    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }

    public static ChatColor of(Color color) {
        return of("#" + String.format( "%08x", color.getRGB() ).substring(2));
    }

    public static ChatColor of(String string) {
        Preconditions.checkArgument( string != null, "string cannot be null" );
        if (string.startsWith("#") && string.length() == 7) {
            int rgb;
            try {
                rgb = Integer.parseInt(string.substring(1), 16);
            } catch ( NumberFormatException ex ) {
                throw new IllegalArgumentException("Illegal hex string " + string);
            }

            StringBuilder magic = new StringBuilder( COLOR_CHAR + "x" );
            for ( char c : string.substring(1).toCharArray()) {
                magic.append(COLOR_CHAR).append(c);
            }
            return new ChatColor(string, magic.toString(), rgb);
        }

        ChatColor defined = BY_NAME.get(string.toUpperCase(Locale.ROOT));
        if (defined != null)
            return defined;
        throw new IllegalArgumentException( "Could not parse ChatColor " + string );
    }
}