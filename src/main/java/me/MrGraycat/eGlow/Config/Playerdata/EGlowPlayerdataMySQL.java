package me.MrGraycat.eGlow.Config.Playerdata;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;

public class EGlowPlayerdataMySQL {
    Object mysql;

    /**
     * Initialise external playerdata using MySQL
     */
    public EGlowPlayerdataMySQL() {
        setupMySQLConnection();

        if (testMySQLConnection()) {
            ChatUtil.sendToConsole("&aSuccessfully loaded MySQL.", true);
        } else {
            EGlowPlayerdataManager.setMysql_Failed(true);
            ChatUtil.sendToConsole("&cFailed to load MySQL.", true);
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
            con = getConnection();
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
            e.printStackTrace();
        } finally {
            closeMySQLConnection(con, ps, res);
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
            con = getConnection();
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
            e.printStackTrace();
        } finally {
            closeMySQLConnection(con, ps, null);
        }
    }

    public void savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
        Connection con = null;
        PreparedStatement ps = null;
        String statement;

        statement = "INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?";

        try {
            con = getConnection();
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeMySQLConnection(con, ps, null);
        }
    }

    private void setupMySQLConnection() {
        mysql = getMySQLDataSource();

        setServerName(EGlowMainConfig.MainConfig.MYSQL_HOST.getString());
        setPort(EGlowMainConfig.MainConfig.MYSQL_PORT.getInt());
        setDatabaseName(((!EGlowMainConfig.MainConfig.ADVANCED_MYSQL_USESSL.getBoolean()) ? "?useSSL=false" : "")); //no database name for now
        setUser(EGlowMainConfig.MainConfig.MYSQL_USERNAME.getString());
        setPassword(EGlowMainConfig.MainConfig.MYSQL_PASSWORD.getString());

        testMySQLConnection();
    }

    private boolean testMySQLConnection() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String statement = "CREATE DATABASE IF NOT EXISTS " + ((!EGlowMainConfig.MainConfig.MYSQL_DBNAME.getString().isEmpty()) ? EGlowMainConfig.MainConfig.MYSQL_DBNAME.getString() : "eglow");

        try {
            con = getConnection();
            ps = con.prepareStatement(statement);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            closeMySQLConnection(con, ps, null);
        }

        setDatabaseName(((!EGlowMainConfig.MainConfig.MYSQL_DBNAME.getString().isEmpty()) ? EGlowMainConfig.MainConfig.MYSQL_DBNAME.getString() : "eglow") + ((!EGlowMainConfig.MainConfig.ADVANCED_MYSQL_USESSL.getBoolean()) ? "?useSSL=false" : ""));

        try {
            con = getConnection();
            DatabaseMetaData dbm = con.getMetaData();
            res = dbm.getTables(null, null, "eglow", null);

            if (!res.next()) {
                statement = "CREATE TABLE eglow (UUID VARCHAR(255) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(255), glowVisibility VARCHAR(255), glowDisableReason VARCHAR(255), PRIMARY KEY (UUID))";
            } else {
                statement = "ALTER TABLE eglow DROP lastType, ADD lastGlowData VARCHAR(255), ADD glowVisibility VARCHAR(255), ADD glowDisableReason VARCHAR(255)";
            }
            ps = con.prepareStatement(statement);
            try {ps.executeUpdate();} catch(Exception e) {/*Ignored*/}
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeMySQLConnection(con, ps, res);
        }
    }

    private void closeMySQLConnection(Connection con, PreparedStatement ps, ResultSet res) {
        try {
            if (con != null)
                con.close();
            if (ps != null)
                ps.close();
            if (res != null)
                res.close();
        } catch (SQLException e) {/*Ignored*/}
    }

    private Object getMySQLDataSource() {
        try {
            return NMSHook.nms.newMySQLDataSource.newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Connection getConnection() {
        try {
            return (Connection) NMSHook.nms.MySQL_getConnection.invoke(mysql);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setServerName(String serverName) {
        try {
            NMSHook.nms.MySQL_setServerName.invoke(mysql, serverName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setPort(int port) {
        try {
            NMSHook.nms.MySQL_setPort.invoke(mysql, port);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setDatabaseName(String databaseName) {
        try {
            NMSHook.nms.MySQL_setDatabaseName.invoke(mysql, databaseName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setUser(String user) {
        try {
            NMSHook.nms.MySQL_setUser.invoke(mysql, user);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setPassword(String password) {
        try {
            NMSHook.nms.MySQL_setPassword.invoke(mysql, password);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
