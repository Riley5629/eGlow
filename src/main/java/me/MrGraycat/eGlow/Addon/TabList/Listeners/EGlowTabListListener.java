package me.MrGraycat.eGlow.Addon.TabList.Listeners;

import hu.montlikadani.tablist.tablist.TabText;
import hu.montlikadani.tablist.tablist.groups.TeamHandler;
import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Addon.TabList.TabListAddon;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ConcurrentModificationException;

public class EGlowTabListListener implements Listener {

    @EventHandler
    public void onColorChange(GlowColorChangeEvent event) {
        Player player = event.getPlayer();
        ChatColor chatColor = event.getChatColor();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    TabListAddon tablistAddon = EGlow.getInstance().getTablistAddon();

                    if (tablistAddon != null) {
                        IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

                        if (ePlayer == null)
                            return;

                        tablistAddon.getAPI().getUser(player).ifPresent(user -> {
                           TeamHandler team = user.getGroupPlayer().getGroup();

                           if (team != null) {
                               String prefix = team.prefix.getPlainText();
                               team.prefix = TabText.parseFromText(prefix + chatColor);
                           }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(EGlow.getInstance());
    }
}
