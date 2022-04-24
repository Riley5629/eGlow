package me.MrGraycat.eGlow.Command.SubCommands;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class HelpCommand extends SubCommand {

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "Shows all the available commands.";
	}

	@Override
	public String getPermission() {
		return "eglow.command.help";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow help"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		String cmd = "&f- &eeGlow &f";
		
		ChatUtil.sendPlainMsg(sender, "&f&m                 &r &fCommands for &eeGlow &r&f&m                 ", false);
		ChatUtil.sendPlainMsg(sender, "&fUser commands:", false);
		ChatUtil.sendPlainMsg(sender, cmd + "(&eOpens GUI&f)", false);
		ChatUtil.sendPlainMsg(sender, cmd + "help", false);
		ChatUtil.sendPlainMsg(sender, cmd + "toggle", false);
		ChatUtil.sendPlainMsg(sender, cmd + "visibility <&eall&f/&eown&f/&enone&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "list", false);
		ChatUtil.sendPlainMsg(sender, cmd + "<&eColor&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "<&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "<&eEffect&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, "&fAdmin commands:", false);
		ChatUtil.sendPlainMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eColor&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eEffect&f> <&eSpeed>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "set <&ePlayer&f> glowonjoin <&eTrue&f/&eFalse&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "unset <&ePlayer&f/&eNPC*&f>", false);
		ChatUtil.sendPlainMsg(sender, cmd + "debug", false);
		ChatUtil.sendPlainMsg(sender, cmd + "reload", false);
		ChatUtil.sendPlainMsg(sender, "&f*&enpc:s&f, &enpc:sel&f, &enpc:selected&f, &enpc:<ID>", false);
		ChatUtil.sendPlainMsg(sender, "&f&m                                                             ", false);
	}
}