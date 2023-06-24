package me.MrGraycat.eGlow.util.chat;

import lombok.experimental.UtilityClass;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.util.packet.chat.rgb.RGBUtils;
import me.MrGraycat.eGlow.config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.packet.chat.ChatColor;
import me.MrGraycat.eGlow.util.packet.PacketUtil;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatUtil {

	private final Pattern RGB = Pattern.compile("#[0-9a-fA-F]{6}");

	public String setToBasicName(String effect) {
		effect = ChatColor.stripColor(effect).toLowerCase();
		
		if (effect.contains("slow")) {
			effect = effect.replace("slow", "");
		}

		if (effect.contains("fast")) {
			effect = effect.replace("fast", "");
		}

		if (effect.contains("(")) {
			effect = effect.replace("(", "").replace(")", "");
		}

		return effect.replace(" ", "");
	}

	public String translateColors(String text) {
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

		Matcher match = RGB.matcher(text);

		while (match.find()) {
			String color = text.substring(match.start(), match.end());
			text = text.replace(color, ChatColor.of(color) + "");
			match = RGB.matcher(text);
		}

		return text.replace("&", "§");
	}

	public void sendPlainMessage(CommandSender sender, String message, boolean withPrefix) {
		if (!message.isEmpty()) {
			sender.sendMessage(translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message));
		}
	}

	public void sendMenuFromMessage(Player player, String message) {
		if (MainConfig.ACTIONBARS_ENABLE.getBoolean() && MainConfig.ACTIONBARS_IN_GUI.getBoolean()) {
			sendMessage(player, message, true);
		} else {
			sendPlainMessage(player, message, true);
		}
	}

	public void sendMessage(CommandSender sender, String message, boolean withPrefix) {
		if (message.isEmpty()) {
			return;
		}

		message = translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message);

		if (sender instanceof Player) {
			if (MainConfig.ACTIONBARS_ENABLE.getBoolean()) {
				sendActionbar((Player) sender, message);
			} else {
				sender.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}

	public void sendMessage(IEGlowPlayer player, String message, boolean withPrefix) {
		sendMessage(player.getPlayer(), message, withPrefix);
	}

	public void sendToConsole(String message, boolean withPrefix) {
		Bukkit.getConsoleSender().sendMessage(translateColors(((withPrefix) ? Message.PREFIX.get() : "") + message));
	}

	private void sendActionbar(Player player, String message) {
		IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

		if (ePlayer.getVersion().getMinorVersion() < 9) {
			sendPlainMessage(player, message, false);
		} else {
			PacketUtil.sendActionbar(ePlayer, message);
		}
	}
	
	public void reportError(Exception e) {
		sendToConsole("&f[&eeGlow&f]: &4Please report this error to MrGraycat&f!:", false);

		e.printStackTrace();
	}
	
	public String getEffectChatName(IEGlowPlayer entity) {
		return (entity.getGlowEffect() == null) ? Message.GUI_NOT_AVAILABLE.get() : entity.getGlowEffect().getDisplayName();
	}
	
	public String getEffectName(String effect) {
		return "&e" + effect + " &f(" + Objects.requireNonNull(DataManager.getEGlowEffect(effect),
				"Unable to retrieve effect from given name").getDisplayName() + "&f)";
	}
}