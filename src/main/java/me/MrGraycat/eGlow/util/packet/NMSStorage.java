package me.MrGraycat.eGlow.util.packet;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.MrGraycat.eGlow.util.chat.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings({"rawtypes"})
public class NMSStorage {

	private final String serverPackage;
	public int minorVersion;

	private boolean is1_19_3OrAbove;
	private boolean is1_19_4OrAbove;

	public Class<?> packetClass;
	public Class<?> entityPlayerClass;
	public Class<?> craftPlayerClass;
	public Class<?> playerConnectionClass;
	public Class<?> networkManagerClass;
	public Field playerConnectionField;
	public Field networkManagerField;
	public Field channelField;
	public Method getHandle;
	public Method sendPacket;
	public Method setFlag;
	public Method getDataWatcher;

	public Class<Enum> enumChatFormatClass;
	public Class<?> iChatBaseComponentClass;
	public Class<?> chatSerializerClass;
	public Method chatSerializerDeserialize;

	//Spigot
	public Class<?> spigotConfigClass;
	public Class<?> itemStackClass;
	public Class<?> skullMetaClass;
	public Constructor<?> itemStackConstructor;
	public Method setOwningPlayer;
	public Field bungeeField;

	//MySQL
	public Class<?> mySqlDataSourceClass;
	public Constructor<?> mySqlDataSourceConstructor;
	public Method mySqlGetConnection;
	public Method mySqlSetHostname;
	public Method mySqlSetPort;
	public Method mySqlSetDatabaseName;
	public Method mySqlSetUsername;
	public Method mySqlSetPassword;

	//PacketPlayOutChat
	public Class<?> chatMessageTypeClass;
	public Constructor<?> newPacketPlayOutChat;
	public Enum[] chatMessageTypeValues;

	//PacketPlayOutActionBar
	public Class<?> packetPlayOutActionBarClass;
	public Constructor<?> newPlayOutPacketActionBar;

	//PacketPlayOutEntityMetadata
	public Class<?> packetPlayOutEntityMetadataClass;//
	public Constructor<?> newPacketPlayOutEntityMetadata;//
	public Field packetPlayOutScoreboardTeamActionField;
	public Field packetPlayOutEntityMetadataListfield;

	public Constructor<?> newScoreboard;
	public Constructor<?> newScoreboardTeam;
	public Method scoreboardTeamSetPrefix;
	public Method scoreboardTeamSetSuffix;
	public Method scoreboardTeamSetNameTagVisibility;
	public Method scoreboardTeamSetCollisionRule;
	public Method scoreboardTeamSetColor;
	public Method scoreboardTeamGetPlayerNameSet;
	public Class<?> packetPlayOutScoreboardTeamAClass;
	public Method packetPlayOutScoreboardTeamOf;
	public Method packetPlayOutScoreboardTeamOfBoolean;
	public Method packetPlayOutScoreboardTeamofString;

	//PacketPlayOutScoreboardTeam
	public Class<?> packetPlayOutScoreboardTeamClas;//
	public Class<?> enumNameTagVisibilityClass;
	public Class<?> enumTeamPushClass;
	public Constructor<?> newPacketPlayOutScoreboardTeam;
	public Field packetPlayOutScoreboardTeamName;
	public Field packetPlayOutScoreboardTeamPlayers;

	//DataWatcher
	public Class<?> dataWatcherClass;
	public Constructor<?> newDataWatcher;
	public Method dataWatcherRegister;

	//1.19.3
	public Class<?> dataWatcherDataValueClass;
	public Field dataWatcherItemsField;
	public Method dataWatcherItemToData;

	public Field dataWatcherItemTypeField;
	public Field dataWatcherItemValue;

	public Class<?> dataWatcherObjectClass;
	public Constructor<?> newDataWatcherObject;
	public Field dataWatcherObjectSlot;
	public Field dataWatcherObjectSerializer;
	public Class<?> dataWatcherRegistryClass;

	public NMSStorage() {
		this.serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		this.minorVersion = Integer.parseInt(this.serverPackage.split("_")[1]);

		is1_19_3Check();
		is1_19_4Check();
		initializeValues();
	}

