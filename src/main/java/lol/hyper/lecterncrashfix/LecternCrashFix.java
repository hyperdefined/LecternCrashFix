package lol.hyper.lecterncrashfix;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.lecterncrashfix.events.InventoryClick;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class LecternCrashFix extends JavaPlugin {

    public final Logger logger = this.getLogger();
    final int CONFIG_VERSION = 1;
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;

    public InventoryClick inventoryClick;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        inventoryClick = new InventoryClick(this);
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        new Metrics(this, 14959);

        PacketEvents.getAPI().getEventManager().registerListener(inventoryClick);
        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public void runCommand(Player player) {
        String command = config.getString("command");
        if (command == null || command.isEmpty()) {
            return;
        }
        if (command.contains("%player%")) {
            command = command.replace("%player%", player.getName());
        }
        if (command.contains("%uuid%")) {
            command = command.replace("%uuid%", player.getUniqueId().toString());
        }
        if (command.contains("%location%")) {
            command = command.replace("%location%", player.getLocation().toString());
        }
        String finalCommand = command;
        Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand), 20);
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("LecternCrashFix", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }
}
