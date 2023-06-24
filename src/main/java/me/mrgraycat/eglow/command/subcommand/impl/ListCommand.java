package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig.Effect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.command.CommandSender;

public class ListCommand extends SubCommand {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getDescription() {
		return "Shows a list of all available colors/effects.";
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
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		ChatUtil.sendPlainMessage(commandSender, "&m        &r &fColors & effects for &eeGlow&f: &m          ", false);
		ChatUtil.sendPlainMessage(commandSender, "&fColors:", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("red") + ", " + ChatUtil.getEffectName("darkred") + ", " + ChatUtil.getEffectName("gold") + ",", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("yellow") + ", " + ChatUtil.getEffectName("green") + ", " + ChatUtil.getEffectName("darkgreen") + ",", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("aqua") + ", " + ChatUtil.getEffectName("darkaqua") + ", " + ChatUtil.getEffectName("blue") + ",", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("darkblue") + ", " + ChatUtil.getEffectName("purple") + ", " + ChatUtil.getEffectName("pink") + ",", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("white") + ", " + ChatUtil.getEffectName("gray") + ", " + ChatUtil.getEffectName("darkgray") + ",", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("black") + ".", false);
		ChatUtil.sendPlainMessage(commandSender, "&eoff, disable, " + ChatUtil.getEffectName("none") + " &ewill stop the glow.", false);
		ChatUtil.sendPlainMessage(commandSender, "&fEffects:", false);
		ChatUtil.sendPlainMessage(commandSender, ChatUtil.getEffectName("rainbowslow") + ", " + ChatUtil.getEffectName("rainbowfast"), false);
		ChatUtil.sendPlainMessage(commandSender, "&fCustom effects:", false);

		StringBuilder text = new StringBuilder();
		int i = 1;

		for (String effect : Effect.GET_ALL_EFFECTS.get()) {
			text.append(ChatUtil.getEffectName(effect)).append(", ");

			if (i == 3) {
				ChatUtil.sendPlainMessage(commandSender, text.toString(), false);
				text = new StringBuilder();
				i = 0;
			}
			i++;
		}

		if (text.length() > 0)
			ChatUtil.sendPlainMessage(commandSender, text.toString(), false);
		ChatUtil.sendPlainMessage(commandSender, "&f&m                                                       ", false);
	}
}