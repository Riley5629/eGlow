package me.MrGraycat.eGlow.migration;

import lombok.AllArgsConstructor;
import me.MrGraycat.eGlow.EGlow;

import java.io.File;

@AllArgsConstructor
public abstract class Migration {

    protected final EGlow instance;

    public abstract String getName();

    public abstract boolean applies();

    public abstract boolean migrate();

    protected File getFile(String path) {
        return new File(instance.getDataFolder() + File.separator + path);
    }
}
