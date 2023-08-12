package me.mrgraycat.eglow.util.packets.chat.rgb;

import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.packets.chat.TextColor;
import me.mrgraycat.eglow.util.packets.chat.rgb.format.*;
import me.mrgraycat.eglow.util.packets.chat.rgb.gradient.CMIGradient;
import me.mrgraycat.eglow.util.packets.chat.rgb.gradient.CommonGradient;
import me.mrgraycat.eglow.util.packets.chat.rgb.gradient.GradientPattern;
import me.mrgraycat.eglow.util.packets.chat.rgb.gradient.KyoriGradient;

import java.util.regex.Pattern;

public class RGBUtils {
	/**
	 * Instance of the class
	 */
	private static final RGBUtils instance = new RGBUtils();

	/**
	 * Registered RGB formatters
	 */
	private final RGBFormatter[] formats;

	/**
	 * Registered gradient patterns
	 */
	private final GradientPattern[] gradients;

	/**
	 * Constructs new instance and loads all RGB patterns and gradients
	 */
	public RGBUtils() {
		formats = new RGBFormatter[]{
				new BukkitFormat(),
				new CMIFormat(),
				new UnnamedFormat1(),
				new HtmlFormat(),
				new KyoriFormat()
		};
		gradients = new GradientPattern[]{
				//{#RRGGBB>}text{#RRGGBB<}
				new CMIGradient(),
				//<#RRGGBB>Text</#RRGGBB>
				new CommonGradient(Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>"),
						Pattern.compile("<#[0-9a-fA-F]{6}\\|.>[^<]*</#[0-9a-fA-F]{6}>"),
						"<#", 9, 2, 9, 7),
				//<$#RRGGBB>Text<$#RRGGBB>
				new CommonGradient(Pattern.compile("<\\$#[0-9a-fA-F]{6}>[^<]*<\\$#[0-9a-fA-F]{6}>"),
						Pattern.compile("<\\$#[0-9a-fA-F]{6}\\|.>[^<]*<\\$#[0-9a-fA-F]{6}>"),
						"<$", 10, 3, 10, 7),
				new KyoriGradient()
		};
	}

	/**
	 * Returns instance of this class
	 *
	 * @return instance
	 */
	public static RGBUtils getInstance() {
		return instance;
	}

	/**
	 * Applies all RGB formats and gradients to text and returns it.
	 *
	 * @param text original text
	 * @return text where everything is converted to #RRGGBB
	 */
	public String applyFormats(String text) {
		if (text == null)
			return "";

		String replaced = text;
		for (GradientPattern pattern : gradients) {
			replaced = pattern.applyPattern(replaced, false);
		}

		for (RGBFormatter formatter : formats) {
			replaced = formatter.reformat(replaced);
		}

		return replaced;
	}

	/**
	 * Converts all hex codes in given string to legacy codes
	 *
	 * @param text text to convert
	 * @return translated text
	 */
	public String convertRGBtoLegacy(String text) {
		if (text == null) return null;
		if (!text.contains("#")) return EnumChatFormat.color(text);
		String applied = applyFormats(text);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < applied.length(); i++) {
			char c = applied.charAt(i);
			if (c == '#' && applied.length() > i + 6) {
				String hexCode = applied.substring(i + 1, i + 7);
				if (isHexCode(hexCode)) {
					if (containsLegacyCode(applied, i)) {
						sb.append(new TextColor(hexCode, EnumChatFormat.getByChar(applied.charAt(i + 8))).getLegacyColor().getFormat());
						i += 8;
					} else {
						sb.append(new TextColor(hexCode).getLegacyColor().getFormat());
						i += 6;
					}
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns true if entered string is a valid 6-digit combination of
	 * hexadecimal numbers, false if not
	 *
	 * @param string string to check
	 * @return {@code true} if valid, {@code false} if not
	 */
	public boolean isHexCode(String string) {
		if (string == null)
			return false;

		if (string.length() != 6) return false;
		for (int i = 0; i < 6; i++) {
			char c = string.charAt(i);
			if (c < 48 || (c > 57 && c < 65) || (c > 70 && c < 97) || c > 102) return false;
		}
		return true;
	}

	/**
	 * Returns true if text contains legacy color request at defined RGB index start
	 *
	 * @param text text to check
	 * @param i    current index start
	 * @return {@code true} if legacy color is defined and valid, {@code false} otherwise
	 */
	private static boolean containsLegacyCode(String text, int i) {
		if (text.length() - i < 9 || text.charAt(i + 7) != '|')
			return false;
		return EnumChatFormat.getByChar(text.charAt(i + 8)) != null;
	}
}