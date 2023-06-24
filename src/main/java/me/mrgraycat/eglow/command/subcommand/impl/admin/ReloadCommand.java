package me.mrgraycat.eglow.command.subcommand.impl.admin;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.EGlowPlayerdataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReloadCommand extends SubCommand {

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "Reload the plugin";
	}

	@Override
	public String getPermission() {
		return "eglow.command.reload";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow reload"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		if (EGlowMainConfig.reloadConfig() && EGlowMessageConfig.reloadConfig() && EGlowCustomEffectsConfig.reloadConfig()) {
			EGlowPlayerdataManager.setMysql_Failed(false);
			DataManager.addEGlowEffects();

			Bukkit.getOnlinePlayers().stream()
					.map(DataManager::getEGlowPlayer)
					.filter(Objects::nonNull)
					.forEach(glowPlayer -> {
						glowPlayer.updatePlayerTabName();

						IEGlowEffect effect = glowPlayer.getForceGlow();
						GlowDisableReason oldGlowDisableReason = glowPlayer.getGlowDisableReason();

						glowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false);

						GlowDisableReason newGlowDisableReason = glowPlayer.getGlowDisableReason();

						if (newGlowDisableReason.equals(GlowDisableReason.NONE) && !oldGlowDisableReason.equals(GlowDisableReason.NONE)) {
							if (glowPlayer.isGlowing()) {
								return;
							}

							if (effect == null) {
								glowPlayer.activateGlow();
							} else {
								glowPlayer.activateGlow(effect);
							}
							ChatUtil.sendMessage(glowPlayer, Message.WORLD_ALLOWED.get(), true);
						} else {
							if (!glowPlayer.isGlowing() || newGlowDisableReason.equals(oldGlowDisableReason) || oldGlowDisableReason.equals(GlowDisableReason.NONE)) {
								return;
							}

							switch (newGlowDisableReason) {
								case BLOCKEDWORLD:
									ChatUtil.sendMessage(glowPlayer, Message.WORLD_BLOCKED.get(), true);
									break;
								case DISGUISE:
									ChatUtil.sendMessage(glowPlayer, Message.DISGUISE_BLOCKED.get(), true);
									break;
								case INVISIBLE:
									ChatUtil.sendMessage(glowPlayer, Message.INVISIBILITY_DISABLED.get(), true);
									break;
							}
							glowPlayer.disableGlow(false);
						}
					});

			try {
				String alias = MainConfig.COMMAND_ALIAS.getString();

				if (MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null && Bukkit.getServer().getPluginCommand(alias) == null) {
					Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
					commandMapField.setAccessible(true);

					CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
					commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
				}
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				ChatUtil.reportError(e);
			}

			ChatUtil.sendMessage(commandSender, Message.RELOAD_SUCCESS.get(), true);
		} else {
			ChatUtil.sendMessage(commandSender, Message.RELOAD_FAIL.get(), true);
		}
	}
}