package net.infyrium.ifirstjoinrandomspawn.listeners;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import net.infyrium.ifirstjoinrandomspawn.iFirstJoinRandomSpawnMain;

public class SpawnLocationListener implements Listener {

    // Longer than this the player would wait on the loading screen
    private static final long TIMEOUT_SECONDS = 15;

    private final iFirstJoinRandomSpawnMain plugin;

    public SpawnLocationListener(iFirstJoinRandomSpawnMain plugin) {
        this.plugin = plugin;
    }

    // The event is async and the player waits in the configuration phase, so waiting here lags nobody
    @EventHandler
    public void onSpawnLocation(AsyncPlayerSpawnLocationEvent event) {
        if (!event.isNewPlayer()) return;

        try {
            Location location = plugin.getRandomSpawn().find().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (location != null) {
                event.setSpawnLocation(location);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not find a random spawn, the player stays at the default spawn", e);
        }
    }
}