	@SuppressWarnings("unchecked")
	public void initializeValues() {
		try {
			this.packetClass = getNMSClass("net.minecraft.network.protocol.Packet", "Packet");
			this.entityPlayerClass = getNMSClass("net.minecraft.server.level.EntityPlayer", "EntityPlayer");
			this.craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer");
			this.playerConnectionClass = getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection");
			this.networkManagerClass = getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager");
			this.playerConnectionField = getFields(this.entityPlayerClass, this.playerConnectionClass).get(0);
			this.networkManagerField = getFields(this.playerConnectionClass, this.networkManagerClass).get(0);
			this.channelField = getFields(this.networkManagerClass, Channel.class).get(0);
			this.getHandle = getMethod(this.craftPlayerClass, new String[]{"getHandle"});
			this.sendPacket = getMethod(this.playerConnectionClass, new String[]{"sendPacket", "a", "func_147359_a"}, this.packetClass);
			this.setFlag = getMethod(this.entityPlayerClass, new String[]{"setFlag", "b", "setEntityFlag"}, int.class, boolean.class);

			if (isIs1_19_4OrAbove()) {
				this.getDataWatcher = getMethod(this.entityPlayerClass, new String[]{"aj"});
			} else if (isIs1_19_3OrAbove()) {
				this.getDataWatcher = getMethod(this.entityPlayerClass, new String[]{"al"});
			} else {
				this.getDataWatcher = getMethod(this.entityPlayerClass, new String[]{"getDataWatcher", "ai"});
			}

			this.enumChatFormatClass = (Class) getNMSClass(new String[]{"net.minecraft.EnumChatFormat", "EnumChatFormat"});
			this.iChatBaseComponentClass = getNMSClass("net.minecraft.network.chat.IChatBaseComponent", "IChatBaseComponent");
			this.chatSerializerClass = getNMSClass("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
			this.chatSerializerDeserialize = getMethod(this.chatSerializerClass, new String[]{"a", "func_150699_a"}, String.class);

			this.spigotConfigClass = getNormalClass("org.spigotmc.SpigotConfig");

			if (this.minorVersion <= 12) {
				this.itemStackClass = getNormalClass("org.bukkit.inventory.ItemStack");
				this.itemStackConstructor = this.itemStackClass.getConstructor(Material.class, int.class, short.class);
				this.skullMetaClass = getNormalClass("org.bukkit.inventory.meta.SkullMeta");
				this.setOwningPlayer = getMethod(this.skullMetaClass, new String[]{"setOwner"}, String.class);
			}

			this.bungeeField = getField(this.spigotConfigClass);

			this.mySqlDataSourceClass = getNormalClass("com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "com.mysql.cj.jdbc.MysqlDataSource");
			this.mySqlDataSourceConstructor = this.mySqlDataSourceClass.getConstructors()[0];
			this.mySqlGetConnection = getMethod(this.mySqlDataSourceClass, new String[]{"getConnection"});
			this.mySqlSetHostname = getMethod(this.mySqlDataSourceClass, new String[]{"setServerName"}, String.class);
			this.mySqlSetPort = getMethod(this.mySqlDataSourceClass, new String[]{"setPort"}, int.class);
			this.mySqlSetDatabaseName = getMethod(this.mySqlDataSourceClass, new String[]{"setDatabaseName"}, String.class);
			this.mySqlSetUsername = getMethod(this.mySqlDataSourceClass, new String[]{"setUser"}, String.class);
			this.mySqlSetPassword = getMethod(this.mySqlDataSourceClass, new String[]{"setPassword"}, String.class);

			this.dataWatcherClass = getNMSClass("net.minecraft.network.syncher.DataWatcher", "DataWatcher");
			Class<?> dataWatcherItem = getNMSClass("net.minecraft.network.syncher.DataWatcher$Item", "DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
			this.dataWatcherObjectClass = getNMSClass("net.minecraft.network.syncher.DataWatcherObject", "DataWatcherObject");
			this.dataWatcherRegistryClass = getNMSClass("net.minecraft.network.syncher.DataWatcherRegistry", "DataWatcherRegistry");
			Class<?> dataWatcherSerializer = getNMSClass("net.minecraft.network.syncher.DataWatcherSerializer", "DataWatcherSerializer");
			this.newDataWatcher = this.dataWatcherClass.getConstructors()[0];
			this.newDataWatcherObject = this.dataWatcherObjectClass.getConstructor(int.class, dataWatcherSerializer);

			if (is1_19_3OrAbove) {
				this.dataWatcherDataValueClass = getNMSClass("net.minecraft.network.syncher.DataWatcher$b");
				this.dataWatcherItemsField = getFields(this.dataWatcherClass, Int2ObjectMap.class).get(0);
				this.dataWatcherItemToData = getMethods(dataWatcherItem, this.dataWatcherDataValueClass).get(0);
			}

			this.dataWatcherItemTypeField = getFields(dataWatcherItem, this.dataWatcherObjectClass).get(0);
			this.dataWatcherItemValue = getFields(dataWatcherItem, Object.class).get(0);
			this.dataWatcherObjectSlot = getFields(this.dataWatcherObjectClass, int.class).get(0);
			this.dataWatcherObjectSerializer = getFields(this.dataWatcherObjectClass, dataWatcherSerializer).get(0);
			this.dataWatcherRegister = getMethod(this.dataWatcherClass, new String[]{"register", "a"}, this.dataWatcherObjectClass, Object.class);

			this.packetPlayOutEntityMetadataClass = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");

			if (is1_19_3OrAbove) {
				this.newPacketPlayOutEntityMetadata = this.packetPlayOutEntityMetadataClass.getConstructor(int.class, List.class);
			} else {
				this.newPacketPlayOutEntityMetadata = this.packetPlayOutEntityMetadataClass.getConstructor(int.class, this.dataWatcherClass, boolean.class);
			}

			this.packetPlayOutEntityMetadataListfield = getFields(this.packetPlayOutEntityMetadataClass, List.class).get(0);

			//Scoreboard
			Class<?> scoreboard = getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard");
			Class<?> scoreboardTeam = getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam");
			this.newScoreboard = scoreboard.getConstructor();
			this.newScoreboardTeam = scoreboardTeam.getConstructor(scoreboard, String.class);

			if (this.minorVersion >= 13) {
				this.scoreboardTeamSetPrefix = getMethod(scoreboardTeam, new String[]{"setPrefix", "b"}, this.iChatBaseComponentClass);
				this.scoreboardTeamSetSuffix = getMethod(scoreboardTeam, new String[]{"setSuffix", "c"}, this.iChatBaseComponentClass);
				this.scoreboardTeamSetColor = getMethod(scoreboardTeam, new String[]{"setColor", "a"}, this.enumChatFormatClass);
			} else {
				this.scoreboardTeamSetPrefix = getMethod(scoreboardTeam, new String[]{"setPrefix", "func_96666_b"}, String.class);
				this.scoreboardTeamSetSuffix = getMethod(scoreboardTeam, new String[]{"setSuffix", "func_96662_c"}, String.class);
			}

			this.enumNameTagVisibilityClass = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
			this.enumTeamPushClass = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
			this.scoreboardTeamSetNameTagVisibility = getMethod(scoreboardTeam, new String[]{"setNameTagVisibility", "a"}, this.enumNameTagVisibilityClass);
			this.scoreboardTeamSetCollisionRule = getMethod(scoreboardTeam, new String[]{"setCollisionRule", "a"}, this.enumTeamPushClass);
			this.scoreboardTeamGetPlayerNameSet = getMethod(scoreboardTeam, new String[]{"getPlayerNameSet", "g", "func_96670_d"});

			this.packetPlayOutScoreboardTeamClas = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
			this.packetPlayOutScoreboardTeamName = getFields(this.packetPlayOutScoreboardTeamClas, String.class).get(0);
			this.packetPlayOutScoreboardTeamPlayers = getFields(this.packetPlayOutScoreboardTeamClas, Collection.class).get(0);
			this.packetPlayOutScoreboardTeamActionField = getInstanceFields(packetPlayOutScoreboardTeamClas, int.class).get(0);

			if (this.minorVersion >= 17) {
				this.packetPlayOutScoreboardTeamAClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
				this.packetPlayOutScoreboardTeamOf = this.packetPlayOutScoreboardTeamClas.getMethod("a", scoreboardTeam);
				this.packetPlayOutScoreboardTeamOfBoolean = this.packetPlayOutScoreboardTeamClas.getMethod("a", scoreboardTeam, boolean.class);
				this.packetPlayOutScoreboardTeamofString = this.packetPlayOutScoreboardTeamClas.getMethod("a", scoreboardTeam, String.class, this.packetPlayOutScoreboardTeamAClass);
			} else {
				this.newPacketPlayOutScoreboardTeam = this.packetPlayOutScoreboardTeamClas.getConstructor(scoreboardTeam, int.class);
			}

			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
				packetPlayOutActionBarClass = getNMSClass("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
				newPlayOutPacketActionBar = packetPlayOutActionBarClass.getConstructor(iChatBaseComponentClass);
			}

			Class<?> packetPlayOutChatClass = getNMSClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket", "net.minecraft.network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat", "Packet3Chat");
			if (minorVersion >= 12 && minorVersion <= 18) {
				chatMessageTypeClass = getNMSClass("net.minecraft.network.chat.ChatMessageType", "ChatMessageType");
				chatMessageTypeValues = getEnumValues(chatMessageTypeClass);
			}
			if (minorVersion >= 19) {
				if (ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
					newPacketPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, int.class);
				}
			} else if (minorVersion >= 16) {
				newPacketPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass, UUID.class);
			} else if (minorVersion >= 12) {
				newPacketPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass);
			} else if (minorVersion >= 8) {
				newPacketPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class);
			}
		} catch (Exception e) {
			ChatUtil.reportError(e);
		}
	}

	private void is1_19_3Check() {
		try {
			Class.forName("net.minecraft.network.syncher.DataWatcher$b");
			this.is1_19_3OrAbove = true;
		} catch (ClassNotFoundException e) {
			this.is1_19_3OrAbove = false;
		}
	}

	private void is1_19_4Check() {
		this.is1_19_4OrAbove = isIs1_19_3OrAbove() && !serverPackage.equals("v1_19_R2");
	}

	public boolean isIs1_19_3OrAbove() {
		return this.is1_19_3OrAbove;
	}

	public boolean isIs1_19_4OrAbove() {
		return this.is1_19_4OrAbove;
	}

	private Class<?> getNMSClass(String... names) throws ClassNotFoundException {
		for (String name : names) {
			try {
				return getNMSClass(name);
			} catch (ClassNotFoundException classNotFoundException) {
				//Nothing
			}
		}
		throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
	}

	private Class<?> getNormalClass(String... names) throws ClassNotFoundException {
		for (String name : names) {
			try {
				return Class.forName(name);
			} catch (ClassNotFoundException ignored) {
			}
		}
		throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
	}

	private Class<?> getNMSClass(String name) throws ClassNotFoundException {
		if (this.minorVersion >= 17)
			return Class.forName(name);
		try {
			return Class.forName("net.minecraft.server." + this.serverPackage + "." + name);
		} catch (NullPointerException e) {
			throw new ClassNotFoundException(name);
		}
	}

	public Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
		for (String name : names) {
			try {
				return clazz.getMethod(name, parameterTypes);
			} catch (Exception exception) {
				//Nothing
			}
		}
		throw new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " in class " + clazz.getName());
	}

	protected List<Method> getMethods(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
		List<Method> list = new ArrayList<>();
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getReturnType() == returnType && m.getParameterCount() == parameterTypes.length && Modifier.isPublic(m.getModifiers())) {
				Class<?>[] types = m.getParameterTypes();
				boolean valid = true;
				for (int i = 0; i < types.length; i++) {
					if (types[i] != parameterTypes[i]) {
						valid = false;
						break;
					}
				}
				if (valid)
					list.add(m);
			}
		}
		return list;
	}

	//Only used for getting the bungee setting
	private Field getField(Class<?> clazz) {
		if (clazz == null) return null;
		try {
			Field field = clazz.getField("bungee");
			field.setAccessible(true);

			return field;
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	private List<Field> getFields(Class<?> clazz, Class<?> type) {
		List<Field> list = new ArrayList<>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}

	private List<Field> getInstanceFields(Class<?> clazz, Class<?> type) {
		if (clazz == null) throw new IllegalArgumentException("Source class cannot be null");
		List<Field> list = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getType() == type && !Modifier.isStatic(field.getModifiers())) {
				list.add(setAccessible(field));
			}
		}
		return list;
	}

	private Enum[] getEnumValues(Class<?> enumClass) {
		if (!enumClass.isEnum()) throw new IllegalArgumentException(enumClass.getName() + " is not an enum class");
		return (Enum[]) enumClass.getEnumConstants();
	}

	public <T extends AccessibleObject> T setAccessible(T o) {
		o.setAccessible(true);
		return o;
	}
}