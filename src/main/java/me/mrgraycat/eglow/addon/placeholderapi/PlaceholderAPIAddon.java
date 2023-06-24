package me.mrgraycat.eglow.addon.placeholderapi;

import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPIAddon extends PlaceholderExpansion {

	private final EGlow instance;

	/**
	 * Register all eGlow placeholders in PlaceholderAPI
	 */
	public PlaceholderAPIAddon() {
		this.instance = EGlow.getInstance();

		register();
	}

	@Override
	public String getAuthor() {
		return instance.getDescription().getAuthors().toString();
	}

	@Override
	public String getVersion() {
		return instance.getDescription().getVersion();
	}

	@Override
	public String getIdentifier() {
		return "eglow";
	}

	@Override
	public String getRequiredPlugin() {
		return "eGlow";
	}

	@Override
	public boolean canRegister() {
		return instance != null;
	}

	@Override
	public boolean register() {
		if (!canRegister())
			return false;
		return super.register();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (player == null)
			return "";

		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (eGlowPlayer == null) {
			return "";
		}

		switch (identifier.toLowerCase()) {
			case "client_version":
				return eGlowPlayer.getVersion().getFriendlyName();
			case "glowcolor":
				return eGlowPlayer.getGlowStatus() && !eGlowPlayer.getFakeGlowStatus() ? eGlowPlayer.getActiveColor().toString() : "";
			case "colorchar":
				return eGlowPlayer.getGlowStatus() && !eGlowPlayer.getFakeGlowStatus() ? String.valueOf(eGlowPlayer.getActiveColor().getChar()) : "r";
			case "activeglow":
				return eGlowPlayer.isGlowing() ? ChatUtil.getEffectChatName(eGlowPlayer) : Message.COLOR.get("none");
			case "activeglow_raw":
				return eGlowPlayer.isGlowing() ? ChatUtil.setToBasicName(ChatUtil.getEffectChatName(eGlowPlayer)) : ChatUtil.setToBasicName(Message.COLOR.get("none"));
			case "lastglow":
				return eGlowPlayer.getLastGlowName();
			case "lastglow_raw":
				return ChatUtil.setToBasicName(eGlowPlayer.getLastGlowName());
			case "glow_speed":
				return getSpeedFromEffect(eGlowPlayer.getGlowEffect(), false);
			case "glow_speed_raw":
				return getSpeedFromEffect(eGlowPlayer.getGlowEffect(), true);
			case "glowstatus":
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.getFakeGlowStatus()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
			case "glowstatus_raw":
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.getFakeGlowStatus()) ? "true" : "false";
			case "glowstatus_join":
				return eGlowPlayer.isGlowOnJoin() ? Message.GUI_YES.get() : Message.GUI_NO.get();
			case "glowstatus_join_raw":
				return Boolean.toString(eGlowPlayer.isGlowOnJoin());
			case "glow_visibility":
				return (eGlowPlayer.getGlowVisibility().equals(Common.GlowVisibility.UNSUPPORTEDCLIENT)) ?
						Message.VISIBILITY_UNSUPPORTED.get() :
						Message.valueOf("VISIBILITY_" + eGlowPlayer.getGlowVisibility()).get();
			default:
				boolean raw = identifier.toLowerCase().endsWith("_raw");

				if (identifier.toLowerCase().contains("has_permission_")) {
					IEGlowEffect effect = DataManager.getEGlowEffect(identifier.toLowerCase().replace("has_permission_", "").replace("_raw", ""));

					if (effect != null) {
						if (player.hasPermission(effect.getPermission())) {
							return (raw) ? "true" : Message.GUI_YES.get();
						} else {
							return (raw) ? "false" : Message.GUI_NO.get();
						}
					} else {
						return "Invalid effect";
					}
				}
		}

		return null;
	}

	private String getSpeedFromEffect(IEGlowEffect effect, boolean raw) {
		if (effect == null) {
			return raw ? "none" : Message.COLOR.get("none");
		}

		String effectName = effect.getName();

		if (effectName.contains("slow")) {
			return raw ? "slow" : Message.COLOR.get("slow");
		}

		if (effectName.contains("fast")) {
			return raw ? "fast" : Message.COLOR.get("fast");
		}

		return raw ? "none" : Message.COLOR.get("none");
	}
}