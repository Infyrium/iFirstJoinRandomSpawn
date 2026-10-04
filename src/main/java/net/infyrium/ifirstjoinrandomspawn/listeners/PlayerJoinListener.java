package net.infyrium.ifirstjoinrandomspawn.listeners;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.infyrium.ifirstjoinrandomspawn.iFirstJoinRandomSpawnMain;

public class PlayerJoinListener implements Listener {

    private final Random random = new Random();
    private final World world;
    private final int radius;

    public PlayerJoinListener(iFirstJoinRandomSpawnMain plugin) {
        var config = plugin.getConfig();

        this.radius = Math.max(1, config.getInt("settings.radius", 10000));

        String worldName = config.getString("settings.world");
        if (worldName == null || worldName.isEmpty()) {
            plugin.getLogger().warning("World name is missing in config.");
            this.world = null;
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found. Players will not be teleported.");
            this.world = null;
            return;
        }

        this.world = world;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (world == null) return;
        if (event.getPlayer().hasPlayedBefore()) return;

        event.getPlayer().teleport(getRandomLocation());
    }

    private Location getRandomLocation() {
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }
}
