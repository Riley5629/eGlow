package me.MrGraycat.eGlow.Config.Custom;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.neznamy.yamlassist.YamlAssist;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EGlowCustomGuiConfig {

    private static YamlConfiguration config;
    private static File configFile;

    public static void initialize() {
        configFile = new File(EGlow.getInstance().getDataFolder() + "/custom", "CustomGui.yml");

        try {
            if (!EGlow.getInstance().getDataFolder().exists()) {
                EGlow.getInstance().getDataFolder().mkdirs();
            }

            if (!configFile.exists()) {
                ChatUtil.sendToConsole("&f[&eeGlow&f]: &4CustomGui.yml not found&f! &eCreating&f...", false);
                configFile.getParentFile().mkdirs();
                EGlow.getInstance().saveResource("custom/CustomGui.yml", false);
            } else {
                ChatUtil.sendToConsole("&f[&eeGlow&f]: &aLoading CustomGui config&f.", false);
            }

            config = new YamlConfiguration();
            config.load(configFile);
        } catch(Exception e) {
            ChatUtil.reportError(e);
            if (e.getCause() instanceof YAMLException) {
                List<String> suggestions = YamlAssist.getSuggestions(configFile);
                for(String suggestion : suggestions) {
                    ChatUtil.sendToConsole("&c" + suggestion, false);
                }
            }
        }
    }

    public static boolean reloadConfig() {
        YamlConfiguration configBackup = config;
        File configFileBackup = configFile;

        try {
            config = null;
            configFile = null;

            configFile = new File(EGlow.getInstance().getDataFolder() + "/custom", "CustomGui.yml");
            config = new YamlConfiguration();
            config.load(configFile);
            return true;
        } catch(Exception e) {
            config = configBackup;
            configFile = configFileBackup;

            ChatUtil.reportError(e);
            if (e.getCause() instanceof YAMLException) {
                List<String> suggestions = YamlAssist.getSuggestions(configFile);
                for(String suggestion : suggestions) {
                    ChatUtil.sendToConsole("&c" + suggestion, false);
                }
            }
            return false;
        }
    }

    public enum CustomGui {
        GET_TITLE("Title"),
        GET_ITEMS("Items"),
        GET_SIZE("Size"),
        GET_ITEM("Items.%item%");

        private final String configPath;

        CustomGui(String configPath) {
            this.configPath = configPath;
        }

        public String getConfigPath() {
            return configPath;
        }

        public Set<String> get() {
            try {
                return config.getConfigurationSection(getConfigPath()).getKeys(false);
            } catch (NullPointerException e) {
                return Collections.emptySet();
            }
        }

        public ConfigurationSection getItem(String item) {
            if (config.get(getConfigPath().replace("%item%", item)) != null) {
                return config.getConfigurationSection(getConfigPath().replace("%item%", item));
            }
            else
                return null;
        }

        public ConfigurationSection getSection(){
            return config.getConfigurationSection(getConfigPath());
        }

        /**
         * Uses {@link #getConfigPath()} to get the value of the config.
         * @return The size of the inventory, 54 by default.
         */
        public int getSize(){
            if (config.getInt(configPath) == 0) {
                ChatUtil.sendToConsole("&f[&eeGlow&f]: &cCustomGui.yml is missing " + configPath + "!", false);
                return 54;
            }
            int size = config.getInt(configPath);
            if (size % 9 != 0) {
                ChatUtil.sendToConsole("&f[&eeGlow&f]: &cCustomGui.yml " + configPath + " is not a multiple of 9!", false);
                return 54;
            }
            return size;
        }

        /**
         * Uses {@link #getConfigPath()} to get the value of the config.
         * @return The title of the inventory, "&eCustom Menu" by default.
         */
        public String getTitle(){
            if (config.getString(configPath) == null) {
                ChatUtil.sendToConsole("&f[&eeGlow&f]: &cCustomGui.yml is missing " + configPath + "!", false);
                return "&eeGlow&f Custom Menu";
            }
            return config.getString(configPath);
        }

    }
}
