package me.mrgraycat.eglow.addon;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.entity.Player;

public class PlaceholderAPIAddon extends PlaceholderExpansion {
	/**
	 * Register all eGlow placeholders in PlaceholderAPI
	 */
	public PlaceholderAPIAddon() {
		register();
	}

	@Override
	public String getAuthor() {
		return EGlow.getInstance().getDescription().getAuthors().toString();
	}

	@Override
	public String getVersion() {
		return EGlow.getInstance().getDescription().getVersion();
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
		return EGlow.getInstance() != null;
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

		EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (eGlowPlayer == null)
			return "";

		switch (identifier.toLowerCase()) {
			case ("client_version"):
				return eGlowPlayer.getVersion().getFriendlyName();
			case ("glowcolor"):
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.isFakeGlowStatus()) ? eGlowPlayer.getActiveColor().toString() : "";
			case ("colorchar"):
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.isFakeGlowStatus()) ? String.valueOf(eGlowPlayer.getActiveColor().getChar()) : "r";
			case ("activeglow"):
				return (eGlowPlayer.isGlowing()) ? ChatUtil.getEffectChatName(eGlowPlayer) : Message.COLOR.get("none");
			case ("activeglow_raw"):
				return (eGlowPlayer.isGlowing()) ? ChatUtil.setToBasicName(ChatUtil.getEffectChatName(eGlowPlayer)) : ChatUtil.setToBasicName(Message.COLOR.get("none"));
			case ("lastglow"):
				return (eGlowPlayer.getLastGlowName());
			case ("lastglow_raw"):
				return ChatUtil.setToBasicName(eGlowPlayer.getLastGlowName());
			case ("glow_speed"):
				return getSpeedFromEffect(eGlowPlayer.getGlowEffect(), false);
			case ("glow_speed_raw"):
				return getSpeedFromEffect(eGlowPlayer.getGlowEffect(), true);
			case ("glowstatus"):
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.isFakeGlowStatus()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
			case ("glowstatus_raw"):
				return (eGlowPlayer.getGlowStatus() && !eGlowPlayer.isFakeGlowStatus()) ? "true" : "false";
			case ("glowstatus_join"):
				return (eGlowPlayer.isGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
			case ("glowstatus_join_raw"):
				return (eGlowPlayer.isGlowOnJoin()) ? "true" : "false";
			case ("glow_visibility"):
				return (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) ? Message.VISIBILITY_UNSUPPORTED.get() : Message.valueOf("VISIBILITY_" + eGlowPlayer.getGlowVisibility().toString()).get();
			case ("glow_visibility_all"):
				return ((eGlowPlayer.getGlowVisibility().equals(GlowVisibility.ALL)) ? Message.GLOW_VISIBILITY_INDICATOR.get() : "") + Message.VISIBILITY_ALL.get();
			case ("glow_visibility_other"):
				return ((eGlowPlayer.getGlowVisibility().equals(GlowVisibility.OTHER)) ? Message.GLOW_VISIBILITY_INDICATOR.get() : "") + Message.VISIBILITY_OTHER.get();
			case ("glow_visibility_own"):
				return ((eGlowPlayer.getGlowVisibility().equals(GlowVisibility.OWN)) ? Message.GLOW_VISIBILITY_INDICATOR.get() : "") + Message.VISIBILITY_OWN.get();
			case ("glow_visibility_none"):
				return ((eGlowPlayer.getGlowVisibility().equals(GlowVisibility.NONE)) ? Message.GLOW_VISIBILITY_INDICATOR.get() : "") + Message.VISIBILITY_NONE.get();
			default:
				boolean raw = identifier.toLowerCase().endsWith("_raw");
				if (identifier.toLowerCase().contains("has_permission_")) {
					EGlowEffect effect = DataManager.getEGlowEffect(identifier.toLowerCase().replace("has_permission_", "").replace("_raw", ""));
					if (effect != null) {
						if (player.hasPermission(effect.getPermissionNode())) {
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

	private String getSpeedFromEffect(EGlowEffect effect, boolean raw) {
		if (effect == null)
			return (raw) ? "none" : Message.COLOR.get("none");

		String effectName = effect.getName();

		if (effectName.contains("slow"))
			return (raw) ? "slow" : Message.COLOR.get("slow");

		if (effectName.contains("fast"))
			return (raw) ? "fast" : Message.COLOR.get("fast");
		return (raw) ? "none" : Message.COLOR.get("none");
	}
}