package me.MrGraycat.eGlow.Util.Packets;

import org.bukkit.entity.Player;

import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher.DataWatcher;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher.DataWatcherObject;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.Datawatcher.DataWatcherRegistry;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class NMSHook {
	public DataWatcherRegistry registry;
	public NMSStorage nms;
	
	public NMSHook() {
		try {
			nms = new NMSStorage();
			registry = new DataWatcherRegistry(nms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object getChannel(Player p) {
		if (nms.CHANNEL == null) return null;
		try {
			return nms.CHANNEL.get(nms.NETWORK_MANAGER.get(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p))));
		} catch (Exception e) {
			ChatUtil.reportError(e);
			return null;
		} 
	}
	
	public void sendPacket(Player p, Object nmsPacket) throws Exception {
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p)), nmsPacket);
	}
	
	public void sendPacket(IEGlowPlayer ep, Object nmsPacket) throws Exception {
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(ep.getPlayer())), nmsPacket);
	}
	
	public Object stringToComponent(String json) throws Exception {
		if (json == null) return null;
		return nms.ChatSerializer_DESERIALIZE.invoke(null, json);
	}
	
	public DataWatcher setGlowFlag(Object entity, boolean status) {
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
