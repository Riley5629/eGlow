package me.MrGraycat.eGlow.migration.impl;

import lombok.SneakyThrows;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.migration.Migration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessagesMigration extends Migration {

    public MessagesMigration(EGlow instance) {
        super(instance);
    }

    @Override
    public String getName() {
        return "messages";
    }

    @Override
    public boolean applies() {
        return getFile("messages.yml").exists();
    }

    @Override
    @SneakyThrows
    public boolean migrate() {
        Path oldPath = Paths.get(getFile("Messages.yml").getPath());
        Path newPath = Paths.get(getFile("messages.yml").getPath());

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
