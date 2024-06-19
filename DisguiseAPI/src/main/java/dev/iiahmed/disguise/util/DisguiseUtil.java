package dev.iiahmed.disguise.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.iiahmed.disguise.Skin;
import dev.iiahmed.disguise.util.reflection.FieldAccessor;
import dev.iiahmed.disguise.util.reflection.Reflections;
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
import java.util.logging.Level;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class DisguiseUtil {

    private static final String HANDLER_NAME = "ModernDisguise";
    private static final HashMap<EntityType, Constructor<?>> ENTITIES = new HashMap<>();
    private static final HashMap<EntityType, Object> ENTITY_FIELDS = new HashMap<>();
    public static final String PREFIX = "net.minecraft.server." + (Version.isBelow(17) ? "v" + Version.NMS + "." : "");

    public static final Field PROFILE_NAME;
    public static final boolean PRIMARY, SECONDARY;

    public static Field CONNECTION;
    public static Field NETWORK_MANAGER;
    public static FieldAccessor<Channel> CHANNEL;
    public static int found, living, registered;

    private static final Method GET_PROFILE, GET_HANDLE;
    private static Method GET_ENTITY;
    private static Class<?> ENTITY_TYPES, WORLD;
    private static final Map PLAYERS_MAP;

    static {
        final boolean obf = Version.isOrOver(17);
        try {
            final Class<?> craftPlayer;
            if (Version.IS_PAPER && Version.IS_20_R4_PLUS) {
                craftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            } else {
                craftPlayer = Class.forName("org.bukkit.craftbukkit.v" + Version.NMS + ".entity.CraftPlayer");
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

        PRIMARY = true;
        boolean secondary;
        Class<?> entityLiving = null;
        try {
            entityLiving = Class.forName((obf ?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityLiving");
            WORLD = Class.forName((obf ?
                    "net.minecraft.world.level." : PREFIX)
                    + "World");

            if (Version.isOrOver(13)) {
                ENTITY_TYPES = Class.forName((obf ?
                        "net.minecraft.world.entity." : PREFIX)
                        + "EntityTypes");
                GET_ENTITY = ENTITY_TYPES.getMethod("a", String.class);
            }

            final Class<?> entityPlayer = Class.forName((obf ?
                    PREFIX + "level." : PREFIX)
                    + "EntityPlayer");
            CONNECTION = entityPlayer.getDeclaredField(obf ? (Version.isBelow(20) ? "b" : "c") : "playerConnection");
            CONNECTION.setAccessible(true);
            final Class<?> playerConnection = Class.forName(
                    (obf ? PREFIX + "network." : PREFIX) + (Version.IS_20_R2_PLUS ? "ServerCommonPacketListenerImpl" : "PlayerConnection")
            );
            NETWORK_MANAGER = playerConnection.getDeclaredField(
                    Version.isBelow(17) ? "networkManager" : (
                            Version.isOrBelow(18) ? "a" : (
                                            Version.isBelow(20) ? "b" : Version.IS_20_R2_PLUS ? (Version.IS_20_R4_PLUS ? "e" : "c") : "h"
                            )
                    )
            );
            NETWORK_MANAGER.setAccessible(true);
            final Class<?> networkManager = Class.forName((obf ?
                    "net.minecraft.network." : PREFIX)
                    + "NetworkManager");
            CHANNEL = Reflections.getField(networkManager, Channel.class);
            secondary = true;
        } catch (final Throwable exception) {
            secondary = false;
            Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to load ModernDisguise's secondary features (disguising as entities)", exception);
        }

        SECONDARY = secondary;

        if (SECONDARY) {
            final Map<String, String> overrideNames = new HashMap<String, String>() {
                {
                    put("ELDER_GUARDIAN", "GuardianElder");
                    put("WITHER_SKELETON", "SkeletonWither");
                    put("STRAY", "SkeletonStray");
                    put("HUSK", "ZombieHusk");
                    put("ZOMBIE_HORSE", "HorseZombie");
                    put("SKELETON_HORSE", "HorseSkeleton");
                    put("DONKEY", "HorseDonkey");
                    put("MULE", "HorseMule");
                    put("ILLUSIONER", "IllagerIllusioner");
                    put("GIANT", "GiantZombie");
                    put("ZOMBIFIED_PIGLIN", "PigZombie");
                    put("MOOSHROOM", "MushroomCow");
                    put("SNOW_GOLEM", "Snowman");
                    put("PUFFERFISH", "PufferFish");
                    put("TRADER_LLAMA", "LlamaTrader");
                    put("WANDERING_TRADER", "VillagerTrader");
                }
            };

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

    }

    /**
     * Finds the {@link Class} of any NMS entity
     *
     * @param name the name of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Class<?> findEntity(final String name) {
        if (Version.isBelow(17)) {
            return Reflections.getClass(PREFIX + name);
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
            final Class<?> firstTry = Reflections.getClass("net.minecraft.world.entity." + additon + name);
            if (firstTry != null) {
                return firstTry;
            }

            final Class<?> secondTry = Reflections.getClass("net.minecraft.world.entity." + additon + "Entity" + name);
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
        if (Version.isBelow(13)) {
            return Reflections.getConstructor(entityClass, WORLD);
        }
        try {
            final Object obj = GET_ENTITY.invoke(null, type.name().toLowerCase(Locale.ENGLISH));
            if (obj == null) {
                return null;
            }
            if (Version.is(13)) {
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
        return Reflections.getConstructor(entityClass, ENTITY_TYPES, WORLD);
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
            PLAYERS_MAP.put(Version.IS_13_R2_PLUS ? name.toLowerCase(Locale.ENGLISH) : name, entityPlayer);
        } catch (final Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "[ModernDisguise] Couldn't put into players map player: " + player.getName(), exception);
        }
    }

    /**
     * Unregisters a name as an online player to allow {@link Player}s to register as
     *
     * @param name the unregistered name
     */
    public static void unregister(@NotNull final String name) {
        PLAYERS_MAP.remove(Version.IS_13_R2_PLUS ? name.toLowerCase(Locale.ENGLISH) : name);
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
            return CHANNEL.get(networkManager);
        } catch (final Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "[ModernDisguise] Couldn't hook into player: " + player.getName(), exception);
            return null;
        }
    }

    /**
     * @return the parsed {@link JSONObject} of the URL input
     */
    public static JSONObject getJSONObject(@NotNull final String urlString) {
        try {
            final Scanner scanner = getScanner(urlString);
            final StringBuilder builder = new StringBuilder();
            while (scanner.hasNext()) {
                builder.append(scanner.next());
            }

            return (JSONObject) new JSONParser().parse(builder.toString());
        } catch (final IOException | ParseException exception) {
            throw new RuntimeException("Failed to Scan/Parse the URL", exception);
        }
    }

    private static @NotNull Scanner getScanner(@NotNull String urlString) throws IOException {
        final URL url = new URL(urlString);
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "ModernDisguiseAPI/v1.0");
        connection.setRequestMethod("GET");
        connection.connect();
        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("The used URL doesn't seem to be working (the api is down?) " + urlString);
        }

        return new Scanner(url.openStream());
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
        final String textures, signature;
        if (Version.IS_20_R2_PLUS) {
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