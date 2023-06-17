package me.mrgraycat.eglow.api.enums;

public enum EGlowEffect {
	RAINBOW_SLOW,
	RAINBOW_FAST;
	
	@Override
	public String toString() {
		return super.toString().toLowerCase().replace("_", "");
	}
}