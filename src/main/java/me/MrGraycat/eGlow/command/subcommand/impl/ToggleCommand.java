package me.MrGraycat.eGlow.command.subcommand.impl;

import me.MrGraycat.eGlow.command.subcommand.SubCommand;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.Common.GlowDisableReason;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
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
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (ePlayer.isGlowing()) {
			ePlayer.disableGlow(false);
			ChatUtil.sendMessage(sender, Message.DISABLE_GLOW.get(), true);
		} else {
			if (ePlayer.getGlowEffect() == null || ePlayer.getGlowEffect().getName().equals("none")) {
				ChatUtil.sendMessage(sender, Message.NO_LAST_GLOW.get(), true);
			} else {
				if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMessage(sender, Message.DISGUISE_BLOCKED.get(), true);
					return;
				}

				if (ePlayer.isInvisible()) {
					ChatUtil.sendMessage(sender, Message.INVISIBILITY_BLOCKED.get(), true);
					return;
				}

				if (ePlayer.getPlayer().hasPermission(ePlayer.getGlowEffect().getPermission()) || ePlayer.isForcedGlow(ePlayer.getGlowEffect())) {
					ePlayer.activateGlow();
				} else {
					ChatUtil.sendMessage(sender, Message.NO_PERMISSION.get(), true);
					return;
				}
				ChatUtil.sendMessage(sender, Message.NEW_GLOW.get(ePlayer.getLastGlowName()), true);
			}
		}
	}
}