package xyz.skaerf.yesssirbox;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.ChatMessageType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import xyz.skaerf.yesssirbox.cmds.DiscordCommand;
import xyz.skaerf.yesssirbox.cmds.YesssirboxCommand;

import java.awt.*;
import java.util.HashMap;
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
            TextComponent comp;
            String[] split = previous.split(" x");
            if (split[0].contains(String.valueOf(value)) && (System.currentTimeMillis() - blockValues.get(player)) < 10000) {
                // add to the multiplier number, but not if it was more than ten seconds ago that the last block was broken
                toSend = "&a$"+value+" x"+Integer.parseInt(split[1])+value;
            }
            else {
                toSend = "&a$"+value;
            }
            player.sendActionBar(new net.kyori.adventure.text.TextComponent() {
                @Override
                public @NotNull String content() {
                    return ChatColor.translateAlternateColorCodes('&', toSend);
                }

                @Override
                public net.kyori.adventure.text.@NotNull TextComponent content(@NotNull String content) {
                    return null;
                }

                @Override
                public @NotNull Builder toBuilder() {
                    return null;
                }

                @Override
                public net.kyori.adventure.text.@NotNull TextComponent children(@NotNull List<? extends ComponentLike> children) {
                    return null;
                }

                @Override
                public net.kyori.adventure.text.@NotNull TextComponent style(@NotNull Style style) {
                    return null;
                }

                @Override
                public @Unmodifiable @NotNull List<net.kyori.adventure.text.Component> children() {
                    return null;
                }

                @Override
                public @NotNull Style style() {
                    return null;
                }
            });
        }
    }
}
