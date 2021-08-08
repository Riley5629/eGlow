package me.MrGraycat.eGlow.Command.SubCommands.Admin;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
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
		IEGlowPlayer eTarget = getTarget(sender, args);
		IEGlowEffect effect = null;
		
		if (eTarget == null)
			return;
		
		switch(args.length) {
		case(3):
			effect = getInstance().getDataManager().getEGlowEffect(args[2].toLowerCase().replace("off", "none").replace("disable", "none"));
			break;
		case(4):
			if (args[2].equalsIgnoreCase("glowonjoin") && Boolean.valueOf(args[3].toLowerCase()) != null) {
				eTarget.setGlowOnJoin(Boolean.valueOf(args[3].toLowerCase()));
				ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_GLOW_ON_JOIN_CONFIRM.get(eTarget, args[3].toLowerCase()));
				return;

			}
			effect = getInstance().getDataManager().getEGlowEffect(args[2] + args[3]);
			break;
		case(5):
			effect = getInstance().getDataManager().getEGlowEffect(args[2] + args[3] + args[4]);
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
				ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.OTHER_PLAYER_DISGUISE.get());
				return;
			}
			
			if (EGlowMainConfig.OptionDisableGlowWhenInvisible() && eTarget.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
				ChatUtil.sendMsgWithPrefix(ePlayer.getPlayer(), Message.OTHER_PLAYER_INVISIBLE.get());
				return;
			}
		}
		
		if (effect.getName().equals("none")) {
			if (eTarget.getGlowStatus())
				eTarget.toggleGlow();
			
			if (eTarget.getEntityType().equals("PLAYER") && EGlowMainConfig.OptionSendTargetNotification() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
				ChatUtil.sendMsgWithPrefix(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get());
			ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_CONFIRM_OFF.get(eTarget));
			return;
		}
		
		if (!eTarget.isSameGlow(effect)) {
			eTarget.disableGlow(true);
			eTarget.activateGlow(effect);
			
			if (eTarget.getEntityType().equals("PLAYER") && EGlowMainConfig.OptionSendTargetNotification() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
				ChatUtil.sendMsgWithPrefix(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.NEW_GLOW.get(effect.getDisplayName()));
			ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_CONFIRM.get(eTarget, effect.getDisplayName()));
			return;
		}
		
		ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_CONFIRM.get(eTarget, effect.getDisplayName()));
	}
}
