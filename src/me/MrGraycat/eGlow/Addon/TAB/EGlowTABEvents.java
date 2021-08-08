package me.MrGraycat.eGlow.Addon.TAB;

import java.util.ConcurrentModificationException;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;

public class EGlowTABEvents implements Listener { 
	
	private EGlow instance;
	
	public EGlowTABEvents(EGlow instance) {
		setInstance(instance);
		getInstance().getServer().getPluginManager().registerEvents(this, getInstance());
	}
	
	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (getInstance().getTABAddon() != null) {
						if (getInstance().getTABAddon().installedOnBukkit() && !getInstance().getTABAddon().isUnlimitedNametagModeEnabled()) {
							cancel();
							return;
						}				
						
						if (event.getPlayer() != null) {
							if (getInstance().getTABAddon().installedOnBukkit() && TABAPI.getPlayer(event.getPlayerUUID()) != null) {
								TabPlayer tabPlayer = (TabPlayer) TABAPI.getPlayer(event.getPlayerUUID()); 
								String tagPrefix = "";
								String color = (event.getChatColor().equals(ChatColor.RESET)) ? "" : event.getColor();
								
								try {tagPrefix = tabPlayer.getOriginalValue(EnumProperty.TAGPREFIX);} catch(Exception ex) {tagPrefix = "";}
								
								try {
									if (!EGlowMainConfig.OptionAdvancedTABIntegration()) {
										tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, (!tagPrefix.isEmpty()) ? tagPrefix + color : color);
									} else {
										String customTagName = "";
										
										try {customTagName = tabPlayer.getOriginalValue(EnumProperty.CUSTOMTAGNAME);} catch(Exception ex) {customTagName = event.getPlayer().getName();}

										tabPlayer.setValueTemporarily(EnumProperty.CUSTOMTAGNAME, tagPrefix.replace("%eglow_glowcolor%", "") + customTagName);
										tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, color);
									}
								} catch (IllegalStateException e) {
									//Ignored :I
								}
							}
						}
					}
					
					if (getInstance().getDebugUtil().onBungee())
						getInstance().getDataManager().TABProxyUpdateRequest(event.getPlayer(), event.getColor());	
					
				} catch (ConcurrentModificationException ex2) {
					//Ignore cause by updating to fast
				} catch (Exception ex) {
					ChatUtil.reportError(ex);
				}
			}
		}.runTaskAsynchronously(getInstance());		
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if (getInstance().getTABAddon() != null)
			getInstance().getTABAddon().removePlayerGroup(event.getPlayer());
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