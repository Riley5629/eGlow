package me.MrGraycat.eGlow.Util.Text;

import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;

public class ItemPlaceholders {

    public String getPlaceholders(IEGlowPlayer player, IEGlowEffect effect, String message){
        if (player.getEffect() != null) message = message.replace("%option_speed_state%", player.getEffect().getName().contains("fast") ? "fast" : "slow");
        else message = message.replace("%option_speed_state%", "no color");
        message = message.replace("%option_join_state%", player.getGlowOnJoin() ? "on" : "off");
        message = message.replace("%option_toggle_state%", player.getFakeGlowStatus() || player.getGlowStatus() ? "true" : "false");

        if (player.getEffect().getName().contains("rainbow")) message = message.replace("%rainbow_speed%", player.getEffect().getName().contains("fast") ? "fast" : "slow");

        if (effect == null)
            return message;

        message = message.replace("%color_is_used%", checkColor(player, effect) ? "true" : "false");
        if (player.getEffect() != null){
            String color = player.getEffect().getName();
            color = color.replace("fast", "");
            color = color.replace("slow", "");
            color = color.replace("blink", "");
            if (color.equals(effect.getName()) || color.equals(effect.getName() + "fast") || color.equals(effect.getName() + "slow")){
                message = message.replace("%color_name%", player.getEffect().getName());
                message = message.replace("%color_displayname%", player.getEffect().getDisplayName());
            } else {
                message = message.replace("%color_name%", effect.getName());
                message = message.replace("%color_displayname%", effect.getDisplayName());
            }
        }
        else {
            message = message.replace("%color_name%", effect.getName());
            message = message.replace("%color_displayname%", effect.getDisplayName());
        }

        message = message.replace("%color_permission%", effect.getPermission());
        message = message.replace("%color_has_permission%", player.getPlayer().hasPermission(effect.getPermission()) ? "true" : "false");

        IEGlowEffect blinkSlow = DataManager.getEGlowEffect("blink" + effect.getName() + "slow");
        if (blinkSlow == null) return message;
        message = message.replace("%blink_slow_has_permission%", player.getPlayer().hasPermission(blinkSlow.getPermission()) ? "true" : "false");

        IEGlowEffect blinkFast = DataManager.getEGlowEffect("blink" + effect.getName() + "fast");
        if (blinkFast == null) return message;
        message = message.replace("%blink_fast_has_permission%", player.getPlayer().hasPermission(blinkFast.getPermission()) ? "true" : "false");
        return message;
    }

    public boolean checkColor(IEGlowPlayer player, IEGlowEffect effect){
        if (player.getEffect() == null) return false;
        return player.getEffect().getName().equals(effect.getName());
    }

}