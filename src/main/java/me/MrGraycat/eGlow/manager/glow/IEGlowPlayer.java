package me.MrGraycat.eGlow.manager.glow;

import lombok.Getter;
import lombok.Setter;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.addon.vault.VaultAddon;
import me.MrGraycat.eGlow.config.EGlowMainConfig;
import me.MrGraycat.eGlow.config.EGlowMessageConfig;
import me.MrGraycat.eGlow.manager.DataManager;
import me.MrGraycat.eGlow.util.Common;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import me.MrGraycat.eGlow.util.dependency.Dependency;
import me.MrGraycat.eGlow.util.packet.PacketUtil;
import me.MrGraycat.eGlow.util.packet.ProtocolVersion;
import me.MrGraycat.eGlow.util.packet.chat.EnumChatFormat;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.ScoreboardTrait;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
public class IEGlowPlayer {

	private final List<IEGlowEffect> forcedEffects = new ArrayList<>();

	private final String entityType;
	private final ProtocolVersion version;

	private NPC citizensNPC;
	private Player player;

	private ChatColor activeColor = ChatColor.RESET;
	private String displayName;
	private UUID uuid;

	private boolean glowStatus = false;
	private boolean fakeGlowStatus = false;

	@Setter private IEGlowEffect glowEffect;

	private boolean glowOnJoin;
	@Setter private boolean activeOnQuit;
	@Setter private boolean saveData = false;

	private Common.GlowDisableReason glowDisableReason = Common.GlowDisableReason.NONE;
	private Common.GlowVisibility glowVisibility;

	private Common.GlowTargetMode glowTarget = Common.GlowTargetMode.ALL;
	private List<Player> customTargetList = new ArrayList<>();

	public IEGlowPlayer(Player player) {
		this.entityType = "PLAYER";
		this.player = player;
		this.displayName = player.getName();
		this.uuid = player.getUniqueId();
		this.customTargetList = new ArrayList<>(Collections.singletonList(player));
		this.version = ProtocolVersion.getPlayerVersion(this);

		if (this.version.getNetworkId() <= 110 && !this.version.getFriendlyName().equals("1.9.4")) {
			this.glowVisibility = Common.GlowVisibility.UNSUPPORTEDCLIENT;
		} else {
			this.glowVisibility = Common.GlowVisibility.ALL;
		}

		setupForceGlows();
	}

	public IEGlowPlayer(NPC npc) {
		this.entityType = "NPC";
		this.citizensNPC = npc;

		this.version = ProtocolVersion.SERVER_VERSION;
	}

	// Glowing stuff
	public void setGlowing(boolean status, boolean fake) {
		if (!fake && status == this.glowStatus) {
			return;
		}

		this.glowStatus = status;
		this.fakeGlowStatus = fake;

		switch (entityType) {
			case "PLAYER":
				PacketUtil.updateGlowing(this, status);
				break;
			case "NPC":
				try {
					citizensNPC.data().setPersistent(NPC.Metadata.GLOWING, status);
				} catch (Exception e) {
					ChatUtil.sendToConsole("&cYour Citizens version is outdated please use it's latest version", true);
				}
		}
	}

	public void setColor(ChatColor color, boolean status, boolean fake) {
		if (saveData) {
			setSaveData(true);
		}

		this.fakeGlowStatus = fake;

		if (color.equals(ChatColor.RESET)) {
			setGlowing(false, fake);
		} else {
			setGlowing(status, fake);

			if (getActiveColor() != null && getActiveColor().equals(color))
				return;

			this.glowStatus = status;
			this.activeColor = color;

			switch (getEntityType()) {
				case ("PLAYER"):
					PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), ((EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "") + color, (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.valueOf(color.name()));
					break;
				case ("NPC"):
					if (!fake && citizensNPC.isSpawned())
						citizensNPC.getOrAddTrait(ScoreboardTrait.class).setColor(color);
					break;
			}
		}

		if (getEntityType().equals("NPC"))
			return;

		updatePlayerTabName();
		DataManager.sendAPIEvent(this, fake);
	}

	public boolean isSameGlow(IEGlowEffect newGlowEffect) {
		return glowStatus && getGlowEffect() != null && newGlowEffect.equals(getGlowEffect());
	}

	public void activateGlow() {
		this.glowStatus = true;

		if (getGlowEffect() != null) {
			activateGlow(getGlowEffect());
		} else {
			this.glowStatus = false;
		}
	}

