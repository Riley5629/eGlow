package me.mrgraycat.eglow.command.subcommands.admin;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand extends SubCommand {

	@Override
	public String getName() {
		return "debug";
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
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		ChatUtil.sendPlainMsg(sender, "&f&m                        &r &fDebug info for &eeGlow: &f&m                          ", false);

		if (args.length >= 2) {
			Player player = Bukkit.getPlayer(args[1]);

			if (player != null)
				eGlowPlayer = DataManager.getEGlowPlayer(player);
		}

		DebugUtil.sendDebug(sender, eGlowPlayer);
		ChatUtil.sendPlainMsg(sender, "&f&m                                                                               ", false);
	}
}