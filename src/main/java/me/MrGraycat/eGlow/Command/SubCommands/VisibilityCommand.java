package me.MrGraycat.eGlow.Command.SubCommands;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.command.CommandSender;

public class VisibilityCommand extends SubCommand {

	@Override
	public String getName() {
		return "visibility";
	}

	@Override
	public String getDescription() {
		return "Set the way you see the glowing.";
	}

	@Override
	public String getPermission() {
		return "eglow.command.visibility";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow visibility <all/own/none>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (args.length >= 2) {
			if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
				ChatUtil.sendMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}
			
			if (!args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("own") && !args[1].equalsIgnoreCase("none")) {
				sendSyntax(sender, getSyntax()[0], true);
				return;
			}
			
			GlowVisibility oldVisibility = ePlayer.getGlowVisibility();
			GlowVisibility newVisibility = GlowVisibility.valueOf(args[1].toUpperCase());
			
			if (!ePlayer.getGlowVisibility().equals(newVisibility) && !ePlayer.getSaveData())
					ePlayer.setSaveData(true);
				
			ePlayer.setGlowVisibility(newVisibility);
			
			if (oldVisibility != newVisibility) 
				PacketUtil.forceUpdateGlow(ePlayer);
			
			ChatUtil.sendMsg(sender, Message.VISIBILITY_CHANGE.get(newVisibility.getVisibility()), true);
		} else {
			sendSyntax(sender, getSyntax()[0], true);
		}
	}
}
