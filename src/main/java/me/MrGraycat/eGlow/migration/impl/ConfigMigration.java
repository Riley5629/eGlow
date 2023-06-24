package me.MrGraycat.eGlow.migration.impl;

import lombok.SneakyThrows;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.migration.Migration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigMigration extends Migration {

    public ConfigMigration(EGlow instance) {
        super(instance);
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public boolean applies() {
        return getFile("Config.yml").exists();
    }

    @Override
    @SneakyThrows
    public boolean migrate() {
        Path oldPath = Paths.get(getFile("Config.yml").getPath());
        Path newPath = Paths.get(getFile("config.yml").getPath());

        if (!newPath.toFile().exists()) {
            newPath.toFile().createNewFile();
        }

        byte[] bytes = Files.readAllBytes(oldPath);
        Files.write(newPath, bytes);

        return oldPath.toFile().delete();
    }

    public File getFile(String path) {
        return new File(instance.getDataFolder() + File.separator + path);
    }
}