	public void activateGlow(IEGlowEffect newGlow) {
		disableGlow(true);
		setGlowEffect(newGlow);

		newGlow.activateForEntity(getEntity());
		setGlowing(true, false);
	}

	public void disableGlow(boolean hardReset) {
		if (this.fakeGlowStatus || this.glowStatus) {
			if (getGlowEffect() != null) {
				getGlowEffect().deactivateForEntity(getEntity());
			}

			if (hardReset)
				setGlowEffect(DataManager.getEGlowEffect("none"));

			this.activeColor = ChatColor.RESET;
			setGlowing(false, false);

			if (getPlayer() != null) {
				PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "", (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.RESET);
				DataManager.sendAPIEvent(this, false);
				updatePlayerTabName();
			}

			if (this.citizensNPC != null)
				citizensNPC.getOrAddTrait(ScoreboardTrait.class).setColor(ChatColor.RESET);
		}
	}

	/**
	 * Update the tabname of the player
	 */
	public void updatePlayerTabName() {
		if (!EGlowMainConfig.MainConfig.FORMATTING_TABLIST_ENABLE.getBoolean())
			return;

		if (getPlayer() == null)
			return;

		String format = EGlowMainConfig.MainConfig.FORMATTING_TABLIST_FORMAT.getString();

		String prefix = "";
		String suffix = "";

		VaultAddon vaultAddon = EGlow.getInstance().getVaultAddon();

		if (vaultAddon != null) {
			prefix = vaultAddon.getPlayerPrefix(this);
			suffix = vaultAddon.getPlayerSuffix(this);
		}

		if (format.contains("%name%")) {
			format = format.replace("%name%", player.getDisplayName());
		}

		if (format.contains("%prefix%") || format.contains("%suffix%")) {
			format = format.replace("%prefix%", prefix).replace("%suffix%", suffix);
		}

		if (Dependency.PLACEHOLDER_API.isLoaded()) {
			format = PlaceholderAPI.setPlaceholders(getPlayer(), format);
		}

		player.setPlayerListName(ChatUtil.translateColors(format));
	}

	public boolean isGlowing() {
		return glowStatus || fakeGlowStatus;
	}

	public String getTeamName() {
		String playerName = String.valueOf(displayName.hashCode()).replace("-", "2");
		return (playerName.length() > 15) ? "E" + playerName.substring(0, 14) : "E" + playerName;
	}

	public Object getEntity() {
		if (player != null)
			return player;
		return citizensNPC;
	}

