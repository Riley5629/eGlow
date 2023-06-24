package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.menu.impl.EGlowMainMenu;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.Common.GlowVisibility;
import me.mrgraycat.eglow.util.chat.ChatUtil;
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
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		if (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
			ChatUtil.sendPlainMessage(commandSender, Message.UNSUPPORTED_GLOW.get(), true);

		if (eGlowPlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(commandSender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (eGlowPlayer.isInvisible()) {
			ChatUtil.sendMessage(commandSender, Message.INVISIBILITY_BLOCKED.get(), true);
			return;
		}

		if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			ChatUtil.sendMessage(commandSender, Message.DISGUISE_BLOCKED.get(), true);
			return;
		}

		new EGlowMainMenu(eGlowPlayer.getPlayer()).openInventory();
	}
}
