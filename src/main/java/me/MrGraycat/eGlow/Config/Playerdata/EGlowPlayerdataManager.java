package me.MrGraycat.eGlow.Config.Playerdata;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.ConfigType;

public class EGlowPlayerdataManager {
	private static EGlowPlayerdataSQLite sqlite;
	private static Object mysql;
	
	/**
	 * Initialise the playerdata storage config/mysql
	 */
	//TODO switch to sqlite when mysql would fail
	public static void initialize() {
		switch((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			sqlite = new EGlowPlayerdataSQLite();
			break;
		case MYSQL:
			try {
				Class.forName("com.mysql.cj.jdbc.MysqlDataSource");
				mysql = new EGlowPlayerdataMySQL8();
			} catch(ClassNotFoundException e) {
				mysql = new EGlowPlayerdataMySQL();
			}
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

			if (mysql instanceof EGlowPlayerdataMySQL8) {
				((EGlowPlayerdataMySQL8) mysql).loadPlayerdata(ePlayer);
			} else {
				((EGlowPlayerdataMySQL) mysql).loadPlayerdata(ePlayer);
			}
			break;
		}
	}
	
	/**
	 * Save the data for the given player
	 * @param ePlayer player to save the data for
	 */
	public static void savePlayerdata(IEGlowPlayer ePlayer) {
		if (!ePlayer.getSaveData())
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

			if (mysql instanceof EGlowPlayerdataMySQL8) {
				((EGlowPlayerdataMySQL8) mysql).savePlayerdata(ePlayer);
			} else {
				((EGlowPlayerdataMySQL) mysql).savePlayerdata(ePlayer);
			}
			break;
		}
	}
	
	/**
	 * Save the data for the given player
	 *
	 */
	public static void savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
		switch((MainConfig.MYSQL_ENABLE.getBoolean()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return;
			
			sqlite.savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
		case MYSQL:
			if (mysql == null)
				return;

			if (mysql instanceof EGlowPlayerdataMySQL8) {
				((EGlowPlayerdataMySQL8) mysql).savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
			} else {
				((EGlowPlayerdataMySQL) mysql).savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
			}
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