package xyz.skaerf.yesssirbox;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class YSBItemStack extends ItemStack {

    private double value;
    private List<ItemStack> requiredToCraft = new ArrayList<>();

    public YSBItemStack(Material type, int amount) {
        this.setType(type);
        this.setAmount(amount);
    }

    public YSBItemStack (Material type) {
        this.setType(type);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setRequiredToCraft(List<ItemStack> items) {
        this.requiredToCraft = items;
    }

    public List<ItemStack> getRequiredToCraft() {
        return this.requiredToCraft;
    }

    public boolean canCraft(Player player) {
        for (ItemStack i : this.requiredToCraft) {
            if (!player.getInventory().contains(i.getType())) {
                return false;
            }
        }
        return true;
    }

    public ItemStack toItemStack() {
        ItemStack convert = new ItemStack(getType(), getAmount());
        convert.setItemMeta(getItemMeta());
        return convert;
    }

    public boolean canAfford(Player player) {
        double res = Yesssirbox.econ.getBalance(player);
        return res >= this.value;
    }

    public List<Component> addDependentLore(List<Component> list, Player player) {
        if (this.canCraft(player) && this.canAfford(player)) {
            list.add(Component.text(ChatColor.GREEN+"Click here to buy!"));
        }
        else {
            list.add(Component.text(ChatColor.RED+"You don't have enough materials!"));
            String needs = "Need ";
            for (ItemStack i : this.requiredToCraft) {
                String name = i.getType().toString();
                if (name.charAt(name.length()-1) != 's') {
                    name = name + "s";
                }
                needs = needs + i.getAmount()+" "+name.replace("_", " ").toLowerCase()+", ";
            }
            needs = needs.substring(0, needs.length() - 2);
            list.add(Component.text(ChatColor.GRAY+needs));
        }
        return list;
    }
}
