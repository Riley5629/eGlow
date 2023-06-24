package me.mrgraycat.eglow.manager;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.database.credentials.impl.MySqlDatabaseCredentials;
import me.mrgraycat.eglow.database.credentials.impl.SqLiteDatabaseCredentials;
import me.mrgraycat.eglow.database.impl.SqlDatabaseClient;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.ConfigType;
import me.mrgraycat.eglow.util.chat.ChatUtil;

import java.io.File;

public class EGlowPlayerdataManager {
	private static SqlDatabaseClient sqlDatabaseClient;
	private static boolean mysql_Failed = false;

	/**
	 * Initialise the playerdata storage config/mysql
	 */
	public static void initialize() {
		sqlDatabaseClient = new SqlDatabaseClient();

		switch ((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
			case SQLITE:
				sqlDatabaseClient.init(new SqLiteDatabaseCredentials(EGlow.getInstance().getDataFolder() + File.separator + "Playerdata.db"));
				break;
			case MYSQL:
				sqlDatabaseClient.init(new MySqlDatabaseCredentials(MainConfig.MYSQL_HOST.getString(), MainConfig.MYSQL_PORT.getInt(), MainConfig.MYSQL_DBNAME.getString(), MainConfig.MYSQL_USERNAME.getString(), MainConfig.MYSQL_PASSWORD.getString()));
				break;
		}
	}

	/**
	 * Load a players data into eGlow
	 *
	 * @param ePlayer player to load data from
	 */
	public static void loadPlayerdata(IEGlowPlayer ePlayer) {
		if (sqlDatabaseClient == null) {
			return;
		}

		sqlDatabaseClient.loadPlayerData(ePlayer);
	}

	/**
	 * Save the data for the given player
	 *
	 * @param ePlayer player to save the data for
	 */
	public static void savePlayerdata(IEGlowPlayer ePlayer) {
		if (ePlayer.getSaveData() || sqlDatabaseClient == null) {
			return;
		}

		sqlDatabaseClient.savePlayerData(ePlayer);
	}

	public static boolean getMySQL_Failed() {
		return mysql_Failed;
	}

	public static void setMysql_Failed(boolean state) {
		if (mysql_Failed == state)
			return;

		mysql_Failed = state;

		if (!state) {
			ChatUtil.sendToConsole("&6trying to reestablishing MySQL connection&f.", true);
			initialize();
		}
	}

	/**
	 * Set non initialised player values
	 *
	 * @param ePlayer to set the uninitialised values for
	 */
	public static void setDefaultValues(IEGlowPlayer ePlayer) {
		ePlayer.setActiveOnQuit(false);
		ePlayer.setDataFromLastGlow("none");
		ePlayer.setGlowOnJoin(MainConfig.SETTINGS_JOIN_DEFAULT_GLOW_ON_JOIN_VALUE.getBoolean());
	}
}