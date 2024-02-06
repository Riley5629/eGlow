package me.mrgraycat.eglow.util.packets;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.datawatcher.DataWatcher;
import me.mrgraycat.eglow.util.packets.datawatcher.DataWatcherObject;
import me.mrgraycat.eglow.util.packets.datawatcher.DataWatcherRegistry;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Objects;

public class NMSHook {
	public static DataWatcherRegistry registry;
	public static NMSStorage nms;

	public static void initialize() {
		try {
			nms = new NMSStorage();
			registry = new DataWatcherRegistry(nms);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static boolean isBungee() {
		try {
			return (Boolean) (nms.bungee.get(nms.SpigotConfig));

		} catch (IllegalAccessException exception) {
			ChatUtil.reportError(exception);
			return false;
		}
	}

	public static void setOwningPlayer(SkullMeta skullMeta, String owner) {
		try {
			nms.setOwningPlayer.invoke(skullMeta, owner);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static Object getChannel(Player player) {
		if (nms.CHANNEL == null) return null;
		try {
			return nms.CHANNEL.get(nms.NETWORK_MANAGER.get(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(player))));
		} catch (Exception exception) {
			ChatUtil.reportError(exception);
			return null;
		}
	}

	public static void sendPacket(Player player, Object nmsPacket) throws Exception {
		if (nmsPacket == null)
			return;
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(player)), nmsPacket);
	}

	public static void sendPacket(EGlowPlayer eGlowPlayer, Object nmsPacket) throws Exception {
		sendPacket(eGlowPlayer.getPlayer(), nmsPacket);
	}

	public static Object stringToComponent(String json) throws Exception {
		if (json == null) return null;
		return nms.ChatSerializer_DESERIALIZE.invoke(null, json);
	}

	public static DataWatcher setGlowFlag(Object entity, boolean status) {
		try {
			Object nmsPlayer = nms.getHandle.invoke(entity);
			DataWatcher dw = DataWatcher.fromNMS(nms.getDataWatcher.invoke(nmsPlayer));

			byte initialBitMask = (byte) dw.getItem(0).value;
			byte bitMaskIndex = (byte) 6;

			if (status) {
				dw.setValue(new DataWatcherObject(0, registry.Byte), (byte) (initialBitMask | 1 << bitMaskIndex));
			} else {
				dw.setValue(new DataWatcherObject(0, registry.Byte), (byte) (initialBitMask & ~(1 << bitMaskIndex)));
			}
			return dw;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public static DataWatcher setGlowFlag(Object entity, Object metadataPacket, boolean status) {
		try {
			Object nmsPlayer = nms.getHandle.invoke(entity);
			DataWatcher dw = DataWatcher.fromNMSPacket(metadataPacket);

			if (dw.getItem(0) == null)
				return dw;
			
			byte initialBitMask = (byte) dw.getItem(0).value;
			byte bitMaskIndex = (byte) 6;

			if (status) {
				dw.setValue(new DataWatcherObject(0, registry.Byte), (byte) (initialBitMask | 1 << bitMaskIndex));
			} else {
				dw.setValue(new DataWatcherObject(0, registry.Byte), (byte) (initialBitMask & ~(1 << bitMaskIndex)));
			}

			return dw;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public static void registerCommandAlias() {
		try {
			String alias = EGlowMainConfig.MainConfig.COMMAND_ALIAS.getString();

			if (EGlowMainConfig.MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null) {
				final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
				commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
			}
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
			ChatUtil.reportError(exception);
		}
	}
}