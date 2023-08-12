package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class VisibilityCommand extends SubCommand {

	@Override
	public String getName() {
		return "visibility";
	}

	@Override
	public String getPermission() {
		return "eglow.command.visibility";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow visibility <all/other/own/none>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		if (args.length >= 2) {
			if (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
				ChatUtil.sendMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}

			switch (args[1].toLowerCase()) {
				case "all":
				case "other":
				case "own":
				case "none":
					break;
				default:
					sendSyntax(sender);
					return;
			}

			GlowVisibility oldVisibility = eGlowPlayer.getGlowVisibility();
			GlowVisibility newVisibility = GlowVisibility.valueOf(args[1].toUpperCase());

			if (!oldVisibility.equals(newVisibility)) {
				eGlowPlayer.setGlowVisibility(newVisibility);
				PacketUtil.forceUpdateGlow(eGlowPlayer);
			}

			ChatUtil.sendMsg(sender, Message.VISIBILITY_CHANGE.get(newVisibility.name()), true);
		} else {
			switch (eGlowPlayer.getGlowVisibility()) {
				case ALL:
					eGlowPlayer.setGlowVisibility(GlowVisibility.OTHER);
					break;
				case OTHER:
					eGlowPlayer.setGlowVisibility(GlowVisibility.OWN);
					break;
				case OWN:
					eGlowPlayer.setGlowVisibility(GlowVisibility.NONE);
					break;
				case NONE:
					eGlowPlayer.setGlowVisibility(GlowVisibility.ALL);
					break;
			}

			PacketUtil.forceUpdateGlow(eGlowPlayer);
			ChatUtil.sendMsg(sender, Message.VISIBILITY_CHANGE.get(eGlowPlayer.getGlowVisibility().name()), true);
		}
	}
}