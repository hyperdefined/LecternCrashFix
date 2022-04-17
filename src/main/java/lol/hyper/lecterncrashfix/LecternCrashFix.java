package lol.hyper.lecterncrashfix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.lecterncrashfix.wrapper.WrapperPlayClientWindowClick;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class LecternCrashFix extends JavaPlugin {

    private final Logger logger = this.getLogger();
    final int CONFIG_VERSION = 1;
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;

    @Override
    public void onEnable() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        new Metrics(this, 14959);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) {
                    return;
                }

                WrapperPlayClientWindowClick packet = new WrapperPlayClientWindowClick(event.getPacket());
                Player player = event.getPlayer();
                InventoryView inv = player.getOpenInventory();
                if (inv.getType() == InventoryType.LECTERN) {
                    if (packet.getShift() == WrapperPlayClientWindowClick.InventoryClickType.QUICK_MOVE) {
                        event.setCancelled(true);
                        logger.warning(player.getName() + " tried to illegally click a slot in a lectern! Location: " + player.getLocation());
                        if (config.getBoolean("run-command")) {
                            runCommand(player);
                        }
                    }
                }
            }
        });
    }

    private void runCommand(Player player) {
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
        Bukkit.getScheduler().runTaskLater(this, ()-> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand), 20);
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
