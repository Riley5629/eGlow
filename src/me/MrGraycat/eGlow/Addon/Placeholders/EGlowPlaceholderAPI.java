package me.MrGraycat.eGlow.Addon.Placeholders;

import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class EGlowPlaceholderAPI extends PlaceholderExpansion {
	
	private EGlow instance;
	
	/**
	 * Register all eGlow placeholders in PlaceholderAPI
	 */
	public EGlowPlaceholderAPI(EGlow instance) {
		setInstance(instance);
		register();
	}
	
	@Override
	public String getAuthor() {
		return getInstance().getDescription().getAuthors().toString();
	}

	@Override
	public String getVersion() {
		return getInstance().getDescription().getVersion();
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
		return getInstance() != null;
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
    	
        IEGlowPlayer eglowPlayer = getInstance().getDataManager().getEGlowPlayer(player);
        
        if (eglowPlayer == null)
        	return "";
        
        switch(identifier.toLowerCase()) {
        case("glowcolor"):
        	return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor() + "" : "";
        case("colorchar"):
        	return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor().getChar() + "" : "r";	
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
    
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}
}
