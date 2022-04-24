package me.MrGraycat.eGlow.Util.Packets.Chat.rgb.format;

/**
 * Formatter for &amp;#RRGGBB
 */
public class UnnamedFormat1 implements RGBFormatter {

    @Override
    public String reformat(String text) {
        return text.contains("&#") ? text.replace("&#", "#") : text;
    }
}