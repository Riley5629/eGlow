package me.MrGraycat.eGlow.addon.vault;

import lombok.Getter;
import me.MrGraycat.eGlow.addon.GlowAddon;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.dependency.Dependency;
import me.MrGraycat.eGlow.config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAddon extends GlowAddon {

	@Getter private Chat chat;

	/**
	 * Get vault's chat & check if PlaceholderAPI is installed for placeholder support
	 */
	public VaultAddon(EGlow instance) {
		super(instance);

		RegisteredServiceProvider<Chat> rsp = EGlow.getInstance().getServer().getServicesManager().getRegistration(Chat.class);

		if (rsp != null) {
			this.chat = rsp.getProvider();
		}
	}

	/**
	 * Get the players prefix following eGlow's Layout settings
	 *
	 * @param eGlowPlayer IEGlowEntity to get the prefix from
	 * @return Formatted prefix as String
	 */
	public String getPlayerTagPrefix(IEGlowPlayer eGlowPlayer) {
		if (!MainConfig.FORMATTING_TAGNAME_ENABLE.getBoolean()) {
			return "";
		}

		Player player = eGlowPlayer.getPlayer();
		String prefix = MainConfig.FORMATTING_TAGNAME_PREFIX.getString();

		if (prefix.contains("%prefix%")) {
			prefix = prefix.replace("%prefix%", getPlayerPrefix(eGlowPlayer));
		}

		if (Dependency.PLACEHOLDER_API.isLoaded()) {
			prefix = PlaceholderAPI.setPlaceholders(player, prefix);
		}

		if (prefix.length() > 14 && ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12) {
			prefix = prefix.substring(0, 14);
		}

		return !prefix.isEmpty() ? ChatUtil.translateColors(prefix) : prefix;
	}

	/**
	 * Get the players suffix following eGlow's Layout settings
	 *
	 * @param eGlowPlayer IEGlowEntity to get the suffix from
	 * @return Formatted suffix as String
	 */
	public String getPlayerTagSuffix(IEGlowPlayer eGlowPlayer) {
		if (!MainConfig.FORMATTING_TAGNAME_ENABLE.getBoolean()) {
			return "";
		}

		Player player = eGlowPlayer.getPlayer();
		String suffix = MainConfig.FORMATTING_TAGNAME_SUFFIX.getString();

		if (suffix.contains("%suffix%")) {
			suffix = suffix.replace("%suffix%", getPlayerSuffix(eGlowPlayer));
		}

		if (Dependency.PLACEHOLDER_API.isLoaded()) {
			suffix = PlaceholderAPI.setPlaceholders(player, suffix);
		}

		return !suffix.isEmpty() ? ChatUtil.translateColors(suffix) : "";
	}

	/**
	 * Get the players prefix from Vault
	 *
	 * @param eGlowPlayer IEGlowEntity to get the prefix from
	 * @return Vault prefix + glow color (cut to 16 chars if needed)
	 */
	public String getPlayerPrefix(IEGlowPlayer eGlowPlayer) {
		if (EGlow.getInstance().getVaultAddon() == null || getChat() == null) {
			return "";
		}

		Player player = eGlowPlayer.getPlayer();
		String prefix = getChat().getPlayerPrefix(player);

		if (prefix != null && !prefix.isEmpty()) {
			return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && prefix.length() > 14) ?
					((eGlowPlayer.getActiveColor().equals(ChatColor.RESET)) ? (prefix.length() > 16) ?
							prefix.substring(0, 16) : prefix : prefix.substring(0, 14) + eGlowPlayer.getActiveColor()) : prefix;
		}

		return "";
	}

	/**
	 * Get the players suffix from Vault
	 *
	 * @param eGlowPlayer IEGlowEntity to get the suffix from
	 * @return Vault suffix + glow color (cut to 16 chars if needed)
	 */
	public String getPlayerSuffix(IEGlowPlayer eGlowPlayer) {
		if (EGlow.getInstance().getVaultAddon() == null || getChat() == null) {
			return "";
		}

		Player player = eGlowPlayer.getPlayer();
		String suffix = getChat().getPlayerSuffix(player);

		if (suffix != null && !suffix.isEmpty()) {
			return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && suffix.length() > 16) ? suffix.substring(0, 16) : suffix;
		}

		return "";
	}
}