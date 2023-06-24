package me.MrGraycat.eGlow.Command.SubCommands;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
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
			ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (ePlayer.isGlowing()) {
			ePlayer.disableGlow(false);
			ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
		} else {
			if (ePlayer.getEffect() == null || ePlayer.getEffect().getName().equals("none")) {
				ChatUtil.sendMsg(sender, Message.NO_LAST_GLOW.get(), true);
			} else {
				if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMsg(sender, Message.DISGUISE_BLOCKED.get(), true);
					return;
				}

				if (ePlayer.isInvisible()) {
					ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
					return;
				}

				if (ePlayer.getPlayer().hasPermission(ePlayer.getEffect().getPermission()) || ePlayer.isForcedGlow(ePlayer.getEffect())) {
					ePlayer.activateGlow();
				} else {
					ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
					return;
				}
				ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(ePlayer.getLastGlowName()), true);
			}
		}
	}
}