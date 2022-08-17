package me.MrGraycat.eGlow.GUI.Menus.Custom.Command;

import me.MrGraycat.eGlow.GUI.Manager.MenuManager;
import me.MrGraycat.eGlow.GUI.Menus.Custom.EGlowCustomMenu;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.MrGraycat.eGlow.Util.Text.ItemPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ClickCommands {

    private final Plugin plugin;
    private final Player player;
    private final ConfigurationSection section;
    private final CommandClickType command;

    public ClickCommands(Plugin plugin, Player player, ConfigurationSection section, CommandClickType command){
        this.plugin = plugin;
        this.player = player;
        this.command = command;
        this.section = section;
    }

    public void runCommands(IEGlowPlayer eGlowPlayer, IEGlowEffect effect){
        if (section.getConfigurationSection(command.getConfigurationName()) == null) return;
        ConfigurationSection clickConfig = section.getConfigurationSection(command.getConfigurationName());
        if (clickConfig == null){
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7ClickConfig for &e" + command.getConfigurationName() + "&7 is null", false);
            return;
        }
        if (clickConfig.getStringList("Commands").isEmpty()) {
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7No player commands found for &e" + command.getConfigurationName() + "&7 in &e" + section.getCurrentPath(), false);
            return;
        }
        List<CommandComponentHolder> commandComponentHolders = new ArrayList<>();
        for (String command : clickConfig.getStringList("Commands")){
            if (command.isEmpty()) continue;
            CommandComponent commandComponent = CommandComponent.findByPrefix(command);
            if (commandComponent == null) continue;
            String message = command.replace(commandComponent.getPrefix(), "");
            if (message.startsWith(" "))
                message = message.trim();
            message = ChatUtil.convertedColoredText(player, message);
            message = new ItemPlaceholders().getPlaceholders(eGlowPlayer, effect, message);
            commandComponentHolders.add(new CommandComponentHolder(commandComponent, message));
        }
        if (commandComponentHolders.isEmpty()){
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7No commands found for &e" + command.getConfigurationName() + "&7 in &e" + section.getCurrentPath(), false);
            return;
        }
        for (CommandComponentHolder commandComponent : commandComponentHolders){
            runComponent(commandComponent.getComponent(), ChatUtil.convertedColoredText(player, commandComponent.getMessage()));
        }
    }

    public void noPermissionCommands(IEGlowPlayer eGlowPlayer, IEGlowEffect effect){
        if (section.getConfigurationSection(command.getConfigurationName()) == null) return;
        ConfigurationSection clickConfig = section.getConfigurationSection(command.getConfigurationName());
        if (clickConfig == null){
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7ClickConfig for &e" + command.getConfigurationName() + "&7 is null", false);
            return;
        }
        if (clickConfig.getStringList("Options.Commands").isEmpty()) {
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7No permission commands found for &e" + command.getConfigurationName() + "&7 in &e" + section.getCurrentPath(), false);
            return;
        }
        List<CommandComponentHolder> commandComponentHolders = new ArrayList<>();
        for (String command : clickConfig.getStringList("Options.Commands")){
            if (command.isEmpty()) continue;
            CommandComponent commandComponent = CommandComponent.findByPrefix(command);
            if (commandComponent == null) continue;
            String message = command.replace(commandComponent.getPrefix(), "");
            if (message.startsWith(" "))
                message = message.trim();
            message = ChatUtil.convertedColoredText(player, message);
            message = new ItemPlaceholders().getPlaceholders(eGlowPlayer, effect, message);
            commandComponentHolders.add(new CommandComponentHolder(commandComponent, message));
        }
        if (commandComponentHolders.isEmpty()){
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7No commands found for &e" + command.getConfigurationName() + "&7 in &e" + section.getCurrentPath(), false);
            return;
        }
        for (CommandComponentHolder commandComponent : commandComponentHolders){
            runComponent(commandComponent.getComponent(), ChatUtil.convertedColoredText(player, commandComponent.getMessage()));
        }
    }

    private void runComponent(CommandComponent component, String command) {
        new BukkitRunnable() {

            @Override
            public void run() {
                switch (component) {
                    case CHAT:
                        player.chat(command);
                        break;
                    case MESSAGE:
                        player.sendMessage(command);
                        break;
                    case COMMAND:
                        Bukkit.dispatchCommand(player, command);
                        break;
                    case SOUND:
                        component.playSound(player, command, false);
                        break;
                    case BROADCAST:
                        Bukkit.broadcastMessage(command);
                        break;
                    case SOUND_BROADCAST:
                        component.playSound(player, command, true);
                        break;
                    case CONSOLE:
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        break;
                    case CLOSE:
                        new MenuManager().getMenuMetadata(player).getOwner().closeInventory();
                        break;
                    case REFRESH:
                        new EGlowCustomMenu(player).refreshMenu();
                        break;
                }
            }
        }.runTask(plugin);
    }

    public boolean getSpeed(){
        return section.getBoolean(command.getConfigurationName() + ".Options.Speed");
    }

    public boolean getBlink(){
        return section.getBoolean(command.getConfigurationName() + ".Options.Blink");
    }

    public boolean usePermission(){
        return section.getBoolean(command.getConfigurationName() + ".Options.Permission");
    }

}
