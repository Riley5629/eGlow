package me.MrGraycat.eGlow.Util.Packets;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.Datawatcher.DataWatcher;
import me.MrGraycat.eGlow.Util.Packets.Datawatcher.DataWatcherObject;
import me.MrGraycat.eGlow.Util.Packets.Datawatcher.DataWatcherRegistry;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class NMSHook {
	public static DataWatcherRegistry registry;
	public static NMSStorage nms;
	
	public static void initialize() {
		try {
			nms = new NMSStorage();
			registry = new DataWatcherRegistry(nms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isBungee() {
		try {
			return (Boolean) (nms.bungee.get(nms.SpigotConfig));
		} catch (IllegalAccessException e) {
			ChatUtil.reportError(e);
			return false;
		}
	}

	public static void setOwningPlayer(SkullMeta skullMeta, String owner) {
		try {
			nms.setOwningPlayer.invoke(skullMeta, owner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object getChannel(Player p) {
		if (nms.CHANNEL == null) return null;
		try {
			return nms.CHANNEL.get(nms.NETWORK_MANAGER.get(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p))));
		} catch (Exception e) {
			ChatUtil.reportError(e);
			return null;
		} 
	}
	
	public static void sendPacket(Player p, Object nmsPacket) throws Exception {
		if (nmsPacket == null)
			return;
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p)), nmsPacket);
	}
	
	public static void sendPacket(IEGlowPlayer ep, Object nmsPacket) throws Exception {
		if (nmsPacket == null)
			return;
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(ep.getPlayer())), nmsPacket);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}