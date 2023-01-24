package me.MrGraycat.eGlow.Addon.Citizens;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class EGlowCitizensTrait extends Trait {
	IEGlowPlayer eGlowNPC = null;
	
	@Persist("LastEffect")
	String lastEffect = "none";
	
	public EGlowCitizensTrait() {
		super("eGlow");
	}
	
	public void load(DataKey key) {
		setLastEffect(key.getString("LastEffect", "none"));
	}
	
	public void save(DataKey key) {
		if (getEGlowNPC() != null) {
			key.setString("LastEffect", (getEGlowNPC().isGlowing()) ? getEGlowNPC().getEffect().getName() : "none");
		}
	}
	
	public void onSpawn() {
		if (getEGlowNPC() == null) {
			setEGlowNPC(new IEGlowPlayer(npc));
		}

		getEGlowNPC().disableGlow(true);
		getEGlowNPC().setDataFromLastGlow(getLastEffect());
		
		try {
			if (!npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none"))
				getEGlowNPC().activateGlow();
		} catch(NoSuchMethodError e) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
		}
	}
	
	public void onDespawn() {}

	public void onRemove() {}
	
	private void setEGlowNPC(IEGlowPlayer entity) {
		this.eGlowNPC = entity;
	}
	
	public IEGlowPlayer getEGlowNPC() {
		return this.eGlowNPC;
	}
	
	private void setLastEffect(String lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	private String getLastEffect() {
		return this.lastEffect;
	}
}