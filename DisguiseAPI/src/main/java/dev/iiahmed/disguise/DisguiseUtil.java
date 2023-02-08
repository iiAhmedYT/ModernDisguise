package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class DisguiseUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(24);
    public static final int INT_VER = Integer.parseInt(VERSION.split("_")[1]);
    public static final String PREFIX = "net.minecraft.server." + (INT_VER < 17 ? "v" + VERSION + "." : "");
    private static final String HANDLER_NAME = "ModernDisguise";
    private static final HashMap<EntityType, Constructor<?>> ENTITIES = new HashMap<>();
    private static final HashMap<EntityType, Object> ENTITIY_FIELDS = new HashMap<>();
    private static final boolean IS_13_R2_PLUS = INT_VER > 12 && !"1_13_R1".equals(VERSION);
    private static Map PLAYERS_MAP;
    public static Field PROFILE_NAME, CONNECTION, NETWORK_MANAGER, CHANNEL;
    private static Class<?> CRAFT_PLAYER, ENTITY_LIVING, ENTITY_TYPES, WORLD;
    private static Method GET_PROFILE, GET_HANDLE;
    public static int found, living, registered;

    static {
        try {
            CRAFT_PLAYER = Class.forName("org.bukkit.craftbukkit.v" + VERSION + ".entity.CraftPlayer");
            GET_PROFILE = CRAFT_PLAYER.getMethod("getProfile");
            GET_HANDLE = CRAFT_PLAYER.getMethod("getHandle");
            PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            PROFILE_NAME.setAccessible(true);
            ENTITY_LIVING = Class.forName((INT_VER >= 17 ?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityLiving");
            WORLD = Class.forName((INT_VER >= 17 ?
                    "net.minecraft.world.level." : PREFIX)
                    + "World");
            ENTITY_TYPES = Class.forName((INT_VER >= 17 ?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityTypes");
            final Class<?> entityPlayer = Class.forName((INT_VER >= 17 ?
                    PREFIX + "level." : PREFIX)
                    + "EntityPlayer");
            CONNECTION = entityPlayer.getDeclaredField(INT_VER >= 17 ? "b" : "playerConnection");
            final Class<?> playerConnection = Class.forName((INT_VER >= 17 ?
                    PREFIX + "network." : PREFIX)
                    + "PlayerConnection");
            NETWORK_MANAGER = playerConnection.getDeclaredField(INT_VER < 17 ? "networkManager" : (INT_VER > 18 ? "b" : "a"));
            final Class<?> networkManager = Class.forName((INT_VER >= 17 ?
                    "net.minecraft.network." : PREFIX)
                    + "NetworkManager");
            CHANNEL = networkManager.getDeclaredField(INT_VER < 17 ? "channel" : (INT_VER > 18 || VERSION.equals("1_18_R2") ? "m" : "k"));
            final Field listFiled = Bukkit.getServer().getClass().getDeclaredField("playerList");
            listFiled.setAccessible(true);
            final Class<?> playerListClass = Class.forName((INT_VER >= 17 ?
                    PREFIX + "players." : PREFIX)
                    + "PlayerList");
            final Object playerList = listFiled.get(Bukkit.getServer());
            final Field playersByName = playerListClass.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            PLAYERS_MAP = (Map) playersByName.get(playerList);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        for (final EntityType type : EntityType.values()) {
            final StringBuilder builder = new StringBuilder("Entity");
            boolean cap = true;
            for (final char c : type.name().toCharArray()) {
                if (c == '_') {
                    cap = true;
                    continue;
                }
                builder.append(cap ? c : String.valueOf(c).toLowerCase());
                cap = false;
            }
            final Class<?> clazz = findEntity(builder.toString());
            if (clazz == null) {
                continue;
            }
            found++;
            if (!ENTITY_LIVING.isAssignableFrom(clazz)) {
                continue;
            }
            living++;
            final Constructor<?> constructor = findConstructor(clazz, type);
            if(constructor != null) {
                registered++;
                ENTITIES.put(type, constructor);
            }
        }
    }

    /**
     * Finds the {@link Class} of any NMS entity
     *
     * @param name the name of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Class<?> findEntity(final String name) {
        if (INT_VER < 17) {
            return getClass(PREFIX + name);
        }
        for (final String path : Arrays.asList("animal", "monster", "ambient", "npc", "raid",
                "monster.warden", "monster.piglin", "monster.hoglin", "boss.wither",
                "boss.enderdragon", "animal.allay", "animal.axolotl", "animal.camel",
                "animal.frog", "animal.goat", "animal.horse")) {
            final Class<?> clazz = getClass("net.minecraft.world.entity." + path + "." + name);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Finds the {@link Constructor} of any NMS entity
     *
     * @param entityClass the class of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Constructor<?> findConstructor(@NotNull final Class<?> entityClass, @NotNull final EntityType type) {
        Constructor<?> constructor = getConstructor(entityClass, WORLD);
        if (constructor != null) {
            return constructor;
        }
        if (INT_VER < 13) {
            return null;
        }
        final Field field = getField(ENTITY_TYPES, type.name());
        if (field == null) {
            return null;
        }
        constructor = getConstructor(entityClass, ENTITY_TYPES, WORLD);
        if (constructor != null) {
            try {
                ENTITIY_FIELDS.put(type, field.get(null));
            } catch (final Exception ignored) {
            }
        }
        return constructor;
    }

    /**
     * A nullable {@link Class#forName(String)} instead of throwing exceptions
     *
     * @return null if the {@link Class} was NOT found
     */
    public static Class<?> getClass(@NotNull final String path) {
        try {
            return Class.forName(path);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * A nullable {@link Class#getDeclaredConstructor(Class[])} instead of throwing exceptions
     *
     * @return null if the {@link Constructor} was NOT found
     */
    private static Constructor<?> getConstructor(@NotNull final Class<?> clazz, final Class<?>... classes) {
        try {
            return clazz.getDeclaredConstructor(classes);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * A nullable {@link Class#getDeclaredField(String)} (String)} instead of throwing exceptions
     *
     * @return null if the {@link Field} was NOT found
     */
    public static Field getField(@NotNull final Class<?> clazz, @NotNull final String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * Creates an NMS entity instance of the provided {@link EntityType}
     * as long as it's a supported one
     *
     * @param type the {@link EntityType}
     * @param world the NMS world the entity should spawn in
     */
    public static Object createEntity(@NotNull final EntityType type, @NotNull final Object world) throws Exception {
        if (!ENTITIES.containsKey(type)) {
            throw new RuntimeException("Creating a not supported entity.");
        }
        final Constructor<?> constructor = ENTITIES.get(type);
        final Object entity;
        if (constructor.getParameterCount() == 1) {
            entity = constructor.newInstance(world);
        } else {
            entity = constructor.newInstance(ENTITIY_FIELDS.get(type), world);
        }
        return entity;
    }

    /**
     * Checks if the provided {@link EntityType} is supported to be disguised as
     */
    public static boolean isEntitySupported(@NotNull final EntityType type) {
        return ENTITIES.containsKey(type);
    }

    /**
     * @return the {@link GameProfile} of the given {@link Player}
     */
    public static GameProfile getProfile(@NotNull final Player player) {
        try {
            return (GameProfile) GET_PROFILE.invoke(CRAFT_PLAYER.cast(player));
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * @return the {@link Skin} of the given {@link Player}
     */
    @SuppressWarnings("unused")
    public static Skin getSkin(@NotNull final Player player) {
        final GameProfile profile = getProfile(player);
        if (profile == null) {
            return new Skin(null, null);
        }
        String textures = null, signature = null;
        final Optional<Property> optional = profile.getProperties().get("textures").stream().findFirst();
        if (optional.isPresent()) {
            textures = optional.get().getValue();
            signature = optional.get().getSignature();
        }
        return new Skin(textures, signature);
    }

    /**
     * @return the parsed {@link JSONObject} of the URL input
     */
    public static JSONObject getJSONObject(@NotNull final String urlString) {
        try {
            final URL url = new URL(urlString);
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "ModernDisguiseAPI/v1.0");
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("The used URL doesn't seem to be working (the api is down?) " + urlString);
            }

            final Scanner scanner = new Scanner(url.openStream());
            final StringBuilder builder = new StringBuilder();
            while (scanner.hasNext()) {
                builder.append(scanner.next());
            }

            return (JSONObject) new JSONParser().parse(builder.toString());
        } catch (final IOException | ParseException exception) {
            throw new RuntimeException("Failed to Scan/Parse the URL", exception);
        }
    }

    /**
     * Registeres a name as an online player to disallow {@link Player}s to register as
     *
     * @param name the registered name
     * @param player the registered player
     */
    public static void register(@NotNull final String name, @NotNull final Player player) {
        try {
            final Object entityPlayer = GET_HANDLE.invoke(CRAFT_PLAYER.cast(player));
            PLAYERS_MAP.put(IS_13_R2_PLUS ? name.toLowerCase(Locale.ENGLISH) : name, entityPlayer);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Unegisteres a name as an online player to allow {@link Player}s to register as
     *
     * @param name the unregistered name
     */
    public static void unregister(@NotNull final String name) {
        PLAYERS_MAP.remove(IS_13_R2_PLUS? name.toLowerCase(Locale.ENGLISH) : name);
    }

    /**
     * Checks if any {@link Player} is online with X name since the {@link Bukkit#getPlayer(String)}
     * is deprecated in most new versions and doesn't support original names
     *
     * @param name the checked name
     */
    public static boolean isPlayerOnline(@NotNull final String name) {
        final String lowercase = name.toLowerCase(Locale.ENGLISH);
        if (IS_13_R2_PLUS) {
            return PLAYERS_MAP.containsKey(lowercase);
        }
        for (final Object obj : PLAYERS_MAP.values()) {
            if (obj == null) {
                continue;
            }
            final String playerName = obj.toString().toLowerCase(Locale.ENGLISH);
            if (lowercase.equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Injects into the {@link Player}'s netty {@link Channel}
     *
     * @param player  the player getting injected into
     * @param handler the {@link ChannelHandler} injected into the channel
     */
    public static void inject(@NotNull final Player player, @NotNull final ChannelHandler handler) {
        final Channel ch = getChannel(player);
        if (ch == null) {
            return;
        }
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get(HANDLER_NAME) == null) {
                ch.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
            }
        });
    }

    /**
     * Uninjects out of the {@link Player}'s netty channel
     *
     * @param player the player getting uninjected out of
     */
    public static void uninject(@NotNull final Player player) {
        final Channel ch = getChannel(player);
        if (ch == null) {
            return;
        }
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get(HANDLER_NAME) != null) {
                ch.pipeline().remove(HANDLER_NAME);
            }
        });
    }

    /**
     * @return the {@link Player}'s netty channel
     */
    private static Channel getChannel(@NotNull final Player player) {
        try {
            final Object entityPlayer = GET_HANDLE.invoke(CRAFT_PLAYER.cast(player));
            final Object connection = CONNECTION.get(entityPlayer);
            final Object networkManager = NETWORK_MANAGER.get(connection);
            return (Channel) CHANNEL.get(networkManager);
        } catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

}