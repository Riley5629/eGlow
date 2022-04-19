package me.MrGraycat.eGlow.Addon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;

public class VaultAddon {
	private Chat chat;
	
	/**
	 * Get vault's chat & check if PlaceholderAPI is installed for placeholder support
	 */
	public VaultAddon() {
		RegisteredServiceProvider<Chat> rsp = EGlow.getInstance().getServer().getServicesManager().getRegistration(Chat.class);

		if (rsp != null)
			chat = rsp.getProvider();
	}
	
	/**
	 * Get the players prefix following eGlow's Layout settings
	 * @param player IEGlowEntity to get the prefix from
	 * @return Formatted prefix as String
	 */
	public String getPlayerTagPrefix(IEGlowPlayer player) {
		if (!EGlowMainConfig.setTagnameFormat())
			return "";
		
		Player p = player.getPlayer();
		String prefix = EGlowMainConfig.getTagPrefix();
		
		if (prefix.contains("%prefix%"))
			prefix = prefix.replace("%prefix%", getPlayerPrefix(player));
		
		if (DebugUtil.isPAPIInstalled())
			prefix = PlaceholderAPI.setPlaceholders(p, prefix);
		
		if (prefix.length() > 14 && ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12)
			prefix = prefix.substring(0,14);
		
		return (!prefix.isEmpty()) ? ChatUtil.translateColors(prefix) : prefix;
	}
	
	/**
	 * Get the players suffix following eGlow's Layout settings
	 * @param player IEGlowEntity to get the suffix from
	 * @return Formatted suffix as String
	 */
	public String getPlayerTagSuffix(IEGlowPlayer player) {
		if (!EGlowMainConfig.setTagnameFormat())
			return "";
		
		Player p = player.getPlayer();
		String suffix = EGlowMainConfig.getTagSuffix();
		
		if (suffix.contains("%suffix%"))
			suffix = suffix.replace("%suffix%", getPlayerSuffix(player));
		
		if (DebugUtil.isPAPIInstalled())
			suffix = PlaceholderAPI.setPlaceholders(p, suffix);
		
		return (!suffix.isEmpty()) ? ChatUtil.translateColors(suffix) : "";
	}
	
	/**
	 * Get the players prefix from Vault
	 * @param player IEGlowEntity to get the prefix from
	 * @return Vault prefix + glow color (cut to 16 chars if needed)
	 */
	public String getPlayerPrefix(IEGlowPlayer player) {
		if (EGlow.getInstance().getVaultAddon() == null || chat == null)
			return "";
		
		Player p = player.getPlayer();
		String prefix = chat.getPlayerPrefix(p);
		
		if (prefix != null && !prefix.equals(""))
			return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && prefix.length() > 14) ? ((player.getActiveColor().equals(ChatColor.RESET)) ? (prefix.length() > 16) ? prefix.substring(0,16) : prefix : prefix.substring(0,14) + player.getActiveColor()) : prefix;
		return "";
	}
	
	/**
	 * Get the players suffix from Vault
	 * @param player IEGlowEntity to get the suffix from
	 * @return Vault suffix + glow color (cut to 16 chars if needed)
	 */
	public String getPlayerSuffix(IEGlowPlayer player) {
		if (EGlow.getInstance().getVaultAddon() == null || chat == null)
			return "";
		
		Player p = player.getPlayer();
		String suffix = chat.getPlayerSuffix(p);
		
		if (suffix != null && !suffix.equals(""))
			return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && suffix.length() > 16) ? suffix.substring(0,16) : suffix;
		return "";
	}
}
