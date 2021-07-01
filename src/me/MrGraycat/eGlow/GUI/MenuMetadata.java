package me.MrGraycat.eGlow.GUI;

import org.bukkit.entity.Player;

public class MenuMetadata {
	private Player owner;
	
	public MenuMetadata(Player p) {
		this.owner = p;
	}
	
	public Player getOwner() {
		return owner;
	}
}
