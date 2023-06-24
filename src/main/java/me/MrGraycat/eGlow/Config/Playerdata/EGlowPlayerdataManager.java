package me.MrGraycat.eGlow.Config.Playerdata;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.ConfigType;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlowPlayerdataManager {
	private static EGlowPlayerdataSQLite sqlite;
	private static EGlowPlayerdataMySQL mysql;

	private static boolean mysql_Failed = false;
	
	/**
	 * Initialise the playerdata storage config/mysql
	 */
	public static void initialize() {
		switch((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
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
	 * @param ePlayer player to load data from
	 */
	public static void loadPlayerdata(IEGlowPlayer ePlayer) {
		switch((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return;
			
			sqlite.loadPlayerdata(ePlayer);
			break;
		case MYSQL:
			if (mysql == null)
				return;

			mysql.loadPlayerdata(ePlayer);
			break;
		}
	}
	
	/**
	 * Save the data for the given player
	 * @param ePlayer player to save the data for
	 */
	public static void savePlayerdata(IEGlowPlayer ePlayer) {
		if (ePlayer.getSaveData())
			return;
		
		switch((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return;
			
			sqlite.savePlayerdata(ePlayer);
			break;
		case MYSQL:
			if (mysql == null)
				return;

			mysql.savePlayerdata(ePlayer);
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
	 * @param ePlayer to set the uninitialised values for
	 */
	public static void setDefaultValues(IEGlowPlayer ePlayer) {
		ePlayer.setActiveOnQuit(false);
		ePlayer.setDataFromLastGlow("none");
		ePlayer.setGlowOnJoin(MainConfig.SETTINGS_JOIN_DEFAULT_GLOW_ON_JOIN_VALUE.getBoolean());
	}
}