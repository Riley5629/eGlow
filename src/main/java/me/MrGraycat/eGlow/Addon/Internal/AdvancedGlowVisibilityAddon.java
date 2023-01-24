package me.MrGraycat.eGlow.Addon.Internal;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdvancedGlowVisibilityAddon {
    private final BukkitTask runnable;
    private static final int MAX_DISTANCE = 50;
    private static final Set<Material> ignoredBlocks = EnumSet.noneOf(Material.class);

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

    private final Map<UUID, Location> cache = Collections.synchronizedMap(new HashMap<>());

    public AdvancedGlowVisibilityAddon() {
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Collection<IEGlowPlayer> ePlayers = DataManager.getEGlowPlayers();

                List<BiPair<UUID, UUID>> checkedPlayers = new ArrayList<>(ePlayers.size());

                for (IEGlowPlayer ePlayer : ePlayers) {
                    Player player = ePlayer.getPlayer();
                    Location playerLoc = player.getEyeLocation();
                    boolean playerIsGlowing = ePlayer.getGlowStatus();

                    if (checkLocationCache(ePlayer, playerLoc))
                        continue;

                    for (Player p : Objects.requireNonNull(playerLoc.getWorld()).getPlayers()) {
                        if (p != player && distance(p.getEyeLocation(), playerLoc) < MAX_DISTANCE && p.getWorld().equals(playerLoc.getWorld())) {
                            IEGlowPlayer ePlayerNearby = DataManager.getEGlowPlayer(p);
                            if (ePlayerNearby != null) {
                                boolean nearbyIsGlowing = ePlayerNearby.getGlowStatus();
                                if (!playerIsGlowing && !nearbyIsGlowing)
                                    continue;

                                BiPair<UUID, UUID> pair = new BiPair<>(ePlayer.getUUID(), ePlayerNearby.getUUID());
                                if (checkedPlayers.contains(pair)) {
                                    continue; // We've already checked visibility between these two players.
                                }

                                Location nearbyLoc = ePlayerNearby.getPlayer().getEyeLocation();

                                if (isOutsideView(playerLoc, nearbyLoc) && isOutsideView(nearbyLoc, playerLoc)) {
                                    toggleGlow(ePlayer, ePlayerNearby, false);
                                    continue;
                                } else {
                                    Raytrace trace = new Raytrace(playerLoc, nearbyLoc);
                                    boolean hasLineOfSight = trace.hasLineOfSight();
                                    toggleGlow(ePlayer, ePlayerNearby, hasLineOfSight);
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
        Location cached = cache.get(ePlayer.getUUID());

        if (cached == null) {
            cache.put(ePlayer.getUUID(), playerLoc);
        } else {
            if (cached.equals(playerLoc)) {
                return true;
            } else {
                cache.replace(ePlayer.getUUID(), playerLoc);
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

    public void uncachePlayer(IEGlowPlayer ePlayer) {
        cache.remove(ePlayer.getUUID());
    }

    private int distance(Location start, Location end) {
        return (int) Math.floor(Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getY() - end.getY()), 2) + Math.pow((start.getZ() - end.getZ()), 2)));
    }

    public void shutdown() {
        if (this.runnable != null)
            this.runnable.cancel();
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

            if (distance() <= 1)
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
            return (int) Math.floor(Math.sqrt(Math.pow((origin.getX() - target.getX()), 2) + Math.pow((origin.getY() - target.getY()), 2) + Math.pow((origin.getZ() - target.getZ()), 2)));
        }
    }
}