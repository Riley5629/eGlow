package me.MrGraycat.eGlow.command.subcommand.impl;

import me.MrGraycat.eGlow.command.subcommand.SubCommand;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.menu.impl.EGlowMainMenu;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.Common.GlowDisableReason;
import me.MrGraycat.eGlow.util.Common.GlowVisibility;
import org.bukkit.command.CommandSender;

public class GUICommand extends SubCommand {

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	public String getDescription() {
		return "Opens GUI.";
	}

	@Override
	public String getPermission() {
		return "eglow.command.gui";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
			ChatUtil.sendPlainMessage(sender, Message.UNSUPPORTED_GLOW.get(), true);

		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (ePlayer.isInvisible()) {
			ChatUtil.sendMessage(sender, Message.INVISIBILITY_BLOCKED.get(), true);
			return;
		}

		if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			ChatUtil.sendMessage(sender, Message.DISGUISE_BLOCKED.get(), true);
			return;
		}

		new EGlowMainMenu(ePlayer.getPlayer()).openInventory();
	}
}
