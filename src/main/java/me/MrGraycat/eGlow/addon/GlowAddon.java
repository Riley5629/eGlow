package me.MrGraycat.eGlow.addon;

import me.MrGraycat.eGlow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class GlowAddon {

    protected final EGlow instance;

    public GlowAddon(EGlow instance) {
        this.instance = instance;

        if (this instanceof Listener) {
            instance.registerEvents((Listener) this);
        }
    }

    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTask(instance, runnable);
    }

    public void runTaskLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(instance, runnable, delay);
    }

    public void runTaskLaterAsynchronously(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, runnable, delay);
    }
}
