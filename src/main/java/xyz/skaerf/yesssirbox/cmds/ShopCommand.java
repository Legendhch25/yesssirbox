package xyz.skaerf.yesssirbox.cmds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skaerf.yesssirbox.YSBItemStack;
import xyz.skaerf.yesssirbox.Yesssirbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopCommand implements CommandExecutor {

    private static Component shopInvName = Component.text("Shop");
    private static HashMap<Integer, YSBItemStack> shopItems = new HashMap<>();
    private static HashMap<ItemStack, YSBItemStack> toYSB = new HashMap<>();
    private static HashMap<ItemStack, Boolean> canTheyHaveIt = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("go away");
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
                }
                catch (NumberFormatException e) {
                    displayShop((Player)sender);
                    return true;
                }
                item.setValue(cost);
                this.saveItemToConfig(item, slot);
                sender.sendMessage(ChatColor.GREEN+"New item saved to config. Please reload the plugin's config (/ysb reload) to apply it to the shop GUI.");
            }
        }
        displayShop((Player)sender);
        return true;
    }

    private void saveItemToConfig(YSBItemStack item, int location) {
        JavaPlugin pl = Yesssirbox.getPlugin(Yesssirbox.class);
        List<String> savedItems = pl.getConfig().getStringList("shopItems");
        String newLine = "";
        // lists are split with ][
        // type::amount::locInShopInv::displayName::lore][::cost::requiredToCraft][
        StringBuilder lore = new StringBuilder();
        for (Component lor : item.getItemMeta().lore()) {
            lore.append(PlainTextComponentSerializer.plainText().serialize(lor) + "][");
        }
        lore = new StringBuilder(lore.substring(0, lore.length() - 2));
        StringBuilder requiredToCraft = new StringBuilder();
        for (ItemStack it : item.getRequiredToCraft()) {
            requiredToCraft.append(it.getType()+","+it.getAmount()+"][");
        }
        requiredToCraft = new StringBuilder(requiredToCraft.substring(0, requiredToCraft.length() - 2));
        String name = "0";

        newLine = newLine + item.getType() + "::" + item.getAmount() + "::" + location + "::" + name + "::" + lore + "::" + item.getValue() + "::" + requiredToCraft;

        savedItems.add(newLine);
        pl.getConfig().set("shopItems", savedItems);
        pl.saveConfig();
    }

    private static void displayShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 54, shopInvName);

        // WOOD AXE
        for (int key : shopItems.keySet()) {
            YSBItemStack item = shopItems.get(key);
            List<Component> uneditedLore = item.lore();
            item.lore(item.addDependentLore(item.lore(), player));
            if (!item.canAfford(player) || !item.canCraft(player)) {
                canTheyHaveIt.put(item.toItemStack(), false);
            }
            else {
                canTheyHaveIt.put(item.toItemStack(), true);
            }
            shop.setItem(key, item.toItemStack());
            toYSB.put(item.toItemStack(), item);
            // revert lore after adding it to inventory
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
            // lists are split with ][
            // type::amount::locInShopInv::displayName::lore][::cost::requiredToCraft][
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

            YSBItemStack toAdd = new YSBItemStack(Material.valueOf(vars[0]), amount);
            ItemMeta meta = toAdd.getItemMeta();
            if (!name.equals("0")) meta.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', name)));
            toAdd.setRequiredToCraft(requiredToCraft);
            toAdd.setValue(value);
            meta.lore(lore);
            toAdd.setItemMeta(meta);
            shopItems.put(slot, toAdd);
        }
    }

    public static void inventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        event.setCancelled(true);
        if (canTheyHaveIt.get(itemStack)) {
            YSBItemStack item = toYSB.get(itemStack);
            EconomyResponse res = Yesssirbox.econ.withdrawPlayer(player, item.getValue());
            if (res.transactionSuccess()) {
                for (ItemStack remove : item.getRequiredToCraft()) {
                    player.getInventory().remove(remove);
                }
                player.updateInventory();
                int iterator = 0;
                for (ItemStack i : player.getInventory()) {
                    if (i == null) {
                        player.getInventory().setItem(iterator, item);
                        player.sendMessage(ChatColor.GREEN+"Item is in your inventory!");
                        player.closeInventory();
                        displayShop(player);
                        break;
                    }
                    iterator++;
                }
                player.updateInventory();
            }
            else {
                player.sendMessage(ChatColor.RED+"Payment did not go through.");
            }
        }
    }
}
