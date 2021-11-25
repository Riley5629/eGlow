package me.MrGraycat.eGlow.Addon.TAB.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.neznamy.tab.platforms.bukkit.event.TabLoadEvent;

public class EGlowTABListenerBukkit implements Listener {
	private EGlow instance;
	
	public EGlowTABListenerBukkit(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
	}
	
	@EventHandler
	public void onTABReloadBukkit(TabLoadEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					getInstance().getTABAddon().loadConfigSettings();
					
					if (getInstance().getTABAddon().getTABNametagPrefixSuffixEnabled() && getInstance().getTABAddon().getTABTeamPacketBlockingEnabled()) {
						for (IEGlowPlayer ePlayer : getInstance().getDataManager().getEGlowPlayers()) {
							if (ePlayer.getFakeGlowStatus() || ePlayer.getGlowStatus())
								getInstance().getTABAddon().updateTABPlayer(ePlayer, ePlayer.getActiveColor());
							continue;
						}
					} else {
						cancel();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(getInstance());
	}
	
	//Getter
	private EGlow getInstance() {
		return this.instance;
	}
		
	//Setter
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}
}
