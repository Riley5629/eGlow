package me.MrGraycat.eGlow.Util.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;

public class ChatUtil {
	private final static Pattern rgb = Pattern.compile("#[0-9a-fA-F]{6}");
	//private final static Pattern gradient = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
	public final static char COLOR_CHAR = ChatColor.COLOR_CHAR;

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
			return text;
		
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 15)
				return text.replace("&", "§");
		} catch (NullPointerException e) {
			return text.replace("&", "§");
		}
		
		
		Matcher match = rgb.matcher(text);
		while(match.find()) {
			String color = text.substring(match.start(), match.end());
			text = text.replace(color, me.MrGraycat.eGlow.Util.Text.ChatColor.of(color) + "");
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
		if (EGlowMainConfig.EnableActionbarMessages() && EGlowMainConfig.UseActionbarInGUI()) {
			sendMsg(player, message, true);
		} else {
			sendPlainMsg(player, message, true);
		}
	}

	public static void sendMsg(Object sender, String message, boolean withPrefix) {
		if (!message.isEmpty()) {
			message = translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message);

			if (sender instanceof Player) {
				if (EGlowMainConfig.EnableActionbarMessages()) {
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
		IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

		if (ePlayer.getVersion().getMinorVersion() < 9) {
			sendPlainMsg(player, message, false);
		} else {
			PacketUtil.sendActionbar(ePlayer, message);
		}
	}
	
	public static void reportError(Exception e) {
		sendToConsole("&f[&eeGlow&f]: &4Please report this error to MrGraycat&f!:", false);
		e.printStackTrace();
	}
	
	public static String getEffectChatName(IEGlowPlayer entity) {
		return (entity.getEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : entity.getEffect().getDisplayName();
	}
	
	public static String getEffectName(String effect) {
		return "&e" + effect + " &f(" + DataManager.getEGlowEffect(effect).getDisplayName() + "&f)";
	}
}
