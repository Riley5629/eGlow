package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.Common.GlowVisibility;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.util.effect.EffectMapper;
import org.bukkit.command.CommandSender;

public class EffectCommand extends SubCommand {

	@Override
	public String getName() {
		return "effect";
	}

	@Override
	public String getDescription() {
		return "Activate a glow effect";
	}

	@Override
	public String getPermission() {
		return "";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow <color>", "/eGlow blink <color> <speed>", "/eGlow <effect> <speed>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		if (eGlowPlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(commandSender, Message.WORLD_BLOCKED.get(), true);
			return;
		}

		if (eGlowPlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			ChatUtil.sendMessage(commandSender, Message.DISGUISE_BLOCKED.get(), true);
			return;
		}

		if (eGlowPlayer.isInvisible()) {
			ChatUtil.sendMessage(commandSender, Message.INVISIBILITY_BLOCKED.get(), true);
			return;
		}

		IEGlowEffect effect = null;

		switch (args.length) {
			case 1:
				effect = DataManager.getEGlowEffect(args[0].replace("off", "none").replace("disable", "none"));

				if (effect == null && eGlowPlayer.getGlowEffect() != null) {
					IEGlowEffect effectNew = null;

					if (eGlowPlayer.getGlowEffect().getName().contains(args[0].toLowerCase())) {
						effectNew = EffectMapper.flip(eGlowPlayer.getGlowEffect());
					} else if (DataManager.getEGlowEffect(args[0].toLowerCase() + eGlowPlayer.getGlowEffect().getName() + "slow") != null) {
						effectNew = DataManager.getEGlowEffect(args[0].toLowerCase() + eGlowPlayer.getGlowEffect().getName() + "slow");
					}

					if (effectNew != null) {
						if (!commandSender.hasPermission(effectNew.getPermission())) {
							ChatUtil.sendMessage(commandSender, Message.NO_PERMISSION.get(), true);
							return;
						}

						eGlowPlayer.disableGlow(true);
						eGlowPlayer.activateGlow(effectNew);
						ChatUtil.sendMessage(commandSender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
						return;
					}
				}
				break;
			case 2:
				effect = DataManager.getEGlowEffect(args[0] + args[1]);

				if (effect == null && eGlowPlayer.getGlowEffect() != null && eGlowPlayer.getGlowEffect().getName().contains(args[0].toLowerCase() + args[1].toLowerCase())) {
					IEGlowEffect effectNew = EffectMapper.flip(eGlowPlayer.getGlowEffect());

					if (effectNew != null) {
						if (!commandSender.hasPermission(effectNew.getPermission())) {
							ChatUtil.sendMessage(commandSender, Message.NO_PERMISSION.get(), true);
							return;
						}

						eGlowPlayer.disableGlow(true);
						eGlowPlayer.activateGlow(effectNew);
						ChatUtil.sendMessage(commandSender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
						return;
					}
				}
				break;
			case 3:
				effect = DataManager.getEGlowEffect(args[0] + args[1] + args[2]);
				break;
		}

		if (effect == null) {
			sendSyntax(commandSender);
			return;
		}

		if (eGlowPlayer.getPlayer().hasPermission(effect.getPermission()) || DataManager.isCustomEffect(effect.getName()) && eGlowPlayer.getPlayer().hasPermission("eglow.effect.*")) {
			if (effect.getName().equals("none")) {
				if (eGlowPlayer.getGlowStatus() || eGlowPlayer.getFakeGlowStatus()) {
					eGlowPlayer.disableGlow(false);
				}
				ChatUtil.sendMessage(commandSender, Message.DISABLE_GLOW.get(), true);
				return;
			}

			if (!eGlowPlayer.isSameGlow(effect)) {
				eGlowPlayer.disableGlow(true);
				eGlowPlayer.activateGlow(effect);

				ChatUtil.sendMessage(commandSender, Message.NEW_GLOW.get(effect.getDisplayName()), true);

				if (eGlowPlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMessage(commandSender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}

			ChatUtil.sendMessage(commandSender, Message.SAME_GLOW.get(), true);
			return;
		}

		ChatUtil.sendMessage(commandSender, Message.NO_PERMISSION.get(), true);
	}
}