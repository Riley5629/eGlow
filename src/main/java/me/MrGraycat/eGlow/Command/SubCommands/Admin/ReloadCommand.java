package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import me.MrGraycat.eGlow.Addon.Internal.AdvancedGlowVisibilityAddon;
import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReloadCommand extends SubCommand {

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "Reload the plugin";
	}

	@Override
	public String getPermission() {
		return "eglow.command.reload";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow reload"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (EGlowMainConfig.reloadConfig() && EGlowMessageConfig.reloadConfig() && EGlowCustomEffectsConfig.reloadConfig()) {
			EGlowPlayerdataManager.setMysql_Failed(false);
			DataManager.addEGlowEffects();
			DataManager.addCustomEffects();

			for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
				ePlayer = DataManager.getEGlowPlayer(onlinePlayer);
				
				if (ePlayer == null)
					continue;
				
				ePlayer.updatePlayerTabname();
				
				IEGlowEffect effect = ePlayer.getForceGlow();
				
				if (effect != null) {
					if (getInstance().getLibDisguiseAddon() != null && getInstance().getLibDisguiseAddon().isDisguised(ePlayer.getPlayer()) || getInstance().getIDisguiseAddon() != null && getInstance().getIDisguiseAddon().isDisguised(ePlayer.getPlayer())) {
						ePlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsg(ePlayer.getPlayer(), Message.DISGUISE_BLOCKED.get(), true);
					} else {
						ePlayer.activateGlow(effect);
					}
					continue;
				}
				
				if (MainConfig.WORLD_ENABLE.getBoolean() && ePlayer.isInBlockedWorld()) {
					if (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus()) {
						ePlayer.toggleGlow();
						ePlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
						ChatUtil.sendMsg(ePlayer.getPlayer(), Message.WORLD_BLOCKED_RELOAD.get(), true);
					}
				} else {
					if (ePlayer.getGlowDisableReason() != null && ePlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
						ePlayer.toggleGlow();
						ePlayer.setGlowDisableReason(GlowDisableReason.NONE);
						ChatUtil.sendMsg(ePlayer.getPlayer(), Message.WORLD_ALLOWED.get(), true);
					}
				}
			}

			if (MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean()) {
				if (getInstance().getAdvancedGlowVisibility() == null)
					getInstance().setAdvancedGlowVisibility(new AdvancedGlowVisibilityAddon());
			} else {
				if (getInstance().getAdvancedGlowVisibility() != null)
					getInstance().setAdvancedGlowVisibility(null);
			}
			
			try{
			    String alias = MainConfig.COMMAND_ALIAS.getString();
			    
			    if (MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null && Bukkit.getServer().getPluginCommand(alias) == null) {
			    	 final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			    	 commandMapField.setAccessible(true);
					 CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
					commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
			    }
			} catch (NoSuchFieldException  | IllegalArgumentException | IllegalAccessException e){
			    ChatUtil.reportError(e);
			}
			
			ChatUtil.sendMsg(sender, Message.RELOAD_SUCCESS.get(), true);
		} else {
			ChatUtil.sendMsg(sender, Message.RELOAD_SUCCESS.get(), true);
		}
	}
}