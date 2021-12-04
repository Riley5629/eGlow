package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

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
		if (getInstance().getMainConfig().reloadConfig() && getInstance().getMessageConfig().reloadConfig() && getInstance().getCustomEffectConfig().reloadConfig()) {
			getInstance().getDataManager().addEGlowEffects();
			getInstance().getDataManager().addCustomEffects();
			for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
				if (getInstance().getVaultAddon() != null)
					getInstance().getVaultAddon().updatePlayerTabname(ePlayer);
				ePlayer = getInstance().getDataManager().getEGlowPlayer(onlinePlayer);
				
				if (ePlayer == null)
					break;
				
				IEGlowEffect effect = ePlayer.getForceGlow();
				
				if (effect != null) {
					if (getInstance().getLibDisguiseAddon() != null && getInstance().getLibDisguiseAddon().isDisguised(ePlayer.getPlayer()) || getInstance().getIDisguiseAddon() != null && getInstance().getIDisguiseAddon().isDisguised(ePlayer.getPlayer())) {
						ePlayer.setGlowDisableReason(GlowDisableReason.DISGUISE);
						ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.DISGUISE_BLOCKED.get());
					} else {
						ePlayer.activateGlow(effect);
					}
					continue;
				}
				
				if (EGlowMainConfig.getWorldCheckEnabled()) {
					if (ePlayer.isInBlockedWorld() && (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus())) {
						ePlayer.toggleGlow();
						ePlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
						ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.WORLD_BLOCKED_RELOAD.get());
					} else {
						if (ePlayer.getGlowDisableReason() != null && ePlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
							ePlayer.toggleGlow();
							ePlayer.setGlowDisableReason(GlowDisableReason.NONE);
							ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.WORLD_ALLOWED.get());
						}
					}
				} else {
					if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
						ePlayer.toggleGlow();
						ePlayer.setGlowDisableReason(GlowDisableReason.NONE);
						ChatUtil.sendMsgWithPrefix(sender, Message.WORLD_ALLOWED.get());
					}
				}
			}
			
			try{
			    String alias = EGlowMainConfig.OptionCommandAlias();
			    
			    if (EGlowMainConfig.OptionEnableCommandAlias() && alias != null && Bukkit.getServer().getPluginCommand(alias) == null) {
			    	 final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			    	 commandMapField.setAccessible(true);
					 CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
					 commandMap.register(alias, alias, getInstance().getCommand("eglow"));
			    }
			} catch (NoSuchFieldException  | IllegalArgumentException | IllegalAccessException e){
			    ChatUtil.reportError(e);
			}
			
			//if (getInstance().getTABAddon() != null && getInstance().getTABAddon().getTABOnBukkit() && EGlowMainConfig.OptionAdvancedTABIntegration())
				//getInstance().getTABAddon().loadConfigSettings();
			
			ChatUtil.sendMsgWithPrefix(sender, Message.RELOAD_SUCCESS.get());
		} else {
			ChatUtil.sendMsgWithPrefix(sender, Message.RELOAD_SUCCESS.get());
		}
	}
}