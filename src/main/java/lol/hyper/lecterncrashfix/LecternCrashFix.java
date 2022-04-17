package lol.hyper.lecterncrashfix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lol.hyper.lecterncrashfix.wrapper.WrapperPlayClientWindowClick;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
}
