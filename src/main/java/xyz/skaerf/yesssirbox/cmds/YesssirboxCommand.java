package xyz.skaerf.yesssirbox.cmds;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.skaerf.yesssirbox.Yesssirbox;

import java.util.List;

public class YesssirboxCommand implements CommandExecutor {

    JavaPlugin pl = Yesssirbox.getPlugin(Yesssirbox.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) if (!sender.hasPermission("yesssirbox.admin")) sender.sendMessage(ChatColor.RED+"You don't have permission to execute this command!");
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN+"/yesssirbox <reload/addBlock> [value]");
        }
        else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            pl.reloadConfig();
            ShopCommand.setItems(pl.getConfig());
            sender.sendMessage(ChatColor.GREEN+"Config reloaded!");
        }
        else if (args[0].equalsIgnoreCase("addBlock") || args[0].equalsIgnoreCase("ab")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED+"Please put the value that you want the block to have! /yesssirbox addBlock [value]");
                return true;
            }
            List<String> blockValues =  pl.getConfig().getStringList("blockValues");
            blockValues.add(((Player) sender).getInventory().getItemInMainHand().getType()+":"+args[1]);
            pl.getConfig().set("blockValues", blockValues);
            pl.saveConfig();
            pl.reloadConfig();
            Yesssirbox.refreshBlockValues();
            sender.sendMessage(ChatColor.GREEN+((Player)sender).getInventory().getItemInMainHand().getType().toString()+" has been added to the config with a value of $"+args[1]+"!");
        }
        return true;
    }
}
