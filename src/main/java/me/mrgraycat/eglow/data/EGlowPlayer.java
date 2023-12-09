package me.mrgraycat.eglow.data;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.enums.Dependency;
import me.mrgraycat.eglow.util.enums.EnumUtil.*;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.ScoreboardTrait;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@Setter
public class EGlowPlayer {
	private final EntityType entityType;

	private NPC citizensNPC;
	//private Npc fancyNPC;
	private Player player;
	private String displayName;
	private UUID uuid;
	private String teamName = "";
	private ProtocolVersion version = ProtocolVersion.SERVER_VERSION;

	private ChatColor activeColor = ChatColor.RESET;
	private boolean glowStatus = false;
	private boolean fakeGlowStatus = false;

	private EGlowEffect glowEffect;
	private EGlowEffect forcedEffect;

	private boolean glowOnJoin;
	private boolean activeOnQuit;
	private boolean saveData = false;
	private GlowDisableReason glowDisableReason = GlowDisableReason.NONE;
	private GlowVisibility glowVisibility;

	private GlowTargetMode glowTargetMode = GlowTargetMode.ALL;
	private List<Player> customTargetList = new ArrayList<>();

	public EGlowPlayer(Player player) {
		this.entityType = EntityType.PLAYER;
		this.player = player;
		this.displayName = player.getName();
		this.uuid = player.getUniqueId();
		this.teamName = getTeamName();
		this.customTargetList = new ArrayList<>(Collections.singletonList(player));
		this.version = ProtocolVersion.getPlayerVersion(this);

		if (this.version.getNetworkId() <= 110 && !this.version.getFriendlyName().equals("1.9.4")) {
			this.glowVisibility = GlowVisibility.UNSUPPORTEDCLIENT;
		} else {
			this.glowVisibility = GlowVisibility.ALL;
		}

		setupForceGlows();
	}

	public EGlowPlayer(NPC npc) {
		this.entityType = EntityType.CITIZENNPC;
		this.citizensNPC = npc;
		this.displayName = npc.getName();
	}

	// Glowing stuff
	public void setGlowing(boolean status, boolean fake) {
		if (!fake && status == getGlowStatus()) {
			return;
		}

		setGlowStatus(status);
		setFakeGlowStatus(fake);

		switch (getEntityType()) {
			case PLAYER:
				PacketUtil.updateGlowing(this, status);
				break;
			case CITIZENNPC:
				try {
					getCitizensNPC().data().setPersistent(NPC.Metadata.GLOWING, status);
				} catch (Exception e) {
					ChatUtil.sendToConsole("&cYour Citizens version is outdated please use it's latest version", true);
				}
				break;
			case FANCYNPC:
				/*try {
					getFancyNPC().getData().setGlowing(status);
				} catch (Exception e) {
					ChatUtil.sendToConsole("&cSomething went wrong enabling glow for fancynpc npc", true);
				}*/
				break;
		}
	}

	public void setColor(ChatColor color, boolean status, boolean fake) {
		if (skipSaveData()) {
			setSaveData(true);
		}

		setFakeGlowStatus(fake);

		if (color.equals(ChatColor.RESET)) {
			setGlowing(false, fake);
		} else {
			setGlowing(status, fake);

			if (getActiveColor() != null && getActiveColor().equals(color))
				return;

			setGlowStatus(status);
			setActiveColor(color);

			switch (getEntityType()) {
				case PLAYER:
					PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), ((EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "") + color, (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.valueOf(color.name()));
					break;
				case CITIZENNPC:
					if (!fake && getCitizensNPC().isSpawned())
						getCitizensNPC().getOrAddTrait(ScoreboardTrait.class).setColor(color);
					break;
				case FANCYNPC:
					/*getFancyNPC().getData().setGlowingColor(NamedTextColor.NAMES.value(color.name()));
					getFancyNPC().updateForAll();*/
					break;
			}
		}

		if (getEntityType().equals(EntityType.CITIZENNPC))
			return;

		updatePlayerTabname();
		DataManager.sendAPIEvent(this, fake);
	}

	public boolean isSameGlow(EGlowEffect newGlowEffect) {
		return getGlowStatus() && getGlowEffect() != null && newGlowEffect.equals(getGlowEffect());
	}

	public void activateGlow() {
		if (getGlowEffect() != null) {
			setGlowStatus(true);
			activateGlow(getGlowEffect());
		} else {
			setGlowStatus(false);
		}
	}

