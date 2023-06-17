package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.command.subcommand.SubCommand;
import org.bukkit.command.CommandSender;

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
		return new String[]{"/eGlow help"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		String cmd = "&f- &eeGlow &f";

		ChatUtil.sendPlainMessage(sender, "&f&m                 &r &fCommands for &eeGlow &r&f&m                 ", false);
		ChatUtil.sendPlainMessage(sender, "&fUser commands:", false);
		ChatUtil.sendPlainMessage(sender, cmd + "(&eOpens GUI&f)", false);
		ChatUtil.sendPlainMessage(sender, cmd + "help", false);
		ChatUtil.sendPlainMessage(sender, cmd + "toggle", false);
		ChatUtil.sendPlainMessage(sender, cmd + "toggleglowonjoin", false);
		ChatUtil.sendPlainMessage(sender, cmd + "visibility <&eall&f/&eother&f/&eown&f/&enone&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "list", false);
		ChatUtil.sendPlainMessage(sender, cmd + "<&eColor&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "<&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "<&eEffect&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(sender, "&fAdmin commands:", false);
		ChatUtil.sendPlainMessage(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eColor&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eEffect&f> <&eSpeed>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "set <&ePlayer&f> glowonjoin <&eTrue&f/&eFalse&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "unset <&ePlayer&f/&eNPC*&f>", false);
		ChatUtil.sendPlainMessage(sender, cmd + "debug", false);
		ChatUtil.sendPlainMessage(sender, cmd + "reload", false);
		ChatUtil.sendPlainMessage(sender, "&f*&enpc:s&f, &enpc:sel&f, &enpc:selected&f, &enpc:<ID>", false);
		ChatUtil.sendPlainMessage(sender, "&f&m                                                             ", false);
	}
}