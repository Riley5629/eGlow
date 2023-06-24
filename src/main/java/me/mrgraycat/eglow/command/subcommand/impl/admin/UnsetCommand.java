package me.mrgraycat.eglow.command.subcommand.impl.admin;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

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
		return new String[]{"/eGlow unset <player/npc>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		List<IEGlowPlayer> eTargets = getTarget(commandSender, args);

		if (eTargets == null) {
			sendSyntax(commandSender);
			return;
		}

		for (IEGlowPlayer eTarget : eTargets) {
			if (eTarget == null)
				continue;

			if (eTarget.isGlowing()) {
				eTarget.disableGlow(false);

				if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean())
					ChatUtil.sendMessage(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get(), true);
			}

			ChatUtil.sendMessage(commandSender, Message.OTHER_CONFIRM_OFF.get(eTarget), true);
		}
	}
}