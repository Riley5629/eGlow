package me.mrgraycat.eglow.migration.impl;

import lombok.SneakyThrows;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.migration.Migration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomEffectsMigration extends Migration {

    public CustomEffectsMigration(EGlow instance) {
        super(instance);
    }

    @Override
    public String getName() {
        return "custom_effects";
    }

    @Override
    public boolean applies() {
        return getFile("custom-effects.yml").exists();
    }

    @Override
    @SneakyThrows
    public boolean migrate() {
        Path oldPath = Paths.get(getFile("custom-effects.yml").getPath());
        Path newPath = Paths.get(getFile("custom-effects.yml").getPath());

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
