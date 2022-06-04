package lol.hyper.lecterncrashfix.events;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import lol.hyper.lecterncrashfix.LecternCrashFix;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

public class InventoryClick extends PacketListenerAbstract {

    private final LecternCrashFix lecternCrashFix;

    public InventoryClick(LecternCrashFix lecternCrashFix) {
        super(PacketListenerPriority.HIGHEST);
        this.lecternCrashFix = lecternCrashFix;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            WrapperPlayClientClickWindow.WindowClickType type = wrapper.getWindowClickType();
            if (type == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                Player player = (Player) event.getPlayer();
                InventoryView inv = player.getOpenInventory();
                if (inv.getType() == InventoryType.LECTERN) {
                    event.setCancelled(true);
                    lecternCrashFix.logger.warning(player.getName() + " tried to illegally click a slot in a lectern! Location: " + player.getLocation());
                    if (lecternCrashFix.config.getBoolean("run-command")) {
                        lecternCrashFix.runCommand(player);
                    }
                }
            }
        }
    }
}
