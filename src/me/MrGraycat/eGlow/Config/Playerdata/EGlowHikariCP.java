package me.MrGraycat.eGlow.Config.Playerdata;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

import java.sql.*;
import java.util.Properties;

public class EGlowHikariCP {
    HikariConfig config;
    HikariDataSource hikari;

    public EGlowHikariCP() {
        setupHikariCPConnection();

        if (testHikariCPConnection()) {
            ChatUtil.sendToConsoleWithPrefix("&aSuccessfully loaded MySQL.");
        } else {
            ChatUtil.sendToConsoleWithPrefix("&cFailed to load MySQL.");
        }
    }

    /**
     * Load a players data into eGlow
     * @param ePlayer player to load data from
     */
    public void loadPlayerdata(IEGlowPlayer ePlayer) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        String statement;

        statement = "SELECT * FROM eglow WHERE UUID='" + ePlayer.getUUID().toString() + "'";

        try {
            con = hikari.getConnection();
            ps = con.prepareStatement(statement);
            res = ps.executeQuery();

            if (res.next()) {
                ePlayer.setGlowOnJoin(res.getBoolean("glowOnJoin"));
                ePlayer.setActiveOnQuit(res.getBoolean("activeOnQuit"));

                //Update from <=2.0.9 to 3.0(+) or load values
                if (res.getString("lastGlowData") == null || res.getString("lastGlowData").isEmpty()) {
                    ePlayer.setDataFromLastGlow("none");
                } else {
                    ePlayer.setDataFromLastGlow(res.getString("lastGlowData"));
                }

                if (res.getString("glowVisibility") == null || res.getString("glowVisibility").isEmpty()) {
                    if (!ePlayer.getGlowVisibility().equals(EnumUtil.GlowVisibility.UNSUPPORTEDCLIENT))
                        ePlayer.setGlowVisibility(EnumUtil.GlowVisibility.ALL);
                } else {
                    ePlayer.setGlowVisibility((res.getString("glowVisibility").equals(EnumUtil.GlowVisibility.UNSUPPORTEDCLIENT.name()) ? ePlayer.getGlowVisibility() : EnumUtil.GlowVisibility.valueOf(res.getString("glowVisibility"))));
                }

                if (res.getString("glowDisableReason") == null || res.getString("glowDisableReason").isEmpty()) {
                    ePlayer.setGlowDisableReason(EnumUtil.GlowDisableReason.NONE);
                } else {
                    ePlayer.setGlowDisableReason(EnumUtil.GlowDisableReason.valueOf(res.getString("glowDisableReason")));
                }
            } else {
                EGlowPlayerdataManager.setDefaultValues(ePlayer);
            }
        } catch(SQLException e) {
            ChatUtil.reportError(e);
        } finally {
            closeConnection(con, ps, res);
        }
    }

    /**
     * Save the data for the given player
     * @param ePlayer player to save the data for
     */
    public void savePlayerdata(IEGlowPlayer ePlayer) {
        Connection con = null;
        PreparedStatement ps = null;
        String statement;

        boolean glowOnJoin;
        boolean activeOnQuit;
        String glowDisableReason;
        String glowVisibility;
        String lastGlowData;

        statement = "INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?";

        lastGlowData = ePlayer.getLastGlow();
        glowOnJoin = ePlayer.getGlowOnJoin();
        activeOnQuit = ePlayer.getActiveOnQuit();
        glowVisibility = ePlayer.getGlowVisibility().name();
        glowDisableReason = ePlayer.getGlowDisableReason().name();

        try {
            con = hikari.getConnection();
            ps = con.prepareStatement(statement);

            ps.setString(1, ePlayer.getUUID().toString());
            ps.setBoolean(2, glowOnJoin);
            ps.setBoolean(3, activeOnQuit);
            ps.setString(4, lastGlowData);
            ps.setString(5, glowVisibility);
            ps.setString(6, glowDisableReason);
            ps.setString(7, ePlayer.getUUID().toString());
            ps.setBoolean(8, glowOnJoin);
            ps.setBoolean(9, activeOnQuit);
            ps.setString(10, lastGlowData);
            ps.setString(11, glowVisibility);
            ps.setString(12, glowDisableReason);

            ps.executeUpdate();
        } catch (SQLException e) {
            ChatUtil.reportError(e);
        } finally {
            closeConnection(con, ps, null);
        }
    }

    public boolean savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
        Connection con = null;
        PreparedStatement ps = null;
        String statement;

        statement = "INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?";

        try {
            con = hikari.getConnection();
            ps = con.prepareStatement(statement);

            ps.setString(1, uuid);
            ps.setBoolean(2, glowOnJoin);
            ps.setBoolean(3, activeOnQuit);
            ps.setString(4, lastGlowData);
            ps.setString(5, glowVisibility);
            ps.setString(6, glowDisableReason);
            ps.setString(7, uuid);
            ps.setBoolean(8, glowOnJoin);
            ps.setBoolean(9, activeOnQuit);
            ps.setString(10, lastGlowData);
            ps.setString(11, glowVisibility);
            ps.setString(12, glowDisableReason);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ChatUtil.reportError(e);
            return false;
        } finally {
            closeConnection(con, ps, null);
        }
    }

    private void setupHikariCPConnection() {
        //TODO Ignore default loggers if they still exist
        config = new HikariConfig();

        try {
            Class.forName("com.mysql.cj.jdbc.MysqlDataSource");
            config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        } catch (ClassNotFoundException e) {
            config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        }

        config.setUsername(EGlowMainConfig.getMySQLUsername());
        config.setPassword(EGlowMainConfig.getMySQLPassword());
        config.addDataSourceProperty("serverName", EGlowMainConfig.getMySQLHost());
        config.addDataSourceProperty("portNumber", EGlowMainConfig.getMySQLPort());
        config.addDataSourceProperty("databaseName", ((!EGlowMainConfig.getMySQLUseSSL()) ? "?useSSL=false" : ""));
        hikari = new HikariDataSource(config);

        setupDatabase();

        config = new HikariConfig();

        try {
            Class.forName("com.mysql.cj.jdbc.MysqlDataSource");
            config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        } catch (ClassNotFoundException e) {
            config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        }

        config.setUsername(EGlowMainConfig.getMySQLUsername());
        config.setPassword(EGlowMainConfig.getMySQLPassword());
        config.addDataSourceProperty("serverName", EGlowMainConfig.getMySQLHost());
        config.addDataSourceProperty("portNumber", EGlowMainConfig.getMySQLPort());
        config.addDataSourceProperty("databaseName", (((!EGlowMainConfig.getMySQLDBName().isEmpty()) ? EGlowMainConfig.getMySQLDBName() : "eglow") + ((!EGlowMainConfig.getMySQLUseSSL()) ? "?useSSL=false" : "")));
        hikari = new HikariDataSource(config);
    }

    private void setupDatabase() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String statement = "CREATE DATABASE IF NOT EXISTS " + ((!EGlowMainConfig.getMySQLDBName().isEmpty()) ? EGlowMainConfig.getMySQLDBName() : "eglow");

        try {
            con = hikari.getConnection();
            ps = con.prepareStatement(statement);
            ps.executeUpdate();
        } catch(SQLException e) {
            ChatUtil.reportError(e);
        } finally {
            closeConnection(con, ps, null);
        }
    }

    private boolean testHikariCPConnection() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String statement = "";
        //TODO check if there are no errors at this point for mysql we set the dbname in 2 parts

        try {
            con = hikari.getConnection();
            DatabaseMetaData dbm = con.getMetaData();
            res = dbm.getTables(null, null, "eglow", null);

            if (!res.next()) {
                statement = "CREATE TABLE eglow (UUID VARCHAR(255) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(255), glowVisibility VARCHAR(255), glowDisableReason VARCHAR(255), PRIMARY KEY (UUID))";
            } else {
                statement = "ALTER TABLE eglow DROP lastType, ADD lastGlowData VARCHAR(255), ADD glowVisibility VARCHAR(255), ADD glowDisableReason VARCHAR(255)";
            }
            ps = con.prepareStatement(statement);
            try {ps.executeUpdate();} catch(Exception e) {
                //Forgot why I ignore this
            }
            return true;
        } catch(SQLException e) {
            ChatUtil.reportError(e);
            return false;
        } finally {
            closeConnection(con, ps, res);
        }
    }

    private void closeConnection(Connection con, PreparedStatement ps, ResultSet res) {
        try {
            if (con != null)
                con.close();
            if (ps != null)
                ps.close();
            if (res != null)
                res.close();
        } catch (SQLException e) {
            //
        }
    }
}
