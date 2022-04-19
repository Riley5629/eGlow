package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import java.util.List;

import org.bukkit.command.CommandSender;
import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class UnsetCommand extends SubCommand {
	@Override
	public String getName() {
		return "unset";
	}

	@Override
	public String getDescription() {
		return "Stop the glowing of a player/NPC";
	}

	@Override
	public String getPermission() {
		return "eglow.command.unset";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow unset <player/npc>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		List<IEGlowPlayer> eTargets = getTarget(sender, args);

		if (eTargets == null) {
			sendSyntax(sender, "", true);
			return;
		}

		for (IEGlowPlayer eTarget : eTargets) {
			if (eTarget == null)
				continue;
			
			if (eTarget.getFakeGlowStatus() || eTarget.getGlowStatus()) {
				eTarget.toggleGlow();
				
				if (eTarget.getEntityType().equals("PLAYER") && EGlowMainConfig.OptionSendTargetNotification())
					ChatUtil.sendMsgWithPrefix(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get());
			}
				

			ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_CONFIRM_OFF.get(eTarget));
		}	
	}
}
