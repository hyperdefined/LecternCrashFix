package lol.hyper.lecterncrashfix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lol.hyper.lecterncrashfix.wrapper.WrapperPlayClientWindowClick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class LecternCrashFix extends JavaPlugin {

    private final Logger logger = this.getLogger();

    @Override
    public void onEnable() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = bukkitPackageName.substring(bukkitPackageName.lastIndexOf(".") + 1);
        int ver = Integer.parseInt(bukkitVersion.split("_")[1]);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) {
                    return;
                }

                WrapperPlayClientWindowClick packet = new WrapperPlayClientWindowClick(event.getPacket(), ver);
                Player player = event.getPlayer();
                InventoryView inv = player.getOpenInventory();
                if (inv.getType() == InventoryType.LECTERN) {
                    if (packet.getShift() == WrapperPlayClientWindowClick.InventoryClickType.QUICK_MOVE) {
                        event.setCancelled(true);
                        logger.warning(player.getName() + " tried to illegally click a slot in a lectern! Location: " + player.getLocation());
                    }
                }
            }
        });
    }
}
