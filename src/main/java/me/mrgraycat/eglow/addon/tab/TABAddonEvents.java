package me.mrgraycat.eglow.addon.tab;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class TABAddonEvents implements Listener {
	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		EGlow.getInstance().getTabAddon().requestTABPlayerUpdate(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		EGlow.getInstance().getTabAddon().requestTABPlayerUpdate(event.getPlayer());
	}
}
