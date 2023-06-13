package me.MrGraycat.eGlow.Addon;

import lombok.Getter;
import lombok.Setter;
import me.MrGraycat.eGlow.Addon.TAB.TABAddon;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Packets.Chat.EnumChatFormat;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class LuckPermsAddon implements Listener {
	@Getter
	@Setter
	private EventSubscription<UserDataRecalculateEvent> luckPermsSub;
	@Getter
	@Setter
	private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

	public LuckPermsAddon() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

		if (provider == null)
			return;

		EventBus lpEventBus = provider.getProvider().getEventBus();
		TABAddon tabAddon = EGlow.getInstance().getTABAddon();
		VaultAddon vaultAddon = EGlow.getInstance().getVaultAddon();

		setLuckPermsSub(lpEventBus.subscribe(UserDataRecalculateEvent.class, event -> {
			try {
				if (EGlow.getInstance() == null || event.getUser().getUsername() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(event.getUser().getUniqueId());

						if (eGlowPlayer == null)
							return;

						if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
							tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
						} else {
							eGlowPlayer.updatePlayerTabname();
							if (!DebugUtil.isTABBridgeInstalled())
								PacketUtil.updateScoreboardTeam(eGlowPlayer, eGlowPlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(eGlowPlayer) : "") + eGlowPlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(eGlowPlayer) : "", EnumChatFormat.valueOf(eGlowPlayer.getActiveColor().name()));
						}
					}
				}.runTaskLaterAsynchronously(EGlow.getInstance(), 20);
			} catch (IllegalPluginAccessException ignored) {
				//Prevent error spam when eGlow is unloading
			}
		}));

		setLuckPermsSub2(lpEventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
			try {
				if (EGlow.getInstance() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						for (IEGlowPlayer eGlowPlayer : DataManager.getEGlowPlayers()) {
							if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
								tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
							} else {
								if (!DebugUtil.isTABBridgeInstalled()) {
									PacketUtil.updateScoreboardTeam(eGlowPlayer, eGlowPlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(eGlowPlayer) : "") + eGlowPlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(eGlowPlayer) : "", EnumChatFormat.valueOf(eGlowPlayer.getActiveColor().name()));
								}
							}
						}
					}
				}.runTaskLaterAsynchronously(EGlow.getInstance(), 20);
			} catch (IllegalPluginAccessException ignored) {
				//Prevent error spam when eGlow is unloading
			}
		}));
	}

	public void unload() {
		try {
			getLuckPermsSub().close();
			getLuckPermsSub2().close();
		} catch (NoClassDefFoundError ignored) {
			//Rare error when disabling eGlow, no idea how this is caused
		}
	}
}