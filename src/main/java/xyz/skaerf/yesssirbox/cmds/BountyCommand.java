package xyz.skaerf.yesssirbox.cmds;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.skaerf.yesssirbox.Yesssirbox;

import java.util.List;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("no");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            List<String> playerBounties = Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getStringList("bounties");
            if (playerBounties.isEmpty()) {
                player.sendMessage(ChatColor.GREEN+"There are no bounties open at the moment! Check back later.");
            }
            else {
                for (String bounty : playerBounties) {
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(UUID.fromString(bounty.split(":")[0])))) {
                        player.sendMessage(ChatColor.GREEN + "Bounty on " + Bukkit.getPlayer(UUID.fromString(bounty.split(":")[0])).getName() + " for $" + bounty.split(":")[1]);
                    }
                }
            }
        }
        else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED+"That player is not online!");
                return true;
            }
            if (args[1] == null) {
                player.sendMessage(ChatColor.RED+"Please provide an amount for the bounty! e.g. 1000");
                return true;
            }
            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED+"You can't put a bounty on yourself!");
                return true;
            }
            double bounty;
            try {
                bounty = Double.parseDouble(args[1]);
            }
            catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED+"Please enter a valid number! e.g. 1000");
                return true;
            }
            if (Yesssirbox.econ.getBalance(player) < bounty) {
                player.sendMessage(ChatColor.RED+"You cannot afford that bounty!");
                return true;
            }
            List<String> playerBounties = Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getStringList("bounties");
            playerBounties.add(target.getUniqueId()+":"+bounty);
            Yesssirbox.getPlugin(Yesssirbox.class).getConfig().set("bounties", playerBounties);
            Yesssirbox.getPlugin(Yesssirbox.class).saveConfig();
            Yesssirbox.getPlugin(Yesssirbox.class).reloadConfig();
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lyesssirbox &8&l>> &a"+player.getName()+" has just set a bounty of $"+bounty+" on "+target.getName()+"!"));
            }
            Yesssirbox.econ.withdrawPlayer(player, bounty);
            player.sendMessage(ChatColor.GREEN+"The bounty has been set and the money has been taken from your account.");
        }
        return true;
    }
}
