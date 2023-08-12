package me.mrgraycat.eglow.command;

import me.mrgraycat.eglow.command.subcommands.*;
import me.mrgraycat.eglow.command.subcommands.admin.DebugCommand;
import me.mrgraycat.eglow.command.subcommands.admin.ReloadCommand;
import me.mrgraycat.eglow.command.subcommands.admin.SetCommand;
import me.mrgraycat.eglow.command.subcommands.admin.UnsetCommand;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowEffect;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.event.EGlowEventListener;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class EGlowCommand implements CommandExecutor, TabExecutor {
	private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("red", "darkred", "gold", "yellow", "green", "darkgreen", "aqua", "darkaqua", "blue", "darkblue", "purple", "pink", "white", "gray", "darkgray", "black", "none"));
	private final Set<SubCommand> subcmds = new HashSet<>();

	/**
	 * Register the subcommands & command alias if enabled
	 */
	public EGlowCommand() {
		NMSHook.registerCommandAlias();

		subcmds.add(new GUICommand());
		subcmds.add(new HelpCommand());
		subcmds.add(new ListCommand());
		subcmds.add(new ToggleCommand());
		subcmds.add(new ToggleGlowOnJoinCommand());
		subcmds.add(new EffectCommand());
		subcmds.add(new VisibilityCommand());

		subcmds.add(new SetCommand());
		subcmds.add(new UnsetCommand());
		subcmds.add(new ReloadCommand());
		subcmds.add(new DebugCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("eglow") || MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && command.getName().equalsIgnoreCase(MainConfig.COMMAND_ALIAS.getString())) {
			SubCommand subCommand = null;
			EGlowPlayer eGlowPlayer = null;
			String[] argsCopy = args.clone();

			//Get the correct subcommand
			if (args.length == 0) {
				args = new String[]{"gui"};
			}

			if (DataManager.isValidEffect(args[0], true) || args[0].equalsIgnoreCase("blink") || DataManager.isValidEffect(args[0], false)
					|| args[0].toLowerCase().replace("off", "none").replace("disable", "none").equalsIgnoreCase("none")) {
				args = new String[]{"effect"};
			}

			for (SubCommand subCmd : getSubCommands()) {
				if (args[0].equalsIgnoreCase(subCmd.getName())) {
					subCommand = subCmd;
					break;
				}
			}

			if (subCommand == null) {
				ChatUtil.sendMsg(sender, Message.COMMAND_LIST.get(), true);
				return true;
			}
			if (sender instanceof ConsoleCommandSender && subCommand.isPlayerCmd()) {
				ChatUtil.sendMsg(sender, Message.PLAYER_ONLY.get(), true);
				return true;
			}
			if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
				ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
				return true;
			}
			if (sender instanceof Player) {
				eGlowPlayer = DataManager.getEGlowPlayer((Player) sender);

				if (eGlowPlayer == null) {
					EGlowEventListener.PlayerConnect((Player) sender, ((Player) sender).getUniqueId());
					eGlowPlayer = DataManager.getEGlowPlayer((Player) sender);
				}
			}
			subCommand.perform(sender, eGlowPlayer, argsCopy);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender == null)
			return null;

		if (sender instanceof Player && cmd.getName().equalsIgnoreCase("eglow") || MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && cmd.getName().equalsIgnoreCase(MainConfig.COMMAND_ALIAS.getString())) {
			ArrayList<String> suggestions = new ArrayList<>();
			ArrayList<String> finalSuggestions = new ArrayList<>();

			switch (args.length) {
				case (1):
					if (sender.hasPermission("eglow.command.help"))
						suggestions.add("help");
					if (sender.hasPermission("eglow.command.list"))
						suggestions.add("list");
					if (sender.hasPermission("eglow.command.toggle"))
						suggestions.add("toggle");
					if (sender.hasPermission("eglow.command.toggleglowonjoin"))
						suggestions.add("toggleglowonjoin");
					if (sender.hasPermission("eglow.command.visibility"))
						suggestions.add("visibility");

					suggestions.add("blink");

					for (EGlowEffect effect : DataManager.getEGlowEffects()) {
						String name = effect.getName().replace("slow", "").replace("fast", "");

						if (!name.contains("blink") && sender.hasPermission(effect.getPermissionNode()))
							suggestions.add(name);
						if (name.equals("none")) {
							suggestions.add("off");
							suggestions.add("disable");
						}
					}

					for (EGlowEffect effect : DataManager.getCustomEffects()) {
						if (sender.hasPermission(effect.getPermissionNode()))
							suggestions.add(effect.getName());
					}

					if (sender.hasPermission("eglow.command.set"))
						suggestions.add("set");
					if (sender.hasPermission("eglow.command.unset"))
						suggestions.add("unset");
					if (sender.hasPermission("eglow.command.debug"))
						suggestions.add("debug");
					if (sender.hasPermission("eglow.command.convert"))
						suggestions.add("convert");
					if (sender.hasPermission("eglow.command.reload"))
						suggestions.add("reload");

					StringUtil.copyPartialMatches(args[0], suggestions, finalSuggestions);
					break;
				case (2):
					switch (args[0].toLowerCase()) {
						case ("visibility"):
							suggestions = new ArrayList<>(Arrays.asList("all", "other", "own", "none"));
							break;
						case ("blink"):
							for (String color : colors) {
								if (!color.equals("none") && sender.hasPermission("eglow.blink." + color))
									suggestions.add(color);
							}
							break;
						case ("convert"):
							if (sender.hasPermission("eglow.command.convert")) {
								suggestions = new ArrayList<>(Collections.singletonList("stop"));

								for (int i = 1; i <= 10; i++) {
									suggestions.add(String.valueOf(i));
								}
							}
							break;
						case ("set"):
							if (sender.hasPermission("eglow.command.set")) {
								suggestions = new ArrayList<>(Arrays.asList("npc:ID", "npc:s", "npc:sel", "npc:selected", "*"));

								for (Player p : Bukkit.getServer().getOnlinePlayers()) {
									suggestions.add(p.getName());
								}
							}
							break;
						case ("unset"):
							if (sender.hasPermission("eglow.command.unset")) {
								suggestions = new ArrayList<>(Arrays.asList("npc:ID", "npc:s", "npc:sel", "npc:selected", "*"));

								for (Player p : Bukkit.getServer().getOnlinePlayers()) {
									suggestions.add(p.getName());
								}
							}
							break;
						case ("debug"):
							for (Player p : Bukkit.getServer().getOnlinePlayers()) {
								suggestions.add(p.getName());
							}
							break;
						default:
							if (DataManager.isValidEffect(args[0], false))
								suggestions = new ArrayList<>(Arrays.asList("slow", "fast"));
							break;
					}

					StringUtil.copyPartialMatches(args[1], suggestions, finalSuggestions);
					break;
				case (3):
					if (colors.contains(args[1].toLowerCase()))
						suggestions = new ArrayList<>(Arrays.asList("slow", "fast"));

					if (args[0].equalsIgnoreCase("set") && sender.hasPermission("eglow.command.set") && args[1].toLowerCase().contains("npc:") || Bukkit.getPlayer(args[1]) != null) {
						for (EGlowEffect effect : DataManager.getEGlowEffects()) {
							String name = effect.getName().replace("slow", "").replace("fast", "");

							if (!name.contains("blink"))
								suggestions.add(name);
						}

						for (EGlowEffect effect : DataManager.getCustomEffects()) {
							suggestions.add(effect.getName());
						}

						suggestions.add("blink");
						suggestions.add("off");
						suggestions.add("disable");

						if (Bukkit.getPlayer(args[1]) != null)
							suggestions.add("glowonjoin");
					}

					StringUtil.copyPartialMatches(args[2], suggestions, finalSuggestions);
					break;
				case (4):
					switch (args[2].toLowerCase()) {
						case ("glowonjoin"):
							if (sender.hasPermission("eglow.command.set"))
								suggestions = new ArrayList<>(Arrays.asList("true", "false"));
							break;
						case ("blink"):
							if (sender.hasPermission("eglow.command.set")) {
								for (String color : colors) {
									if (!color.equals("none"))
										suggestions.add(color);
								}
							}
							break;
						default:
							if (DataManager.isValidEffect(args[2], false))
								suggestions = new ArrayList<>(Arrays.asList("slow", "fast"));
							break;
					}
					StringUtil.copyPartialMatches(args[3], suggestions, finalSuggestions);
					break;
				case (5):
					if (sender.hasPermission("eglow.command.set") && colors.contains(args[3].toLowerCase()))
						suggestions = new ArrayList<>(Arrays.asList("slow", "fast"));
					StringUtil.copyPartialMatches(args[3], suggestions, finalSuggestions);
					break;
				default:
					return suggestions;
			}

			if (!finalSuggestions.isEmpty())
				return finalSuggestions;
			return suggestions;
		}
		return null;
	}

	public Set<SubCommand> getSubCommands() {
		return subcmds;
	}
}