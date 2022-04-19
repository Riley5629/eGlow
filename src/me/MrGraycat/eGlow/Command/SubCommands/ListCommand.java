package me.MrGraycat.eGlow.Command.SubCommands;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig.Effect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

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
		return new String[] {"/eGlow list"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		//TODO create other way to get this list		
		ChatUtil.sendMsg(sender,"&m        &r &fColors & effects for &eeGlow&f: &m          ");
		ChatUtil.sendMsg(sender,"&fColors:");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("red") + ", " + ChatUtil.getEffectName("darkred") + ", " + ChatUtil.getEffectName("gold") + ",");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("yellow") + ", " + ChatUtil.getEffectName("green") + ", " + ChatUtil.getEffectName("darkgreen") + ",");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("aqua") + ", " + ChatUtil.getEffectName("darkaqua") + ", " + ChatUtil.getEffectName("blue") + ",");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("darkblue") + ", " + ChatUtil.getEffectName("purple") + ", " + ChatUtil.getEffectName("pink") + ",");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("white") + ", " + ChatUtil.getEffectName("gray") + ", " + ChatUtil.getEffectName("darkgray") + ",");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("black") + ".");
		ChatUtil.sendMsg(sender, "&eoff, disable, " + ChatUtil.getEffectName("none") + " &ewill stop the glow.");
		ChatUtil.sendMsg(sender,"&fEffects:");
		ChatUtil.sendMsg(sender, ChatUtil.getEffectName("rainbowslow") + ", " + ChatUtil.getEffectName("rainbowfast"));
		ChatUtil.sendMsg(sender,"&fCustom effects:");
		
		StringBuilder text = new StringBuilder();
		int i = 1;
		
		for (String effect : Effect.GET_ALL_EFFECTS.get()) {
			text.append(ChatUtil.getEffectName(effect)).append(", ");
			
			if (i == 3) {
				ChatUtil.sendMsg(sender, text.toString());
				text = new StringBuilder();
				i = 0;
			}
			
			i++;
		}
		
		if (text.length() > 0) {
			ChatUtil.sendMsg(sender, text.toString());
		}
		
		ChatUtil.sendMsg(sender,"&f&m                                                       ");
	}
}
