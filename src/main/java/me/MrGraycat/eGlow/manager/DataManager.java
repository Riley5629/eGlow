package me.MrGraycat.eGlow.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.MrGraycat.eGlow.api.event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.config.EGlowCustomEffectsConfig.Effect;
import me.MrGraycat.eGlow.config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.manager.glow.IEGlowEffect;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Lombok's UtilityClass annotation cannot be used here
 * as the class is a subclass of PluginMessageListener.
 * UtilityClass would prevent the class from being instantiated
 * by injecting it with a private constructor with no parameters.
 * It would also make any definition of the overridden method from
 * PluginMessageListener impossible, as it would attempt to make that
 * method static.
 */
public class DataManager implements PluginMessageListener {

	private static final Map<String, IEGlowPlayer> dataPlayers = new ConcurrentHashMap<>();
	private static final Map<String, IEGlowEffect> dataEffects = new ConcurrentHashMap<>();
	private static final Map<String, IEGlowEffect> dataCustomEffects = new ConcurrentHashMap<>();

	private static final List<String> oldEffects = new ArrayList<>();

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		/*
		Empty method body, to possibly be used at a later date.

		If not, change DataManager so that it does not implement
		PluginMessageListener, then annotate it with @UtilityClass.
		 */
	}

	//Server
	public static void initialize() {
		addEGlowEffects();

		Messenger messenger = EGlow.getInstance().getServer().getMessenger();

		messenger.registerOutgoingPluginChannel(EGlow.getInstance(), "eglow:bungee");
		messenger.registerIncomingPluginChannel(EGlow.getInstance(), "eglow:bungee", new DataManager());
	}
	
	//Entities
	public static IEGlowPlayer addEGlowPlayer(Player player) {
		return dataPlayers.computeIfAbsent(player.getUniqueId().toString(),
				(absentPlayer) -> new IEGlowPlayer(player));
	}
	
	public static IEGlowPlayer getEGlowPlayer(Player player) {
		return dataPlayers.get(player.getUniqueId().toString());
	}

	public static IEGlowPlayer getEGlowPlayer(String name) {
		Player player = Bukkit.getPlayer(name);
		
		//Prevent kick loop caused by for example npc's
		if (player == null) {
			return null;
		}

		return dataPlayers.get(player.getUniqueId().toString());
	}
	
	public static IEGlowPlayer getEGlowPlayer(UUID uuid) {
		return dataPlayers.get(uuid.toString());
	}
	
	public static Collection<IEGlowPlayer> getGlowPlayers() {
		return dataPlayers.values();
	}
	
	public static void removeGlowPlayer(Player player) {
		dataPlayers.remove(player.getUniqueId().toString());
	}
	
	//Effects
	public static void addEGlowEffects() {
		IEGlowEffect effect;
		
		for (ChatColor color : ChatColor.values()) {
			if (!color.equals(ChatColor.ITALIC) && !color.equals(ChatColor.MAGIC) && !color.equals(ChatColor.STRIKETHROUGH) && !color.equals(ChatColor.UNDERLINE) && ! color.equals(ChatColor.BOLD)) {
				String configName = color.name().toLowerCase().replace("_", "-").replace("ı", "i").replace("dark-purple", "purple").replace("light-purple", "pink").replace("reset", "none").replace(" ", "");
				String name = configName.replace("-", "");
				
				if (!dataEffects.containsKey(name)) {
					addEGlowEffect(name, (MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName), "eglow.color." + name, color);

					if (!name.equals("none")) {
						addEGlowEffect("blink" + name + "slow", (MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("slow") + "§f)", "eglow.blink." + name, MainConfig.DELAY_SLOW.getInt(), ChatColor.RESET, color);
						addEGlowEffect("blink" + name + "fast", (MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("fast") + "§f)", "eglow.blink." + name, MainConfig.DELAY_FAST.getInt(), ChatColor.RESET, color);
					}
				} else {
					effect = getEGlowEffect(name);
					
					if (effect != null)
						effect.setDisplayName((MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName));
					
					effect = getEGlowEffect("blink" + name + "slow");
					
					if (effect != null) {
						effect.setDisplayName((MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + ((Message.COLOR.get("effect-blink").isEmpty()) ? "" : " ") + Message.COLOR.get("slow") + "§f)");
						effect.setDelay(MainConfig.DELAY_SLOW.getInt());
						effect.reloadEffect();
					}
					
					effect = getEGlowEffect("blink" + name + "fast");
					
					if (effect != null) {
						effect.setDisplayName((MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + ((Message.COLOR.get("effect-blink").isEmpty()) ? "" : " ") + Message.COLOR.get("fast") + "§f)");
						effect.setDelay(MainConfig.DELAY_FAST.getInt());
						effect.reloadEffect();
					}
				}
			}
		}
		
		if (!dataEffects.containsKey("rainbowslow")) {
			addEGlowEffect("rainbowslow", (MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("slow") + "§f)", "eglow.effect.rainbow", MainConfig.DELAY_SLOW.getInt(), ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE);
			addEGlowEffect("rainbowfast", (MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("fast") + "§f)", "eglow.effect.rainbow", MainConfig.DELAY_FAST.getInt(), ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE);
		} else {
			effect = getEGlowEffect("rainbowslow");
			
			if (effect != null) {
				effect.setDisplayName((MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("slow") + "§f)");
				effect.setDelay(MainConfig.DELAY_SLOW.getInt());
				effect.reloadEffect();
			}
			
			effect = getEGlowEffect("rainbowfast");
			
			if (effect != null) {
				effect.setDisplayName(((MainConfig.SETTINGS_GUI_COLOR_FOR_MESSAGES.getBoolean())) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("fast") + "§f)");
				effect.setDelay(MainConfig.DELAY_FAST.getInt());
				effect.reloadEffect();
			}
		}
		
		addCustomEffects();
	}

	public static void addCustomEffects() {
		List<String> newEffects = new ArrayList<>();

		for (String effectName : Effect.GET_ALL_EFFECTS.get()) {
			if (dataEffects.containsKey(effectName.toLowerCase())) {
				ChatUtil.sendToConsole("&cWARNING! Not registering custom effect: &f" + effectName + " &cdue to it using a default effect name!", true);
				continue;
			}

			String displayName = ChatUtil.translateColors(Effect.GET_DISPLAYNAME.getString(effectName));
			int delay = (int) (Effect.GET_DELAY.getDouble(effectName) * 20);
			List<String> colors = Effect.GET_COLORS.getList(effectName);
			String permission = "eglow.effect." + effectName.toLowerCase();

			if (!oldEffects.isEmpty() && oldEffects.contains(effectName.toLowerCase())) {
				IEGlowEffect effect = getEGlowEffect(effectName.toLowerCase());

				if (effect != null) {
					effect.setDisplayName(displayName);
					effect.setDelay(delay);
					effect.setColors(colors);
					effect.reloadEffect();
				}
				oldEffects.remove(effectName.toLowerCase());

			} else {
				addEGlowEffect(effectName.toLowerCase(), displayName, "eglow.effect." + effectName.toLowerCase(), delay, colors);

				try {
					EGlow.getInstance().getServer().getPluginManager().addPermission(new Permission(permission, "Activate " + effectName + " effect.", PermissionDefault.FALSE));
				} catch (IllegalArgumentException e) { /*Permission already registered*/ }
			}

			newEffects.add(effectName.toLowerCase());
		}

		for (String effect : oldEffects) {
			IEGlowEffect glowEffect = getEGlowEffect(effect.toLowerCase());

			if (glowEffect != null) {
				removeCustomEGlowEffect(effect.toLowerCase());
				glowEffect.removeEffect();
			}
		}
		
		oldEffects.clear();
		oldEffects.addAll(newEffects);
	}
	
	private static void addEGlowEffect(String name, String displayName, String permissionNode, ChatColor color) {
		if (!dataEffects.containsKey(name.toLowerCase())) {
			dataEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, 0, color));
		}
	}
	
	public static void addEGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		if (!dataEffects.containsKey(name.toLowerCase())) {
			dataEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, delay, colors));
		}
	}
	
	private static void addEGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		for (String color : colors) {
			try {
				color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
				ChatColor.valueOf(color.toUpperCase());
			} catch (IllegalArgumentException | NullPointerException e) {
				ChatUtil.sendToConsole("&cInvalid color &f'&e" + color + "&f' &cfor effect &f'&e" + name + "&f'", true);
				return;
			}
		}
		
		if (!dataCustomEffects.containsKey(name.toLowerCase())) {
			dataCustomEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, delay, colors));
		}
	}
	
	public static List<IEGlowEffect> getEGlowEffects() {
		return new ArrayList<>(dataEffects.values());
	}
	
	public static List<IEGlowEffect> getCustomEffects() {
		return new ArrayList<>(dataCustomEffects.values());
	}
	
	public static boolean isValidEffect(String name, boolean containsSpeed) {
		return containsSpeed
				? (dataEffects.containsKey(name.toLowerCase()) || dataCustomEffects.containsKey(name.toLowerCase()))
				: (dataEffects.containsKey(name.toLowerCase() + "slow") && dataEffects.containsKey(name.toLowerCase() + "fast"));
	}

	public static boolean isCustomEffect(String name) {
		return dataCustomEffects.containsKey(name);
	}
	
	public static IEGlowEffect getEGlowEffect(String name) {
		if (dataEffects.containsKey(name.toLowerCase())) {
			return dataEffects.get(name.toLowerCase());
		}

		return dataCustomEffects.get(name.toLowerCase());
	}
	
	private static void removeCustomEGlowEffect(String name) {
		dataCustomEffects.remove(name.toLowerCase());
	}

	/*
	I've commented this method out to allow the Lombok annotation
	@UtilityClass to be used in this class, as the method is completely
	empty. In the future, if you wish to uncomment this method, remove
	the @UtilityClass method and make
	 */
//	//API
//	@Override
//	public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
//
//	}
	
	public static void proxyTabUpdateRequest(Player player, String glowColor) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF("TABProxyUpdateRequest");
		out.writeUTF(player.getUniqueId().toString());
		out.writeUTF(glowColor);

		Bukkit.getServer().sendPluginMessage(EGlow.getInstance(), "eglow:bungee", out.toByteArray());
	}
	
	public static void sendAPIEvent(IEGlowPlayer player, boolean fake) {
		if (fake) {
			return;
		}

		Bukkit.getScheduler().runTask(EGlow.getInstance(), () -> {
			Bukkit.getPluginManager().callEvent(new GlowColorChangeEvent(
					player.getPlayer(), player.getUuid(), player.getActiveColor(), player.getGlowStatus())
			);
		});
	}
}