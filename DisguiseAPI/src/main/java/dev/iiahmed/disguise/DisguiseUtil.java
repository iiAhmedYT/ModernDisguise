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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

@SuppressWarnings("all")
public final class DisguiseUtil {

    public static final boolean IS_PAPER = findClass("com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration");
    public static final String VERSION_EXACT = Bukkit.getBukkitVersion().split("-")[0];
    public static final int INT_VER = Integer.parseInt(VERSION_EXACT.split("\\.")[1]);
    public static final String VERSION = findVersion();

    public static final String PREFIX = "net.minecraft.server." + (INT_VER < 17 ? "v" + VERSION + "." : "");
    public static final Field PROFILE_NAME;
    private static final String HANDLER_NAME = "ModernDisguise";
    public static final boolean IS_SUPPORTED;
    private static final boolean IS_13_R2_PLUS = INT_VER > 12 && !"1_13_R1".equals(VERSION);
    public static final boolean IS_20_R2_PLUS = INT_VER > 19 && !"1_20_R1".equals(VERSION);
    public static final boolean IS_20_R4_PLUS = INT_VER > 19 && !"1_20_R1".equals(VERSION) && !"1_20_R2".equals(VERSION) && !"1_20_R3".equals(VERSION);
    private static final HashMap<EntityType, Constructor<?>> ENTITIES = new HashMap<>();
    private static final HashMap<EntityType, Object> ENTITY_FIELDS = new HashMap<>();
    private static final Field CONNECTION, NETWORK_MANAGER, CHANNEL;
    private static final Method GET_PROFILE, GET_HANDLE, GET_ENTITY;
    private static final Class<?> ENTITY_TYPES, WORLD;
    private static final Map PLAYERS_MAP;
    public static int found, living, registered;

