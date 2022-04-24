package me.MrGraycat.eGlow.Config.Playerdata;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.scheduler.BukkitRunnable;
import org.sqlite.SQLiteDataSource;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowPlayerdataSQLite {
	private final ConcurrentHashMap<String, String> SavingQueue = new ConcurrentHashMap<>();
	
	SQLiteDataSource sqlite;

	/**
	 * Initialise external playerdata using SQLite
	 */
	public EGlowPlayerdataSQLite() {
		if (setupSQLiteConnection()) {
			ChatUtil.sendToConsole("&aSuccessfully loaded Playerdata database.", true);
			startSavingQueueHandler(); //TODO NEW STUFF HERE
		} else {
			ChatUtil.sendToConsole("&cFailed to load Playerdata database!.", true);
		}
	}
	
	/**
	 * Load a players data into eGlow
	 * @param ePlayer player to load data from
	 */
	public void loadPlayerdata(IEGlowPlayer ePlayer) {
		String playerUUID = ePlayer.getUUID().toString();
		
		if (SavingQueue.containsKey(playerUUID)) {
			String[] data = SavingQueue.get(playerUUID).split(",");
			SavingQueue.remove(playerUUID);
			
			ePlayer.setDataFromLastGlow(data[0]);
			ePlayer.setGlowOnJoin(Boolean.parseBoolean(data[1]));
			ePlayer.setActiveOnQuit(Boolean.parseBoolean(data[2]));
			ePlayer.setGlowVisibility(GlowVisibility.valueOf(data[3]));
			ePlayer.setGlowDisableReason(GlowDisableReason.valueOf(data[4]));
			return;
		}
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		String statement;
		
		statement = "SELECT * FROM eglow WHERE UUID='" + playerUUID + "'";
		
		try {
			con = sqlite.getConnection();
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
					//ePlayer.setGlowVisibility((GlowVisibility.valueOf(res.getString("glowVisibility")).equals(GlowVisibility.UNSUPPORTEDCLIENT) ? ePlayer.getGlowVisibility() : GlowVisibility.valueOf(res.getString("glowVisibility"))));
				}
			
				if (res.getString("glowDisableReason") == null || res.getString("glowDisableReason").isEmpty()) {
					ePlayer.setGlowDisableReason(GlowDisableReason.NONE);
				} else {
					ePlayer.setGlowDisableReason(GlowDisableReason.valueOf(res.getString("glowDisableReason")));
				}
			} else {
				EGlowPlayerdataManager.setDefaultValues(ePlayer);
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
		//TODO NEW STUFF HERE
		String values = ePlayer.getLastGlow() + "," + ePlayer.getGlowOnJoin() + "," + ePlayer.getActiveOnQuit() + "," + ePlayer.getGlowVisibility().name() + "," + ePlayer.getGlowDisableReason().name();
		
		if (SavingQueue.containsKey(ePlayer.getUUID().toString())) {
			SavingQueue.replace(ePlayer.getUUID().toString(), values);
		} else {
			SavingQueue.put(ePlayer.getUUID().toString(), values);
		}
		
		/*Connection con = null;
		PreparedStatement ps = null;
		String statement = "";
		
		boolean glowOnJoin;
		boolean activeOnQuit;
		String glowDisableReason;
		String glowVisibility;
		String lastGlowData;
		
		statement = "INSERT OR REPLACE INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?)";
		
		lastGlowData = ePlayer.getLastGlow();
		glowOnJoin = ePlayer.getGlowOnJoin();
		activeOnQuit = ePlayer.getActiveOnQuit();
		glowVisibility = ePlayer.getGlowVisibility().name();
		glowDisableReason = ePlayer.getGlowDisableReason().name(); 
		
		try {
			con = sqlite.getConnection();
			ps = con.prepareStatement(statement);
			
			ps.setString(1, ePlayer.getUUID().toString());
			ps.setBoolean(2, glowOnJoin);
			ps.setBoolean(3, activeOnQuit);
			ps.setString(4, lastGlowData);
			ps.setString(5, glowVisibility);
			ps.setString(6, glowDisableReason);
			
			ps.executeUpdate();
		} catch (SQLException e) {
			ChatUtil.reportError(e);
		} finally {
			closeMySQLConnection(con, ps, null);
		}*/
	}
	private boolean isActive = false;
	
	private void startSavingQueueHandler() {
		new BukkitRunnable() {
			public void run() {
				if (!isActive && !SavingQueue.isEmpty()) {
					isActive = true;
					processSavingQueue();
				}
			}
		}.runTaskTimerAsynchronously(EGlow.getInstance(), 0L, 20L);
	}
	
	private void processSavingQueue() {
		if (!SavingQueue.isEmpty()) {
			for (Map.Entry<String, String> entry : SavingQueue.entrySet()) {
				String statement = "INSERT OR REPLACE INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?)";
				String playerUUID = entry.getKey();
				String values = entry.getValue();
				String[] splitValues = values.split(",");
				
				Connection con = null;
				PreparedStatement ps = null;
				
				try {
					con = sqlite.getConnection();
					ps = con.prepareStatement(statement);
					
					ps.setString(1, playerUUID);
					ps.setBoolean(2, Boolean.parseBoolean(splitValues[1]));
					ps.setBoolean(3, Boolean.parseBoolean(splitValues[2]));
					ps.setString(4, splitValues[0]);
					ps.setString(5, splitValues[3]);
					ps.setString(6, splitValues[4]);
					
					ps.executeUpdate();
					SavingQueue.remove(playerUUID);
				} catch (Exception e) {
					if (e.getMessage().startsWith("[SQLITE_BUSY]")) {
						isActive = false;
						return;
					} else {
						e.printStackTrace();
						isActive = false;
						return;
					}
				} finally {
					closeMySQLConnection(con, ps, null);
				}
			}
		}
		
		isActive = false;
	}
	
	public boolean savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
		//TODO new method convertion 
		/*String values = lastGlowData + "," + glowOnJoin + "," + activeOnQuit + "," + glowVisibility + "," + glowDisableReason;
		
		SavingQueue.put(uuid, values);*/
		
		Connection con = null;
		PreparedStatement ps = null;
		String statement;
		
		statement = "INSERT OR REPLACE INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?)";
		
		try {
			con = sqlite.getConnection();
			ps = con.prepareStatement(statement);
			
			ps.setString(1, uuid);
			ps.setBoolean(2, glowOnJoin);
			ps.setBoolean(3, activeOnQuit);
			ps.setString(4, lastGlowData);
			ps.setString(5, glowVisibility);
			ps.setString(6, glowDisableReason);
			
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			ChatUtil.reportError(e);
			return false;
		} finally {
			closeMySQLConnection(con, ps, null);
		}
	}
	
	private boolean setupSQLiteConnection() {
		File dbFile = new File(EGlow.getInstance().getDataFolder(), "Playerdata.db;PRAGMA journal_mode=WAL;");
		
		//Check if the db exists with incorrect WAL journal mode implementation and renaming it to a proper DB file
		if (dbFile.exists())
			dbFile.renameTo(new File(EGlow.getInstance().getDataFolder(), "Playerdata.db"));
		
		sqlite = new SQLiteDataSource();

		sqlite.setUrl("jdbc:sqlite:" + EGlow.getInstance().getDataFolder() + File.separator + "Playerdata.db");
		
		//sqlite.setDatabaseName("eglow");
		//sqlite.setJournalMode("WAL");
		
		return testSQLiteConnection();
	}
	
	private boolean testSQLiteConnection() {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		
		String statement = "";
		
		try {
			con = sqlite.getConnection();
			DatabaseMetaData dbm = con.getMetaData();
			res = dbm.getTables(null, null, "eglow", null);
			
			if (!res.next()) {
				statement = "CREATE TABLE eglow (UUID VARCHAR(255) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(255), glowVisibility VARCHAR(255), glowDisableReason VARCHAR(255), PRIMARY KEY (UUID))";
			} 
			
			if (statement.isEmpty())
				return true;
			
			ps = con.prepareStatement(statement);
			try {ps.executeUpdate();} catch(Exception e) {
				//Forgot why I ignore this
			}
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
		} catch (SQLException e) {
			//
		}
	}
}