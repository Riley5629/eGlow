package me.MrGraycat.eGlow.Addon;

import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

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
    	
        IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
        
        if (eglowPlayer == null)
        	return "";
        
        switch(identifier.toLowerCase()) {
        case("glowcolor"):
        	return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor() + "" : "";
        case("colorchar"):
        	return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor().getChar() + "" : "r";	
        case("lastglow"):
        	return (eglowPlayer.getLastGlowName());
        case("glow_visibility"):
        	switch (eglowPlayer.getGlowVisibility()) {
			case UNSUPPORTEDCLIENT:
				return Message.VISIBILITY_UNSUPPORTED.get();
			default:
				return Message.valueOf("VISIBILITY_" + eglowPlayer.getGlowVisibility().toString()).get();
			}
        case("selectedglow"):
        	return (!eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? Message.COLOR.get("none") : ChatUtil.getEffectChatName(eglowPlayer);
        case("selectedglow_raw"):
        	return (!eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? ChatUtil.setToBasicName(Message.COLOR.get("none")) : ChatUtil.setToBasicName(ChatUtil.getEffectChatName(eglowPlayer));
        case("glowstatus"):
        	return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
        case("glowstatus_join"):
        	return (eglowPlayer.getGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
        case("glowstatus_raw"):
        	return String.valueOf(eglowPlayer.getGlowStatus());
        }
        return null;
    }
}
