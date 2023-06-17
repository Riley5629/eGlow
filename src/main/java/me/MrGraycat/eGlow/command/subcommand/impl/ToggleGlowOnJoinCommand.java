package me.MrGraycat.eGlow.command.subcommand.impl;

import me.MrGraycat.eGlow.command.subcommand.SubCommand;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import org.bukkit.command.CommandSender;

public class ToggleGlowOnJoinCommand extends SubCommand {

	@Override
	public String getName() {
		return "toggleglowonjoin";
	}

	@Override
	public String getDescription() {
		return "Toggle your glow on join status";
	}

	@Override
	public String getPermission() {
		return "eglow.command.toggleglowonjoin";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow toggleglowonjoin"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		ePlayer.setGlowOnJoin(!ePlayer.isGlowOnJoin());
		ChatUtil.sendMessage(sender, EGlowMessageConfig.Message.GLOWONJOIN_TOGGLE.get(String.valueOf(ePlayer.isGlowOnJoin())), true);
	}
}
