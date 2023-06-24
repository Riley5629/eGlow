package me.MrGraycat.eGlow.command.subcommand.impl;

import me.MrGraycat.eGlow.command.subcommand.SubCommand;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.manager.glow.IEGlowEffect;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.effect.EffectMapper;
import me.MrGraycat.eGlow.util.Common.GlowDisableReason;
import me.MrGraycat.eGlow.util.Common.GlowVisibility;
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
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMessage(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}
		
		if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			ChatUtil.sendMessage(sender, Message.DISGUISE_BLOCKED.get(), true);
			return;
		}

		if (ePlayer.isInvisible()) {
			ChatUtil.sendMessage(sender, Message.INVISIBILITY_BLOCKED.get(), true);
			return;
		}

		IEGlowEffect effect = null;

		switch (args.length) {
			case 1:
				effect = DataManager.getEGlowEffect(args[0].replace("off", "none").replace("disable", "none"));

				if (effect == null && ePlayer.getGlowEffect() != null) {
					IEGlowEffect effectNew = null;

					if (ePlayer.getGlowEffect().getName().contains(args[0].toLowerCase())) {
						effectNew = EffectMapper.flip(ePlayer.getGlowEffect());
					} else if (DataManager.getEGlowEffect(args[0].toLowerCase() + ePlayer.getGlowEffect().getName() + "slow") != null) {
						effectNew = DataManager.getEGlowEffect(args[0].toLowerCase() + ePlayer.getGlowEffect().getName() + "slow");
					}

					if (effectNew != null) {
						if (!sender.hasPermission(effectNew.getPermission())) {
							ChatUtil.sendMessage(sender, Message.NO_PERMISSION.get(), true);
							return;
						}

						ePlayer.disableGlow(true);
						ePlayer.activateGlow(effectNew);
						ChatUtil.sendMessage(sender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
						return;
					}
				}
				break;
			case 2:
				effect = DataManager.getEGlowEffect(args[0] + args[1]);

				if (effect == null && ePlayer.getGlowEffect() != null && ePlayer.getGlowEffect().getName().contains(args[0].toLowerCase() + args[1].toLowerCase())) {
					IEGlowEffect effectNew = EffectMapper.flip(ePlayer.getGlowEffect());

					if (effectNew != null) {
						if (!sender.hasPermission(effectNew.getPermission())) {
							ChatUtil.sendMessage(sender, Message.NO_PERMISSION.get(), true);
							return;
						}

						ePlayer.disableGlow(true);
						ePlayer.activateGlow(effectNew);
						ChatUtil.sendMessage(sender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
						return;
					}
				}
				break;
			case 3:
				effect = DataManager.getEGlowEffect(args[0] + args[1] + args[2]);
				break;
		}

		if (effect == null) {
			sendSyntax(sender);
			return;
		}

		if (ePlayer.getPlayer().hasPermission(effect.getPermission()) || DataManager.isCustomEffect(effect.getName()) && ePlayer.getPlayer().hasPermission("eglow.effect.*")) {
			if (effect.getName().equals("none")) {
				if (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus()) {
					ePlayer.disableGlow(false);
				}
				ChatUtil.sendMessage(sender, Message.DISABLE_GLOW.get(), true);
				return;
			}

			if (!ePlayer.isSameGlow(effect)) {
				ePlayer.disableGlow(true);
				ePlayer.activateGlow(effect);

				ChatUtil.sendMessage(sender, Message.NEW_GLOW.get(effect.getDisplayName()), true);

				if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMessage(sender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}

			ChatUtil.sendMessage(sender, Message.SAME_GLOW.get(), true);
			return;
		}

		ChatUtil.sendMessage(sender, Message.NO_PERMISSION.get(), true);
	}
}