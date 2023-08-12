package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.gui.menus.EGlowMainMenu;
import me.mrgraycat.eglow.util.enums.EnumUtil;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class GUICommand extends SubCommand {

	@Override
	public String getName() {
		return "gui";
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
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		switch (eGlowPlayer.getGlowDisableReason()) {
			case BLOCKEDWORLD:
				ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
				return;
			case INVISIBLE:
				ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
				return;
			case ANIMATION:
				ChatUtil.sendMsg(sender, Message.ANIMATION_BLOCKED.get(), true);
				return;
		}

		if (eGlowPlayer.getGlowVisibility().equals(EnumUtil.GlowVisibility.UNSUPPORTEDCLIENT))
			ChatUtil.sendPlainMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
		
		new EGlowMainMenu(eGlowPlayer).openInventory();
	}
}