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
		//TODO loop through subcommands and list it this way
		String cmd = "&f- &eeGlow &f";
		
		ChatUtil.sendMsg(sender, "&f&m                 &r &fCommands for &eeGlow &r&f&m                 ");
		ChatUtil.sendMsg(sender, "&fUser commands:");
		ChatUtil.sendMsg(sender, cmd + "(&eOpens GUI&f)");
		ChatUtil.sendMsg(sender, cmd + "help");
		ChatUtil.sendMsg(sender, cmd + "toggle");
		ChatUtil.sendMsg(sender, cmd + "visibility <&eall&f/&eown&f/&enone&f>");
		ChatUtil.sendMsg(sender, cmd + "list");
		ChatUtil.sendMsg(sender, cmd + "<&eColor&f>");
		ChatUtil.sendMsg(sender, cmd + "<&eBlink&f> <&eColor&f> <&eSpeed&f>");
		ChatUtil.sendMsg(sender, cmd + "<&eEffect&f> <&eSpeed&f>");
		ChatUtil.sendMsg(sender, "&fAdmin commands:");
		ChatUtil.sendMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eColor&f>");
		ChatUtil.sendMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eBlink&f> <&eColor&f> <&eSpeed&f>");
		ChatUtil.sendMsg(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eEffect&f> <&eSpeed>");
		ChatUtil.sendMsg(sender, cmd + "set <&ePlayer&f> glowonjoin <&eTrue&f/&eFalse&f>");
		ChatUtil.sendMsg(sender, cmd + "unset <&ePlayer&f/&eNPC*&f>");
		ChatUtil.sendMsg(sender, cmd + "debug");
		ChatUtil.sendMsg(sender, cmd + "reload");
		ChatUtil.sendMsg(sender, "&f*&enpc:s&f, &enpc:sel&f, &enpc:selected&f, &enpc:<ID>");                                                       
		ChatUtil.sendMsg(sender, "&f&m                                                             ");
	}
}
