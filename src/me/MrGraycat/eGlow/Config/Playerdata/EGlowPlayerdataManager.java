package me.MrGraycat.eGlow.Config.Playerdata;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.ConfigType;

public class EGlowPlayerdataManager {
	private EGlow instance;
	
	private EGlowPlayerdataSQLite sqlite;
	private Object mysql;
	
	/**
	 * Initialise the playerdata storage config/mysql
	 */
	public EGlowPlayerdataManager(EGlow instance) {
		setInstance(instance);
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			sqlite = new EGlowPlayerdataSQLite(getInstance());
			break;
		case MYSQL:
			try {
				Class.forName("com.mysql.cj.jdbc.MysqlDataSource");
				mysql = new EGlowPlayerdataMySQL8(getInstance());
			} catch(ClassNotFoundException e) {
				mysql = new EGlowPlayerdataMySQL(getInstance());
			}
			break;
		}
	}
	
	/**
	 * Load a players data into eGlow
	 * @param ePlayer player to load data from
	 */
	public void loadPlayerdata(IEGlowPlayer ePlayer) {
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return; //TODO
			
			sqlite.loadPlayerdata(ePlayer);
			break;
		case MYSQL:
			if (mysql == null)
				return; //TODO
			
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
	public void savePlayerdata(IEGlowPlayer ePlayer) {
		if (!ePlayer.getSaveData())
			return;
		
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return; //TODO
			
			sqlite.savePlayerdata(ePlayer);
			break;
		case MYSQL:
			if (mysql == null)
				return; //TODO
			
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
	 * @param ePlayer player to save the data for
	 */
	public boolean savePlayerdata(String uuid, String lastGlowData, boolean glowOnJoin, boolean activeOnQuit, String glowVisibility, String glowDisableReason) {
		switch((EGlowMainConfig.useMySQL()) ? ConfigType.MYSQL : ConfigType.SQLITE) {
		case SQLITE:
			if (sqlite == null)
				return false;
			
			return sqlite.savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
		case MYSQL:
			if (mysql == null)
				return false;
			
			if (mysql instanceof EGlowPlayerdataMySQL8) {
				return ((EGlowPlayerdataMySQL8) mysql).savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
			} else {
				return ((EGlowPlayerdataMySQL) mysql).savePlayerdata(uuid, lastGlowData, glowOnJoin, activeOnQuit, glowVisibility, glowDisableReason);
			}
		}
		return false;
	}
	
	/**
	 * Set non initialised player values
	 * @param ePlayer to set the uninitialised values for
	 */
	public void setDefaultValues(IEGlowPlayer ePlayer) {
		ePlayer.setActiveOnQuit(false);
		ePlayer.setDataFromLastGlow("none");
		ePlayer.setGlowOnJoin(EGlowMainConfig.OptionDefaultGlowOnJoinValue());
	}
	
	//Setters
	private void setInstance(EGlow instance) {
		this.instance = instance;
	}

	//Getters
	public EGlow getInstance() {
		return this.instance;
	}
}
