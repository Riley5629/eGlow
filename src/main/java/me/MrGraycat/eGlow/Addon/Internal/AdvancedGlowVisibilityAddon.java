package me.MrGraycat.eGlow.Addon.Internal;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdvancedGlowVisibilityAddon {

    private static List<Material> ignoredBlocks = new ArrayList<>();
    private ConcurrentHashMap<UUID, Location> cache = new ConcurrentHashMap<>();

    public AdvancedGlowVisibilityAddon() {
        ignoredBlockInit();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (DataManager.getEGlowPlayers().isEmpty())
                    return;

                for (IEGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
                    Player player = ePlayer.getPlayer();
                    Location playerLoc = player.getLocation().getBlock().getLocation();

                    if (cache.containsKey(player.getUniqueId())) {
                        if (cache.get(player.getUniqueId()).equals(playerLoc))
                            return;
                        cache.replace(player.getUniqueId(), playerLoc);
                    } else {
                        cache.put(player.getUniqueId(), playerLoc);
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
        }.runTaskTimer(EGlow.getInstance(), 0, 10);
    }

    public void ignoredBlockInit() {
        ignoredBlocks.add(Material.valueOf("ICE"));
        ignoredBlocks.add(Material.valueOf("COCOA"));
        ignoredBlocks.add(Material.valueOf("END_ROD"));
        ignoredBlocks.add(Material.valueOf("FLOWER_POT"));
        ignoredBlocks.add(Material.valueOf("LADDER"));

        if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 13) {
            ignoredBlocks.add(Material.valueOf("MOB_SPAWNER"));
            ignoredBlocks.add(Material.valueOf("SKULL"));
        } else {
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

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 17) {
                ignoredBlocks.add(Material.valueOf("BIG_DRIPLEAF"));
                ignoredBlocks.add(Material.valueOf("BIG_DRIPLEAF_STEM"));
                ignoredBlocks.add(Material.valueOf("LIGHTNING_ROD"));
                ignoredBlocks.add(Material.valueOf("MOSS_CARPET"));
            }

            if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >=18)
                ignoredBlocks.add(Material.valueOf("MANGROVE_ROOTS"));
        }
    }

    private int distance(Location start, Location end) {
        return (int) Math.floor(Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getY() - end.getY()), 2) + Math.pow((start.getZ() - end.getZ()), 2)));
    }

    public static class Raytrace {
        Location origin, target;
        Vector direction;

        Raytrace(Location origin, Location target) {
            this.origin = origin;
            this.target = target;
            this.direction = target.clone().toVector().subtract(origin.clone().toVector()).normalize();
        }

        /*public void materials() {
            Material.ICE; //Universal
            Material.valueOf("MOB_SPAWNER");//1.13-
            Material.SPAWNER //1.13+

            //Block data instanceof AmethystCluster //1.17+
            Material.BIG_DRIPLEAF //1.17+
            Material.BIG_DRIPLEAF_STEM //1.17+
            //Block data instanceof candle //1.17+
            //contains carpet
            Material.CHAIN //1.16+
            Material.COCOA //Universal
            Material.CONDUIT //1.13+
            Material.END_ROD //Universal
            //contains fence
            Material.FLOWER_POT //Universal
            Material.HONEY_BLOCK //1.15+
            Material.IRON_BARS // 1.13+
            // Don't know the 'old name' contained under fence 1.13-
            Material.LADDER //Universal
            Material.LIGHTNING_ROD //1.17
            //contains head old skull //1.13+
            Material.valueOf("SKULL"); //1.13-
            Material.MOSS_CARPET //1.17+
            Material.SCAFFOLDING //1.14+
            Material.SEA_PICKLE //1.13+
            Material.TURTLE_EGG //1.13+
            //contains glass
            //contains door
            //contains fence_gate
            //contains leaves
        }*/

        /*  private List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength) {
    if (maxDistance > 120)
      maxDistance = 120;
    ArrayList<Block> blocks = new ArrayList<>();
    BlockIterator<Block> blockIterator = new BlockIterator(this, maxDistance);
    while (blockIterator.hasNext()) {
      Block block = blockIterator.next();
      blocks.add(block);
      if (maxLength != 0 && blocks.size() > maxLength)
        blocks.remove(0);
      Material material = block.getType();
      if ((transparent == null) ?
        !material.equals(Material.AIR) :

        !transparent.contains(material))
        break;
    }
    return blocks;
  }*/
        public boolean hasLineOfSight() {
            if (origin.equals(target))
                return true;

            BlockIterator blocks = new BlockIterator(Objects.requireNonNull(origin.getWorld()), origin.toVector(), direction, 0.0, distance());

            while(blocks.hasNext()) {
                Block block = blocks.next();
                String blockName = block.getType().name();

                if (!block.isLiquid() && !block.isPassable() &&
                    !AdvancedGlowVisibilityAddon.ignoredBlocks.contains(block.getType()) &&
                    !blockName.contains("FENCE") && !blockName.contains("GLASS") && !blockName.contains("DOOR") && !blockName.contains("LEAVES") && !blockName.contains("FENCE_GATE") && !blockName.contains("HEAD") && !blockName.contains("CARPET") && !blockName.contains("CANDLE")) {
                    return false;
                }

                if (blockName.contains("AMETHYST") && blockName.contains("BLOCK")) {
                    return true;
                }
            }

            return true;
        }

        private int distance() {
            return (int) Math.floor(Math.sqrt(Math.pow((origin.getX() - target.getX()), 2) + Math.pow((origin.getY() - target.getY()), 2) + Math.pow((origin.getZ() - target.getZ()), 2)));
        }
    }


            /*or (int i = 0; i < distance() ; i++) {
                origin.add(direction);
                Block block = origin.getBlock();

                if (!block.getType().equals(Material.AIR))
                    return false;
            }
            return true;*/

            /*   public boolean traceLocation(Location g, Location l, Player p) {
        double x = g.getX()-l.getX(), y = g.getY()-l.getY(), z = g.getZ()-l.getZ();
        Vector direction = new Vector(x, y, z).normalize();
        p.sendMessage(""+g.distanceSquared(l));
        for (int i = 0; i < 15; i++) {
            l.add(direction);
            if (l.getBlock().getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }*/

    /*public class Raytrace {
        //origin = start position
        //direction = direction in which the raytrace will go
        Vector origin, direction;

        Raytrace(Vector origin, Vector direction) {
            this.origin = origin;
            this.direction = direction;
        }

        //get a point on the raytrace at X blocks away
        public Vector getPosition(double blocksAway) {
            return origin.clone().add(direction.clone().multiply(blocksAway));
        }

        //checks if a position is on contained within the position
        public boolean isOnLine(Vector position) {
            double t = (position.getX() - origin.getX()) / direction.getX();
            ;
            if (position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ())) {
                return true;
            }
            return false;
        }

        //get all postions on a raytrace
        public ArrayList<Vector> traverse(double blocksAway, double accuracy) {
            ArrayList<Vector> positions = new ArrayList<>();
            for (double d = 0; d <= blocksAway; d += accuracy) {
                positions.add(getPosition(d));
            }
            return positions;
        }

        //intersection detection for current raytrace with return
        public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);
            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return position;
                }
            }
            return null;
        }

        //intersection detection for current raytrace
        public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);
            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return true;
                }
            }
            return false;
        }

        //general intersection detection
        public boolean intersects(Vector position, Vector min, Vector max) {
            if (position.getX() < min.getX() || position.getX() > max.getX()) {
                return false;
            } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
                return false;
            } else if (position.getZ() < min.getZ() || position.getZ() > max.getZ()) {
                return false;
            }
            return true;
        }

        //debug
        public void highlight(World world, double blocksAway, double accuracy){
            for(Vector position : traverse(blocksAway,accuracy)){
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, position.toLocation(world), 5);
            }
        }
    }*/
}