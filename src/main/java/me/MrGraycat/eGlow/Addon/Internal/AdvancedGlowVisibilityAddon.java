package me.mrgraycat.eglow.addon.internal;

import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class AdvancedGlowVisibilityAddon {
	private final BukkitTask bukkitTask;
	private final Set<Material> ignoredBlocks = EnumSet.noneOf(Material.class);
	private final Map<UUID, Location> cache = Collections.synchronizedMap(new HashMap<>());

	public AdvancedGlowVisibilityAddon() {
		// This config is internal only, and is assumed to be correct.
		InputStream resource = EGlow.getInstance().getResource("internal/advanced-visibility-ignored-blocks.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(resource)));
		List<Material> materials = config.getStringList("ignored-blocks").stream()
				.map(Material::matchMaterial)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		getIgnoredBlocks().addAll(materials);

		this.bukkitTask = new BukkitRunnable() {
			@Override
			public void run() {
				Collection<EGlowPlayer> eGlowPlayers = DataManager.getEGlowPlayers();

				List<BiPair<UUID, UUID>> checkedPlayers = new ArrayList<>(eGlowPlayers.size());

				for (EGlowPlayer eGlowPlayer : eGlowPlayers) {
					Player player = eGlowPlayer.getPlayer();
					Location playerLoc = player.getEyeLocation();
					boolean playerIsGlowing = eGlowPlayer.getGlowStatus();

					if (checkLocationCache(eGlowPlayer, playerLoc))
						continue;

					for (Player player1 : Objects.requireNonNull(playerLoc.getWorld()).getPlayers()) {
						if (player1 != player && distance(player1.getEyeLocation(), playerLoc) < 50 && player1.getWorld().equals(playerLoc.getWorld())) {
							EGlowPlayer ePlayerNearby = DataManager.getEGlowPlayer(player1);
							if (ePlayerNearby != null) {
								boolean nearbyIsGlowing = ePlayerNearby.getGlowStatus();
								if (!playerIsGlowing && !nearbyIsGlowing)
									continue;

								BiPair<UUID, UUID> pair = new BiPair<>(eGlowPlayer.getUuid(), ePlayerNearby.getUuid());
								if (checkedPlayers.contains(pair))
									continue; // We've already checked visibility between these two players.

								Location nearbyLoc = ePlayerNearby.getPlayer().getEyeLocation();

								if (isOutsideView(playerLoc, nearbyLoc) && isOutsideView(nearbyLoc, playerLoc)) {
									toggleGlow(eGlowPlayer, ePlayerNearby, false);
									continue;
								} else {
									Raytrace trace = new Raytrace(playerLoc, nearbyLoc);
									boolean hasLineOfSight = trace.hasLineOfSight();
									toggleGlow(eGlowPlayer, ePlayerNearby, hasLineOfSight);
								}
								checkedPlayers.add(pair);
							}
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(EGlow.getInstance(), 0, Math.max(EGlowMainConfig.MainConfig.ADVANCED_GLOW_VISIBILITY_DELAY.getInt(), 10));
	}

	/**
	 * Checks if a location is outside the view of another.
	 * This expects both locations to have a yaw and pitch.
	 *
	 * @param location1 The first location.
	 * @param location2 The second location.
	 * @return Whether the second location can be seen from the first location.
	 */
	private boolean isOutsideView(Location location1, Location location2) {
		Vector vector = location1.toVector().subtract(location2.toVector());
		Vector direction = location1.getDirection();

		double delta = vector.dot(direction);
		return delta > 0;
	}

	/**
	 * Checks if a player's location has changed from the cached value, updating the cache if necessary.
	 *
	 * @param eGlowPlayer    The player whose cache to check against.
	 * @param playerLocation The location to check against.
	 * @return True if the player's location hasn't changed, false otherwise.
	 */
	private boolean checkLocationCache(EGlowPlayer eGlowPlayer, Location playerLocation) {
		UUID uuid = eGlowPlayer.getUuid();
		Location cached = getCache().get(uuid);

		if (cached == null) {
			getCache().put(uuid, playerLocation);
		} else {
			if (cached.equals(playerLocation)) {
				return true;
			} else {
				getCache().replace(uuid, playerLocation);
			}
		}
		return false;
	}

	/**
	 * Toggles glow visibility for two players.
	 *
	 * @param eGlowPlayer1 Player 1.
	 * @param eGlowPlayer2 Player 2.
	 * @param toggle       Whether to enable, or disable glow.
	 */
	private void toggleGlow(EGlowPlayer eGlowPlayer1, EGlowPlayer eGlowPlayer2, boolean toggle) {
		if (toggle) {
			eGlowPlayer1.addGlowTarget(eGlowPlayer2.getPlayer());
			eGlowPlayer2.addGlowTarget(eGlowPlayer1.getPlayer());
		} else {
			eGlowPlayer1.removeGlowTarget(eGlowPlayer2.getPlayer());
			eGlowPlayer2.removeGlowTarget(eGlowPlayer1.getPlayer());
		}
	}

	public void uncachePlayer(UUID uuid) {
		getCache().remove(uuid);
	}

	private int distance(Location start, Location end) {
		return (int) Math.floor(Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getY() - end.getY()), 2) + Math.pow((start.getZ() - end.getZ()), 2)));
	}

	public void shutdown() {
		if (getBukkitTask() != null)
			getBukkitTask().cancel();
		EGlow.getInstance().setAdvancedGlowVisibilityAddon(null);
	}

	@Getter
	public static class Raytrace {
		private final Location origin;
		private final Location target;
		private final Vector direction;

		protected Raytrace(Location origin, Location target) {
			this.origin = origin;
			this.target = target;
			this.direction = target.clone().toVector().subtract(origin.clone().toVector()).normalize();
		}

		protected boolean hasLineOfSight() {
			int distance = distance();

			if (distance() <= 1)
				return true;

			BlockIterator blocks = new BlockIterator(Objects.requireNonNull(getOrigin().getWorld()), getOrigin().toVector(), getDirection(), Math.min(distance, 50));

			while (blocks.hasNext()) {
				Block block = blocks.next();

				if (!block.isLiquid() && !block.isPassable() && !EGlow.getInstance().getAdvancedGlowVisibilityAddon().getIgnoredBlocks().contains(block.getType())) {
					return false;
				}
			}
			return true;
		}

		private int distance() {
			return (int) Math.floor(Math.sqrt(Math.pow((getOrigin().getX() - getTarget().getX()), 2) + Math.pow((getOrigin().getY() - getTarget().getY()), 2) + Math.pow((getOrigin().getZ() - getTarget().getZ()), 2)));
		}
	}
}