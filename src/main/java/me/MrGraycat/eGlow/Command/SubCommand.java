package me.mrgraycat.eglow.command;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.citizens.EGlowCitizensTrait;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public abstract class SubCommand {
	public abstract String getName();

	public abstract String getPermission();

	public abstract String[] getSyntax();

	public abstract boolean isPlayerCmd();

	public abstract void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args);

	/**
	 * Sends the correct command syntax
	 *
	 * @param sender The receiver of the message
	 */
	public void sendSyntax(CommandSender sender) {
		sendSyntax(sender, getSyntax());
	}

	/**
	 * Send correct command syntax
	 *
	 * @param sender         console/player that will receive this message
	 * @param syntaxMessages correct command syntax
	 */
	private void sendSyntax(CommandSender sender, String[] syntaxMessages) {
		ChatUtil.sendPlainMsg(sender, "", true);

		for (String message : syntaxMessages) {
			ChatUtil.sendPlainMsg(sender, Message.INCORRECT_USAGE.get(message), false);
		}
	}

	/**
	 * Get the IEGlowEntity instance of the player/npc being targeted
	 *
	 * @param sender console/player to send message to if something should be incorrect
	 * @param args   command args to retrieve the required info
	 * @return IEGlowEntity instance of the targeted player/npc
	 */
	public Set<EGlowPlayer> getTarget(CommandSender sender, String[] args) {
		Set<EGlowPlayer> results = new HashSet<>();

		if (args.length >= 2) {
			if (args[1].toLowerCase().contains("npc:")) {
				if (getInstance().getCitizensAddon() == null) {
					ChatUtil.sendMsg(sender, Message.CITIZENS_NOT_INSTALLED.get(), true);
					return results;
				}

				String argument = args[1].toLowerCase().replace("npc:", "");
				NPC npc = null;

				try {
					if (argument.equals("s") || argument.equals("sel") || argument.equals("selected")) {
						npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
					} else {
						npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(argument));
					}
				} catch (NullPointerException ignored) {
					ChatUtil.sendMsg(sender, Message.CITIZENS_NPC_NOT_FOUND.get(), true);
				}

				if (npc == null || !npc.isSpawned()) {
					ChatUtil.sendMsg(sender, Message.CITIZENS_NPC_NOT_FOUND.get(), true);
					return results;
				}

				try {
					if (!getInstance().getCitizensAddon().traitCheck(npc)) {
						ChatUtil.sendMsg(sender, Message.PREFIX.get() + "&cYour Citizens plugin is outdated&f!", true);
						return results;
					}

					results.add(npc.getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC());
					return results;
				} catch (NoSuchMethodError ignored) {
					ChatUtil.sendToConsole("&cYour Citizens plugin is outdated&f!", true);
				}

			} else {
				if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("all")) {
					results.addAll(DataManager.getEGlowPlayers());
					return results;
				}

				Player player = Bukkit.getPlayer(args[1].toLowerCase());

				if (player == null) {
					ChatUtil.sendMsg(sender, Message.PLAYER_NOT_FOUND.get(), true);
					return results;
				}

				EGlowPlayer eGlowTarget = DataManager.getEGlowPlayer(player);

				if (eGlowTarget == null) {
					ChatUtil.sendMsg(sender, Message.PLAYER_NOT_FOUND.get(), true);
					return results;
				}

				if (args.length >= 3 && !args[2].equalsIgnoreCase("glowonjoin")) {
					switch (eGlowTarget.getGlowDisableReason()) {
						case BLOCKEDWORLD:
							ChatUtil.sendMsg(sender, Message.OTHER_PLAYER_IN_DISABLED_WORLD.get(eGlowTarget), true);
							return results;
						case INVISIBLE:
							ChatUtil.sendMsg(sender, Message.OTHER_PLAYER_INVISIBLE.get(eGlowTarget), true);
							return results;
						case ANIMATION:
							ChatUtil.sendMsg(sender, Message.OTHER_PLAYER_ANIMATION.get(eGlowTarget), true);
							return results;
					}
				}
				results.add(eGlowTarget);
			}
		}
		return results;
	}

	public EGlow getInstance() {
		return EGlow.getInstance();
	}
}