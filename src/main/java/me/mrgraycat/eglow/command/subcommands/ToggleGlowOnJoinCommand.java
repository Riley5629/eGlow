package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ToggleGlowOnJoinCommand extends SubCommand {

	@Override
	public String getName() {
		return "toggleglowonjoin";
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
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		eGlowPlayer.setGlowOnJoin(!eGlowPlayer.isGlowOnJoin());
		ChatUtil.sendMsg(sender, EGlowMessageConfig.Message.GLOWONJOIN_TOGGLE.get(String.valueOf(eGlowPlayer.isGlowOnJoin())), true);
	}
}