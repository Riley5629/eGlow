package me.mrgraycat.eglow.addon.citizens;

import lombok.Getter;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

@Getter
public class EGlowCitizensTrait extends Trait {

	private IEGlowPlayer eGlowNPC;

	@Persist("LastEffect")
	private String lastEffect = "none";

	public EGlowCitizensTrait() {
		super("eGlow");
	}

	@Override
	public void load(DataKey key) {
		this.lastEffect = key.getString("LastEffect", "none");
	}

	@Override
	public void save(DataKey key) {
		if (getEGlowNPC() == null) {
			return;
		}

		key.setString("LastEffect", (getEGlowNPC().isGlowing()) ? getEGlowNPC().getGlowEffect().getName() : "none");
	}

	@Override
	public void onSpawn() {
		if (getEGlowNPC() == null) {
			this.eGlowNPC = new IEGlowPlayer(getNPC());
		}

		getEGlowNPC().disableGlow(true);
		getEGlowNPC().setDataFromLastGlow(getLastEffect());

		try {
			if (!getNPC().getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none")) {
				getEGlowNPC().activateGlow();
			}
		} catch (NoSuchMethodError ignored) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
		}
	}
}