	public void setupForceGlows() {
		if (!EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || player == null ||
				isInBlockedWorld() && EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
			return;

		EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection().stream()
				.filter(permission -> player.hasPermission("eglow.force." + permission.toLowerCase()))
				.map(permission -> DataManager.getEGlowEffect(EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getString(permission)))
				.filter(effect -> !forcedEffects.contains(effect))
				.forEach(forcedEffects::add);
	}

	public boolean isForcedGlow(IEGlowEffect effect) {
		return forcedEffects.contains(effect);
	}

	public IEGlowEffect getForceGlow() {
		if (forcedEffects.isEmpty() || !EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || player == null ||
				isInBlockedWorld() && EGlowMainConfig.MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
			return null;
		return forcedEffects.get(0);
	}

	public boolean isInBlockedWorld() {
		if (!EGlowMainConfig.MainConfig.WORLD_ENABLE.getBoolean())
			return false;

		Common.GlowWorldAction action;

		try {
			action = Common.GlowWorldAction.valueOf(EGlowMainConfig.MainConfig.WORLD_ACTION.getString().toUpperCase() + "ED");
		} catch (IllegalArgumentException e) {
			action = Common.GlowWorldAction.UNKNOWN;
		}

		List<String> worldList = EGlowMainConfig.MainConfig.WORLD_LIST.getStringList();
		String worldName = player.getWorld().getName();

		switch (action) {
			case BLOCKED:
				if (worldList.contains(worldName.toLowerCase()))
					return true;
				break;
			case ALLOWED:
				if (!worldList.contains(worldName.toLowerCase()))
					return true;
				break;
			case UNKNOWN:
				return false;
		}
		return false;
	}

	public boolean isInvisible() {
		return EGlowMainConfig.MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean() && player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}

	public boolean setGlowDisableReason(Common.GlowDisableReason reason, boolean skip) {
		if (skip) {
			this.glowDisableReason = reason;
			return false;
		}

		switch (reason) {
			case BLOCKEDWORLD:
			case INVISIBLE:
			case DISGUISE:
			case ANIMATION:
				break;
			case NONE:
				if (!this.glowDisableReason.equals(reason)) {
					if (isInBlockedWorld()) {
						this.glowDisableReason = Common.GlowDisableReason.BLOCKEDWORLD;
						return false;
					}

					if (isInvisible()) {
						this.glowDisableReason = Common.GlowDisableReason.INVISIBLE;
						return false;
					}

					if (EGlow.getInstance().getLibDisguiseAddon() != null && EGlow.getInstance().getLibDisguiseAddon().isDisguised(getPlayer()) || EGlow.getInstance().getIDisguiseAddon() != null && EGlow.getInstance().getIDisguiseAddon().isDisguised(getPlayer())) {
						this.glowDisableReason = Common.GlowDisableReason.DISGUISE;
						return false;
					}
				}
				break;
		}

		this.glowDisableReason = reason;
		return true;
	}

	public void setGlowVisibility(Common.GlowVisibility visibility) {
		if (!this.glowVisibility.equals(Common.GlowVisibility.UNSUPPORTEDCLIENT))
			this.glowVisibility = visibility;
	}

	public void setGlowTargetMode(Common.GlowTargetMode glowTarget) {
		if (glowTarget != this.glowTarget) {
			this.glowTarget = glowTarget;
			PacketUtil.updateGlowTarget(this);
		}
	}

	public void addGlowTarget(Player p) {
		if (!customTargetList.contains(p)) {
			customTargetList.add(p);
			PacketUtil.glowTargetChange(this, p, true);
		}
		if (!customTargetList.contains(player))
			customTargetList.add(player);
		if (glowTarget.equals(Common.GlowTargetMode.ALL))
			setGlowTargetMode(Common.GlowTargetMode.CUSTOM);
	}

	public void removeGlowTarget(Player p) {
		if (customTargetList.contains(p))
			PacketUtil.glowTargetChange(this, p, false);
		customTargetList.remove(p);

		if (glowTarget.equals(Common.GlowTargetMode.CUSTOM) && customTargetList.isEmpty())
			setGlowTargetMode(Common.GlowTargetMode.ALL);
	}

	public void setGlowTargets(List<Player> targets) {
		if (targets == null) {
			customTargetList.clear();
			customTargetList.add(player);
		} else {
			if (!targets.contains(player))
				targets.add(player);

			customTargetList = targets;
		}

		if (glowTarget.equals(Common.GlowTargetMode.ALL)) {
			setGlowTargetMode(Common.GlowTargetMode.CUSTOM);
		} else {
			for (Player player : Objects.requireNonNull(targets, "Can't loop over 'null'")) {
				PacketUtil.glowTargetChange(this, player, true);
			}
		}
	}

	public void resetGlowTargets() {
		customTargetList.clear();
		setGlowTargetMode(Common.GlowTargetMode.ALL);
	}

	public void setGlowOnJoin(boolean status) {
		if (this.glowOnJoin != status) {
			if (getSaveData())
				setSaveData(true);
		}

		this.glowOnJoin = status;
	}

	public boolean getSaveData() {
		return !this.saveData;
	}

	public void setDataFromLastGlow(String lastGlow) {
		IEGlowEffect effect = DataManager.getEGlowEffect(lastGlow);
		setGlowEffect(effect);
	}

	public String getLastGlowName() {
		return (getGlowEffect() != null) ? getGlowEffect().getDisplayName() : EGlowMessageConfig.Message.COLOR.get("none");
	}

	public String getLastGlow() {
		return (getGlowEffect() != null) ? getGlowEffect().getName() : "none";
	}

	public boolean isDisguised() {
		EGlow instance = EGlow.getInstance();

		return (instance.getLibDisguiseAddon() != null && instance.getLibDisguiseAddon().isDisguised(player)) ||
				instance.getIDisguiseAddon() != null && instance.getIDisguiseAddon().isDisguised(player);
	}

	public boolean getGlowStatus() {
		return glowStatus;
	}

	public boolean getFakeGlowStatus() {
		return fakeGlowStatus;
	}
}