    static {
        final boolean obf = INT_VER >= 17;
        try {
            final Class<?> craftPlayer;
            if (IS_PAPER && (INT_VER > 20 || "1_20_R4".equals(VERSION))) {
                craftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            } else {
                craftPlayer = Class.forName("org.bukkit.craftbukkit.v" + VERSION + ".entity.CraftPlayer");
            }

            GET_PROFILE = craftPlayer.getMethod("getProfile");
            GET_HANDLE = craftPlayer.getMethod("getHandle");
            PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            PROFILE_NAME.setAccessible(true);
            final Field listFiled = Bukkit.getServer().getClass().getDeclaredField("playerList");
            listFiled.setAccessible(true);
            final Class<?> playerListClass = Class.forName((obf ?
                    PREFIX + "players." : PREFIX)
                    + "PlayerList");
            final Object playerList = listFiled.get(Bukkit.getServer());
            final Field playersByName = playerListClass.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            PLAYERS_MAP = (Map) playersByName.get(playerList);
        } catch (final Exception exception) {
            throw new RuntimeException("Failed to load ModernDisguise's primary features", exception);
        }

        IS_SUPPORTED = true;
        final Class<?> entityLiving;
        try {
            entityLiving = Class.forName((obf ?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityLiving");
            WORLD = Class.forName((obf ?
                    "net.minecraft.world.level." : PREFIX)
                    + "World");

            if (INT_VER >= 13) {
                ENTITY_TYPES = Class.forName((obf ?
                        "net.minecraft.world.entity." : PREFIX)
                        + "EntityTypes");
                GET_ENTITY = ENTITY_TYPES.getMethod("a", String.class);
            } else {
                ENTITY_TYPES = null;
                GET_ENTITY = null;
            }

            final Class<?> entityPlayer = Class.forName((obf ?
                    PREFIX + "level." : PREFIX)
                    + "EntityPlayer");
            CONNECTION = entityPlayer.getDeclaredField(obf ? (INT_VER < 20 ? "b" : "c") : "playerConnection");
            CONNECTION.setAccessible(true);
            final Class<?> playerConnection = Class.forName(
                    (obf ? PREFIX + "network." : PREFIX) + (IS_20_R2_PLUS ? "ServerCommonPacketListenerImpl" : "PlayerConnection"));
            NETWORK_MANAGER = playerConnection.getDeclaredField(INT_VER < 17 ? "networkManager" :
                    (
                            INT_VER <= 18 ? "a" :
                                    (
                                            INT_VER < 20 ? "b"
                                                    : IS_20_R2_PLUS ? (IS_20_R4_PLUS ? "e" : "c") : "h"
                                    )
                    )
            );
            NETWORK_MANAGER.setAccessible(true);
            final Class<?> networkManager = Class.forName((obf ?
                    "net.minecraft.network." : PREFIX)
                    + "NetworkManager");
            CHANNEL = networkManager.getDeclaredField(INT_VER < 17 ? "channel"
                    : (INT_VER > 18 || VERSION.equals("1_18_R2") ? (IS_20_R2_PLUS ? "n" : "m") : "k"));
        } catch (final Exception exception) {
            throw new RuntimeException("Failed to load ModernDisguise's secondary features (disguising as entities)", exception);
        }

        final Map<String, String> overrideNames = new HashMap<>();
        overrideNames.put("ELDER_GUARDIAN", "GuardianElder");
        overrideNames.put("WITHER_SKELETON", "SkeletonWither");
        overrideNames.put("STRAY", "SkeletonStray");
        overrideNames.put("HUSK", "ZombieHusk");
        overrideNames.put("ZOMBIE_HORSE", "HorseZombie");
        overrideNames.put("SKELETON_HORSE", "HorseSkeleton");
        overrideNames.put("DONKEY", "HorseDonkey");
        overrideNames.put("MULE", "HorseMule");
        overrideNames.put("ILLUSIONER", "IllagerIllusioner");
        overrideNames.put("GIANT", "GiantZombie");
        overrideNames.put("ZOMBIFIED_PIGLIN", "PigZombie");
        overrideNames.put("MOOSHROOM", "MushroomCow");
        overrideNames.put("SNOW_GOLEM", "Snowman");
        overrideNames.put("PUFFERFISH", "PufferFish");
        overrideNames.put("TRADER_LLAMA", "LlamaTrader");
        overrideNames.put("WANDERING_TRADER", "VillagerTrader");

        for (final EntityType type : EntityType.values()) {
            if (!type.isAlive())  {
                continue;
            }

            final String name = type.name();
            final String className;
            if (overrideNames.containsKey(name)) {
                className = overrideNames.get(name);
            } else {
                final StringBuilder builder = new StringBuilder();
                boolean cap = true;
                for (final char c : name.toCharArray()) {
                    if (c == '_') {
                        cap = true;
                        continue;
                    }
                    builder.append(cap ? c : String.valueOf(c).toLowerCase());
                    cap = false;
                }
                className = builder.toString();
            }
            final Class<?> clazz = findEntity(className);
            if (clazz == null) {
                continue;
            }
            found++;
            if (!entityLiving.isAssignableFrom(clazz)) {
                continue;
            }
            living++;
            final Constructor<?> constructor = findConstructor(clazz, type);
            if (constructor == null) {
                ENTITY_FIELDS.remove(type);
                continue;
            }
            registered++;
            ENTITIES.put(type, constructor);
        }
    }

    private static String findVersion() {
        if (IS_PAPER && INT_VER >= 20) {
            switch (VERSION_EXACT) {
                case "1.20":
                case "1.20.1":
                    return "1_20_R1";
                case "1.20.2":
                case "1.20.3":
                    return "1_20_R2";
                case "1.20.4":
                    return "1_20_R3";
                // just wild-guessing lol
                case "1.20.5":
                case "1.20.6":
                    return "1_20_R4";
                case "1.21":
                    return "1_21_R1";
                default:
                    return "UNKNOWN";
            }
        }
        return Bukkit.getServer().getClass().getPackage().getName().substring(24);
    }

    /**
     * Finds any {@link Class} of the provided paths
     *
     * @param paths all possible class paths
     * @return false if the {@link Class} was NOT found
     */
    private static boolean findClass(final String... paths) {
        for (final String path : paths) {
            if (getClass(path) != null) return true;
        }
        return false;
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
        for (final String path : new String[]{
                // animals
                "animal", "animal.allay", "animal.armadillo",
                "animal.axolotl", "animal.camel", "animal.frog",
                "animal.horse", "animal.goat", "animal.sniffer",

                // monster
                "monster", "monster.warden", "monster.piglin", "monster.hoglin", "monster.breeze",

                // other
                "ambient", "npc", "raid", "boss.wither", "boss.enderdragon",

                // root directory (so far only GlowSquid is like that)
                ""
        }) {
            final String additon = path.isEmpty() ? "" : path + ".";
            final Class<?> firstTry = getClass("net.minecraft.world.entity." + additon + name);
            if (firstTry != null) {
                return firstTry;
            }

            final Class<?> secondTry = getClass("net.minecraft.world.entity." + additon + "Entity" + name);
            if (secondTry != null) {
                return secondTry;
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
    private static Constructor<?> findConstructor(@NotNull final Class<?> entityClass, final EntityType type) {
        if (INT_VER < 13) {
            return getConstructor(entityClass, WORLD);
        }
        try {
            final Object obj = GET_ENTITY.invoke(null, type.name().toLowerCase(Locale.ENGLISH));
            if (obj == null) {
                return null;
            }
            if (INT_VER == 13) {
                ENTITY_FIELDS.put(type, obj);
            } else {
                final Optional<?> o = (Optional<?>) obj;
                if (o.isPresent()) {
                    ENTITY_FIELDS.put(type, o.get());
                } else {
                    return null;
                }
            }
        } catch (final Exception ignored) {
            return null;
        }
        return getConstructor(entityClass, ENTITY_TYPES, WORLD);
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
     * Creates an NMS entity instance of the provided {@link EntityType}
     * as long as it's a supported one
     *
     * @param type the {@link EntityType}
     * @param world the NMS world the entity should spawn in
     */
    public static Object createEntity(final EntityType type, @NotNull final Object world) throws Exception {
        if (!ENTITIES.containsKey(type)) {
            throw new RuntimeException("Creating a not supported entity.");
        }
        final Constructor<?> constructor = ENTITIES.get(type);
        final Object entity;
        if (constructor.getParameterCount() == 1) {
            entity = constructor.newInstance(world);
        } else {
            entity = constructor.newInstance(ENTITY_FIELDS.get(type), world);
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
            return (GameProfile) GET_PROFILE.invoke(player);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * Registers a name as an online player to disallow {@link Player}s to register as
     *
     * @param name the registered name
     * @param player the registered player
     */
    public static void register(@NotNull final String name, @NotNull final Player player) {
        try {
            final Object entityPlayer = GET_HANDLE.invoke(player);
            PLAYERS_MAP.put(IS_13_R2_PLUS ? name.toLowerCase(Locale.ENGLISH) : name, entityPlayer);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Unregisters a name as an online player to allow {@link Player}s to register as
     *
     * @param name the unregistered name
     */
    public static void unregister(@NotNull final String name) {
        PLAYERS_MAP.remove(IS_13_R2_PLUS ? name.toLowerCase(Locale.ENGLISH) : name);
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
     * Un-injects out of the {@link Player}'s netty channel
     *
     * @param player the player getting un-injected out of
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
            final Object entityPlayer = GET_HANDLE.invoke(player);
            final Object connection = CONNECTION.get(entityPlayer);
            final Object networkManager = NETWORK_MANAGER.get(connection);
            return (Channel) CHANNEL.get(networkManager);
        } catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
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
     * @return the {@link Skin} of the given {@link Player}
     */
    @SuppressWarnings("all")
    public static @NotNull Skin getSkin(@NotNull final Player player) {
        final GameProfile profile = getProfile(player);
        if (profile == null) {
            return new Skin(null, null);
        }
        final Optional<Property> optional = profile.getProperties().get("textures").stream().findFirst();
        if (optional.isPresent()) {
            return getSkin(optional.get());
        }
        return new Skin(null, null);
    }

    /**
     * @return the {@link Skin} of the given {@link Property}
     */
    @SuppressWarnings("all")
    public static @NotNull Skin getSkin(@NotNull final Property property) {
        String textures, signature;
        if (IS_20_R2_PLUS) {
            try {
                textures = (String) Property.class.getMethod("value").invoke(property);
                signature = (String) Property.class.getMethod("signature").invoke(property);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            textures = property.getValue();
            signature = property.getSignature();
        }
        return new Skin(textures, signature);
    }

}