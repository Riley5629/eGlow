package me.mrgraycat.eglow.util.enums;

public class EnumUtil {

	/*
	 * Glow visbility
	 * ALL: Show all glows
	 * OTHER: Show all glows besides your own
	 * OWN: Show your own glow
	 * NONE: Show no glows
	 * UNSUPPORTEDCLIENT: Client doesn't know what glows are
	 */
	public enum GlowVisibility {
		ALL,
		OTHER,
		OWN,
		NONE,
		UNSUPPORTEDCLIENT
	}

	/*
	 * Glow disable reasons:
	 * BLOCKEDWORLD: Glow is blocked in the players world
	 * DISGUISED: Glow is blocked while in disguise
	 * INVISIBLE: Glow is blocked (if configured) while being invisible
	 */
	public enum GlowDisableReason {
		BLOCKEDWORLD,
		INVISIBLE,
		ANIMATION,
		NONE
	}

	/*
	 * Glow actions for worlds
	 * BLOCKED: block glows in given worlds
	 * ALLOWED: allow glows in given worlds
	 * NONE: allow glows in all worlds
	 */
	public enum GlowWorldAction {
		BLOCKED,
		ALLOWED,
		UNKNOWN
	}

	/*
	 * Glow target modes:
	 * ALL: Glow will be shown to all players
	 * CUSTOM: Glow will only show to defined players
	 */
	public enum GlowTargetMode {
		ALL,
		CUSTOM
	}

	/*
	 * Config types:
	 * SQLITE: local db file
	 * MYSQL: cloud based storage
	 */
	public enum ConfigType {
		SQLITE,
		MYSQL
	}

	/*
	 * Entity types:
	 * PLAYER
	 * NPC
	 * ENTITY
	 */
	public enum EntityType {
		PLAYER,
		CITIZENNPC,
		FANCYNPC,
		ENTITY
	}
}