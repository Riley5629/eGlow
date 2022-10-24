package me.MrGraycat.eGlow.Addon.TabList;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.api.TabListAPI;
import me.MrGraycat.eGlow.Addon.TabList.Listeners.EGlowTabListListener;
import me.MrGraycat.eGlow.EGlow;

public class TabListAddon {

    private TabList API;

    public TabListAddon() {
        setAPI(TabListAPI.getPlugin());
        EGlow.getInstance().getServer().getPluginManager().registerEvents(new EGlowTabListListener(), EGlow.getInstance());
    }

    public TabList getAPI() {
        return this.API;
    }

    private void setAPI(TabList tabList) {
        this.API = tabList;
    }
}
