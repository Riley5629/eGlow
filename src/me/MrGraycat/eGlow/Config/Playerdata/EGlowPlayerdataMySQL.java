package me.MrGraycat.eGlow.Config.Playerdata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowPlayerdataMySQL {
	private EGlow instance;
	
	MysqlDataSource mysql;

	/**
	 * Initialise externam playerdata using MySQL
	 */
	public EGlowPlayerdataMySQL(EGlow instance) {
		setInstance(instance);
		setupMySQLConnection();
		
		if (testMySQLConnection()) {
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
		String statement = "";
		
		statement = "SELECT * FROM eglow WHERE UUID='" + ePlayer.getUUID().toString() + "'";
		
		try {
			con = mysql.getConnection();
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
					if (!ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) 
						ePlayer.setGlowVisibility(GlowVisibility.ALL);
					} else {
						ePlayer.setGlowVisibility((res.getString("glowVisibility").equals(GlowVisibility.UNSUPPORTEDCLIENT.name()) ? ePlayer.getGlowVisibility() : GlowVisibility.valueOf(res.getString("glowVisibility"))));
					}
			
				if (res.getString("glowDisableReason") == null || res.getString("glowDisableReason").isEmpty()) {
					ePlayer.setGlowDisableReason(GlowDisableReason.NONE);
				} else {
					ePlayer.setGlowDisableReason(GlowDisableReason.valueOf(res.getString("glowDisableReason")));
				}
			} else {
				getInstance().getPlayerdataManager().setDefaultValues(ePlayer);
			}
		} catch(SQLException e) {
			ChatUtil.reportError(e);
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
		String statement = "";
		
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
			con = mysql.getConnection();
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
			closeMySQLConnection(con, ps, null);
		}
	}
	
	public boolean savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
		Connection con = null;
		PreparedStatement ps = null;
		String statement = "";
		
		statement = "INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?";
		
		try {
			con = mysql.getConnection();
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
			closeMySQLConnection(con, ps, null);
		}
	}

	//https://www.programcreek.com/java-api-examples/index.php?api=com.mysql.cj.jdbc.MysqlDataSource
	private boolean setupMySQLConnection() {
		mysql = new MysqlDataSource();
		
		mysql.setServerName(EGlowMainConfig.getMySQLHost());
		mysql.setPort(EGlowMainConfig.getMySQLPort());
		mysql.setDatabaseName(((!EGlowMainConfig.getMySQLUseSSL()) ? "?useSSL=false" : "")); //no database name for now
		mysql.setUser(EGlowMainConfig.getMySQLUsername());
		mysql.setPassword(EGlowMainConfig.getMySQLPassword());
		
		return testMySQLConnection();
	}
	
	private boolean testMySQLConnection() {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		
		String statement = "CREATE DATABASE IF NOT EXISTS " + ((!EGlowMainConfig.getMySQLDBName().isEmpty()) ? EGlowMainConfig.getMySQLDBName() : "eglow");
		
		try {
			con = mysql.getConnection();
			ps = con.prepareStatement(statement);
			ps.executeUpdate();
		} catch(SQLException e) {
			ChatUtil.reportError(e);
		} finally {
			closeMySQLConnection(con, ps, res);
		}
		
		mysql.setDatabaseName(((!EGlowMainConfig.getMySQLDBName().isEmpty()) ? EGlowMainConfig.getMySQLDBName() : "eglow") + ((!EGlowMainConfig.getMySQLUseSSL()) ? "?useSSL=false" : ""));
		
		try {
			con = mysql.getConnection();
			DatabaseMetaData dbm = con.getMetaData();
			res = dbm.getTables(null, null, "eglow", null);
			
			if (!res.next()) {
				statement = "CREATE TABLE eglow (UUID VARCHAR(255) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(255), glowVisibility VARCHAR(255), glowDisableReason VARCHAR(255), PRIMARY KEY (UUID))";
			} else {
				statement = "ALTER TABLE eglow DROP lastType, ADD lastGlowData VARCHAR(255), ADD glowVisibility VARCHAR(255), ADD glowDisableReason VARCHAR(255)";
			}
			ps = con.prepareStatement(statement);
			try {ps.executeUpdate();} catch(Exception e) {}
			return true;
		} catch(SQLException e) {
			ChatUtil.reportError(e);
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
		} catch (SQLException e) {}
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	private EGlow getInstance() {
		return this.instance;
	}
}
