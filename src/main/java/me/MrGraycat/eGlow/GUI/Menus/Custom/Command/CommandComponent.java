package me.MrGraycat.eGlow.GUI.Menus.Custom.Command;

import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum CommandComponent {

    MESSAGE("Message:"),
    REFRESH("Refresh: true"),
    CLOSE("Close: true"),
    COMMAND("Command:"),
    CONSOLE("Console:"),
    CHAT("Chat:"),
    BROADCAST("Broadcast:"),
    SOUND("Sound:"),
    SOUND_BROADCAST("SoundBroadcast:");

    private final String prefix;

    CommandComponent(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public static CommandComponent findByPrefix(String prefix) {
        for (CommandComponent commandComponent : values()) {
            if (prefix.startsWith(commandComponent.getPrefix())) {
                return commandComponent;
            }
        }
        return null;
    }

    public void playSound(Player player, String message, boolean broadcast){
        String[] split = message.split(",");
        if(split.length > 3) return;
        int volume;
        int pitch;
        String sound = split[0];
        if(sound.isEmpty()) {
            ChatUtil.sendToConsole("&f[&eeGlow&f]: &cError: &7Sound is empty", false);
            return;
        }
        if(split.length < 2 || !isNumber(split[1])) volume = 20;
        else volume = Integer.parseInt(split[1]);

        if(split.length < 3 || !isNumber(split[2])) pitch = 1;
        else pitch = Integer.parseInt(split[2]);
        if (!broadcast)
            player.playSound(player.getLocation(), Sound.valueOf(sound), volume, pitch);
        else Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.valueOf(sound), volume, pitch));
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
