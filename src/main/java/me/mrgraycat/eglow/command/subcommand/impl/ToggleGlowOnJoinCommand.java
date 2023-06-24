package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
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
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		eGlowPlayer.setGlowOnJoin(!eGlowPlayer.isGlowOnJoin());
		ChatUtil.sendMessage(commandSender, EGlowMessageConfig.Message.GLOWONJOIN_TOGGLE.get(String.valueOf(eGlowPlayer.isGlowOnJoin())), true);
	}
}
