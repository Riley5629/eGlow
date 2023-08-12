package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {

	@Override
	public String getName() {
		return "help";
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
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		String prefix = "&f- &eeGlow &f";

		ChatUtil.sendPlainMsg(sender, "&f&m                 &r &fCommands for &eeGlow &r&f&m                 ", false);
		ChatUtil.sendPlainMsg(sender, "&fUser commands:", false);
		ChatUtil.sendPlainMsg(sender, prefix + "(&eOpens GUI&f)", false);
		ChatUtil.sendPlainMsg(sender, prefix + "help", false);
		ChatUtil.sendPlainMsg(sender, prefix + "toggle", false);
		ChatUtil.sendPlainMsg(sender, prefix + "toggleglowonjoin", false);
		ChatUtil.sendPlainMsg(sender, prefix + "visibility <&eall&f/&eother&f/&eown&f/&enone&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "list", false);
		ChatUtil.sendPlainMsg(sender, prefix + "<&eColor&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "<&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "<&eEffect&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, "&fAdmin commands:", false);
		ChatUtil.sendPlainMsg(sender, prefix + "set <&ePlayer&f/&eNPC*&f> <&eColor&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "set <&ePlayer&f/&eNPC*&f> <&eBlink&f> <&eColor&f> <&eSpeed&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "set <&ePlayer&f/&eNPC*&f> <&eEffect&f> <&eSpeed>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "set <&ePlayer&f> glowonjoin <&eTrue&f/&eFalse&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "unset <&ePlayer&f/&eNPC*&f>", false);
		ChatUtil.sendPlainMsg(sender, prefix + "debug", false);
		ChatUtil.sendPlainMsg(sender, prefix + "reload", false);
		ChatUtil.sendPlainMsg(sender, "&f*&enpc:s&f, &enpc:sel&f, &enpc:selected&f, &enpc:<ID>", false);
		ChatUtil.sendPlainMsg(sender, "&f&m                                                             ", false);
	}
}