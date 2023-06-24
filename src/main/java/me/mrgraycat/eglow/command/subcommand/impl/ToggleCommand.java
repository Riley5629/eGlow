package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends SubCommand {

	@Override
	public String getName() {
		return "toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle your glow on/off";
	}

	@Override
	public String getPermission() {
		return "eglow.command.toggle";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow toggle"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		if (eGlowPlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(commandSender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (eGlowPlayer.isGlowing()) {
			eGlowPlayer.disableGlow(false);
			ChatUtil.sendMessage(commandSender, Message.DISABLE_GLOW.get(), true);
		} else {
			if (eGlowPlayer.getGlowEffect() == null || eGlowPlayer.getGlowEffect().getName().equals("none")) {
				ChatUtil.sendMessage(commandSender, Message.NO_LAST_GLOW.get(), true);
			} else {
				if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMessage(commandSender, Message.DISGUISE_BLOCKED.get(), true);
					return;
				}

				if (eGlowPlayer.isInvisible()) {
					ChatUtil.sendMessage(commandSender, Message.INVISIBILITY_BLOCKED.get(), true);
					return;
				}

				if (eGlowPlayer.getPlayer().hasPermission(eGlowPlayer.getGlowEffect().getPermission()) || eGlowPlayer.isForcedGlow(eGlowPlayer.getGlowEffect())) {
					eGlowPlayer.activateGlow();
				} else {
					ChatUtil.sendMessage(commandSender, Message.NO_PERMISSION.get(), true);
					return;
				}
				ChatUtil.sendMessage(commandSender, Message.NEW_GLOW.get(eGlowPlayer.getLastGlowName()), true);
			}
		}
	}
}