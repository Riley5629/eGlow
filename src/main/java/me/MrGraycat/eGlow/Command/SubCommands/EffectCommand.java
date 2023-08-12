package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class EffectCommand extends SubCommand {
	@Override
	public String getName() {
		return "effect";
	}

	@Override
	public String getPermission() {
		return "";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow <color>",
				"/eGlow blink <color> <speed>",
				"/eGlow <effect> <speed>"};
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

		EGlowEffect eGlowEffect = null;
		EGlowEffect currentEGlowEffect = eGlowPlayer.getGlowEffect();

		switch (args.length) {
			case (1):
				eGlowEffect = DataManager.getEGlowEffect(args[0].replace("off", "none").replace("disable", "none"));

				if (eGlowEffect == null && currentEGlowEffect != null) {
					if (currentEGlowEffect.getName().contains(args[0].toLowerCase())) {
						eGlowEffect = switchEffectSpeed(currentEGlowEffect);
					} else {
						eGlowEffect = DataManager.getEGlowEffect(args[0].toLowerCase() + currentEGlowEffect.getName() + "slow");
					}
				}
				break;
			case (2):
				eGlowEffect = DataManager.getEGlowEffect(args[0] + args[1]);

				if (eGlowEffect == null && currentEGlowEffect != null) {
					if (currentEGlowEffect.getName().contains((args[0] + args[1]).toLowerCase()))
						eGlowEffect = switchEffectSpeed(currentEGlowEffect);
				}
				break;
			case (3):
				eGlowEffect = DataManager.getEGlowEffect(args[0] + args[1] + args[2]);
				break;
		}

		if (eGlowEffect == null) {
			sendSyntax(sender);
			return;
		}

		if (eGlowPlayer.hasPermission(eGlowEffect.getPermissionNode()) || (DataManager.isCustomEffect(eGlowEffect.getName()) && eGlowPlayer.hasPermission("eglow.egloweffect.*"))) {
			if (eGlowEffect.getName().equals("none")) {
				eGlowPlayer.disableGlow(false);
				ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
			} else {
				if (eGlowPlayer.isSameGlow(eGlowEffect)) {
					ChatUtil.sendMsg(sender, Message.SAME_GLOW.get(), true);
					return;
				}

				eGlowPlayer.activateGlow(eGlowEffect);
				ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(eGlowEffect.getDisplayName()), true);

				if (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
			}
		} else {
			ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
		}
	}

	private EGlowEffect switchEffectSpeed(EGlowEffect eGlowEffect) {
		String effectName = eGlowEffect.getName();

		if (effectName.contains("slow")) {
			return DataManager.getEGlowEffect(effectName.replace("slow", "fast"));
		} else {
			return DataManager.getEGlowEffect(effectName.replace("fast", "slow"));
		}
	}
}