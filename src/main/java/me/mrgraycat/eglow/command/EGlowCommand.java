package me.mrgraycat.eglow.command;

import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.command.subcommand.impl.*;
import me.mrgraycat.eglow.command.subcommand.impl.admin.DebugCommand;
import me.mrgraycat.eglow.command.subcommand.impl.admin.ReloadCommand;
import me.mrgraycat.eglow.command.subcommand.impl.admin.SetCommand;
import me.mrgraycat.eglow.command.subcommand.impl.admin.UnsetCommand;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.GlowPlayerUtil;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

@Getter
public class EGlowCommand implements CommandExecutor, TabExecutor {
	private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("red", "darkred", "gold", "yellow", "green", "darkgreen", "aqua", "darkaqua", "blue", "darkblue", "purple", "pink", "white", "gray", "darkgray", "black", "none"));
	private final Set<SubCommand> subCommands = new HashSet<>();

	/**
	 * Register the subcommands & command alias if enabled
	 */
	public EGlowCommand() {

		try {
			final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			String alias = MainConfig.COMMAND_ALIAS.getString();

			if (MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null) {
				commandMapField.setAccessible(true);
				CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
				commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
			}
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			ChatUtil.reportError(e);
		}

		getSubCommands().add(new GUICommand());
		getSubCommands().add(new HelpCommand());
		getSubCommands().add(new ListCommand());
		getSubCommands().add(new ToggleCommand());
		getSubCommands().add(new ToggleGlowOnJoinCommand());
		getSubCommands().add(new EffectCommand());
		getSubCommands().add(new VisibilityCommand());

		getSubCommands().add(new SetCommand());
		getSubCommands().add(new UnsetCommand());
		getSubCommands().add(new ReloadCommand());
		getSubCommands().add(new DebugCommand());
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("eglow") || MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && command.getName().equalsIgnoreCase(MainConfig.COMMAND_ALIAS.getString())) {
			SubCommand subCommand = null;
			IEGlowPlayer eGlowPlayer = null;
			String[] argsCopy = args.clone();

			//Get the correct subcommand
			if (args.length == 0)
				args = new String[]{"gui"};

			if (DataManager.isValidEffect(args[0], true) || args[0].equalsIgnoreCase("blink") || DataManager.isValidEffect(args[0], false) || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable"))
				args = new String[]{"effect"};

			for (SubCommand subCmd : getSubCommands()) {
				if (subCmd.getName().equalsIgnoreCase(args[0])) {
					subCommand = subCmd;
					break;
				}
			}

			if (subCommand == null) {
				ChatUtil.sendMessage(commandSender, Message.COMMAND_LIST.get(), true);
				return true;
			}
			if (commandSender instanceof ConsoleCommandSender && subCommand.isPlayerCmd()) {
				ChatUtil.sendMessage(commandSender, Message.PLAYER_ONLY.get(), true);
				return true;
			}
			if (!subCommand.getPermission().isEmpty() && !commandSender.hasPermission(subCommand.getPermission())) {
				ChatUtil.sendMessage(commandSender, Message.NO_PERMISSION.get(), true);
				return true;
			}
			if (commandSender instanceof Player) {
				eGlowPlayer = DataManager.getEGlowPlayer((Player) commandSender);

				if (eGlowPlayer == null) {
					GlowPlayerUtil.handlePlayerJoin((Player) commandSender);
					eGlowPlayer = DataManager.getEGlowPlayer((Player) commandSender);
				}
			}
			subCommand.perform(commandSender, eGlowPlayer, argsCopy);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
		if (commandSender == null)
			return null;

		if (commandSender instanceof Player && command.getName().equalsIgnoreCase("eGlow") || MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && command.getName().equalsIgnoreCase(MainConfig.COMMAND_ALIAS.getString())) {
			ArrayList<String> suggestions = new ArrayList<>();
			ArrayList<String> finalSuggestions = new ArrayList<>();

			switch (args.length) {
				case (1):
					if (commandSender.hasPermission("eglow.command.help"))
						suggestions.add("help");
					if (commandSender.hasPermission("eglow.command.list"))
						suggestions.add("list");
					if (commandSender.hasPermission("eglow.command.toggle"))
						suggestions.add("toggle");
					if (commandSender.hasPermission("eglow.command.toggleglowonjoin"))
						suggestions.add("toggleglowonjoin");
					if (commandSender.hasPermission("eglow.command.visibility"))
						suggestions.add("visibility");

					suggestions.add("blink");

					for (IEGlowEffect effect : DataManager.getEGlowEffects()) {
						String name = effect.getName().replace("slow", "").replace("fast", "");

						if (!name.contains("blink") && commandSender.hasPermission(effect.getPermission()))
							suggestions.add(name);
						if (name.equals("none")) {
							suggestions.add("off");
							suggestions.add("disable");
						}
					}

					for (IEGlowEffect effect : DataManager.getCustomEffects()) {
						if (commandSender.hasPermission(effect.getPermission()))
							suggestions.add(effect.getName());
					}

					if (commandSender.hasPermission("eglow.command.set"))
						suggestions.add("set");
					if (commandSender.hasPermission("eglow.command.unset"))
						suggestions.add("unset");
					if (commandSender.hasPermission("eglow.command.debug"))
						suggestions.add("debug");
					if (commandSender.hasPermission("eglow.command.convert"))
						suggestions.add("convert");
					if (commandSender.hasPermission("eglow.command.reload"))
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
								if (!color.equals("none") && commandSender.hasPermission("eglow.blink." + color))
									suggestions.add(color);
							}
							break;
						case ("convert"):
							if (commandSender.hasPermission("eglow.command.convert")) {
								suggestions = new ArrayList<>(Collections.singletonList("stop"));

								for (int i = 1; i <= 10; i++) {
									suggestions.add(i + "");
								}
							}
							break;
						case ("set"):
							if (commandSender.hasPermission("eglow.command.set")) {
								suggestions = new ArrayList<>(Arrays.asList("npc:ID", "npc:s", "npc:sel", "npc:selected", "*"));

								for (Player p : Bukkit.getServer().getOnlinePlayers()) {
									suggestions.add(p.getName());
								}
							}
							break;
						case ("unset"):
							if (commandSender.hasPermission("eglow.command.unset")) {
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

					if (args[0].equalsIgnoreCase("set") && commandSender.hasPermission("eglow.command.set") && args[1].toLowerCase().contains("npc:") || Bukkit.getPlayer(args[1]) != null) {
						for (IEGlowEffect effect : DataManager.getEGlowEffects()) {
							String name = effect.getName().replace("slow", "").replace("fast", "");

							if (!name.contains("blink"))
								suggestions.add(name);
						}

						for (IEGlowEffect effect : DataManager.getCustomEffects()) {
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
							if (commandSender.hasPermission("eglow.command.set"))
								suggestions = new ArrayList<>(Arrays.asList("true", "false"));
							break;
						case ("blink"):
							if (commandSender.hasPermission("eglow.command.set")) {
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
					if (commandSender.hasPermission("eglow.command.set") && colors.contains(args[3].toLowerCase()))
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
}