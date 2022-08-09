package me.MrGraycat.eGlow.Util;

public class EnumUtil {

	/*
	 * Glow visbility
	 * ALL: Show all glows
	 * OWN: Show your own glow
	 * NONE: Show no glows
	 * UNSUPPORTEDCLIENT: Client doesn't know what glows are
	 */
	public enum GlowVisibility {
		ALL("ALL"),
		OWN("OWN"),
		NONE("NONE"),
		UNSUPPORTEDCLIENT("UNSUPPORTEDCLIENT");
		
		private final String visibility;
		
		GlowVisibility(String visibility) {
			this.visibility = visibility;
		}
		
		public String getVisibility() {
			return visibility;
		}
	}
	
	/*
	 * Glow disable reasons:
	 * BLOCKEDWORLD: Glow is blocked in the players world
	 * DISGUISED: Glow is blocked while in disguise
	 * INVISIBLE: Glow is blocked (if configured) while being invisible
	 */
	public enum GlowDisableReason {
		BLOCKEDWORLD("BLOCKEDWORLD"),
		DISGUISE("DISGUISE"),
		INVISIBLE("INVISIBLE"),
		NONE("NONE");
		
		private final String reason;
		
		GlowDisableReason(String reason) {
			this.reason = reason;
		}
		
		public String getGlowDisableReason() {
			return reason;
		}
	}
	
	/*
	 * Glow actions for worlds
	 * BLOCKED: block glows in given worlds
	 * ALLOWED: allow glows in given worlds
	 * NONE: allow glows in all worlds
	 */
	public enum GlowWorldAction {
		BLOCKED("BLOCK"),
		ALLOWED("ALLOW"),
		UNKNOWN("NONE");
		
		private final String action;
		
		GlowWorldAction(String action) {
			this.action = action;
		}
		
		public String getGlowWorldAction() {
			return action;
		}
	}
	
	/*
	 * Glow target modes:
	 * ALL: Glow will be shown to all players
	 * CUSTOM: Glow will only show to defined players
	 */
	public enum GlowTargetMode {
		ALL("ALL"),
		CUSTOM("CUSTOM");
		
		private final String target;
		
		GlowTargetMode(String target) {
			this.target = target;
		}
		
		public String getGlowTarget() {
			return target;
		}
	}
	
	/*
	 * Config types:
	 * SQLITE: local db file
	 * MYSQL: cloud based storage
	 */
	public enum ConfigType {
		SQLITE("SQLITE"),
		MYSQL("MYSQL");
		
		private final String configType;
		
		ConfigType(String configType) {
			this.configType = configType;
		}
		
		public String getConfigType() {
			return configType;
		}
	}
}