package me.mrgraycat.eglow.database;

import lombok.Getter;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.text.ChatUtil;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Getter
public class EGlowPlayerdataMySQL {
	Object mysql;

	/**
	 * Initialise external playerdata using MySQL
	 */
	public EGlowPlayerdataMySQL() {
		if (setupMySQLConnection()) {
			ChatUtil.sendToConsole("&aSuccessfully loaded MySQL.", true);
		} else {
			EGlowPlayerdataManager.setMysql_Failed(true);
			ChatUtil.sendToConsole("&cFailed to load MySQL.", true);
		}
	}

	/**
	 * Load a players data into eGlow
	 *
	 * @param eGlowPlayer player to load data from
	 */
	public void loadPlayerdata(EGlowPlayer eGlowPlayer) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String statement;

		statement = "SELECT * FROM eglow WHERE UUID='" + eGlowPlayer.getUuid().toString() + "'";

		try {
			connection = getConnection();
			preparedStatement = Objects.requireNonNull(connection, "Failed to retrieve MySQL connection").prepareStatement(statement);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				try {
					eGlowPlayer.setGlowOnJoin(resultSet.getBoolean("glowOnJoin"));
					eGlowPlayer.setActiveOnQuit(resultSet.getBoolean("activeOnQuit"));
					eGlowPlayer.setDataFromLastGlow(resultSet.getString("lastGlowData"));
					eGlowPlayer.setGlowVisibility((resultSet.getString("glowVisibility").equals(EnumUtil.GlowVisibility.UNSUPPORTEDCLIENT.name()) ? eGlowPlayer.getGlowVisibility() : EnumUtil.GlowVisibility.valueOf(resultSet.getString("glowVisibility"))));
					eGlowPlayer.setForcedGlowDisableReason(EnumUtil.GlowDisableReason.valueOf(resultSet.getString("glowDisableReason")));
				} catch (NullPointerException | IllegalArgumentException exception) {
					ChatUtil.sendToConsole("Playerdata of player: " + eGlowPlayer.getDisplayName() + " has been reset due to a corrupted value.", true);
					EGlowPlayerdataManager.setDefaultValues(eGlowPlayer);
				}
			} else {
				EGlowPlayerdataManager.setDefaultValues(eGlowPlayer);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
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
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String statement;

		boolean glowOnJoin;
		boolean activeOnQuit;
		String glowDisableReason;
		String glowVisibility;
		String lastGlowData;

		statement = "INSERT INTO eglow (UUID, glowOnJoin, activeOnQuit, lastGlowData, glowVisibility, glowDisableReason)" + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?, glowonJoin=?, activeOnQuit=?, lastGlowData=?, glowVisibility= ?, glowDisableReason=?";

		lastGlowData = eGlowPlayer.getLastGlow();
		glowOnJoin = eGlowPlayer.isGlowOnJoin();
		activeOnQuit = eGlowPlayer.isActiveOnQuit();
		glowVisibility = eGlowPlayer.getGlowVisibility().name();
		glowDisableReason = eGlowPlayer.getGlowDisableReason().name();

		try {
			connection = getConnection();
			preparedStatement = Objects.requireNonNull(connection, "Failed to retrieve MySQL connection").prepareStatement(statement);

			preparedStatement.setString(1, eGlowPlayer.getUuid().toString());
			preparedStatement.setBoolean(2, glowOnJoin);
			preparedStatement.setBoolean(3, activeOnQuit);
			preparedStatement.setString(4, lastGlowData);
			preparedStatement.setString(5, glowVisibility);
			preparedStatement.setString(6, glowDisableReason);
			preparedStatement.setString(7, eGlowPlayer.getUuid().toString());
			preparedStatement.setBoolean(8, glowOnJoin);
			preparedStatement.setBoolean(9, activeOnQuit);
			preparedStatement.setString(10, lastGlowData);
			preparedStatement.setString(11, glowVisibility);
			preparedStatement.setString(12, glowDisableReason);

			preparedStatement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		} finally {
			closeMySQLConnection(connection, preparedStatement, null);
		}
	}

	private boolean setupMySQLConnection() {
		mysql = getMySQLDataSource();

		setServerName(MainConfig.MYSQL_HOST.getString());
		setPort(MainConfig.MYSQL_PORT.getInt());
		setDatabaseName(((!MainConfig.ADVANCED_MYSQL_USESSL.getBoolean()) ? "?useSSL=false" : "")); //no database name for now
		setUser(MainConfig.MYSQL_USERNAME.getString());
		setPassword(MainConfig.MYSQL_PASSWORD.getString());

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		String statement = "CREATE DATABASE IF NOT EXISTS " + ((!MainConfig.MYSQL_DBNAME.getString().isEmpty()) ? "`" + MainConfig.MYSQL_DBNAME.getString() + "`" : "eglow");

		try {
			connection = getConnection();
			preparedStatement = Objects.requireNonNull(connection, "Failed to retrieve MySQL connection").prepareStatement(statement);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeMySQLConnection(connection, preparedStatement, null);
		}

		setDatabaseName(((!MainConfig.MYSQL_DBNAME.getString().isEmpty()) ? MainConfig.MYSQL_DBNAME.getString() : "eglow") + ((!MainConfig.ADVANCED_MYSQL_USESSL.getBoolean()) ? "?useSSL=false" : ""));

		try {
			connection = getConnection();
			statement = "CREATE TABLE IF NOT EXISTS eglow (UUID VARCHAR(190) NOT NULL, glowOnJoin BOOLEAN, activeOnQuit BOOLEAN, lastGlowData VARCHAR(190), glowVisibility VARCHAR(190), glowDisableReason VARCHAR(190), PRIMARY KEY (UUID))";
			preparedStatement = connection.prepareStatement(statement);
			try {
				preparedStatement.executeUpdate();
			} catch (Exception ignored) {
			}
			return true;
		} catch (SQLException exception) {
			exception.printStackTrace();
			return false;
		} finally {
			closeMySQLConnection(connection, preparedStatement, null);
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

	private Object getMySQLDataSource() {
		try {
			return NMSHook.nms.newMySQLDataSource.newInstance();
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private Connection getConnection() {
		try {
			return (Connection) NMSHook.nms.MySQL_getConnection.invoke(getMysql());
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private void setServerName(String serverName) {
		try {
			NMSHook.nms.MySQL_setServerName.invoke(getMysql(), serverName);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}

	private void setPort(int port) {
		try {
			NMSHook.nms.MySQL_setPort.invoke(getMysql(), port);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}

	private void setDatabaseName(String databaseName) {
		try {
			NMSHook.nms.MySQL_setDatabaseName.invoke(getMysql(), databaseName);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}

	private void setUser(String user) {
		try {
			NMSHook.nms.MySQL_setUser.invoke(getMysql(), user);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}

	private void setPassword(String password) {
		try {
			NMSHook.nms.MySQL_setPassword.invoke(getMysql(), password);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}
}