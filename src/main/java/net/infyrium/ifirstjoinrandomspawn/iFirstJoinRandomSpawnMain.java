package net.infyrium.ifirstjoinrandomspawn;

import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.ifirstjoinrandomspawn.listeners.SpawnLocationListener;
import net.infyrium.ifirstjoinrandomspawn.managers.RandomSpawn;
import net.infyrium.ifirstjoinrandomspawn.utils.ConfigUpdater;

public class iFirstJoinRandomSpawnMain extends JavaPlugin {

    private RandomSpawn randomSpawn;

    @Override
    public void onEnable() {
        ConfigUpdater.backupIfOutdated(this, "config.yml");
        saveDefaultConfig();

        randomSpawn = new RandomSpawn(this);

        getServer().getPluginManager().registerEvents(new SpawnLocationListener(this), this);

        getLogger().info("Plugin has been enabled!");
        getLogger().info("Plugin developed by: " + String.join(", ", getPluginMeta().getAuthors()));
        getLogger().info("Website: " + getPluginMeta().getWebsite());
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled!");
    }

    public RandomSpawn getRandomSpawn() {
        return randomSpawn;
    }
}
