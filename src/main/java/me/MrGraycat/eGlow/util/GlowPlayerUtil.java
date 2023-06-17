package me.MrGraycat.eGlow.util;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.manager.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.manager.glow.IEGlowEffect;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.packet.PacketUtil;
import me.MrGraycat.eGlow.util.packet.PipelineInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

public class GlowPlayerUtil {
    /**
     * Code to initialise the player
     *
     * @param player player to initialise
     */
    public static void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        //Fixes permanent player glows from old eGlow versions/other glow plugins that use Player#setGlowing(true)
        if (player.isGlowing()) {
            player.setGlowing(false);
        }

        IEGlowPlayer glowPlayer = DataManager.addEGlowPlayer(player);
        PipelineInjector.inject(glowPlayer);
        PacketUtil.scoreboardPacket(glowPlayer, true);

        new BukkitRunnable() {
            @Override
            public void run() {
                EGlowPlayerdataManager.loadPlayerdata(glowPlayer);
                glowPlayer.setSaveData(false);

                if (!EGlow.getInstance().isUpToDate() && EGlowMainConfig.MainConfig.SETTINGS_NOTIFICATIONS_UPDATE.getBoolean() && player.hasPermission("eglow.option.update"))
                    ChatUtil.sendPlainMessage(player, "&aA new update is available&f!", true);

                if (EGlowPlayerdataManager.getMySQL_Failed() && player.hasPermission("eglow.option.update"))
                    ChatUtil.sendPlainMessage(player, "&cMySQL failed to enable properly, have a look at this asap&f.", true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PacketUtil.updatePlayer(glowPlayer);
                    }
                }.runTask(EGlow.getInstance());

                glowPlayer.updatePlayerTabName();

                IEGlowEffect effect = glowPlayer.getForceGlow();

                if (effect != null) {
                    if (EGlow.getInstance().getLibDisguiseAddon() != null && EGlow.getInstance().getLibDisguiseAddon().isDisguised(player) || EGlow.getInstance().getIDisguiseAddon() != null && EGlow.getInstance().getIDisguiseAddon().isDisguised(player)) {
                        glowPlayer.setGlowDisableReason(Common.GlowDisableReason.DISGUISE, false);
                        ChatUtil.sendMessage(player, EGlowMessageConfig.Message.DISGUISE_BLOCKED.get(), true);
                    } else if (glowPlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY) && !glowPlayer.getGlowDisableReason().equals(Common.GlowDisableReason.INVISIBLE)) {
                        glowPlayer.setGlowDisableReason(Common.GlowDisableReason.INVISIBLE, false);
                        ChatUtil.sendMessage(glowPlayer.getPlayer(), EGlowMessageConfig.Message.INVISIBILITY_DISABLED.get(), true);
                    } else {
                        glowPlayer.activateGlow(effect);
                        if (EGlowMainConfig.MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && glowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
                            ChatUtil.sendMessage(glowPlayer.getPlayer(), EGlowMessageConfig.Message.GLOWING_STATE_ON_JOIN.get(effect.getDisplayName()), true);
                        return;
                    }
                    if (EGlowMainConfig.MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && glowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
                        ChatUtil.sendMessage(glowPlayer.getPlayer(), EGlowMessageConfig.Message.NON_GLOWING_STATE_ON_JOIN.get(), true);
                    return;
                }

                if (glowPlayer.isActiveOnQuit()) {
                    if (glowPlayer.getGlowEffect() == null || !glowPlayer.isGlowOnJoin() || !player.hasPermission("eglow.option.glowonjoin") || EGlowMainConfig.MainConfig.SETTINGS_JOIN_CHECK_PERMISSION.getBoolean() && !player.hasPermission(glowPlayer.getGlowEffect().getPermission()))
                        return;

                    if (glowPlayer.isInBlockedWorld()) {
                        if (glowPlayer.isGlowing()) {
                            glowPlayer.disableGlow(false);
                            glowPlayer.setGlowDisableReason(Common.GlowDisableReason.BLOCKEDWORLD, false);
                            ChatUtil.sendMessage(player, EGlowMessageConfig.Message.WORLD_BLOCKED.get(), true);
                            return;
                        }
                    }

                    if (EGlow.getInstance().getLibDisguiseAddon() != null && EGlow.getInstance().getLibDisguiseAddon().isDisguised(player) || EGlow.getInstance().getIDisguiseAddon() != null && EGlow.getInstance().getIDisguiseAddon().isDisguised(player)) {
                        glowPlayer.setGlowDisableReason(Common.GlowDisableReason.DISGUISE, false);
                        ChatUtil.sendMessage(player, EGlowMessageConfig.Message.DISGUISE_BLOCKED.get(), true);
                        return;
                    }

                    try {
                        glowPlayer.activateGlow();
                    } catch (NullPointerException e) {
                        //Prevent rare but useless message whenever something causes the player to disconnect whilst joining the server
                    }

                    if (EGlowMainConfig.MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && glowPlayer.getPlayer().hasPermission("eglow.option.glowstate") && glowPlayer.getGlowEffect() != null)
                        ChatUtil.sendMessage(glowPlayer.getPlayer(), EGlowMessageConfig.Message.GLOWING_STATE_ON_JOIN.get(glowPlayer.getGlowEffect().getDisplayName()), true);
                    return;
                }

                if (EGlowMainConfig.MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && glowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
                    ChatUtil.sendMessage(glowPlayer.getPlayer(), EGlowMessageConfig.Message.NON_GLOWING_STATE_ON_JOIN.get(), true);
            }
        }.runTaskLaterAsynchronously(EGlow.getInstance(), 2L);
    }

    /**
     * Code to unload the player from eGlow
     *
     * @param player player to unload
     */
    public static void handlePlayerDisconnect(Player player, boolean shutdown) {
        IEGlowPlayer glowPlayer = DataManager.getEGlowPlayer(player);
        PacketUtil.scoreboardPacket(glowPlayer, false);

        if (!shutdown) {
            Bukkit.getScheduler().runTaskAsynchronously(EGlow.getInstance(), () -> {
                disconnectPlayer(glowPlayer);
            });
        } else {
            disconnectPlayer(glowPlayer);
        }
    }

    private static void disconnectPlayer(IEGlowPlayer glowPlayer) {
        if (glowPlayer == null) {
            return;
        }

        glowPlayer.setActiveOnQuit(glowPlayer.isGlowing());
        EGlowPlayerdataManager.savePlayerdata(glowPlayer);

        PipelineInjector.uninject(glowPlayer);
        DataManager.removeGlowPlayer(glowPlayer.getPlayer());

        Optional.of(EGlow.getInstance().getGlowAddon())
                .ifPresent((glowAddon) -> glowAddon.removePlayerFromCache(glowPlayer));
    }
}
