package me.mrgraycat.eglow.addon.citizens;

import lombok.Getter;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

@Getter
public class EGlowCitizensTrait extends Trait {
	EGlowPlayer eGlowNPC = null;

	@Persist("LastEffect")
	String lastEffect = "none";

	public EGlowCitizensTrait() {
		super("eGlow");
	}

	@Override
	public void load(DataKey key) {
		this.lastEffect = key.getString("LastEffect", "none");
	}

	@Override
	public void save(DataKey key) {
		if (getEGlowNPC() != null) {
			this.lastEffect = getEGlowNPC().getGlowEffect().getName();
			key.setString("LastEffect", lastEffect);
		}
	}

	@Override
	public void onSpawn() {
		if (getEGlowNPC() == null)
			this.eGlowNPC = new EGlowPlayer(this.npc);

		getEGlowNPC().disableGlow(true);
		getEGlowNPC().setDataFromLastGlow(getLastEffect());

		try {
			if (!this.npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none"))
				getEGlowNPC().activateGlow();
		} catch (NoSuchMethodError ignored) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
		}
	}
}