package me.MrGraycat.eGlow.Addon.TAB.Util;

import me.MrGraycat.eGlow.EGlow;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.TabConstants;

public class TABLegacyUtil {
	public void registerFeature() {
		TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, new NameTagX(EGlow.getInstance()));
	}
}