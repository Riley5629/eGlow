 package me.MrGraycat.eGlow.Command.SubCommands;

 import me.MrGraycat.eGlow.Command.SubCommand;
 import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
 import me.MrGraycat.eGlow.GUI.Menus.EGlowMainMenu;
 import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
 import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
 import me.MrGraycat.eGlow.Util.Text.ChatUtil;
 import org.bukkit.command.CommandSender;

public class GUICommand extends SubCommand {

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	public String getDescription() {
		return "Opens GUI.";
	}

	@Override
	public String getPermission() {
		return "eglow.command.gui";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {	
		if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
			 ChatUtil.sendPlainMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
		
		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}
		
		new EGlowMainMenu(ePlayer.getPlayer()).openInventory();
	}
}
