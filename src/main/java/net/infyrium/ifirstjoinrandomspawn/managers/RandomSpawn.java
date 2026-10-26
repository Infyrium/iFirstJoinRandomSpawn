package net.infyrium.ifirstjoinrandomspawn.managers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Finds a safe random place in the world from the config. Chunks are loaded asynchronously.
 */
public class RandomSpawn {

    private final JavaPlugin plugin;

    private final String worldName;
    private final int radius;
    private final int attempts;

    public RandomSpawn(JavaPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        this.worldName = config.getString("settings.world", "");
        this.radius = Math.max(1, config.getInt("settings.radius", 10000));
        this.attempts = Math.max(1, config.getInt("settings.attempts", 10));
    }

    /**
     * Completes with null if the world is not loaded or no safe place was found.
     */
    public CompletableFuture<Location> find() {
        // The world is looked up on the main thread: a world loaded after start is found too
        return CompletableFuture.supplyAsync(() -> Bukkit.getWorld(worldName), task -> Bukkit.getScheduler().runTask(plugin, task))
                .thenCompose(world -> {
                    if (world == null) {
                        plugin.getLogger().warning("World '" + worldName + "' not found. The player stays at the default spawn.");
                        return CompletableFuture.completedFuture(null);
                    }
                    return attempt(world, attempts);
                });
    }

    private CompletableFuture<Location> attempt(World world, int left) {
        if (left == 0) {
            plugin.getLogger().warning("No safe place found in " + attempts + " attempts. The player stays at the default spawn.");
            return CompletableFuture.completedFuture(null);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(-radius, radius + 1);
        int z = random.nextInt(-radius, radius + 1);

        // The chunk is ready here and the callback runs on the main thread
        return world.getChunkAtAsync(x >> 4, z >> 4).thenCompose(chunk -> {
            Location location = safeLocation(world, x, z);
            return location != null ? CompletableFuture.completedFuture(location) : attempt(world, left - 1);
        });
    }

    private Location safeLocation(World world, int x, int z) {
        Block ground = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING);
        if (!isSafeGround(ground)) return null;

        Block feet = ground.getRelative(0, 1, 0);
        Block head = ground.getRelative(0, 2, 0);
        if (!feet.isPassable() || feet.isLiquid() || !head.isPassable() || head.isLiquid()) return null;

        return new Location(world, x + 0.5, feet.getY(), z + 0.5);
    }

    private boolean isSafeGround(Block block) {
        Material type = block.getType();
        if (block.isLiquid() || !type.isSolid()) return false;
        if (Tag.LEAVES.isTagged(type)) return false;
        return switch (type) {
            case MAGMA_BLOCK, CACTUS, CAMPFIRE, SOUL_CAMPFIRE, POWDER_SNOW, SWEET_BERRY_BUSH, POINTED_DRIPSTONE -> false;
            default -> true;
        };
    }
}
