package me.MrGraycat.eGlow.Addon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;

public class VaultAddon {
	private EGlow instance;
	
	private boolean PAPI = false;
	private Chat chat;
	
	/**
	 * Get vault's chat & check if PlaceholderAPI is installed for placeholder support
	 */
	public VaultAddon(EGlow instance) {
		setInstance(instance);
		RegisteredServiceProvider<Chat> rsp = getInstance().getServer().getServicesManager().getRegistration(Chat.class);
		PAPI = getInstance().getDebugUtil().pluginCheck("PlaceholderAPI");
		
		if (rsp != null)
			chat = (Chat) rsp.getProvider();
	}

	/**
	 * Update the tabname of the player
	 * @param player IEGlowEntity to update the tabname for
	 */
	public void updatePlayerTabname(IEGlowPlayer player) {
		if (!EGlowMainConfig.setTabnameFormat())
			return;
		
		Player p = player.getPlayer();
		String format = EGlowMainConfig.getTabPrefix() + ((!EGlowMainConfig.getTabName().isEmpty()) ? EGlowMainConfig.getTabName() : player.getDisplayName()) + EGlowMainConfig.getTabSuffix();
		
		if (format.contains("%name%"))
			format = format.replace("%name%", player.getDisplayName());
		
		if (format.contains("%prefix%") || format.contains("%suffix%"))
			format = format.replace("%prefix%", getPlayerPrefix(player)).replace("%suffix%", getPlayerSuffix(player));
		
		if (PAPI)
			format = PlaceholderAPI.setPlaceholders(p, format);
		
		p.setPlayerListName(ChatUtil.translateColors(format));	
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
		
		if (PAPI)
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
		
		if (PAPI)
			suffix = PlaceholderAPI.setPlaceholders(p, suffix);
		
		return (!suffix.isEmpty()) ? ChatUtil.translateColors(suffix) : "";
	}
	
	/**
	 * Get the players prefix from Vault
	 * @param player IEGlowEntity to get the prefix from
	 * @return Vault prefix + glow color (cut to 16 chars if needed)
	 */
	private String getPlayerPrefix(IEGlowPlayer player) {
		if (getInstance().getVaultAddon() == null || chat == null)
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
	private String getPlayerSuffix(IEGlowPlayer player) {
		if (getInstance().getVaultAddon() == null || chat == null)
			return "";
		
		Player p = player.getPlayer();
		String suffix = chat.getPlayerSuffix(p);
		
		if (suffix != null && !suffix.equals(""))
			return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && suffix.length() > 16) ? suffix.substring(0,16) : suffix;
		return "";
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}
}
