package me.MrGraycat.eGlow.API.Enum;

public enum EGlowEffect {
	RAINBOW_SLOW,
	RAINBOW_FAST;
	
	@Override
	public String toString() {
		return super.toString().toLowerCase().replace("_", "");
	}
}