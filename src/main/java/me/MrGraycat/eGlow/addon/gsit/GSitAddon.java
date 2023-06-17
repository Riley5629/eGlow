package me.MrGraycat.eGlow.addon.gsit;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import me.MrGraycat.eGlow.addon.GlowAddon;
import me.MrGraycat.eGlow.api.event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.Common.GlowDisableReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class GSitAddon extends GlowAddon implements Listener {

	private final Set<Player> posingPlayers = new HashSet<>();

	public GSitAddon(EGlow instance) {
		super(instance);

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

		runTaskLater(() -> {
			checkGlow(player, GSitAPI.isPosing(player));
		}, 5L);
	}

	private void checkGlow(Player player, boolean isPosing) {
		IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (isPosing) {
			if (eGlowPlayer.isGlowing()) {
				posingPlayers.add(player);
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.ANIMATION, false);
				eGlowPlayer.disableGlow(false);
			}
		} else {
			if (posingPlayers.remove(player)) {
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false);
				eGlowPlayer.activateGlow();
			}
		}
	}

	// Why is this static?
	public static boolean isPlayerPosing(Player player) {
		return GSitAPI.isPosing(player);
	}
}