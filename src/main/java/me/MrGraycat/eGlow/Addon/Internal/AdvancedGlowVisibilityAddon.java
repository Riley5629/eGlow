package me.MrGraycat.eGlow.Addon.Internal;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdvancedGlowVisibilityAddon {

    private static final Set<Material> ignoredBlocks = EnumSet.noneOf(Material.class);

    static {
        // This config is internal only, and is assumed to be correct.
        InputStream resource = EGlow.getInstance().getResource("internal/AdvancedGlowIgnoreList.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
        List<Material> materials = config.getStringList("ignored-blocks").stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ignoredBlocks.addAll(materials);
    }

    private boolean FORCE_STOP = false;
    private ConcurrentHashMap<UUID, Location> cache = new ConcurrentHashMap<>();

    public AdvancedGlowVisibilityAddon() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getForceStop()) {
                    cancel();
                    EGlow.getInstance().setAdvancedGlowVisibility(null);
                }

                for (IEGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
                    Player player = ePlayer.getPlayer();
                    Location playerLoc = player.getLocation();

                    if (cache.containsKey(ePlayer.getUUID())) {
                        if (cache.get(ePlayer.getUUID()).equals(playerLoc))
                            continue;
                        cache.replace(ePlayer.getUUID(), playerLoc);
                    } else {
                        cache.put(ePlayer.getUUID(), playerLoc);
                    }

                    List<Player> players = Objects.requireNonNull(playerLoc.getWorld()).getPlayers();
                    List<Player> nearbyPlayers = players.stream().filter(p -> distance(p.getLocation(), playerLoc) < 50).collect(Collectors.toList());

                    for (Player nearby : nearbyPlayers) {
                        Raytrace trace = new Raytrace(player.getEyeLocation(), nearby.getEyeLocation());
                        IEGlowPlayer target = DataManager.getEGlowPlayer(nearby);

                        if (trace.hasLineOfSight()) {
                            ePlayer.addGlowTarget(nearby);
                            target.addGlowTarget(player);
                        } else {
                            ePlayer.removeGlowTarget(nearby);
                            target.removeGlowTarget(player);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(EGlow.getInstance(), 0, Math.max(EGlowMainConfig.MainConfig.ADVANCED_GLOW_VISIBILITY_DELAY.getInt(), 10));
    }

    public void shutdown() {
        setForceStop();
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
        Location origin, target;
        Vector direction;

        Raytrace(Location origin, Location target) {
            this.origin = origin;
            this.target = target;
            this.direction = target.clone().toVector().subtract(origin.clone().toVector()).normalize();
        }

        public boolean hasLineOfSight() {
            if (origin.equals(target))
                return true;

            BlockIterator blocks = new BlockIterator(Objects.requireNonNull(origin.getWorld()), origin.toVector(), direction, 0.0, distance());

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