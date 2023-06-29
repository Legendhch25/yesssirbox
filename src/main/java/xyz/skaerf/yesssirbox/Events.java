package xyz.skaerf.yesssirbox;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Events implements Listener {

    private static List<Material> helmetList = new ArrayList<>();
    private static List<Material> chestplateList = new ArrayList<>();
    private static List<Material> leggingsList = new ArrayList<>();
    private static List<Material> bootsList = new ArrayList<>();

    private HashMap<Player, HashMap<Player, String>> noKillList = new HashMap<>(); // Killer : Death-Having Player: TimestampOfDeath:::Amount
    private HashMap<Player, Long> lastBlockBroken = new HashMap<>();

    /*
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player player = (Player) event.getEntity();
            int count = 0;
            for (ItemStack i : player.getInventory().getContents()) {
                if (helmetList.contains(i))  count++;
                if (chestplateList.contains(i)) count++;
                if (leggingsList.contains(i))  count++;
                if (bootsList.contains(i)) count++;
            }
            if (count ==  4) return;
            for (ItemStack i : player.getInventory().getArmorContents()) {
                if (i == null) {
                    damager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lyesssirbox &8&l>> &cYou can't hurt this player, they don't have a full set of armor!"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getPlayer().getKiller() != null) {

        }
    }*/

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        long time = System.currentTimeMillis();
        try {
            double value = Yesssirbox.blockValues.get(event.getBlock().getType());
            if (!player.getGameMode().equals(GameMode.SURVIVAL) && !player.getGameMode().equals(GameMode.ADVENTURE)) return;
            if ((time - lastBlockBroken.get(player)) <= 500) value = value/2;
            EconomyResponse res = Yesssirbox.econ.depositPlayer(player, value);
            if (res.transactionSuccess()) {
                player.sendMessage(ChatColor.GREEN+"+$"+value);
            }
            else {
                Yesssirbox.getPlugin(Yesssirbox.class).getLogger().warning("Could not deposit money into "+player.getName()+"'s account - "+res.errorMessage);
            }
        }
        catch (NullPointerException ignored) {}
        lastBlockBroken.remove(player);
        lastBlockBroken.put(player, time);
    }

    private boolean canBeDamaged(Player damager, Player damaged) {
        String[] data = noKillList.get(damager).get(damaged).split(":::");
        if (Integer.parseInt(data[0]) == 3) {
            return false;
        }
        return true;
    }

    public static void fillArmorLists() {
        helmetList.add(Material.CHAINMAIL_HELMET);
        helmetList.add(Material.DIAMOND_HELMET);
        helmetList.add(Material.IRON_HELMET);
        helmetList.add(Material.GOLDEN_HELMET);
        helmetList.add(Material.LEATHER_HELMET);
        helmetList.add(Material.NETHERITE_HELMET);

        chestplateList.add(Material.CHAINMAIL_CHESTPLATE);
        chestplateList.add(Material.DIAMOND_CHESTPLATE);
        chestplateList.add(Material.IRON_CHESTPLATE);
        chestplateList.add(Material.GOLDEN_CHESTPLATE);
        chestplateList.add(Material.LEATHER_CHESTPLATE);
        chestplateList.add(Material.NETHERITE_CHESTPLATE);

        leggingsList.add(Material.CHAINMAIL_LEGGINGS);
        leggingsList.add(Material.DIAMOND_LEGGINGS);
        leggingsList.add(Material.IRON_LEGGINGS);
        leggingsList.add(Material.GOLDEN_LEGGINGS);
        leggingsList.add(Material.LEATHER_LEGGINGS);
        leggingsList.add(Material.NETHERITE_LEGGINGS);

        bootsList.add(Material.CHAINMAIL_BOOTS);
        bootsList.add(Material.DIAMOND_BOOTS);
        bootsList.add(Material.IRON_BOOTS);
        bootsList.add(Material.GOLDEN_BOOTS);
        bootsList.add(Material.LEATHER_BOOTS);
        bootsList.add(Material.NETHERITE_BOOTS);
    }

}
