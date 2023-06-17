package me.mrgraycat.eglow.util.packet;

import lombok.experimental.UtilityClass;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.packet.watcher.DataWatcher;
import me.mrgraycat.eglow.util.packet.watcher.DataWatcherObject;
import me.mrgraycat.eglow.util.packet.watcher.DataWatcherRegistry;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

@UtilityClass
public class NMSHook {

	public DataWatcherRegistry registry;
	public NMSStorage nms;
	
	public void initialize() {
		try {
			nms = new NMSStorage();
			registry = new DataWatcherRegistry(nms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isBungee() {
		try {
			return (Boolean) (nms.bungeeField.get(nms.spigotConfigClass));
		} catch (IllegalAccessException e) {
			ChatUtil.reportError(e);
			return false;
		}
	}

	public void setOwningPlayer(SkullMeta skullMeta, String owner) {
		try {
			nms.setOwningPlayer.invoke(skullMeta, owner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object getChannel(Player p) {
		if (nms.channelField == null) return null;
		try {
			return nms.channelField.get(nms.networkManagerField.get(nms.playerConnectionField.get(nms.getHandle.invoke(p))));
		} catch (Exception e) {
			ChatUtil.reportError(e);
			return null;
		} 
	}
	
	public void sendPacket(Player p, Object nmsPacket) throws Exception {
		if (nmsPacket == null) {
			return;
		}

		nms.sendPacket.invoke(nms.playerConnectionField.get(nms.getHandle.invoke(p)), nmsPacket);
	}
	
	public void sendPacket(IEGlowPlayer ep, Object nmsPacket) throws Exception {
		if (nmsPacket == null) {
			return;
		}

		nms.sendPacket.invoke(nms.playerConnectionField.get(nms.getHandle.invoke(ep.getPlayer())), nmsPacket);
	}
	
	public Object stringToComponent(String json) throws Exception {
		if (json == null) {
			return null;
		}

		return nms.chatSerializerDeserialize.invoke(null, json);
	}
	
	public DataWatcher setGlowFlag(Object entity, boolean status) {
		try {
			Object nmsPlayer = nms.getHandle.invoke(entity);
			DataWatcher dw = DataWatcher.fromNMS(nms.getDataWatcher.invoke(nmsPlayer));
			
			byte initialBitMask = (byte) dw.getItem(0).value;
			byte bitMaskIndex = (byte) 6;
			
			if (status) {
				dw.setValue(new DataWatcherObject(0, registry.byteType), (byte) (initialBitMask | 1 << bitMaskIndex));
			} else {
				dw.setValue(new DataWatcherObject(0, registry.byteType), (byte) (initialBitMask & ~(1 << bitMaskIndex)));
			}
			return dw;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}