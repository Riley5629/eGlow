package me.mrgraycat.eglow.command.subcommands.admin;

import me.mrgraycat.eglow.addon.internal.AdvancedGlowVisibilityAddon;
import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.data.DataManager;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.database.EGlowPlayerdataManager;
import me.mrgraycat.eglow.util.enums.EnumUtil;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

	@Override
	public String getName() {
		return "reload";
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
	public void perform(CommandSender sender, EGlowPlayer eGlowPlayer, String[] args) {
		if (EGlowMainConfig.reloadConfig() && EGlowMessageConfig.reloadConfig() && EGlowCustomEffectsConfig.reloadConfig()) {
			EGlowPlayerdataManager.setMysql_Failed(false);
			NMSHook.registerCommandAlias();
			DataManager.addEGlowEffects();

			boolean advancedGlowVisibilityEnabled = MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean();

			if (advancedGlowVisibilityEnabled && getInstance().getAdvancedGlowVisibilityAddon() == null) {
				getInstance().setAdvancedGlowVisibilityAddon(new AdvancedGlowVisibilityAddon());
			} else if (!advancedGlowVisibilityEnabled && getInstance().getAdvancedGlowVisibilityAddon() != null) {
				getInstance().getAdvancedGlowVisibilityAddon().shutdown();
			}

			for (EGlowPlayer eGlowTarget : DataManager.getEGlowPlayers()) {
				if (eGlowTarget == null)
					continue;

				eGlowTarget.setupForceGlows();
				eGlowTarget.updatePlayerTabname();

				EnumUtil.GlowDisableReason oldGlowDisableReason = eGlowTarget.getGlowDisableReason();
				EnumUtil.GlowDisableReason newGlowDisableReason = eGlowTarget.setGlowDisableReason(EnumUtil.GlowDisableReason.NONE);

				if (oldGlowDisableReason.equals(newGlowDisableReason))
					continue;

				if (oldGlowDisableReason.equals(EnumUtil.GlowDisableReason.NONE)) {
					if (eGlowTarget.isGlowing())
						eGlowTarget.disableGlow(false);
				} else {
					eGlowTarget.activateGlow();
				}
			}

			ChatUtil.sendMsg(sender, Message.RELOAD_SUCCESS.get(), true);
		} else {
			ChatUtil.sendMsg(sender, Message.RELOAD_FAIL.get(), true);
		}
	}
}