package me.MrGraycat.eGlow.addon.citizens;

import lombok.Getter;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
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
		if (eGlowNPC == null) {
			return;
		}

		key.setString("LastEffect", (eGlowNPC.isGlowing()) ? eGlowNPC.getGlowEffect().getName() : "none");
	}

	@Override
	public void onSpawn() {
		if (eGlowNPC == null) {
			this.eGlowNPC = new IEGlowPlayer(this.npc);
		}

		this.eGlowNPC.disableGlow(true);
		this.eGlowNPC.setDataFromLastGlow(lastEffect);

		try {
			if (!this.npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none")) {
				this.eGlowNPC.activateGlow();
			}
		} catch (NoSuchMethodError ignored) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
		}
	}
}