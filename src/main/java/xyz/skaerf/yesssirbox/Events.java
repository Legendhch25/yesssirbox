package xyz.skaerf.yesssirbox;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class Events implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        try {
            double value = Yesssirbox.blockValues.get(event.getBlock().getType());
            Yesssirbox.econ.bankDeposit(player.getName(), value);
            player.sendMessage(ChatColor.GREEN+"+$"+value);
        }
        catch (NullPointerException ignored) {}
    }
}
