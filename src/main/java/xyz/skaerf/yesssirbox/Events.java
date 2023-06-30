package xyz.skaerf.yesssirbox;

import net.kyori.adventure.text.Component;
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

public class Events implements Listener {

    private static final List<Material> helmetList = new ArrayList<>();
    private static final List<Material> chestplateList = new ArrayList<>();
    private static final List<Material> leggingsList = new ArrayList<>();
    private static final List<Material> bootsList = new ArrayList<>();

    private HashMap<Player, HashMap<Player, String>> noKillList = new HashMap<>(); // Killer : Death-Having Player: TimestampOfFirstDeath:::TimesKilled
    public static HashMap<Player, Long> lastBlockBroken = new HashMap<>();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            int count = 0;
            ((Player) event.getEntity()).updateInventory();
            ((Player) event.getDamager()).updateInventory();
            for (ItemStack i : ((Player)event.getEntity()).getInventory()) {
                if (i != null) {
                    if (helmetList.contains(i.getType())) count++;
                    if (chestplateList.contains(i.getType())) count++;
                    if (leggingsList.contains(i.getType())) count++;
                    if (bootsList.contains(i.getType())) count++;
                }
            }
            if (count < 4) {
                event.getDamager().sendMessage(ChatColor.RED+"That person does not have a full set of armor - you cannot hit them!");
                event.setCancelled(true);
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
