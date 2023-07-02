package xyz.skaerf.yesssirbox;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class ConfigFiles {

    private static File pFolder;
    private static HashMap<Player, File> files = new HashMap<>();
    private static Logger logger = Yesssirbox.getPlugin(Yesssirbox.class).getLogger();


    public static FileConfiguration getPlayerFile(Player player) {
        File pFile = new File(pFolder+File.separator+player.getUniqueId()+".yml");
        if (!pFile.exists()) {
            try {
                if (pFile.createNewFile()) {
                    logger.info("Created new player data file for "+player.getName());
                }
            }
            catch (IOException e) {
                logger.warning("Could not create a new player data file for "+player.getName());
            }
        }
        files.put(player, pFile);
        return YamlConfiguration.loadConfiguration(pFile);
    }

    public static void savePlayerFile(Player player) {

    }

    public static void initPlayerFiles() {
        pFolder = new File(Yesssirbox.getPlugin(Yesssirbox.class).getDataFolder()+File.separator+"players");
        if (!pFolder.exists()) {
            try {
                if (pFolder.mkdir()) {
                    logger.info("Successfully created player data folder");
                }
            }
            catch (SecurityException e) {
                logger.warning("Player data folder could not be created - SecurityException");
            }
        }
    }
}
