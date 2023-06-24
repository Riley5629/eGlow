package me.mrgraycat.eglow.addon;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.ServerUtil;
import me.mrgraycat.eglow.util.packet.PacketUtil;
import me.mrgraycat.eglow.util.packet.chat.EnumChatFormat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsAddon extends GlowAddon implements Listener {

	private EventSubscription<UserDataRecalculateEvent> luckPermsSub;
	private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

	public LuckPermsAddon(EGlow instance) {
		super(instance);

		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

		if (provider == null)
			return;

		EventBus lpEventBus = provider.getProvider().getEventBus();
		TabAddon tabAddon = EGlow.getInstance().getTabAddon();
		VaultAddon vaultAddon = EGlow.getInstance().getVaultAddon();

		this.luckPermsSub = (lpEventBus.subscribe(UserDataRecalculateEvent.class, event -> {
			if (event.getUser().getUsername() == null)
				return;

			runTaskLaterAsynchronously(() -> {
				IEGlowPlayer eGlowPlayer = DataManager.getEGlowPlayer(event.getUser().getUniqueId());

				if (eGlowPlayer == null) {
					return;
				}

				if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
					tabAddon.updateTabPlayer(eGlowPlayer, eGlowPlayer.getActiveColor());
				} else {
					eGlowPlayer.updatePlayerTabName();

					if (!ServerUtil.isBridgeEnabled()) {
						String prefix = "";
						String suffix = "";

						if (vaultAddon != null) {
							prefix = vaultAddon.getPlayerTagPrefix(eGlowPlayer);
							suffix = vaultAddon.getPlayerSuffix(eGlowPlayer);
						}

						PacketUtil.updateScoreboardTeam(eGlowPlayer, eGlowPlayer.getTeamName(), prefix, suffix,
								EnumChatFormat.valueOf(eGlowPlayer.getActiveColor().name()));
					}
				}
			}, 20L);
		}));

		this.luckPermsSub2 = (lpEventBus.subscribe(GroupDataRecalculateEvent.class, event -> runTaskLaterAsynchronously(() -> DataManager.getGlowPlayers().forEach(glowPlayer -> {
			if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
				tabAddon.updateTabPlayer(glowPlayer, glowPlayer.getActiveColor());
			} else {
				String prefix = "";
				String suffix = "";

				if (vaultAddon != null) {
					prefix = vaultAddon.getPlayerPrefix(glowPlayer);
					suffix = vaultAddon.getPlayerSuffix(glowPlayer);
				}

				if (!ServerUtil.isBridgeEnabled()) {
					PacketUtil.updateScoreboardTeam(glowPlayer, glowPlayer.getTeamName(), prefix, suffix,
							EnumChatFormat.valueOf(glowPlayer.getActiveColor().name()));
				}
			}
		}), 20L)));
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