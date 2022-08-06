package me.MrGraycat.eGlow.Addon.Internal;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.BiPair;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvancedGlowVisibilityAddon {

    private static final double FRUSTUM_SIZE = Math.toRadians(120); // How large the frustum volume is, in radians.
    private static final int MAX_DISTANCE = 50;

    private boolean FORCE_STOP = false;
    private static final List<Material> ignoredBlocks = new ArrayList<>();
    private final Map<UUID, Location> cache = new HashMap<>(); //todo: fix memory leak when players disconnect
    /**
     * Whether a glow check is currently being run.
     */
    private final AtomicBoolean executing = new AtomicBoolean();

    public AdvancedGlowVisibilityAddon() {
        ignoredBlockInit();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getForceStop()) {
                    cancel();
                    EGlow.getInstance().setAdvancedGlowVisibility(null);
                    return;
                }
                if (executing.get()) {
                    // This means either an exception was thrown, or the last executing is still running.
                    // If it's still running, we don't have a way to stop it, so we'll let it finish to avoid any conflicts.
                    // The odds of this actually happening are very low, as it would mean the calculation took more than half a second.
                    return;
                }
                executing.set(true);

                Collection<IEGlowPlayer> ePlayers = DataManager.getEGlowPlayers();

                List<BiPair<UUID, UUID>> checkedPlayers = new ArrayList<>(ePlayers.size());

                for (IEGlowPlayer ePlayer : ePlayers) {
                    Player player = ePlayer.getPlayer();
                    Location playerLoc = player.getEyeLocation();

                    if (checkLocationCache(ePlayer, playerLoc))
                        continue;

                    List<IEGlowPlayer> nearbyEPlayers = new ArrayList<>();
                    for (Player p : playerLoc.getWorld().getPlayers()) {
                        if (p != player && distance(p.getEyeLocation(), playerLoc) < MAX_DISTANCE) {
                            nearbyEPlayers.add(DataManager.getEGlowPlayer(p));
                        }
                    }

                    Vector playerDir = playerLoc.getDirection();

                    for (IEGlowPlayer nearby : nearbyEPlayers) {
                        BiPair<UUID, UUID> pair = new BiPair<>(ePlayer.getUUID(), nearby.getUUID());
                        if (checkedPlayers.contains(pair)) {
                            continue; // We've already checked visibility between these two players.
                        }

                        Location nearbyLoc = nearby.getPlayer().getEyeLocation();
                        Vector nearbyDir = nearbyLoc.getDirection();

                        double angle = nearbyDir.angle(playerDir);
                        if (angle > FRUSTUM_SIZE) {
                            toggleGlow(ePlayer, nearby, false);
                            continue;
                        } else {
                            Raytrace trace = new Raytrace(playerLoc, nearbyLoc);
                            boolean hasLineOfSight = trace.hasLineOfSight();
                            toggleGlow(ePlayer, nearby, hasLineOfSight);
                        }
                        checkedPlayers.add(pair);
                    }
                }
                executing.set(false);
            }
        }.runTaskTimerAsynchronously(EGlow.getInstance(), 0, Math.max(EGlowMainConfig.MainConfig.ADVANCED_GLOW_VISIBILITY_DELAY.getInt(), 10));
    }

    /**
     * Checks if a players location has changed from the cached value, updating the cache if necessary.
     *
     * @param ePlayer   The player whose cache to check against.
     * @param playerLoc The location to check against.
     * @return True if the players location hasn't changed, false otherwise.
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

    public void shutdown() {
        setForceStop();
    }

    public void ignoredBlockInit() {
        ignoredBlocks.add(Material.valueOf("GLASS"));
        ignoredBlocks.add(Material.valueOf("ICE"));
        ignoredBlocks.add(Material.valueOf("COCOA"));
        ignoredBlocks.add(Material.valueOf("END_ROD"));
        ignoredBlocks.add(Material.valueOf("FLOWER_POT"));
        ignoredBlocks.add(Material.valueOf("LADDER"));
        ignoredBlocks.add(Material.valueOf("SPRUCE_FENCE"));
        ignoredBlocks.add(Material.valueOf("BIRCH_FENCE"));
        ignoredBlocks.add(Material.valueOf("JUNGLE_FENCE"));
        ignoredBlocks.add(Material.valueOf("ACACIA_FENCE"));
        ignoredBlocks.add(Material.valueOf("DARK_OAK_FENCE"));
        ignoredBlocks.add(Material.valueOf("SPRUCE_FENCE_GATE"));
        ignoredBlocks.add(Material.valueOf("BIRCH_FENCE_GATE"));
        ignoredBlocks.add(Material.valueOf("JUNGLE_FENCE_GATE"));
        ignoredBlocks.add(Material.valueOf("ACACIA_FENCE_GATE"));
        ignoredBlocks.add(Material.valueOf("DARK_OAK_FENCE_GATE"));
        ignoredBlocks.add(Material.valueOf("IRON_DOOR"));
        ignoredBlocks.add(Material.valueOf("SPRUCE_DOOR"));
        ignoredBlocks.add(Material.valueOf("BIRCH_DOOR"));
        ignoredBlocks.add(Material.valueOf("JUNGLE_DOOR"));
        ignoredBlocks.add(Material.valueOf("ACACIA_DOOR"));
        ignoredBlocks.add(Material.valueOf("DARK_OAK_DOOR"));
        ignoredBlocks.add(Material.valueOf("IRON_TRAPDOOR"));

        if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 13) {
            ignoredBlocks.add(Material.valueOf("MOB_SPAWNER"));
            ignoredBlocks.add(Material.valueOf("SKULL"));
            ignoredBlocks.add(Material.valueOf("FENCE"));
            ignoredBlocks.add(Material.valueOf("IRON_FENCE"));
            ignoredBlocks.add(Material.valueOf("FENCE_GATE"));
            ignoredBlocks.add(Material.valueOf("NETHER_FENCE"));
            ignoredBlocks.add(Material.valueOf("TRAP_DOOR"));
            ignoredBlocks.add(Material.valueOf("LEAVES"));
            ignoredBlocks.add(Material.valueOf("LEAVES_2"));
            ignoredBlocks.add(Material.valueOf("CARPET"));
            ignoredBlocks.add(Material.valueOf("STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("STAINED_GLASS_PANE"));
        } else {
            ignoredBlocks.add(Material.valueOf("OAK_FENCE"));
            ignoredBlocks.add(Material.valueOf("NETHER_BRICK_FENCE"));
            ignoredBlocks.add(Material.valueOf("OAK_FENCE_GATE"));
            ignoredBlocks.add(Material.valueOf("OAK_DOOR"));
            ignoredBlocks.add(Material.valueOf("OAK_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("SPRUCE_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("BIRCH_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("JUNGLE_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("ACACIA_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("DARK_OAK_TRAPDOOR"));
            ignoredBlocks.add(Material.valueOf("OAK_LEAVES"));
            ignoredBlocks.add(Material.valueOf("SPRUCE_LEAVES"));
            ignoredBlocks.add(Material.valueOf("BIRCH_LEAVES"));
            ignoredBlocks.add(Material.valueOf("JUNGLE_LEAVES"));
            ignoredBlocks.add(Material.valueOf("ACACIA_LEAVES"));
            ignoredBlocks.add(Material.valueOf("DARK_OAK_LEAVES"));
            ignoredBlocks.add(Material.valueOf("PLAYER_HEAD"));
            ignoredBlocks.add(Material.valueOf("ZOMBIE_HEAD"));
            ignoredBlocks.add(Material.valueOf("SKELETON_SKULL"));
            ignoredBlocks.add(Material.valueOf("CREEPER_HEAD"));
            ignoredBlocks.add(Material.valueOf("WITHER_SKELETON_SKULL"));
            ignoredBlocks.add(Material.valueOf("DRAGON_HEAD"));
            ignoredBlocks.add(Material.valueOf("PISTON_HEAD"));
            ignoredBlocks.add(Material.valueOf("ZOMBIE_WALL_HEAD"));
            ignoredBlocks.add(Material.valueOf("SKELETON_WALL_SKULL"));
            ignoredBlocks.add(Material.valueOf("PLAYER_WALL_HEAD"));
            ignoredBlocks.add(Material.valueOf("CREEPER_WALL_HEAD"));
            ignoredBlocks.add(Material.valueOf("WITHER_SKELETON_WALL_SKULL"));
            ignoredBlocks.add(Material.valueOf("DRAGON_WALL_HEAD"));
            ignoredBlocks.add(Material.valueOf("WHITE_CARPET"));
            ignoredBlocks.add(Material.valueOf("ORANGE_CARPET"));
            ignoredBlocks.add(Material.valueOf("MAGENTA_CARPET"));
            ignoredBlocks.add(Material.valueOf("LIGHT_BLUE_CARPET"));
            ignoredBlocks.add(Material.valueOf("YELLOW_CARPET"));
            ignoredBlocks.add(Material.valueOf("LIME_CARPET"));
            ignoredBlocks.add(Material.valueOf("PINK_CARPET"));
            ignoredBlocks.add(Material.valueOf("GRAY_CARPET"));
            ignoredBlocks.add(Material.valueOf("LIGHT_GRAY_CARPET"));
            ignoredBlocks.add(Material.valueOf("CYAN_CARPET"));
            ignoredBlocks.add(Material.valueOf("PURPLE_CARPET"));
            ignoredBlocks.add(Material.valueOf("BLUE_CARPET"));
            ignoredBlocks.add(Material.valueOf("BROWN_CARPET"));
            ignoredBlocks.add(Material.valueOf("GREEN_CARPET"));
            ignoredBlocks.add(Material.valueOf("RED_CARPET"));
            ignoredBlocks.add(Material.valueOf("BLACK_CARPET"));
            ignoredBlocks.add(Material.valueOf("GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("WHITE_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("ORANGE_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("MAGENTA_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("LIGHT_BLUE_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("YELLOW_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("LIME_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("PINK_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("GRAY_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("LIGHT_GRAY_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("CYAN_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("PURPLE_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("BLUE_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("BROWN_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("GREEN_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("RED_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("BLACK_STAINED_GLASS"));
            ignoredBlocks.add(Material.valueOf("WHITE_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("ORANGE_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("MAGENTA_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("YELLOW_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("LIME_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("PINK_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("GRAY_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("LIGHT_GRAY_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("CYAN_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("PURPLE_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("BLUE_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("BROWN_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("GREEN_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("RED_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("BLACK_STAINED_GLASS_PANE"));
            ignoredBlocks.add(Material.valueOf("SPAWNER"));
            ignoredBlocks.add(Material.valueOf("CONDUIT"));
            ignoredBlocks.add(Material.valueOf("IRON_BARS"));
            ignoredBlocks.add(Material.valueOf("SEA_PICKLE"));
            ignoredBlocks.add(Material.valueOf("TURTLE_EGG"));

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14)
                ignoredBlocks.add(Material.valueOf("SCAFFOLDING"));

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15)
                ignoredBlocks.add(Material.valueOf("HONEY_BLOCK"));

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 16)
                ignoredBlocks.add(Material.valueOf("CHAIN"));
                ignoredBlocks.add(Material.valueOf("CRIMSON_FENCE"));
                ignoredBlocks.add(Material.valueOf("WARPED_FENCE"));
                ignoredBlocks.add(Material.valueOf("CRIMSON_FENCE_GATE"));
                ignoredBlocks.add(Material.valueOf("WARPED_FENCE_GATE"));
                ignoredBlocks.add(Material.valueOf("CRIMSON_DOOR"));
                ignoredBlocks.add(Material.valueOf("WARPED_DOOR"));
                ignoredBlocks.add(Material.valueOf("CRIMSON_TRAPDOOR"));
                ignoredBlocks.add(Material.valueOf("WARPED_TRAPDOOR"));
                ignoredBlocks.add(Material.valueOf("AZALEA_LEAVES"));
                ignoredBlocks.add(Material.valueOf("FLOWERING_AZALEA_LEAVES"));
                ignoredBlocks.add(Material.valueOf("CRIMSON_FENCE_GATE"));
                ignoredBlocks.add(Material.valueOf("WARPED_FENCE_GATE"));

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 17) {
                ignoredBlocks.add(Material.valueOf("SMALL_AMETHYST_BUD"));
                ignoredBlocks.add(Material.valueOf("MEDIUM_AMETHYST_BUD"));
                ignoredBlocks.add(Material.valueOf("LARGE_AMETHYST_BUD"));
                ignoredBlocks.add(Material.valueOf("BIG_DRIPLEAF"));
                ignoredBlocks.add(Material.valueOf("BIG_DRIPLEAF_STEM"));
                ignoredBlocks.add(Material.valueOf("LIGHTNING_ROD"));
                ignoredBlocks.add(Material.valueOf("TINTED_GLASS"));
                ignoredBlocks.add(Material.valueOf("CANDLE"));
                ignoredBlocks.add(Material.valueOf("WHITE_CANDLE"));
                ignoredBlocks.add(Material.valueOf("ORANGE_CANDLE"));
                ignoredBlocks.add(Material.valueOf("MAGENTA_CANDLE"));
                ignoredBlocks.add(Material.valueOf("LIGHT_BLUE_CANDLE"));
                ignoredBlocks.add(Material.valueOf("YELLOW_CANDLE"));
                ignoredBlocks.add(Material.valueOf("LIME_CANDLE"));
                ignoredBlocks.add(Material.valueOf("PINK_CANDLE"));
                ignoredBlocks.add(Material.valueOf("GRAY_CANDLE"));
                ignoredBlocks.add(Material.valueOf("LIGHT_GRAY_CANDLE"));
                ignoredBlocks.add(Material.valueOf("CYAN_CANDLE"));
                ignoredBlocks.add(Material.valueOf("PURPLE_CANDLE"));
                ignoredBlocks.add(Material.valueOf("BLUE_CANDLE"));
                ignoredBlocks.add(Material.valueOf("BROWN_CANDLE"));
                ignoredBlocks.add(Material.valueOf("GREEN_CANDLE"));
                ignoredBlocks.add(Material.valueOf("RED_CANDLE"));
                ignoredBlocks.add(Material.valueOf("BLACK_CANDLE"));
                ignoredBlocks.add(Material.valueOf("MOSS_CARPET"));
            }

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >=19)
                ignoredBlocks.add(Material.valueOf("MANGROVE_FENCE"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_FENCE_GATE"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_LEAVES"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_TRAPDOOR"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_DOOR"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_FENCE_GATE"));
                ignoredBlocks.add(Material.valueOf("MANGROVE_ROOTS"));
        }
    }

    private int distance(Location start, Location end) {
        return (int) Math.floor(Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getY() - end.getY()), 2) + Math.pow((start.getZ() - end.getZ()), 2)));
    }

    private void setForceStop() {
        this.FORCE_STOP = true;
    }

    private boolean getForceStop() {
        return this.FORCE_STOP;
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
            if (origin.equals(target))
                return true;

            BlockIterator blocks = new BlockIterator(origin.getWorld(), origin.toVector(), direction, 0.0, distance());

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