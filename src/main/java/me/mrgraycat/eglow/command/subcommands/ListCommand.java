package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig.Effect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ListCommand extends SubCommand {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getPermission() {
		return "eglow.command.list";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow list"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		ChatUtil.sendPlainMsg(sender, "&m        &r &fColors & effects for &eeGlow&f: &m          ", false);
		ChatUtil.sendPlainMsg(sender, "&fColors:", false);
		ChatUtil.sendPlainMsg(sender, "&cred&f, &4darkred&f, &6gold&f,", false);
		ChatUtil.sendPlainMsg(sender, "&eyellow&f, &agreen&f, &2darkgreen&f,", false);
		ChatUtil.sendPlainMsg(sender, "&baqua&f, &3darkaqua&f, &9blue&f,", false);
		ChatUtil.sendPlainMsg(sender, "&1darkblue&f, &5purple&f, &dpink&f,", false);
		ChatUtil.sendPlainMsg(sender, "&fwhite&f, &7gray&f, &8darkgray&f,", false);
		ChatUtil.sendPlainMsg(sender, "&0black&f.", false);
		ChatUtil.sendPlainMsg(sender, "&eoff&f, &edisable&f or &enone &fwill stop the glow.", false);
		ChatUtil.sendPlainMsg(sender, "&fEffects:", false);
		ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("rainbowslow") + ", " + ChatUtil.getEffectName("rainbowfast"), false);
		ChatUtil.sendPlainMsg(sender, "&fCustom effects:", false);

		StringBuilder text = new StringBuilder();
		int i = 1;

		for (String effect : Effect.GET_ALL_EFFECTS.get()) {
			text.append(ChatUtil.getEffectName(effect)).append(", ");

			if (i == 3) {
				ChatUtil.sendPlainMsg(sender, text.toString(), false);
				text = new StringBuilder();
				i = 0;
			}
			i++;
		}

		if (text.length() > 0)
			ChatUtil.sendPlainMsg(sender, text.toString(), false);
		ChatUtil.sendPlainMsg(sender, "&f&m                                                       ", false);
	}
}