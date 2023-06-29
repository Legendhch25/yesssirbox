package xyz.skaerf.yesssirbox.cmds;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.skaerf.yesssirbox.Yesssirbox;

public class DiscordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getString("discordMessage") != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Yesssirbox.getPlugin(Yesssirbox.class).getConfig().getString("discordMessage")));
        }
        else {
            sender.sendMessage(ChatColor.GREEN+"Please ask a staff member for a link to the Discord server!");
        }
        return true;
    }
}
