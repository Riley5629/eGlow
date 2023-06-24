package me.MrGraycat.eGlow.database.impl;

import lombok.SneakyThrows;
import me.MrGraycat.eGlow.database.DatabaseClient;
import me.MrGraycat.eGlow.database.credentials.impl.MySqlDatabaseCredentials;
import me.MrGraycat.eGlow.database.credentials.impl.SqLiteDatabaseCredentials;
import me.MrGraycat.eGlow.database.credentials.DatabaseCredentials;
import me.MrGraycat.eGlow.manager.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.manager.glow.IEGlowPlayer;
import me.MrGraycat.eGlow.util.Common;

import java.sql.*;

public class SqlDatabaseClient extends DatabaseClient {

    private Connection connection;

    private boolean sqLite;

    @Override
    public void init(DatabaseCredentials paramCredentials) {
        if (paramCredentials instanceof MySqlDatabaseCredentials) {
            MySqlDatabaseCredentials credentials = (MySqlDatabaseCredentials) paramCredentials;

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException exc) {
                throw new IllegalStateException("Failed to establish SQL connection - no MySQL driver found!");
            }

            try {
                this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true",
                                credentials.getIp(), credentials.getPort(), credentials.getDatabaseName()),
                        credentials.getUsername(), credentials.getPassword());
                return;
            } catch (Exception exc) {
                exc.printStackTrace();
                throw new IllegalArgumentException("Database credentials are invalid (type MySQL)!");
            }
        } else if (paramCredentials instanceof SqLiteDatabaseCredentials) {
            SqLiteDatabaseCredentials credentials = (SqLiteDatabaseCredentials) paramCredentials;

            try {
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + credentials.getDatabasePath());
                this.sqLite = true;
                return;
            } catch (Exception exc) {
                exc.printStackTrace();
                throw new IllegalArgumentException("Database credentials are invalid (type SQLite)!");
            }
        }

        throw new IllegalArgumentException("Provided credentials do not support MySql or SqLite!");
    }

    @SneakyThrows
    @Override
    public void close() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    @SneakyThrows
    @Override
    public void loadPlayerData(IEGlowPlayer player) {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM eglow WHERE UUID=?");

        statement.setString(1, player.getUuid().toString());

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            player.setGlowOnJoin(resultSet.getBoolean("glowOnJoin"));
            player.setActiveOnQuit(resultSet.getBoolean("activeOnQuit"));

            //Update from <=2.0.9 to 3.0(+) or load values
            if (resultSet.getString("lastGlowData") == null || resultSet.getString("lastGlowData").isEmpty()) {
                player.setDataFromLastGlow("none");
            } else {
                player.setDataFromLastGlow(resultSet.getString("lastGlowData"));
            }

            if (resultSet.getString("glowVisibility") == null || resultSet.getString("glowVisibility").isEmpty()) {
                if (!player.getGlowVisibility().equals(Common.GlowVisibility.UNSUPPORTEDCLIENT))
                    player.setGlowVisibility(Common.GlowVisibility.ALL);
            } else {
                player.setGlowVisibility(resultSet.getString("glowVisibility").equals(Common.GlowVisibility.UNSUPPORTEDCLIENT.name()) ?
                        player.getGlowVisibility() : Common.GlowVisibility.valueOf(resultSet.getString("glowVisibility")));
            }

            if (resultSet.getString("glowDisableReason") == null || resultSet.getString("glowDisableReason").isEmpty()) {
                player.setGlowDisableReason(Common.GlowDisableReason.NONE, true);
            } else {
                player.setGlowDisableReason(Common.GlowDisableReason.valueOf(resultSet.getString("glowDisableReason")), true);
            }
        } else {
            EGlowPlayerdataManager.setDefaultValues(player);
        }
    }

    @Override
    public void savePlayerData(IEGlowPlayer player) {
        ASYNC_EXECUTOR.execute(() -> {
            PreparedStatement statement = this.sqLite ? getSqLiteInsertionStatement() : getMySqlInsertionStatement();

            try {
                statement.setString(1, player.getUuid().toString());
                statement.setBoolean(2, player.isGlowOnJoin());
                statement.setBoolean(3, player.isActiveOnQuit());
                statement.setString(4, player.getLastGlow());
                statement.setString(5, player.getGlowVisibility().name());
                statement.setString(6, player.getGlowDisableReason().name());

                if (!this.sqLite) {
                    statement.setString(7, player.getUuid().toString());
                    statement.setBoolean(8, player.isGlowOnJoin());
                    statement.setBoolean(9, player.isActiveOnQuit());
                    statement.setString(10, player.getLastGlow());
                    statement.setString(11, player.getGlowVisibility().name());
                    statement.setString(12, player.getGlowDisableReason().name());
                }

                while (!insertOrUpdate(statement)) {
                    /*
                    Accommodates for SqLite's lock system
                     */

                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException exc) {
                        exc.printStackTrace();
                        return;
                    }
                }
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        });
    }

    public boolean insertOrUpdate(PreparedStatement statement) {
        try {
            statement.executeUpdate();
            return true;
        } catch (Exception exc) {
            if (this.sqLite && exc.getMessage().startsWith("[SQLITE_BUSY]")) {
                return false;
            }

            exc.printStackTrace();
            throw new RuntimeException("An unexpected error occurred while attempting to save data!");
        }
    }

    @SneakyThrows
    public PreparedStatement getMySqlInsertionStatement() {
        return connection.prepareStatement("INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, " +
                "glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY " +
                "UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?");
    }

    @SneakyThrows
    public PreparedStatement getSqLiteInsertionStatement() {
        return connection.prepareStatement("INSERT OR REPLACE INTO eglow (UUID, glowOnJoin, activeOnQuit, " +
                "lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?)");
    }
}
