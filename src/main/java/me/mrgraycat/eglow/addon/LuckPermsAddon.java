package me.mrgraycat.eglow.addon;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.tab.TABAddon;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.Dependency;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
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

public class LuckPermsAddon extends AbstractAddonBase implements Listener {
	private EventSubscription<UserDataRecalculateEvent> luckPermsSub;
	private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

	public LuckPermsAddon(EGlow eGlowInstance) {
		super(eGlowInstance);

		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

		if (provider == null)
			return;

		EventBus eventBus = provider.getProvider().getEventBus();
		TABAddon tabAddon = getEGlowInstance().getTabAddon();
		VaultAddon vaultAddon = getEGlowInstance().getVaultAddon();

		luckPermsSub = (eventBus.subscribe(UserDataRecalculateEvent.class, event -> {
			try {
				if (getEGlowInstance() == null || event.getUser().getUsername() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						EGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(event.getUser().getUniqueId());

						if (eGlowPlayer == null)
							return;

						if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
							tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
						} else {
							eGlowPlayer.updatePlayerTabname();
							if (!Dependency.TAB_BRIDGE.isLoaded())
								PacketUtil.updateScoreboardTeam(eGlowPlayer, eGlowPlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(eGlowPlayer) : "") + eGlowPlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(eGlowPlayer) : "", EnumChatFormat.valueOf(eGlowPlayer.getActiveColor().name()));
						}
					}
				}.runTaskLaterAsynchronously(EGlow.getInstance(), 20);
			} catch (IllegalPluginAccessException ignored) {
				//Prevent error spam when eGlow is unloading
			}
		}));

		luckPermsSub2 = (eventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
			try {
				if (EGlow.getInstance() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						for (EGlowPlayer eGlowPlayer : DataManager.getEGlowPlayers()) {
							if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
								tabAddon.updateTABPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
							} else {
								if (!Dependency.TAB_BRIDGE.isLoaded())
									PacketUtil.updateScoreboardTeam(eGlowPlayer, eGlowPlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(eGlowPlayer) : "") + eGlowPlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(eGlowPlayer) : "", EnumChatFormat.valueOf(eGlowPlayer.getActiveColor().name()));
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
			luckPermsSub.close();
			luckPermsSub2.close();
		} catch (NoClassDefFoundError ignored) {
			//Rare error when disabling eGlow, no idea how this is caused
		}
	}
}