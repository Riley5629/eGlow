package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
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
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		String cmd = "&f- &eeGlow &f";

		ChatUtil.sendPlainMessage(commandSender, "&f&m                 &r &fCommands for &eeGlow &r&f&m                 ", false);
		ChatUtil.sendPlainMessage(commandSender, "&fUser commands:", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "(&eOpens GUI&f)", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "help", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "toggle", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "toggleglowonjoin", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "visibility <&eall&f/&eother&f/&eown&f/&enone&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "list", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "<&eColor&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "<&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "<&eEffect&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(commandSender, "&fAdmin commands:", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eColor&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "set <&ePlayer&f/&eNPC*&f> <&eEffect&f> <&eSpeed>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "set <&ePlayer&f> glowonjoin <&eTrue&f/&eFalse&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "unset <&ePlayer&f/&eNPC*&f>", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "debug", false);
		ChatUtil.sendPlainMessage(commandSender, cmd + "reload", false);
		ChatUtil.sendPlainMessage(commandSender, "&f*&enpc:s&f, &enpc:sel&f, &enpc:selected&f, &enpc:<ID>", false);
		ChatUtil.sendPlainMessage(commandSender, "&f&m                                                             ", false);
	}
}