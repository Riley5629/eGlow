package me.mrgraycat.eglow.util.packets;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.mrgraycat.eglow.util.text.ChatUtil;
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

	public Class<?> Packet;
	public Class<?> EntityPlayer;
	public Class<?> CraftPlayer;
	public Class<?> PlayerConnection;
	public Class<?> NetworkManager;
	public Field PLAYER_CONNECTION;
	public Field NETWORK_MANAGER;
	public Field CHANNEL;
	public Method getHandle;
	public Method sendPacket;
	public Method setFlag;
	public Method getDataWatcher;

	public Class<Enum> EnumChatFormat;
	public Class<?> IChatBaseComponent;
	public Class<?> ChatSerializer;
	public Method ChatSerializer_DESERIALIZE;

	//Spigot
	public Class<?> SpigotConfig;
	public Class<?> itemStack;
	public Class<?> skullMeta;
	public Constructor<?> getItemStack;
	public Method setOwningPlayer;
	public Field bungee;

	//MySQL
	public Class<?> MySQLDataSource;
	public Constructor<?> newMySQLDataSource;
	public Method MySQL_getConnection;
	public Method MySQL_setServerName;
	public Method MySQL_setPort;
	public Method MySQL_setDatabaseName;
	public Method MySQL_setUser;
	public Method MySQL_setPassword;

	//PacketPlayOutChat
	public Class<?> ChatMessageType;
	public Constructor<?> newPacketPlayOutChat;
	public Enum[] ChatMessageType_values;

	//PacketPlayOutActionBar
	public Class<?> packetPlayOutActionBar;
	public Constructor<?> newPlayOutPacketActionBar;

	//PacketPlayOutEntityMetadata
	public Class<?> PacketPlayOutEntityMetadata;//
	public Constructor<?> newPacketPlayOutEntityMetadata;//
	public Field PacketPlayOutScoreboardTeam_ACTION;
	public Field PacketPlayOutEntityMetadata_LIST;

	public Constructor<?> newScoreboard;
	public Constructor<?> newScoreboardTeam;
	public Method ScoreboardTeam_setPrefix;
	public Method ScoreboardTeam_setSuffix;
	public Method ScoreboardTeam_setNameTagVisibility;
	public Method ScoreboardTeam_setCollisionRule;
	public Method ScoreboardTeam_setColor;
	public Method ScoreboardTeam_getPlayerNameSet;
	public Class<?> PacketPlayOutScoreboardTeam_a;
	public Method PacketPlayOutScoreboardTeam_of;
	public Method PacketPlayOutScoreboardTeam_ofBoolean;
	public Method PacketPlayOutScoreboardTeam_ofString;

	//PacketPlayOutScoreboardTeam
	public Class<?> PacketPlayOutScoreboardTeam;//
	public Class<?> EnumNameTagVisibility;
	public Class<?> EnumTeamPush;
	public Constructor<?> newPacketPlayOutScoreboardTeam;
	public Field PacketPlayOutScoreboardTeam_NAME;
	public Field PacketPlayOutScoreboardTeam_PLAYERS;

	//DataWatcher
	public Class<?> DataWatcher;
	public Constructor<?> newDataWatcher;
	public Method DataWatcher_REGISTER;

	//1.19.3
	public Class<?> DataWatcher$DataValue;
	public Field DataWatcherItems;
	public Method DataWatcherItemToData;
	public Method DataWatcherB_INT;
	public Method DataWatcherB_Serializer;
	public Method DataWatcherB_VALUE;

	public Field DataWatcherItem_TYPE;
	public Field DataWatcherItem_VALUE;

	public Class<?> DataWatcherObject;
	public Constructor<?> newDataWatcherObject;
	public Field DataWatcherObject_SLOT;
	public Field DataWatcherObject_SERIALIZER;
	public Class<?> DataWatcherRegistry;

	public NMSStorage() {
		serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
		is1_19_3Check();
		is1_19_4Check();
		initializeValues();
	}

	@SuppressWarnings("unchecked")
	public void initializeValues() {
		try {
			this.Packet = getNMSClass("net.minecraft.network.protocol.Packet", "Packet");
			this.EntityPlayer = getNMSClass("net.minecraft.server.level.EntityPlayer", "EntityPlayer");
			this.CraftPlayer = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer");
			this.PlayerConnection = getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection");
			this.NetworkManager = getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager");
			this.PLAYER_CONNECTION = getFields(this.EntityPlayer, this.PlayerConnection).get(0);

			if (is1_20_3Plus() || is1_20_2Plus()) {
				this.NETWORK_MANAGER = getFields(this.PlayerConnection.getSuperclass(), this.NetworkManager).get(0);
			} else {
				this.NETWORK_MANAGER = getFields(this.PlayerConnection, this.NetworkManager).get(0);
			}

			this.CHANNEL = getFields(this.NetworkManager, Channel.class).get(0);
			this.getHandle = getMethod(this.CraftPlayer, new String[]{"getHandle"});

			if (is1_20_3Plus() || is1_20_2Plus()) {
				this.sendPacket = getMethod(this.PlayerConnection, new String[]{"b"}, this.Packet);
			} else {
				this.sendPacket = getMethod(this.PlayerConnection, new String[]{"sendPacket", "a", "func_147359_a"}, this.Packet);
			}

			this.setFlag = getMethod(this.EntityPlayer, new String[]{"setFlag", "b", "setEntityFlag"}, int.class, boolean.class);

			if (is1_20_3Plus()) {
				this.getDataWatcher = getMethod(this.EntityPlayer, new String[]{"an"});
			} else if (is1_20_2Plus()) {
				this.getDataWatcher = getMethod(this.EntityPlayer, new String[]{"al"});
			} else if (isIs1_19_4OrAbove()) {
				this.getDataWatcher = getMethod(this.EntityPlayer, new String[]{"aj"});
			} else if (isIs1_19_3OrAbove()) {
				this.getDataWatcher = getMethod(this.EntityPlayer, new String[]{"al"});
			} else {
				this.getDataWatcher = getMethod(this.EntityPlayer, new String[]{"getDataWatcher", "ai"});
			}

			this.EnumChatFormat = (Class) getNMSClass(new String[]{"net.minecraft.EnumChatFormat", "EnumChatFormat"});
			this.IChatBaseComponent = getNMSClass("net.minecraft.network.chat.IChatBaseComponent", "IChatBaseComponent");
			this.ChatSerializer = getNMSClass("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
			this.ChatSerializer_DESERIALIZE = getMethod(this.ChatSerializer, new String[]{"a", "func_150699_a"}, String.class);

			this.SpigotConfig = getNormalClass("org.spigotmc.SpigotConfig");

			if (this.minorVersion <= 12) {
				this.itemStack = getNormalClass("org.bukkit.inventory.ItemStack");
				this.getItemStack = this.itemStack.getConstructor(Material.class, int.class, short.class);
				this.skullMeta = getNormalClass("org.bukkit.inventory.meta.SkullMeta");
				this.setOwningPlayer = getMethod(this.skullMeta, new String[]{"setOwner"}, String.class);
			}

			this.bungee = getField(this.SpigotConfig);

			this.MySQLDataSource = getNormalClass("com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "com.mysql.cj.jdbc.MysqlDataSource");
			this.newMySQLDataSource = this.MySQLDataSource.getConstructors()[0];
			this.MySQL_getConnection = getMethod(this.MySQLDataSource, new String[]{"getConnection"});
			this.MySQL_setServerName = getMethod(this.MySQLDataSource, new String[]{"setServerName"}, String.class);
			this.MySQL_setPort = getMethod(this.MySQLDataSource, new String[]{"setPort"}, int.class);
			this.MySQL_setDatabaseName = getMethod(this.MySQLDataSource, new String[]{"setDatabaseName"}, String.class);
			this.MySQL_setUser = getMethod(this.MySQLDataSource, new String[]{"setUser"}, String.class);
			this.MySQL_setPassword = getMethod(this.MySQLDataSource, new String[]{"setPassword"}, String.class);

			this.DataWatcher = getNMSClass("net.minecraft.network.syncher.DataWatcher", "DataWatcher");
			Class<?> dataWatcherItem = getNMSClass("net.minecraft.network.syncher.DataWatcher$Item", "DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
			this.DataWatcherObject = getNMSClass("net.minecraft.network.syncher.DataWatcherObject", "DataWatcherObject");
			this.DataWatcherRegistry = getNMSClass("net.minecraft.network.syncher.DataWatcherRegistry", "DataWatcherRegistry");
			Class<?> dataWatcherSerializer = getNMSClass("net.minecraft.network.syncher.DataWatcherSerializer", "DataWatcherSerializer");
			this.newDataWatcher = this.DataWatcher.getConstructors()[0];
			this.newDataWatcherObject = this.DataWatcherObject.getConstructor(int.class, dataWatcherSerializer);

			if (isIs1_19_3OrAbove()) {
				this.DataWatcher$DataValue = getNMSClass("net.minecraft.network.syncher.DataWatcher$b");
				this.DataWatcherItems = getFields(this.DataWatcher, Int2ObjectMap.class).get(0);
				this.DataWatcherItemToData = getMethods(dataWatcherItem, this.DataWatcher$DataValue).get(0);

				this.DataWatcherB_INT = getMethod(this.DataWatcher$DataValue, new String[]{"a"});
				this.DataWatcherB_Serializer = getMethod(this.DataWatcher$DataValue, new String[]{"b"});
				this.DataWatcherB_VALUE = getMethod(this.DataWatcher$DataValue, new String[]{"c"});
			}

			this.DataWatcherItem_TYPE = getFields(dataWatcherItem, this.DataWatcherObject).get(0);
			this.DataWatcherItem_VALUE = getFields(dataWatcherItem, Object.class).get(0);
			this.DataWatcherObject_SLOT = getFields(this.DataWatcherObject, int.class).get(0);
			this.DataWatcherObject_SERIALIZER = getFields(this.DataWatcherObject, dataWatcherSerializer).get(0);
			this.DataWatcher_REGISTER = getMethod(this.DataWatcher, new String[]{"register", "a"}, this.DataWatcherObject, Object.class);
			
			this.PacketPlayOutEntityMetadata = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");

			if (isIs1_19_3OrAbove()) {
				this.newPacketPlayOutEntityMetadata = this.PacketPlayOutEntityMetadata.getConstructor(int.class, List.class);
			} else {
				this.newPacketPlayOutEntityMetadata = this.PacketPlayOutEntityMetadata.getConstructor(int.class, this.DataWatcher, boolean.class);
			}

			this.PacketPlayOutEntityMetadata_LIST = getFields(this.PacketPlayOutEntityMetadata, List.class).get(0);

			//Scoreboard
			Class<?> scoreboard = getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard");
			Class<?> scoreboardTeam = getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam");
			this.newScoreboard = scoreboard.getConstructor();
			this.newScoreboardTeam = scoreboardTeam.getConstructor(scoreboard, String.class);

			if (this.minorVersion >= 13) {
				this.ScoreboardTeam_setPrefix = getMethod(scoreboardTeam, new String[]{"setPrefix", "b"}, this.IChatBaseComponent);
				this.ScoreboardTeam_setSuffix = getMethod(scoreboardTeam, new String[]{"setSuffix", "c"}, this.IChatBaseComponent);
				this.ScoreboardTeam_setColor = getMethod(scoreboardTeam, new String[]{"setColor", "a"}, this.EnumChatFormat);
			} else {
				this.ScoreboardTeam_setPrefix = getMethod(scoreboardTeam, new String[]{"setPrefix", "func_96666_b"}, String.class);
				this.ScoreboardTeam_setSuffix = getMethod(scoreboardTeam, new String[]{"setSuffix", "func_96662_c"}, String.class);
			}

			this.EnumNameTagVisibility = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
			this.EnumTeamPush = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
			this.ScoreboardTeam_setNameTagVisibility = getMethod(scoreboardTeam, new String[]{"setNameTagVisibility", "a"}, this.EnumNameTagVisibility);
			this.ScoreboardTeam_setCollisionRule = getMethod(scoreboardTeam, new String[]{"setCollisionRule", "a"}, this.EnumTeamPush);
			this.ScoreboardTeam_getPlayerNameSet = getMethod(scoreboardTeam, new String[]{"getPlayerNameSet", "g", "func_96670_d"});

			this.PacketPlayOutScoreboardTeam = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
			this.PacketPlayOutScoreboardTeam_NAME = getFields(this.PacketPlayOutScoreboardTeam, String.class).get(0);
			this.PacketPlayOutScoreboardTeam_PLAYERS = getFields(this.PacketPlayOutScoreboardTeam, Collection.class).get(0);
			this.PacketPlayOutScoreboardTeam_ACTION = getInstanceFields(PacketPlayOutScoreboardTeam).get(0);

			if (this.minorVersion >= 17) {
				this.PacketPlayOutScoreboardTeam_a = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
				this.PacketPlayOutScoreboardTeam_of = this.PacketPlayOutScoreboardTeam.getMethod("a", scoreboardTeam);
				this.PacketPlayOutScoreboardTeam_ofBoolean = this.PacketPlayOutScoreboardTeam.getMethod("a", scoreboardTeam, boolean.class);
				this.PacketPlayOutScoreboardTeam_ofString = this.PacketPlayOutScoreboardTeam.getMethod("a", scoreboardTeam, String.class, this.PacketPlayOutScoreboardTeam_a);
			} else {
				this.newPacketPlayOutScoreboardTeam = this.PacketPlayOutScoreboardTeam.getConstructor(scoreboardTeam, int.class);
			}

			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
				packetPlayOutActionBar = getNMSClass("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
				newPlayOutPacketActionBar = packetPlayOutActionBar.getConstructor(IChatBaseComponent);
			}

			Class<?> PacketPlayOutChat = getNMSClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket", "net.minecraft.network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat", "Packet3Chat");
			if (minorVersion >= 12 && minorVersion <= 18) {
				ChatMessageType = getNMSClass("net.minecraft.network.chat.ChatMessageType", "ChatMessageType");
				ChatMessageType_values = getEnumValues(ChatMessageType);
			}
			if (minorVersion >= 19) {
				if (ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
					newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, int.class);
				}
			} else if (minorVersion >= 16) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType, UUID.class);
			} else if (minorVersion >= 12) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
			} else if (minorVersion >= 8) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
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

	private boolean is1_20_3Plus() {
		return minorVersion >= 20 && serverPackage.equals("v1_20_R3");
	}

	private boolean is1_20_2Plus() {
		return minorVersion >= 20 && serverPackage.equals("v1_20_R2");
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
			} catch (ClassNotFoundException ignored) {
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
		} catch (NullPointerException exception) {
			throw new ClassNotFoundException(name);
		}
	}

	public Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
		for (String name : names) {
			try {
				return clazz.getMethod(name, parameterTypes);
			} catch (Exception ignored) {
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

	private List<Field> getInstanceFields(Class<?> clazz) {
		if (clazz == null) throw new IllegalArgumentException("Source class cannot be null");
		List<Field> list = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getType() == int.class && !Modifier.isStatic(field.getModifiers())) {
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