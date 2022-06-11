package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import java.util.List;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class SetCommand extends SubCommand {

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public String getDescription() {
		return "Set an effect for a player/NPC";
	}

	@Override
	public String getPermission() {
		return "eglow.command.set";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow set <player/npc> <color>", 
							 "/eGlow set <player/npc> blink <color> <speed>",
							 "/eGlow set <player/npc> <effect> <speed>", 
							 "/eGlow set <player/npc> glowonjoin <true/false>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		List<IEGlowPlayer> eTargets = getTarget(sender, args);

		if (eTargets == null) {
			sendSyntax(sender, "", true);
			sendSyntax(sender, getSyntax()[0], false);
			sendSyntax(sender, getSyntax()[1], false);
			sendSyntax(sender, getSyntax()[2], false);
			sendSyntax(sender, getSyntax()[3], false);
			return;
		}

		for (IEGlowPlayer eTarget : eTargets) {
			IEGlowEffect effect = null;
			
			if (eTarget == null)
				continue;
			
			switch(args.length) {
			case(3):
				effect = DataManager.getEGlowEffect(args[2].toLowerCase().replace("off", "none").replace("disable", "none"));
				break;
			case(4):
				if (args[2].equalsIgnoreCase("glowonjoin")) {
					eTarget.setGlowOnJoin(Boolean.parseBoolean(args[3].toLowerCase()));
					ChatUtil.sendMsg(sender, Message.OTHER_GLOW_ON_JOIN_CONFIRM.get(eTarget, args[3].toLowerCase()), true);
					continue;

				}
				effect = DataManager.getEGlowEffect(args[2] + args[3]);
				break;
			case(5):
				effect = DataManager.getEGlowEffect(args[2] + args[3] + args[4]);
				break;
			}
			
			if (effect == null) {
				sendSyntax(sender, "", true);
				sendSyntax(sender, getSyntax()[0], false);
				sendSyntax(sender, getSyntax()[1], false);
				sendSyntax(sender, getSyntax()[2], false);
				sendSyntax(sender, getSyntax()[3], false);
				return;
			}
			
			if (eTarget.getEntityType().equals("PLAYER")) {
				if (eTarget.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMsg(sender, Message.OTHER_PLAYER_DISGUISE.get(), true);
					continue;
				}
				
				if (MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean() && eTarget.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
					ChatUtil.sendMsg(sender, Message.OTHER_PLAYER_INVISIBLE.get(), true);
					continue;
				}
			}
			
			if (effect.getName().equals("none")) {
				if (eTarget.getGlowStatus() || eTarget.getFakeGlowStatus())
					eTarget.toggleGlow();
				
				if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMsg(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get(), true);
				ChatUtil.sendMsg(sender, Message.OTHER_CONFIRM_OFF.get(eTarget), true);
				continue;
			}
			
			if (!eTarget.isSameGlow(effect)) {
				eTarget.disableGlow(true);
				eTarget.activateGlow(effect);
				
				if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMsg(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.NEW_GLOW.get(effect.getDisplayName()), true);
			}
			
			ChatUtil.sendMsg(sender, Message.OTHER_CONFIRM.get(eTarget, effect.getDisplayName()), true);
		}
	}
}
