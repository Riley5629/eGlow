package me.MrGraycat.eGlow.Addon.Citizens;

import lombok.Getter;
import lombok.Setter;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class EGlowCitizensTrait extends Trait {
	@Getter
	@Setter
	IEGlowPlayer eGlowNPC = null;

	@Getter
	@Setter
	@Persist("LastEffect")
	String lastEffect = "none";

	public EGlowCitizensTrait() {
		super("eGlow");
	}

	@Override
	public void load(DataKey key) {
		setLastEffect(key.getString("LastEffect", "none"));
	}

	@Override
	public void save(DataKey key) {
		if (getEGlowNPC() != null) {
			key.setString("LastEffect", (getEGlowNPC().isGlowing()) ? getEGlowNPC().getEffect().getName() : "none");
		}
	}

	@Override
	public void onSpawn() {
		if (getEGlowNPC() == null) {
			setEGlowNPC(new IEGlowPlayer(this.npc));
		}

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