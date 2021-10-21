package me.MrGraycat.eGlow.Command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Addon.Citizens.EGlowCitizensTrait;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public abstract class SubCommand {
	public abstract String getName();
	public abstract String getDescription();
	public abstract String getPermission();
	public abstract String[] getSyntax();
	public abstract boolean isPlayerCmd();
	public abstract void perform(CommandSender sender, IEGlowPlayer ePlayer, String args[]);
	
	/**
	 * Send correct command syntax
	 * @param sender console/player that will receive this message
	 * @param text correct command syntax
	 * @param true: prefix + text, false: text
	 */
	public void sendSyntax(CommandSender sender, String text, boolean prefix) {
		if (prefix) {
			ChatUtil.sendMsgWithPrefix(sender, Message.INCORRECT_USAGE.get(text));
		} else {
			ChatUtil.sendMsg(sender, Message.INCORRECT_USAGE.get(text));
		}	
	}
	
	/**
	 * Get the IEGlowEntity instance of the player/npc being targetted
	 * @param sender console/player to send message to if something should be incorrect
	 * @param args command args to retreive the required info
	 * @return IEGlowEntity instance of the targeted player/npc
	 */
	public List<IEGlowPlayer> getTarget(CommandSender sender, String[] args) {
		if (args.length >= 2) {
			List<IEGlowPlayer> results = new ArrayList<>();
			
			if (args[1].toLowerCase().contains("npc:")) {
				if (getInstance().getCitizensAddon() == null) {
					ChatUtil.sendMsgWithPrefix(sender, Message.CITIZENS_NOT_INSTALLED.get());
					return null;
				}
					
				String argument = args[1].toLowerCase().replace("npc:", "");
				NPC npc = null;
				
				if (argument.equals("s") || argument.equals("sel") || argument.equals("selected")) {
					npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
				} else {
					try {
						npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(argument));
					} catch (NumberFormatException e) {
						ChatUtil.sendMsgWithPrefix(sender, "&f'&e" + argument + "&f' &cis an invalid NPC ID");
					}
				}
				
				if (npc == null) {
					ChatUtil.sendMsgWithPrefix(sender, Message.CITIZENS_NPC_NOT_FOUND.get());
					return null;
				}
				
				if (!npc.isSpawned()) {
					ChatUtil.sendMsgWithPrefix(sender, Message.CITIZENS_NPC_NOT_SPAWNED.get());
					return null;
				}
				
				if (!getInstance().getCitizensAddon().traitCheck(npc)) {
					ChatUtil.sendMsg(sender, Message.PREFIX.get() + "&cYour Citizens plugin is outdated&f!");
					return null;
				}
				try {
					results.add(npc.getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC());
					
					return results;
				} catch(NoSuchMethodError e) {
					ChatUtil.sendToConsoleWithPrefix("&cYour Citizens version is outdated please use 2.0.27 or later");
				}
				
			} else {
				if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("all")) {
					results.addAll(getInstance().getDataManager().getEGlowPlayers());
					return results;
				}
				
				Player player = Bukkit.getPlayer(args[1].toLowerCase());
				
				if (player == null) {
					ChatUtil.sendMsgWithPrefix(sender, Message.PLAYER_NOT_FOUND.get());
					return null;
				}
				
				IEGlowPlayer ePlayer = getInstance().getDataManager().getEGlowPlayer(player);
				
				if (ePlayer == null) {
					ChatUtil.sendMsgWithPrefix(sender, Message.PLAYER_NOT_FOUND.get());
					return null;
				}
				
				if (ePlayer.isInBlockedWorld() && args.length >= 3 && !args[2].equalsIgnoreCase("glowonjoin")) {
					ChatUtil.sendMsgWithPrefix(sender, Message.OTHER_PLAYER_IN_DISABLED_WORLD.get(ePlayer));
					return null;
				}
				
				results.add(ePlayer);
				
				return results;
			}
		}
		return null;
	}
	public EGlow getInstance() {
		return EGlow.getInstance();
	}
}
