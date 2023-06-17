package me.mrgraycat.eglow.addon.internal;

import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.pair.BiPair;
import me.mrgraycat.eglow.util.iterator.BlockIterator;
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

public class AdvancedGlowVisibilityAddon {

	private final BukkitTask runnable;
	private static final int MAX_DISTANCE = 50;
	private static final Set<Material> ignoredBlocks = EnumSet.noneOf(Material.class);

	private final Map<UUID, Location> cache = Collections.synchronizedMap(new HashMap<>());

	static {
		// This config is internal only, and is assumed to be correct.
		InputStream resource = EGlow.getInstance().getResource("internal/AdvancedGlowIgnoreList.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(resource)));
		List<Material> materials = config.getStringList("ignored-blocks").stream()
				.map(Material::matchMaterial)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		ignoredBlocks.addAll(materials);
	}

	public AdvancedGlowVisibilityAddon() {
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				Collection<IEGlowPlayer> ePlayers = DataManager.getGlowPlayers();

				// Nothing is ever done with checkedPlayers
				List<BiPair<UUID, UUID>> checkedPlayers = ePlayers.stream()
						.map(glowPlayer -> {
							Player player = glowPlayer.getPlayer();
							Location eyeLocation = player.getEyeLocation();

							boolean playerIsGlowing = glowPlayer.getGlowStatus();

							if (checkLocationCache(glowPlayer, eyeLocation)) {
								return null;
							}

							Set<BiPair<UUID, UUID>> set = new HashSet<>();

							Objects.requireNonNull(eyeLocation.getWorld()).getPlayers().stream()
									.filter(worldPlayer -> worldPlayer != player && worldPlayer.getWorld().equals(player.getWorld())
											&& distance(worldPlayer.getEyeLocation(), eyeLocation) < MAX_DISTANCE)
									.map(DataManager::getEGlowPlayer)
									.filter(Objects::nonNull)
									.forEach(otherGlowPlayer -> {
										boolean nearbyIsGlowing = otherGlowPlayer.getGlowStatus();
										if (!playerIsGlowing && !nearbyIsGlowing) {
											return;
										}

										BiPair<UUID, UUID> pair = new BiPair<>(glowPlayer.getUuid(), otherGlowPlayer.getUuid());

										if (!set.contains(pair)) {
											Location nearbyLoc = otherGlowPlayer.getPlayer().getEyeLocation();

											if (isOutsideView(eyeLocation, nearbyLoc) && isOutsideView(nearbyLoc, eyeLocation)) {
												toggleGlow(glowPlayer, otherGlowPlayer, false);
											} else {
												Raytrace trace = new Raytrace(eyeLocation, nearbyLoc);
												boolean hasLineOfSight = trace.hasLineOfSight();

												toggleGlow(glowPlayer, otherGlowPlayer, hasLineOfSight);
											}

											set.add(pair);
										}
									});
							return set;
						})
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
			}
		}.runTaskTimerAsynchronously(EGlow.getInstance(), 0,
				Math.max(EGlowMainConfig.MainConfig.ADVANCED_GLOW_VISIBILITY_DELAY.getInt(), 10));
	}

	/**
	 * Checks if a location is outside the view of another.
	 * This expects both locations to have a yaw and pitch.
	 *
	 * @param loc1 The first location.
	 * @param loc2 The second location.
	 * @return Whether the second location can be seen from the first location.
	 */
	private boolean isOutsideView(Location loc1, Location loc2) {
		Vector c = loc1.toVector().subtract(loc2.toVector());
		Vector d = loc1.getDirection();

		double delta = c.dot(d);
		return delta > 0;
	}

	/**
	 * Checks if a player's location has changed from the cached value, updating the cache if necessary.
	 *
	 * @param ePlayer   The player whose cache to check against.
	 * @param playerLoc The location to check against.
	 * @return True if the player's location hasn't changed, false otherwise.
	 */
	private boolean checkLocationCache(IEGlowPlayer ePlayer, Location playerLoc) {
		Location cached = cache.get(ePlayer.getUuid());

		if (cached == null) {
			cache.put(ePlayer.getUuid(), playerLoc);
		} else {
			if (cached.equals(playerLoc)) {
				return true;
			} else {
				cache.replace(ePlayer.getUuid(), playerLoc);
			}
		}
		return false;
	}

	/**
	 * Toggles glow visibility for two players.
	 *
	 * @param p1     Player 1.
	 * @param p2     Player 2.
	 * @param toggle Whether to enable, or disable glow.
	 */
	private void toggleGlow(IEGlowPlayer p1, IEGlowPlayer p2, boolean toggle) {
		if (toggle) {
			p1.addGlowTarget(p2.getPlayer());
			p2.addGlowTarget(p1.getPlayer());
		} else {
			p1.removeGlowTarget(p2.getPlayer());
			p2.removeGlowTarget(p1.getPlayer());
		}
	}

	public void removePlayerFromCache(IEGlowPlayer ePlayer) {
		cache.remove(ePlayer.getUuid());
	}

	private int distance(Location start, Location end) {
		return (int) Math.floor(Math.hypot(
				Math.abs(start.getX() - end.getX()),
				Math.abs(start.getZ() - end.getZ())
		));
	}

	public void shutdown() {
		if (this.runnable != null) {
			this.runnable.cancel();
		}

		EGlow.getInstance().setGlowAddon(null);
	}

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

			if (distance <= 1)
				return true;

			BlockIterator blocks = new BlockIterator(Objects.requireNonNull(origin.getWorld()), origin.toVector(), direction, Math.min(distance, 50));

			while (blocks.hasNext()) {
				Block block = blocks.next();

				if (!block.isLiquid() && !block.isPassable() && !AdvancedGlowVisibilityAddon.ignoredBlocks.contains(block.getType())) {
					return false;
				}
			}
			return true;
		}

		private int distance() {
			return (int) Math.floor(Math.sqrt(
					Math.pow((origin.getX() - target.getX()), 2) +
					Math.pow((origin.getY() - target.getY()), 2) +
					Math.pow((origin.getZ() - target.getZ()), 2))
			);
		}
	}
}