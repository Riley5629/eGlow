package me.mrgraycat.eglow.addon;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
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

	//TODO is this needed?
	@EventHandler
	public void onGlowChange(GlowColorChangeEvent event) {
		Player player = event.getPlayer();

		runTaskLater(() -> {
			checkGlow(player, isPlayerPosing(player));
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

	public boolean isPlayerPosing(Player player) {
		return GSitAPI.isPosing(player);
	}
}