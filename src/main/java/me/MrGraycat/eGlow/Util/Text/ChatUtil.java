package me.MrGraycat.eGlow.Util.Text;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Util.Packets.Chat.rgb.RGBUtils;
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
		} catch (NullPointerException e) {
			return text.replace("&", "§");
		}

		text = RGBUtils.getInstance().applyFormats(text);
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