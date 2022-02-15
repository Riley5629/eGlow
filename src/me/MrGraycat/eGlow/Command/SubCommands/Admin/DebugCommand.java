package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class DebugCommand extends SubCommand {

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String getDescription() {
		return "Debug info";
	}

	@Override
	public String getPermission() {
		return "eglow.command.debug";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow debug"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		//ChatUtil.sendMsg(sender, "DEBUG: " + getInstance().getTABAddon().tabPlugin + " " + getInstance().getTABAddon().tabPlugin.getDescription().getVersion() + " | " + getInstance().getTABAddon().version + " " + getInstance().getDebugUtil().pluginCheck("TAB") + getInstance().getDebugUtil().getPlugin("TAB").getClass().getName().startsWith("me.neznamy.tab"));
		ChatUtil.sendMsg(sender, "&f&m                        &r &fDebug info for &eeGlow: &f&m                          ");
		IEGlowPlayer target = ePlayer;
		if (args.length >= 2) {
			Player player = Bukkit.getPlayer(args[1]);
			
			if (player != null) {
				IEGlowPlayer eTarget = DataManager.getEGlowPlayer(player);
				target = eTarget;
			}	
		}
		
		DebugUtil.sendDebug(sender, target);
		ChatUtil.sendMsg(sender, "&f&m                                                                               ");
	}
}
