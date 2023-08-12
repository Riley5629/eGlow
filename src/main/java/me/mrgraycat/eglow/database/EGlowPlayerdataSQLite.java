package me.mrgraycat.eglow.database;

import lombok.Getter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.enums.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class EGlowPlayerdataSQLite {
	private final ConcurrentHashMap<String, String> savingQueue = new ConcurrentHashMap<>();
	private SQLiteDataSource sqlite;

	/**
	 * Initialise external playerdata using SQLite
	 */
	public EGlowPlayerdataSQLite() {
		if (setupSQLiteConnection()) {
			ChatUtil.sendToConsole("&aSuccessfully loaded Playerdata database.", true);
			startSavingQueueHandler();
		} else {
			ChatUtil.sendToConsole("&cFailed to load Playerdata database!.", true);
		}
	}

	/**
	 * Load a players data into eGlow
	 *
	 * @param eGlowPlayer player to load data from
	 */
	public void loadPlayerdata(EGlowPlayer eGlowPlayer) {
		String playerUUID = eGlowPlayer.getUuid().toString();

		if (getSavingQueue().containsKey(playerUUID)) {
			String[] data = getSavingQueue().get(playerUUID).split(",");
			getSavingQueue().remove(playerUUID);

			eGlowPlayer.setDataFromLastGlow(data[0]);
			eGlowPlayer.setGlowOnJoin(Boolean.parseBoolean(data[1]));
			eGlowPlayer.setActiveOnQuit(Boolean.parseBoolean(data[2]));
			eGlowPlayer.setGlowVisibility(GlowVisibility.valueOf(data[3]));
			eGlowPlayer.setForcedGlowDisableReason(GlowDisableReason.valueOf(data[4]));
			return;
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String statement;

		statement = "SELECT * FROM eglow WHERE UUID='" + playerUUID + "'";

		try {
			connection = getSqlite().getConnection();
			preparedStatement = connection.prepareStatement(statement);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				try {
					eGlowPlayer.setGlowOnJoin(resultSet.getBoolean("glowOnJoin"));
					eGlowPlayer.setActiveOnQuit(resultSet.getBoolean("activeOnQuit"));
					eGlowPlayer.setDataFromLastGlow(resultSet.getString("lastGlowData"));
					eGlowPlayer.setGlowVisibility((resultSet.getString("glowVisibility").equals(GlowVisibility.UNSUPPORTEDCLIENT.name()) ? eGlowPlayer.getGlowVisibility() : GlowVisibility.valueOf(resultSet.getString("glowVisibility"))));
					eGlowPlayer.setForcedGlowDisableReason(GlowDisableReason.valueOf(resultSet.getString("glowDisableReason")));
				} catch (NullPointerException | IllegalArgumentException exception) {
					ChatUtil.sendToConsole("Playerdata of player: " + eGlowPlayer.getDisplayName() + " has been reset due to a corrupted value.", true);
					EGlowPlayerdataManager.setDefaultValues(eGlowPlayer);
				}
			} else {
				EGlowPlayerdataManager.setDefaultValues(eGlowPlayer);
			}
		} catch (SQLException exception) {
			ChatUtil.reportError(exception);
		} finally {
			closeMySQLConnection(connection, preparedStatement, resultSet);
		}
	}

	/**
	 * Save the data for the given player
	 *
	 * @param eGlowPlayer player to save the data for
	 */
	public void savePlayerdata(EGlowPlayer eGlowPlayer) {
		String values = eGlowPlayer.getLastGlow() + "," + eGlowPlayer.isGlowOnJoin() + "," + eGlowPlayer.isActiveOnQuit() + "," + eGlowPlayer.getGlowVisibility().name() + "," + eGlowPlayer.getGlowDisableReason().name();

		if (getSavingQueue().containsKey(eGlowPlayer.getUuid().toString())) {
			getSavingQueue().replace(eGlowPlayer.getUuid().toString(), values);
		} else {
			getSavingQueue().put(eGlowPlayer.getUuid().toString(), values);
		}
	}

	private boolean isActive = false;

	private void startSavingQueueHandler() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isActive && !getSavingQueue().isEmpty()) {
					isActive = true;
					processSavingQueue();
				}
			}
		}.runTaskTimerAsynchronously(EGlow.getInstance(), 0L, 20L);
	}

	private void processSavingQueue() {
		for (Map.Entry<String, String> entry : getSavingQueue().entrySet()) {
			String statement = "INSERT OR REPLACE INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?)";
			String playerUUID = entry.getKey();
			String values = entry.getValue();
			String[] splitValues = values.split(",");

			Connection connection = null;
			PreparedStatement preparedStatement = null;

			try {
				connection = getSqlite().getConnection();
				preparedStatement = connection.prepareStatement(statement);

				preparedStatement.setString(1, playerUUID);
				preparedStatement.setBoolean(2, Boolean.parseBoolean(splitValues[1]));
				preparedStatement.setBoolean(3, Boolean.parseBoolean(splitValues[2]));
				preparedStatement.setString(4, splitValues[0]);
				preparedStatement.setString(5, splitValues[3]);
				preparedStatement.setString(6, splitValues[4]);

				preparedStatement.executeUpdate();
				getSavingQueue().remove(playerUUID);
			} catch (Exception exception) {
				if (!exception.getMessage().startsWith("[SQLITE_BUSY]")) {
					exception.printStackTrace();
				}
				isActive = false;
				return;
			} finally {
				closeMySQLConnection(connection, preparedStatement, null);
			}
		}
		isActive = false;
	}

	private boolean setupSQLiteConnection() {
		sqlite = new SQLiteDataSource();
		getSqlite().setUrl("jdbc:sqlite:" + EGlow.getInstance().getDataFolder() + File.separator + "Playerdata.db");

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		String statement = "";

		try {
			connection = getSqlite().getConnection();
			DatabaseMetaData dbm = connection.getMetaData();
			resultSet = dbm.getTables(null, null, "eglow", null);

			if (!resultSet.next()) {
				statement = "CREATE TABLE eglow (UUID VARCHAR(255) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(255), glowVisibility VARCHAR(255), glowDisableReason VARCHAR(255), PRIMARY KEY (UUID))";
			}

			if (statement.isEmpty())
				return true;

			try {
				preparedStatement = connection.prepareStatement(statement);
				preparedStatement.executeUpdate();
			} catch (Exception ignored) {
			}
			return true;
		} catch (SQLException exception) {
			ChatUtil.reportError(exception);
			return false;
		} finally {
			closeMySQLConnection(connection, preparedStatement, resultSet);
		}
	}

	private void closeMySQLConnection(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
		try {
			if (connection != null)
				connection.close();
			if (preparedStatement != null)
				preparedStatement.close();
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException ignored) {
		}
	}
}