package me.MrGraycat.eGlow.Addon.Citizens;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class EGlowCitizensTrait extends Trait {
	IEGlowPlayer eGlowNPC = null;
	
	@Persist("ActiveOnDespawn")
	boolean activeOnDespawn = false;
	
	@Persist("LastEffect")
	String lastEffect = "none";
	
	public EGlowCitizensTrait() {
		super("eGlow");
	}
	
	public void load(DataKey key) {
		if (key.keyExists("ActiveOnDespawn")) {
			activeOnDespawn = key.getBoolean("ActiveOnDespawn");
		} else {
			activeOnDespawn = key.getBoolean("ActiveOnDespawn", true);
		}
			
		if (key.keyExists("LastEffect")) {
			lastEffect = key.getString("LastEffect");
			
			if (lastEffect.contains("SOLID:") || lastEffect.contains("EFFECT:"))
				lastEffect = lastEffect.replace("SOLID:", "").replace("EFFECT:", "");
		} else {
			lastEffect = key.getString("LastEffect", "none");	
		}
	}
	
	public void save(DataKey key) {
		if (eGlowNPC != null) {
			setActiveOnDespawn((eGlowNPC.getFakeGlowStatus() || eGlowNPC.getGlowStatus()));
			setLastEffect(eGlowNPC.getEffect().getName());

			key.setBoolean("ActiveOnDespawn", activeOnDespawn);
			key.setString("LastEffect", lastEffect);
		}
	}
	
	public void onSpawn() {
		if (getEGlowNPC() == null) {
			setEGlowNPC(new IEGlowPlayer(npc));
		}

		eGlowNPC.disableGlow(true);
		eGlowNPC.setDataFromLastGlow(getLastEffect());
		
		try {
			if (!npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none") && npc.getOrAddTrait(EGlowCitizensTrait.class).getActiveOnDespawn())
				eGlowNPC.activateGlow();		
		} catch(NoSuchMethodError e) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please use 2.0.27 or later", true);
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
	
	public void setActiveOnDespawn(boolean activeOnDespawn) {
		this.activeOnDespawn = activeOnDespawn;
	}
	
	private Boolean getActiveOnDespawn() {
		return this.activeOnDespawn;
	}
}