	public void activateGlow(EGlowEffect newGlowEffect) {
		disableGlow(true);
		setGlowEffect(newGlowEffect);

		newGlowEffect.activateForEntity(getEntity());
		setGlowing(true, false);
	}

	public void disableGlow(boolean hardReset) {
		if (isGlowing()) {
			if (getGlowEffect() != null)
				getGlowEffect().deactivateForEntity(getEntity());

			if (hardReset)
				setGlowEffect(DataManager.getEGlowEffect("none"));

			setActiveColor(ChatColor.RESET);
			setGlowing(false, false);

			if (getPlayer() != null) {
				PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "", (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.RESET);
				DataManager.sendAPIEvent(this, false);
				updatePlayerTabname();
			}

			if (getCitizensNPC() != null)
				getCitizensNPC().getOrAddTrait(ScoreboardTrait.class).setColor(ChatColor.RESET);
		}
	}

	/**
	 * Update the tabname of the player
	 */
	public void updatePlayerTabname() {
		if (!MainConfig.FORMATTING_TABLIST_ENABLE.getBoolean())
			return;

		if (getPlayer() == null)
			return;

		String format = MainConfig.FORMATTING_TABLIST_FORMAT.getString();
		String prefix = (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerPrefix(this) : "";
		String suffix = (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerSuffix(this) : "";

		if (format.contains("%name%"))
			format = format.replace("%name%", getDisplayName());

		if (format.contains("%prefix%") || format.contains("%suffix%"))
			format = format.replace("%prefix%", prefix).replace("%suffix%", suffix);

		if (Dependency.PLACEHOLDER_API.isLoaded())
			format = PlaceholderAPI.setPlaceholders(getPlayer(), format);

		getPlayer().setPlayerListName(ChatUtil.translateColors(format));
	}

	public boolean isGlowing() {
		return (getGlowStatus() || isFakeGlowStatus());
	}

	private final String sortingOrder = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	public String getTeamName() {
		if (!this.teamName.isEmpty())
			return this.teamName;

		String playerName = getDisplayName().replace("_", "0");
		StringBuilder obvuscatedTeamname = new StringBuilder();

		if (playerName.length() > 8) {
			playerName = playerName.substring(0, 8);
		}

		for (int i = 0; i < playerName.length(); i++) {
			int number = sortingOrder.indexOf(playerName.charAt(i)) + 1;

			if (number < 10)
				obvuscatedTeamname.append("0");
			obvuscatedTeamname.append(number);
		}

		if (obvuscatedTeamname.length() > 15) {
			obvuscatedTeamname = new StringBuilder(obvuscatedTeamname.substring(0, 15));
		}

		String teamname = obvuscatedTeamname.toString();

		for (EGlowPlayer eGlowPlayer : DataManager.getEGlowPlayers()) {
			if (eGlowPlayer.getTeamName().equals(teamname)) {
				int lastnumber = Integer.parseInt(teamname.substring(teamname.length() - 1));
				if (lastnumber < 9) {
					lastnumber++;
				} else {
					lastnumber--;
				}

				teamname = teamname.substring(0, 14) + lastnumber;
			}
		}

		return teamname;
	}

	public Object getEntity() {
		if (player != null)
			return player;
		return citizensNPC;
	}

	public boolean hasPermission(String permission) {
		return (getPlayer().hasPermission(permission) || getPlayer().hasPermission("eglow.effect.*") || getPlayer().isOp());
	}

	public void setupForceGlows() {
		if (!MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || getPlayer() == null || isInBlockedWorld() && MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
			return;

		for (String permission : MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection()) {
			if (getPlayer().hasPermission("eglow.force." + permission.toLowerCase())) {
				EGlowEffect effect = DataManager.getEGlowEffect(MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getString(permission));

				setForcedEffect(effect);
				setGlowEffect(effect);
				return;
			}
		}
	}

	public boolean isForcedGlow(EGlowEffect effect) {
		if (getForcedEffect() == null) {
			return false;
		}
		return (effect.getName().equals(getForcedEffect().getName()));
	}

	public boolean hasNoForceGlow() {
		return (getForcedEffect() == null);
	}

	public boolean isInBlockedWorld() {
		if (!MainConfig.WORLD_ENABLE.getBoolean())
			return false;

		GlowWorldAction action;

		try {
			action = GlowWorldAction.valueOf(MainConfig.WORLD_ACTION.getString().toUpperCase() + "ED");
		} catch (IllegalArgumentException e) {
			action = GlowWorldAction.UNKNOWN;
		}

		List<String> worldList = MainConfig.WORLD_LIST.getStringList();

		switch (action) {
			case BLOCKED:
				if (worldList.contains(getPlayer().getWorld().getName().toLowerCase()))
					return true;
				break;
			case ALLOWED:
				if (!worldList.contains(getPlayer().getWorld().getName().toLowerCase()))
					return true;
				break;
			case UNKNOWN:
				return false;
		}
		return false;
	}

	public boolean isInvisible() {
		if (!MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean())
			return false;
		return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
	}

	public boolean getGlowStatus() {
		return this.glowStatus;
	}

	public void setForcedGlowDisableReason(GlowDisableReason reason) {
		if (reason.equals(GlowDisableReason.ANIMATION))
			reason = GlowDisableReason.NONE;
		this.glowDisableReason = reason;
	}

	public GlowDisableReason setGlowDisableReason(GlowDisableReason reason) {
		switch (reason) {
			case BLOCKEDWORLD:
			case INVISIBLE:
			case ANIMATION:
				break;
			case NONE:
				if (!this.glowDisableReason.equals(reason)) {
					if (isInBlockedWorld()) {
						this.glowDisableReason = GlowDisableReason.BLOCKEDWORLD;
						return getGlowDisableReason();
					}

					if (isInvisible()) {
						this.glowDisableReason = GlowDisableReason.INVISIBLE;
						return getGlowDisableReason();
					}
				}
				break;
		}

		this.glowDisableReason = reason;
		return getGlowDisableReason();
	}

	public void setGlowVisibility(GlowVisibility visibility) {
		if (!this.glowVisibility.equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
			this.glowVisibility = visibility;

			if (skipSaveData())
				setSaveData(true);
		}

	}

	public void setGlowTargetMode(GlowTargetMode glowTarget) {
		if (glowTarget != this.glowTargetMode) {
			this.glowTargetMode = glowTarget;
			PacketUtil.updateGlowTarget(this);
		}
	}

	public List<Player> getGlowTargets() {
		return this.customTargetList;
	}

	public void addGlowTarget(Player player) {
		if (!getGlowTargets().contains(player)) {
			getGlowTargets().add(player);
			PacketUtil.glowTargetChange(this, player, true);
		}

		if (!getGlowTargets().contains(getPlayer())) {
			getGlowTargets().add(getPlayer());
		}

		if (getGlowTargetMode().equals(GlowTargetMode.ALL)) {
			setGlowTargetMode(GlowTargetMode.CUSTOM);
		}
	}

	public void removeGlowTarget(Player player) {
		if (getGlowTargets().contains(player)) {
			PacketUtil.glowTargetChange(this, player, false);
		}
		getGlowTargets().remove(player);

		if (getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && getGlowTargets().isEmpty()) {
			setGlowTargetMode(GlowTargetMode.ALL);
		}
	}

	public void setGlowTargets(List<Player> targets) {
		if (targets == null) {
			getGlowTargets().clear();
			getGlowTargets().add(getPlayer());
		} else {
			if (!targets.contains(getPlayer()))
				targets.add(getPlayer());
			setCustomTargetList(targets);
		}

		if (getGlowTargetMode().equals(GlowTargetMode.ALL)) {
			setGlowTargetMode(GlowTargetMode.CUSTOM);
		} else {
			for (Player player : Objects.requireNonNull(targets, "Can't loop over 'null'")) {
				PacketUtil.glowTargetChange(this, player, true);
			}
		}
	}

	public void resetGlowTargets() {
		getGlowTargets().clear();
		setGlowTargetMode(GlowTargetMode.ALL);
	}

	public void setGlowOnJoin(boolean status) {
		if (this.glowOnJoin != status) {
			if (skipSaveData())
				setSaveData(true);
		}
		this.glowOnJoin = status;
	}

	public boolean skipSaveData() {
		return !this.saveData;
	}

	public void setDataFromLastGlow(String lastGlow) {
		EGlowEffect effect = DataManager.getEGlowEffect(lastGlow);

		if (hasNoForceGlow()) {
			setGlowEffect(effect);
		}
	}

	public String getLastGlowName() {
		return (getGlowEffect() != null) ? getGlowEffect().getDisplayName() : Message.COLOR.get("none");
	}

	public String getLastGlow() {
		return (getGlowEffect() != null) ? getGlowEffect().getName() : "none";
	}
}