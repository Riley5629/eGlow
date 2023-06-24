package me.mrgraycat.eglow.command.subcommand.impl.admin;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.util.debug.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand extends SubCommand {

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String getDescription() {
		return "Debug info";
	}

	@Override
	public String getPermission() {
		return "eglow.command.debug";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow debug"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		ChatUtil.sendPlainMessage(commandSender, "&f&m                        &r &fDebug info for &eeGlow: &f&m                          ", false);
		IEGlowPlayer target = eGlowPlayer;

		if (args.length >= 2) {
			Player player = Bukkit.getPlayer(args[1]);

			if (player != null) {
				target = DataManager.getEGlowPlayer(player);
			}
		}

		DebugLogger.sendDebug(commandSender, target);
		ChatUtil.sendPlainMessage(commandSender, "&f&m                                                                               ", false);
	}
}