package me.mrgraycat.eglow.database;

import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.data.EGlowPlayer;
import me.mrgraycat.eglow.util.enums.EnumUtil.ConfigType;
import me.mrgraycat.eglow.util.text.ChatUtil;

public class EGlowPlayerdataManager {
	private static EGlowPlayerdataSQLite sqlite;
	private static EGlowPlayerdataMySQL mysql;
	private static boolean mysql_Failed = false;

	/**
	 * Initialise the playerdata storage config/mysql
	 */
	public static void initialize() {
		switch ((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
			case SQLITE:
				sqlite = new EGlowPlayerdataSQLite();
				break;
			case MYSQL:
				mysql = new EGlowPlayerdataMySQL();
				break;
		}
	}

	/**
	 * Load a players data into eGlow
	 *
	 * @param eGlowPlayer player to load data from
	 */
	public static void loadPlayerdata(EGlowPlayer eGlowPlayer) {
		switch ((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
			case SQLITE:
				if (sqlite == null)
					return;
				sqlite.loadPlayerdata(eGlowPlayer);
				break;
			case MYSQL:
				if (mysql == null)
					return;
				mysql.loadPlayerdata(eGlowPlayer);
				break;
		}
		eGlowPlayer.setSaveData(false);
	}

	/**
	 * Save the data for the given player
	 *
	 * @param eGlowPlayer player to save the data for
	 */
	public static void savePlayerdata(EGlowPlayer eGlowPlayer) {
		if (eGlowPlayer.skipSaveData())
			return;

		switch ((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
			case SQLITE:
				if (sqlite == null)
					return;
				sqlite.savePlayerdata(eGlowPlayer);
				break;
			case MYSQL:
				if (mysql == null)
					return;
				mysql.savePlayerdata(eGlowPlayer);
				break;
		}
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
	public static void setDefaultValues(EGlowPlayer ePlayer) {
		ePlayer.setActiveOnQuit(false);
		ePlayer.setDataFromLastGlow("none");
		ePlayer.setGlowOnJoin(MainConfig.SETTINGS_JOIN_DEFAULT_GLOW_ON_JOIN_VALUE.getBoolean());
	}
}