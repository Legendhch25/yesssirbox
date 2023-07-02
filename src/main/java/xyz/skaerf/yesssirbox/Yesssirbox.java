package xyz.skaerf.yesssirbox;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skaerf.yesssirbox.cmds.BountyCommand;
import xyz.skaerf.yesssirbox.cmds.DiscordCommand;
import xyz.skaerf.yesssirbox.cmds.ShopCommand;
import xyz.skaerf.yesssirbox.cmds.YesssirboxCommand;

import java.util.*;
import java.util.List;


public final class Yesssirbox extends JavaPlugin {

    public static HashMap<Material, Double> blockValues = new HashMap<>();
    private static HashMap<Player, String> actionBars = new HashMap<>();
    public static Economy econ;

    @Override
    public void onEnable() {
        Events.fillArmorLists();
        getServer().getPluginManager().registerEvents(new Events(), this);
        this.saveDefaultConfig();
        refreshBlockValues();
        setupEconomy();
        getCommand("yesssirbox").setExecutor(new YesssirboxCommand());
        getCommand("discord").setExecutor(new DiscordCommand());
        getCommand("shop").setExecutor(new ShopCommand());
        getCommand("bounty").setExecutor(new BountyCommand());
        ShopCommand.setItems(this.getConfig());
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public static void refreshBlockValues() {
        List<String> blockValueData = Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getStringList("blockValues");
        if (!blockValueData.isEmpty()) {
            for (String i : blockValueData) {
                blockValues.put(Material.valueOf(i.split(":")[0]), Double.parseDouble(i.split(":")[1]));
            }
        }
    }

    public static void updateActionBar(Player player, double value) {
        String previous = actionBars.get(player);
        String toSend;
        if (previous != null) {
            double oldValue = Double.parseDouble(previous.split("&a\\$")[1].split(" ")[0]);
            if (String.valueOf(oldValue).equals(String.valueOf(value)) && (System.currentTimeMillis() - Events.lastBlockBroken.get(player)) < 10000) {
                try {
                    toSend = "&a$" + value + " x" + (Integer.parseInt(previous.split(" x")[1])+1);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    toSend = "&a$" + value + " x2";
                }
            }
            else {
                toSend = "&a$"+value;
            }
        }
        else {
            toSend = "&a$"+value;
        }
        TextComponent component = Component.text(ChatColor.translateAlternateColorCodes('&', toSend));
        player.sendActionBar(component);
        actionBars.put(player, toSend);
    }
}
