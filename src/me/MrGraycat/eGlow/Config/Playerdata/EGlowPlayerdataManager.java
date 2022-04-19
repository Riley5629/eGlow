package me.MrGraycat.eGlow.Config.Playerdata;

import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.ConfigType;

public class EGlowPlayerdataManager {
	private static EGlowPlayerdataSQLite sqlite;
	private static EGlowHikariCP hikari;
	
	/**
	 * Initialise the playerdata storage config/mysql
	 */
	public static void initialize() {
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			sqlite = new EGlowPlayerdataSQLite();
			break;
		case MYSQL:
			hikari = new EGlowHikariCP();
			break;
		}
	}

	/**
	 * Load a players data into eGlow
	 * @param ePlayer player to load data from
	 */
	public static void loadPlayerdata(IEGlowPlayer ePlayer) {
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return; //TODO
			
			sqlite.loadPlayerdata(ePlayer);
			break;
		case MYSQL:
			if (hikari == null)
				return; //TODO

			hikari.loadPlayerdata(ePlayer);
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
		
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return; //TODO
			
			sqlite.savePlayerdata(ePlayer);
			break;
		case MYSQL:
			if (hikari == null)
				return; //TODO
			
			hikari.savePlayerdata(ePlayer);
			break;
		}
	}
	
	/**
	 * Save the data for the given player
	 *
	 */
	public static boolean savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return false;
			
			return sqlite.savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
		case MYSQL:
			if (hikari == null)
				return false;

			return hikari.savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
		}
		return false;
	}
	
	/**
	 * Set non initialised player values
	 * @param ePlayer to set the uninitialised values for
	 */
	public static void setDefaultValues(IEGlowPlayer ePlayer) {
		ePlayer.setActiveOnQuit(false);
		ePlayer.setDataFromLastGlow("none");
		ePlayer.setGlowOnJoin(EGlowMainConfig.OptionDefaultGlowOnJoinValue());
	}
}