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
	
	@EventHandler
	public void onColorChange(GlowColorChangeEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (EGlow.getTABAddon() != null) {
						if (EGlow.getTABAddon().installedOnBukkit() && !EGlow.getTABAddon().isUnlimitedNametagModeEnabled()) {
							cancel();
							return;
						}				
						
						if (e.getPlayer() != null) {
							if (EGlow.getTABAddon().installedOnBukkit() && TABAPI.getPlayer(e.getPlayerUUID()) != null) {
								TabPlayer tabPlayer = (TabPlayer) TABAPI.getPlayer(e.getPlayerUUID()); 
								String tagPrefix = "";
								String color = (e.getChatColor().equals(ChatColor.RESET)) ? "" : e.getColor();
								
								try {tagPrefix = tabPlayer.getOriginalValue(EnumProperty.TAGPREFIX);} catch(Exception ex) {tagPrefix = "";}
								
								try {
									if (!EGlowMainConfig.OptionAdvancedTABIntegration()) {
										tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, (!tagPrefix.isEmpty()) ? tagPrefix + color : color);
									} else {
										String customTagName = "";
										
										try {customTagName = tabPlayer.getOriginalValue(EnumProperty.CUSTOMTAGNAME);} catch(Exception ex) {customTagName = e.getPlayer().getName();}

										tabPlayer.setValueTemporarily(EnumProperty.CUSTOMTAGNAME, tagPrefix.replace("%eglow_glowcolor%", "") + customTagName);
										tabPlayer.setValueTemporarily(EnumProperty.TAGPREFIX, color);
									}
								} catch (IllegalStateException e) {
									//Ignored :I
								}
							}
						}
					}
					
					if (EGlow.getDebugUtil().onBungee())
						EGlow.getDataManager().TABProxyUpdateRequest(e.getPlayer(), e.getColor());	
					
				} catch (ConcurrentModificationException ex2) {
					//Ignore cause by updating to fast
				} catch (Exception ex) {
					ChatUtil.reportError(ex);
				}
			}
		}.runTaskAsynchronously(EGlow.getInstance());		
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if (EGlow.getTABAddon() != null)
			EGlow.getTABAddon().removePlayerGroup(e.getPlayer());
	}
}