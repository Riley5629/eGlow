package me.mrgraycat.eglow.addon;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowDisableReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GSitAddon extends AbstractAddonBase implements Listener {

	private final Set<Player> posingPlayers = new HashSet<>();

	public GSitAddon(EGlow eGlowInstance) {
		super(eGlowInstance);
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

	//TODO check if this event is needed
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
		EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(player);

		if (isPosing) {
			if (eGlowPlayer.isGlowing()) {
				getPosingPlayers().add(player);
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.ANIMATION);
				eGlowPlayer.disableGlow(false);
			}
		} else {
			if (getPosingPlayers().remove(player)) {
				eGlowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
				eGlowPlayer.activateGlow();
			}
		}
	}
}