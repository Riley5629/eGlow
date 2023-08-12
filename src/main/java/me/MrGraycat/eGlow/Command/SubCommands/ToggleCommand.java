package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends SubCommand {

	@Override
	public String getName() {
		return "toggle";
	}

	@Override
	public String getPermission() {
		return "eglow.command.toggle";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow toggle"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		if (eGlowPlayer.isGlowing()) {
			eGlowPlayer.disableGlow(false);
			ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
		} else {
			if (eGlowPlayer.getGlowEffect() == null || eGlowPlayer.getGlowEffect().getName().equals("none")) {
				ChatUtil.sendMsg(sender, Message.NO_LAST_GLOW.get(), true);
				return;
			}

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

			EGlowEffect currentEGlowEffect = eGlowPlayer.getGlowEffect();

			if (eGlowPlayer.hasPermission(currentEGlowEffect.getPermissionNode()) || (DataManager.isCustomEffect(currentEGlowEffect.getName()) && eGlowPlayer.hasPermission("eglow.egloweffect.*")) || eGlowPlayer.isForcedGlow(currentEGlowEffect)) {
				eGlowPlayer.activateGlow();
			} else {
				ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
				return;
			}
			ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(eGlowPlayer.getLastGlowName()), true);
		}
	}
}