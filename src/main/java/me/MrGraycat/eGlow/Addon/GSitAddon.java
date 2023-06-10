package me.MrGraycat.eGlow.Addon;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import lombok.Getter;
import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class GSitAddon implements Listener {

	@Getter
	Set<Player> posingPlayers = new HashSet<>();

	public GSitAddon() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
	}

	@EventHandler
	public void poseEvent(PlayerPoseEvent event) {
		Player player = event.getPlayer();
		checkGlow(player, true);
	}

	@EventHandler
	public void unPoseEvent(PlayerGetUpPoseEvent event) {
		Player player = event.getPlayer();
		checkGlow(player, false);
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		checkGlow(event.getPlayer(), false);
	}

	@EventHandler
	public void onGlowChange(GlowColorChangeEvent event) {
		Player player = event.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {
				checkGlow(player, GSitAPI.isPosing(player));
			}
		}.runTaskLater(EGlow.getInstance(), 5L);
	}

	private void checkGlow(Player player, boolean isPosing) {
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (isPosing) {
			if (eGlowPlayer.isGlowing()) {
				getPosingPlayers().add(player);
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.ANIMATION, false);
				eGlowPlayer.disableGlow(false);
			}
		} else {
			if (getPosingPlayers().remove(player)) {
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false);
				eGlowPlayer.activateGlow();
			}
		}
	}
}