package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.menu.impl.EGlowMainMenu;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.Common.GlowVisibility;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.command.subcommand.SubCommand;
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
