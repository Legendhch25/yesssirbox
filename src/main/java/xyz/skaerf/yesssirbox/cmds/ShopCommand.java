package xyz.skaerf.yesssirbox.cmds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skaerf.yesssirbox.YSBItemStack;
import xyz.skaerf.yesssirbox.Yesssirbox;

import java.util.*;

public class ShopCommand implements CommandExecutor {

    private static Component shopInvName = Component.text(ChatColor.YELLOW + "Mining Rewards");
    private static HashMap<Integer, YSBItemStack> shopItems = new HashMap<>();
    private static HashMap<ItemStack, YSBItemStack> toYSB = new HashMap<>();
    private static HashMap<ItemStack, Boolean> canTheyHaveIt = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }
        if (args.length != 0 && sender.hasPermission("yesssirbox.admin")) {
            if (args.length == 4 && ((Player) sender).getInventory().getItem(0) != null) {
                ItemStack inh = ((Player) sender).getInventory().getItem(0);
                assert inh != null;
                YSBItemStack item = new YSBItemStack(inh.getType(), inh.getAmount());
                item.setItemMeta(inh.getItemMeta());
                List<ItemStack> toCraft = new ArrayList<>();
                for (int i = 1; i < Integer.parseInt(args[3]); i++) {
                    ItemStack it = ((Player) sender).getInventory().getItem(i);
                    if (it != null) toCraft.add(it);
                }
                item.setRequiredToCraft(toCraft);
                int slot;
                double cost;
                try {
                    slot = Integer.parseInt(args[1]);
                    cost = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    displayShop((Player) sender);
                    return true;
                }
                item.setValue(cost);
                this.saveItemToConfig(item, slot);
                sender.sendMessage(ChatColor.GREEN + "New item saved to config. Please reload the plugin's config (/ysb reload) to apply it to the shop GUI.");
            }
        }
        displayShop((Player) sender);
        return true;
    }

    private void saveItemToConfig(YSBItemStack item, int location) {
        JavaPlugin pl = Yesssirbox.getPlugin(Yesssirbox.class);
        List<String> savedItems = pl.getConfig().getStringList("shopItems");
        String newLine = "";
        // lists are split with ][
        // type::amount::locInShopInv::displayName::lore][::cost::requiredToCraft][::enchants][
        StringBuilder lore = new StringBuilder();
        if (item.getItemMeta().lore() != null) for (Component lor : Objects.requireNonNull(item.getItemMeta().lore())) {
            lore.append(PlainTextComponentSerializer.plainText().serialize(lor)).append("][");
        }
        lore = new StringBuilder(lore.substring(0, lore.length() - 2));
        StringBuilder requiredToCraft = new StringBuilder();
        for (ItemStack it : item.getRequiredToCraft()) {
            requiredToCraft.append(it.getType()).append(",").append(it.getAmount()).append("][");
        }
        requiredToCraft = new StringBuilder(requiredToCraft.substring(0, requiredToCraft.length() - 2));
        StringBuilder enchants = new StringBuilder();
        for (Enchantment ench : item.getEnchantments().keySet()) {
            enchants.append(ench.getKey()).append(",").append(item.getEnchantmentLevel(ench)).append("][");
        }
        enchants = new StringBuilder(enchants.substring(0, enchants.length() - 2));
        String name = "0";

        newLine = newLine + item.getType() + "::" + item.getAmount() + "::" + location + "::" + name + "::" + lore + "::" + item.getValue() + "::" + requiredToCraft + "::" + enchants;

        savedItems.add(newLine);
        pl.getConfig().set("shopItems", savedItems);
        pl.saveConfig();
    }

    private static void displayShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 54, shopInvName);

        // Add items to the shop inventory
        for (int key : shopItems.keySet()) {
            YSBItemStack item = shopItems.get(key);
            List<Component> uneditedLore = item.lore();
            item.lore(item.addDependentLore(item.lore(), player));
            if (!item.canAfford(player) || !item.canCraft(player)) {
                canTheyHaveIt.put(item.toItemStack(), false);
            } else {
                canTheyHaveIt.put(item.toItemStack(), true);
            }
            shop.setItem(key, item.toItemStack());
            toYSB.put(item.toItemStack(), item);
            // Revert lore after adding it to inventory
            item.lore(uneditedLore);
        }

        player.openInventory(shop);
    }

    public static Component getShopInvName() {
        return shopInvName;
    }

    public static void setItems(FileConfiguration config) {
        List<String> items = config.getStringList("shopItems");
        for (String i : items) {
            // Lists are split with ][
            // type::amount::locInShopInv::displayName::lore][::cost::requiredToCraft][::enchants][
            String[] vars = i.split("::");
            int amount = Integer.parseInt(vars[1]);
            int slot = Integer.parseInt(vars[2]);
            double value = Double.parseDouble(vars[5]);

            String name = vars[3];
            List<Component> lore = new ArrayList<>();
            for (String a : vars[4].split("]\\[")) {
                lore.add(Component.text(ChatColor.translateAlternateColorCodes('&', a)));
            }
            List<ItemStack> requiredToCraft = new ArrayList<>();
            for (String j : vars[6].split("]\\[")) {
                requiredToCraft.add(new ItemStack(Material.valueOf(j.split(",")[0]), Integer.parseInt(j.split(",")[1])));
            }
            Map<Enchantment, Integer> enchants = new HashMap<>();
            for (String ench : vars[7].split("]\\[")) {
                Enchantment enchValue = Enchantment.getByKey(NamespacedKey.fromString(ench.split(",")[0]));
                int level = Integer.parseInt(ench.split(",")[1]);
                enchants.put(enchValue, level);
            }
            YSBItemStack toAdd = new YSBItemStack(Material.valueOf(vars[0]), amount);
            toAdd.setName(name);
            toAdd.lore(lore);
            toAdd.setValue(value);
            toAdd.setRequiredToCraft(requiredToCraft);
            toAdd.setEnchantments(enchants);
            shopItems.put(slot, toAdd);
        }
    }

    public static void buyItem(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        YSBItemStack toBuy = toYSB.get(clickedItem);
        if (toBuy == null) return;

        if (canTheyHaveIt.get(clickedItem)) {
            EconomyResponse response = Yesssirbox.getEconomy().withdrawPlayer(p, toBuy.getValue());
            if (response.transactionSuccess()) {
                p.getInventory().addItem(toBuy.toItemStack());
                p.sendMessage(ChatColor.GREEN + "Purchase successful!");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            } else {
                p.sendMessage(ChatColor.RED + "You do not have enough money to purchase this item.");
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
            }
        } else {
            p.sendMessage(ChatColor.RED + "You cannot afford this item or do not have the required materials to craft it.");
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
        }
        e.setCancelled(true);
        p.closeInventory();
    }
}
