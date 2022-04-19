package me.MrGraycat.eGlow.Command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit .Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Command.SubCommands.*;
import me.MrGraycat.eGlow.Command.SubCommands.Admin.*;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Event.EGlowEventListener;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowCommand implements CommandExecutor, TabExecutor {
	private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("red","darkred", "gold", "yellow", "green", "darkgreen", "aqua", "darkaqua", "blue", "darkblue", "purple", "pink", "white", "gray", "darkgray", "black", "none"));
	private final ArrayList<SubCommand> subcmds = new ArrayList<>();
	
	/**
	 * Register the subcommands & command alias if enabled
	 */
	public EGlowCommand() {

		try{
		    final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
		    String alias = EGlowMainConfig.OptionCommandAlias();
		    
		    if (EGlowMainConfig.OptionEnableCommandAlias() && alias != null) {
		    	 commandMapField.setAccessible(true);
				 CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
				 commandMap.register(alias, alias, EGlow.getInstance().getCommand("eglow"));
		    }
		} catch (NoSuchFieldException  | IllegalArgumentException | IllegalAccessException e){
		    ChatUtil.reportError(e);
		}
		
		subcmds.add(new GUICommand());
		subcmds.add(new HelpCommand());
		subcmds.add(new ListCommand());
		subcmds.add(new ToggleCommand());
		subcmds.add(new EffectCommand());
		subcmds.add(new VisibilityCommand());
		
		subcmds.add(new SetCommand());
		subcmds.add(new UnsetCommand());
		subcmds.add(new ReloadCommand());
		subcmds.add(new DebugCommand());
		subcmds.add(new ConvertCommand());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("eglow") || EGlowMainConfig.OptionEnableCommandAlias() && command.getName().equalsIgnoreCase(EGlowMainConfig.OptionCommandAlias())) {
			SubCommand cmd = null;
			IEGlowPlayer ePlayer = null;
			String[] argsCopy = args.clone();
			
			//Get the correct subcommand
			if (args.length == 0)
				args = new String[]{"gui"};
			
			if (DataManager.isValidEffect(args[0], true) || args[0].equalsIgnoreCase("blink") || DataManager.isValidEffect(args[0], false) || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) 
				args = new String[] {"effect"};
			
			for (int i = 0; i < getSubCommands().size(); i++) {
				if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
					cmd = getSubCommands().get(i);
					break;
				}
			}
			
			if (cmd == null) {ChatUtil.sendMsgWithPrefix(sender, Message.COMMAND_LIST.get()); return true;}
			if (sender instanceof ConsoleCommandSender && cmd.isPlayerCmd()) {ChatUtil.sendMsgWithPrefix(sender, Message.PLAYER_ONLY.get()); return true;}
			if (!cmd.getPermission().isEmpty() && !sender.hasPermission(cmd.getPermission())) {ChatUtil.sendMsgWithPrefix(sender, Message.NO_PERMISSION.get()); return true;}
			if (sender instanceof Player) {
				ePlayer = DataManager.getEGlowPlayer((Player) sender);
				
				if (ePlayer == null) {
					EGlowEventListener.PlayerConnect((Player) sender, ((Player) sender).getUniqueId());
					ePlayer = DataManager.getEGlowPlayer((Player) sender);
				}
			}
			cmd.perform(sender, ePlayer, argsCopy);	
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender == null)
			return null;
		
		if (sender instanceof Player && cmd.getName().equalsIgnoreCase("eGlow") || EGlowMainConfig.OptionEnableCommandAlias() && cmd.getName().equalsIgnoreCase(EGlowMainConfig.OptionCommandAlias())) {
			ArrayList<String> args1 = new ArrayList<>(Arrays.asList("help", "list", "off", "disable", "toggle", "visibility",  "blink"));
			ArrayList<String> suggestions = new ArrayList<>();
			ArrayList<String> finalSuggestions = new ArrayList<>();
			
			switch(args.length) {
			case(1):
				suggestions = args1;
				
				for (IEGlowEffect effect : DataManager.getEGlowEffects()) {
					String name = effect.getName().replace("slow", "").replace("fast", "");
					
					if (!name.contains("blink"))
						suggestions.add(name);
				}
				
				for (IEGlowEffect effect : DataManager.getCustomEffects()) {
					suggestions.add(effect.getName());
				}
				
				suggestions.add("off");
				suggestions.add("disable");
				
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
			case(2):
				switch(args[0].toLowerCase()) {
				case("visibility"):
					suggestions = new ArrayList<>(Arrays.asList("all", "own", "none"));
				break;
				case("blink"):
					for (String color : colors) {
						if (!color.equals("none"))
							suggestions.add(color);
					}
				break;
				case("convert"):
					if (sender.hasPermission("eglow.command.convert")) {
						suggestions = new ArrayList<>(Collections.singletonList("stop"));
						
						for (int i = 1 ; i <= 10 ; i ++) {
							suggestions.add(i + "");
						}
					}
				break;
				case("set"):
					if (sender.hasPermission("eglow.command.set")) {
						suggestions = new ArrayList<>(Arrays.asList("npc:ID" ,"npc:s", "npc:sel", "npc:selected", "*"));
						
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							suggestions.add(p.getName());
						}
					}
				break;
				case("unset"):
					if (sender.hasPermission("eglow.command.unset")) {
						suggestions = new ArrayList<>(Arrays.asList("npc:ID" ,"npc:s", "npc:sel", "npc:selected", "*"));
						
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							suggestions.add(p.getName());
						}
					}
				break;
				case("debug"):
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
			case(3):
				if (colors.contains(args[1].toLowerCase()))
					suggestions = new ArrayList<>(Arrays.asList("slow", "fast"));
				
				if (args[0].equalsIgnoreCase("set") && sender.hasPermission("eglow.command.set") && args[1].toLowerCase().contains("npc:") || Bukkit.getPlayer(args[1]) != null) {
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
			case(4):
				switch(args[2].toLowerCase()) {
				case("glowonjoin"):
					if (sender.hasPermission("eglow.command.set"))
						suggestions = new ArrayList<>(Arrays.asList("true", "false"));
				break;
				case("blink"):
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
			case(5):
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
	
	public ArrayList<SubCommand> getSubCommands() {
		return subcmds;
	}
}
