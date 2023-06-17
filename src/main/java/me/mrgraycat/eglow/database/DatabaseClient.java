package me.mrgraycat.eglow.database;

import me.mrgraycat.eglow.database.credentials.DatabaseCredentials;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DatabaseClient {

    protected static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(1);

    public abstract void init(DatabaseCredentials credentials);

    public abstract void close();

    public abstract void loadPlayerData(IEGlowPlayer player);

    public abstract void savePlayerData(IEGlowPlayer player);

}
