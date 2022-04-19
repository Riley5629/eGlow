package me.MrGraycat.eGlow.Command.SubCommands;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

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
		return new String[] {"/eGlow toggle"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMsgWithPrefix(sender, Message.WORLD_BLOCKED.get());
			return;
		}
		
		if (ePlayer.getFakeGlowStatus() || ePlayer.getGlowStatus()) {
			ePlayer.toggleGlow();
			ChatUtil.sendMsgWithPrefix(sender, Message.DISABLE_GLOW.get());
		} else {
			if (ePlayer.getEffect() == null || ePlayer.getEffect().getName().equals("none")) {
				ChatUtil.sendMsgWithPrefix(sender, Message.NO_LAST_GLOW.get());
			} else {
				if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMsgWithPrefix(sender, Message.DISGUISE_BLOCKED.get());
					return;
				}
				
				if (EGlowMainConfig.OptionDisableGlowWhenInvisible() && ePlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
					ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.INVISIBILITY_BLOCKED.get());
					return;
				}
				
				if (ePlayer.getPlayer().hasPermission(ePlayer.getEffect().getPermission()) || ePlayer.isForcedGlow(ePlayer.getEffect())) {
					ePlayer.toggleGlow();
				} else {
					ChatUtil.sendMsgWithPrefix(sender, Message.NO_PERMISSION.get());
					return;
				}	
				ChatUtil.sendMsgWithPrefix(sender, Message.NEW_GLOW.get(ePlayer.getLastGlowName()));
			}
		}
	}
}
