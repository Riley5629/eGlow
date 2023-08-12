package me.mrgraycat.eglow.addon;

import me.mrgraycat.eglow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class AbstractAddonBase {

	protected final EGlow eGlowInstance;

	public AbstractAddonBase(EGlow eGlowInstance) {
		this.eGlowInstance = eGlowInstance;

		if (this instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) this, getEGlowInstance());
	}

	public EGlow getEGlowInstance() {
		return this.eGlowInstance;
	}
}