package me.MrGraycat.eGlow;

import lombok.Getter;
import lombok.Setter;
import me.MrGraycat.eGlow.addon.citizens.CitizensAddon;
import me.MrGraycat.eGlow.addon.disguise.IDisguiseAddon;
import me.MrGraycat.eGlow.addon.disguise.LibDisguiseAddon;
import me.MrGraycat.eGlow.addon.gsit.GSitAddon;
import me.MrGraycat.eGlow.addon.tab.TabAddon;
import me.MrGraycat.eGlow.addon.vault.VaultAddon;
import me.MrGraycat.eGlow.api.EGlowAPI;
import me.MrGraycat.eGlow.command.EGlowCommand;
import me.MrGraycat.eGlow.config.EGlowCustomEffectsConfig;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.event.EGlowEventListener;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.migration.impl.ConfigMigration;
import me.MrGraycat.eGlow.util.GlowPlayerUtil;
import me.MrGraycat.eGlow.util.ServerUtil;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.dependency.Dependency;
import me.MrGraycat.eGlow.addon.Internal.AdvancedGlowVisibilityAddon;
import me.MrGraycat.eGlow.addon.luckperms.LuckPermsAddon;
import me.MrGraycat.eGlow.addon.placeholderapi.PlaceholderAPIAddon;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.migration.impl.CustomEffectsMigration;
import me.MrGraycat.eGlow.migration.impl.MessagesMigration;
import me.MrGraycat.eGlow.util.packet.NMSHook;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class EGlow extends JavaPlugin {

	@Getter private static EGlow instance;
	private EGlowAPI api;
	private boolean upToDate;

	//Addons
	@Setter private AdvancedGlowVisibilityAddon glowAddon;
	private CitizensAddon citizensAddon;
	private IDisguiseAddon iDisguiseAddon;
	private LibDisguiseAddon libDisguiseAddon;
	private TabAddon tabAddon;
	private LuckPermsAddon luckPermsAddon;
	private VaultAddon vaultAddon;

	//TODO
	/*
	Optimize glow blocking system (reduce repeating code)
	 */

	@Override
	public void onEnable() {
		instance = this;
		api = new EGlowAPI();

		saveDefaultConfig();

		Arrays.asList(
				new ConfigMigration(this),
				new CustomEffectsMigration(this),
				new MessagesMigration(this)
		).forEach(migration -> {
			if (migration.applies()) {
				if (!migration.migrate()) {
					ChatUtil.sendToConsole(String.format("An unexpected error occurred while attempting to migrate configuration %s",
							migration.getName()), true);
				} else {
					ChatUtil.sendToConsole(String.format("Successfully migrated configuration %s!",
							migration.getName()), true);
				}
			}
		});

		if (versionIsCompatible()) {
			ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);

			NMSHook.initialize();

			loadConfigs();

			DataManager.initialize();

			registerCommands();
			registerEvents(new EGlowEventListener(this));

			try {
				this.upToDate = checkUpToDate();
			} catch (IOException exc) {
				ChatUtil.sendToConsole("An unexpected error occurred while attempting to check for updates!", true);
			}

			runAddonHooks();
			runPlayerCheckOnEnable();
		} else {
			ChatUtil.sendToConsole("Disabling eGlow! Your server version is not compatible! (" + ServerUtil.getVersion() + ")", false);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		if (glowAddon != null) {
			glowAddon.shutdown();
		}

		if (luckPermsAddon != null) {
			luckPermsAddon.unload();
		}

		runPlayerCheckOnDisable();
	}

	public void registerEvents(Listener... listeners) {
		Arrays.stream(listeners).forEach(listener -> {
			Bukkit.getPluginManager().registerEvents(listener, this);
		});
	}

	private boolean versionIsCompatible() {
		return !ServerUtil.getVersion().equals("v_1_9_R1")
				&& ServerUtil.getMinorVersion() >= 9
				&& ServerUtil.getMinorVersion() <= 20;
	}

	private void loadConfigs() {
		EGlowMainConfig.initialize();
		EGlowMessageConfig.initialize();
		EGlowCustomEffectsConfig.initialize();
		EGlowPlayerdataManager.initialize();
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("eglow")).setExecutor(new EGlowCommand());
	}

	private void runAddonHooks() {
		if (EGlowMainConfig.MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean() && glowAddon == null) {
			glowAddon = new AdvancedGlowVisibilityAddon();
		}

		if (Dependency.PLACEHOLDER_API.isLoaded()) {
			new PlaceholderAPIAddon();
		}

		if (Dependency.VAULT.isLoaded()) {
			vaultAddon = new VaultAddon(this);
		}

		if (Dependency.CITIZENS.isLoaded() && citizensAddon == null) {
			this.citizensAddon = new CitizensAddon(this);
		}

		if (Dependency.I_DISGUISE.isLoaded()) {
			this.iDisguiseAddon = new IDisguiseAddon(this);
		}

		if (Dependency.LIBS_DISGUISES.isLoaded()) {
			this.libDisguiseAddon = new LibDisguiseAddon(this);
		}

		if (Dependency.GSIT.isLoaded()) {
			new GSitAddon(this);
		}

		if (Dependency.TAB.isLoaded()) {
			if (!Dependency.isDefined("me.neznamy.tab")) {
				ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires TAB 3.1.4 or higher!", true);
				return;
			}

			this.tabAddon = new TabAddon(this);
		}

		if (Dependency.LUCK_PERMS.isLoaded()) {
			this.luckPermsAddon = new LuckPermsAddon(this);
		}
	}

	private void runPlayerCheckOnEnable() {
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> DataManager.getEGlowPlayer(player) != null)
				.forEach(GlowPlayerUtil::handlePlayerJoin);
	}

	private void runPlayerCheckOnDisable() {
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> DataManager.getEGlowPlayer(player) != null)
				.forEach(player -> GlowPlayerUtil.handlePlayerDisconnect(player, true));
	}

	private boolean checkUpToDate() throws IOException {
		URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=63295");

		String currentVersion = getInstance().getDescription().getVersion();
		String latestVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();

		if (currentVersion.contains("PRE")) {
			String betaVersion = currentVersion.split("-")[0];

			return !betaVersion.equals(latestVersion);
		} else {
			return latestVersion.contains(currentVersion);
		}
	}

}