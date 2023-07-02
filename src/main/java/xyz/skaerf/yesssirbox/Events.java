package xyz.skaerf.yesssirbox;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.skaerf.yesssirbox.cmds.ShopCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Events implements Listener {

    private static final List<Material> helmetList = new ArrayList<>();
    private static final List<Material> chestplateList = new ArrayList<>();
    private static final List<Material> leggingsList = new ArrayList<>();
    private static final List<Material> bootsList = new ArrayList<>();
    public static HashMap<Player, Long> lastBlockBroken = new HashMap<>();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            int countDamaged = 0;
            int countDefender = 0;
            ((Player) event.getEntity()).updateInventory();
            ((Player) event.getDamager()).updateInventory();
            for (ItemStack i : ((Player)event.getEntity()).getInventory()) {
                if (i != null) {
                    if (helmetList.contains(i.getType())) countDamaged++;
                    if (chestplateList.contains(i.getType())) countDamaged++;
                    if (leggingsList.contains(i.getType())) countDamaged++;
                    if (bootsList.contains(i.getType())) countDamaged++;
                }
            }
            for (ItemStack i : ((Player)event.getDamager()).getInventory()) {
                if (i != null) {
                    if (helmetList.contains(i.getType())) countDefender++;
                    if (chestplateList.contains(i.getType())) countDefender++;
                    if (leggingsList.contains(i.getType())) countDefender++;
                    if (bootsList.contains(i.getType())) countDefender++;
                }
            }
            if (countDefender < 4) {
                event.getDamager().sendMessage(ChatColor.RED + "You don't have any armor - you can't hit that person!");
                event.setCancelled(true);
            }
            if (countDamaged < 4) {
                event.getDamager().sendMessage(ChatColor.RED+"That person does not have a full set of armor - you cannot hit them!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<String> bounties = Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getStringList("bounties");
        for (String line : bounties) {
            if (UUID.fromString(line.split(":")[0]).equals(event.getEntity().getUniqueId())) {
                double amount = Double.parseDouble(line.split(":")[1]);
                EconomyResponse res = Yesssirbox.econ.depositPlayer(event.getEntity().getKiller(), amount);
                if (res.transactionSuccess()) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lyesssirbox &8&l>> &aThe bounty on "+event.getPlayer().getName()+" for $"+amount+" has been claimed by "+event.getPlayer().getKiller().getName()+"!"));
                    }
                    bounties.remove(line);
                    Yesssirbox.getPlugin(Yesssirbox.class).getConfig().set("bounties", bounties);
                    Yesssirbox.getPlugin(Yesssirbox.class).saveConfig();
                    Yesssirbox.getPlugin(Yesssirbox.class).reloadConfig();
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        long time = System.currentTimeMillis();
        try {
            double value = Yesssirbox.blockValues.get(event.getBlock().getType());
            if (!player.getGameMode().equals(GameMode.SURVIVAL) && !player.getGameMode().equals(GameMode.ADVENTURE)) return;
            if ((time - lastBlockBroken.get(player)) <= 500) value = value/2;
            if (!event.isCancelled()) {
                EconomyResponse res = Yesssirbox.econ.depositPlayer(player, value);
                if (res.transactionSuccess()) {
                    Yesssirbox.updateActionBar(player, value);
                }
                else {
                    Yesssirbox.getPlugin(Yesssirbox.class).getLogger().warning("Could not deposit money into "+player.getName()+"'s account - "+res.errorMessage);
                }
            }
        }
        catch (NullPointerException ignored) {}
        lastBlockBroken.remove(player);
        lastBlockBroken.put(player, time);
    }

    @EventHandler
    public void onInvInteract(InventoryClickEvent event) {
        if (event.getView().title().equals(ShopCommand.getShopInvName())) {
            ShopCommand.inventoryClick(event);
        }
    }

    public static void fillArmorLists() {
        helmetList.add(Material.CHAINMAIL_HELMET);
        helmetList.add(Material.DIAMOND_HELMET);
        helmetList.add(Material.IRON_HELMET);
        helmetList.add(Material.GOLDEN_HELMET);
        helmetList.add(Material.LEATHER_HELMET);
        helmetList.add(Material.NETHERITE_HELMET);
        helmetList.add(Material.TURTLE_HELMET);

        chestplateList.add(Material.CHAINMAIL_CHESTPLATE);
        chestplateList.add(Material.DIAMOND_CHESTPLATE);
        chestplateList.add(Material.IRON_CHESTPLATE);
        chestplateList.add(Material.GOLDEN_CHESTPLATE);
        chestplateList.add(Material.LEATHER_CHESTPLATE);
        chestplateList.add(Material.NETHERITE_CHESTPLATE);
        chestplateList.add(Material.ELYTRA);

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
