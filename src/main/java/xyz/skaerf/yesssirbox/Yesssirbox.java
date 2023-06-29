package xyz.skaerf.yesssirbox;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skaerf.yesssirbox.cmds.DiscordCommand;
import xyz.skaerf.yesssirbox.cmds.YesssirboxCommand;

import java.util.HashMap;
import java.util.List;


public final class Yesssirbox extends JavaPlugin {

    public static HashMap<Material, Double> blockValues = new HashMap<>();
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
}
