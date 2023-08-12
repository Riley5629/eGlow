package me.mrgraycat.eglow.util.text;

import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.ChatColor;
import me.mrgraycat.eglow.util.packets.chat.rgb.RGBUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
	private final static Pattern rgb = Pattern.compile("#[0-9a-fA-F]{6}");

	public static String setToBasicName(String effect) {
		effect = ChatColor.stripColor(effect).toLowerCase();

		if (effect.contains("slow"))
			effect = effect.replace("slow", "");

		if (effect.contains("fast"))
			effect = effect.replace("fast", "");

		if (effect.contains("("))
			effect = effect.replace("(", "").replace(")", "");

		return effect.replace(" ", "");
	}

	public static String translateColors(String text) {
		if (text == null || text.isEmpty())
			return "";

		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 15) {
				text = RGBUtils.getInstance().convertRGBtoLegacy(text);
				return text.replace("&", "§");
			}
		} catch (NullPointerException exception) {
			return text.replace("&", "§");
		}

		text = RGBUtils.getInstance().applyFormats(text);

		Matcher match = rgb.matcher(text);
		while (match.find()) {
			String color = text.substring(match.start(), match.end());
			text = text.replace(color, String.valueOf(ChatColor.of(color)));
			match = rgb.matcher(text);
		}

		return text.replace("&", "§");
	}

	public static void sendPlainMsg(Object sender, String message, boolean withPrefix) {
		if (!message.isEmpty()) {
			message = translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message);

			if (sender instanceof Player) {
				((Player) sender).sendMessage(message);
			} else {
				((CommandSender) sender).sendMessage(message);
			}
		}
	}

	public static void sendMsgFromGUI(Player player, String message) {
		if (MainConfig.ACTIONBARS_ENABLE.getBoolean() && MainConfig.ACTIONBARS_IN_GUI.getBoolean()) {
			sendMsg(player, message, true);
		} else {
			sendPlainMsg(player, message, true);
		}
	}

	public static void sendMsg(Object sender, String message, boolean withPrefix) {
		if (!message.isEmpty()) {
			message = translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message);

			if (sender instanceof Player) {
				if (MainConfig.ACTIONBARS_ENABLE.getBoolean()) {
					sendActionbar((Player) sender, message);
				} else {
					((Player) sender).sendMessage(message);
				}
			} else {
				((CommandSender) sender).sendMessage(message);
			}
		}
	}

	public static void sendToConsole(String message, boolean withPrefix) {
		Bukkit.getConsoleSender().sendMessage(translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message));
	}

	private static void sendActionbar(Player player, String message) {
		EGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

		if (ePlayer == null) {
			return;
		}

		if (ePlayer.getVersion().getMinorVersion() < 9) {
			sendPlainMsg(player, message, false);
		} else {
			PacketUtil.sendActionbar(ePlayer, message);
		}
	}

	public static void reportError(Exception exception) {
		sendToConsole("&f[&eeGlow&f]: &4Please report this error to MrGraycat&f!:", false);
		exception.printStackTrace();
	}

	public static String getEffectChatName(EGlowPlayer eGlowPlayer) {
		return (eGlowPlayer.getGlowEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : eGlowPlayer.getGlowEffect().getDisplayName();
	}

	public static String getEffectName(String effect) {
		return "&e" + effect + " &f(" + Objects.requireNonNull(DataManager.getEGlowEffect(effect), "Unable to retrieve effect from given name").getDisplayName() + "&f)";
	}
}