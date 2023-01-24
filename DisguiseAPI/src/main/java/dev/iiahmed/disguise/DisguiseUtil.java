package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public final class DisguiseUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(24);
    public static final int INT_VER = Integer.parseInt(VERSION.split("_")[1]);
    public static final String PREFIX = "net.minecraft.server." + (INT_VER < 17 ? "v" + VERSION + "." : "");
    private static final HashSet<String> NAMES = new HashSet<>();
    private static final HashMap<EntityType, Class<?>> ENTITIES = new HashMap<>();
    public static int found, living, registered;
    public static Field PROFILE_NAME, CONNECTION, NETWORK_MANAGER, CHANNEL;
    private static Class<?> CRAFT_PLAYER, ENTITY_LIVING, ENTITY_TYPES, WORLD;
    private static Method GET_PROFILE, GET_HANDLE;

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
        } catch (Exception ignored) {
        }

        for (final EntityType type : EntityType.values()) {
            StringBuilder builder = new StringBuilder("Entity");
            boolean cap = true;
            for (char c : type.name().toCharArray()) {
                if (c == '_') {
                    cap = true;
                    continue;
                }
                builder.append(cap ? c : String.valueOf(c).toLowerCase());
                cap = false;
            }
            String name = builder.toString();
            Class<?> clazz = findEntity(name);
            if (clazz == null) {
                continue;
            }
            found++;
            if (!ENTITY_LIVING.isAssignableFrom(clazz)) {
                continue;
            }
            living++;
            if (hasConstructor(clazz, WORLD) || hasConstructor(clazz, ENTITY_TYPES, WORLD)) {
                registered++;
                ENTITIES.put(type, clazz);
            }
        }
    }

    /**
     * finds the {@link Class} of any NMS entity
     *
     * @param name of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Class<?> findEntity(String name) {
        if (INT_VER < 17) {
            return getClass(PREFIX + name);
        }
        for (String path : Arrays.asList("animal", "monster", "ambient", "npc", "raid",
                "monster.warden", "monster.piglin", "monster.hoglin", "boss.wither",
                "boss.enderdragon", "animal.allay", "animal.axolotl", "animal.camel",
                "animal.frog", "animal.goat", "animal.horse")) {
            Class<?> clazz = getClass("net.minecraft.world.entity." + path + "." + name);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * @return a {@link Boolean} that determines whether a {@link Class}
     * has a Constructor with the specified type arguments
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... classes) {
        try {
            clazz.getDeclaredConstructor(classes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * A nullable {@link Class#forName(String)} instead of throwing exceptions
     *
     * @return null if the {@link Class} was NOT found
     */
    public static Class<?> getClass(String path) {
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * @return the {@link Class} of a supported NMS entity by it's {@link EntityType}
     */
    public static Class<?> getEntity(@NotNull final EntityType type) {
        return ENTITIES.get(type);
    }

    /**
     * @return the {@link GameProfile} of the given {@link Player}
     */
    public static GameProfile getProfile(@NotNull final Player player) {
        try {
            return (GameProfile) GET_PROFILE.invoke(CRAFT_PLAYER.cast(player));
        } catch (Exception e) {
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
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to Scan/Parse the URL", e);
        }
    }

    /**
     * Registeres a name as an online player to disallow {@link Player}s to register as
     *
     * @param name the registered name
     */
    public static void register(final String name) {
        NAMES.add(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Unegisteres a name as an online player to allow {@link Player}s to register as
     *
     * @param name the unregistered name
     */
    public static void unregister(final String name) {
        NAMES.remove(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Checks if any {@link Player} is online with X name since the {@link Bukkit#getPlayer(String)}
     * is deprecated in most new versions and doesn't support original names
     *
     * @param name the checked name
     */
    public static boolean isPlayerOnline(final String name) {
        return NAMES.contains(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Injects into the {@link Player}'s netty {@link Channel}
     *
     * @param player  the player getting injected into
     * @param handler the {@link ChannelHandler} injected into the channel
     */
    public static void inject(@NotNull final Player player, final @NotNull ChannelHandler handler) throws Exception {
        Object entityPlayer = GET_HANDLE.invoke(CRAFT_PLAYER.cast(player));
        Object connection = CONNECTION.get(entityPlayer);
        Object networkManager = NETWORK_MANAGER.get(connection);
        Channel ch = (Channel) CHANNEL.get(networkManager);
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get("ModernDisguise") == null) {
                ch.pipeline().addBefore("packet_handler", "ModernDisguise", handler);
            }
        });
    }

    /**
     * Uninjects out of the {@link Player}'s netty channel
     *
     * @param player the player getting uninjected out of
     */
    public static void uninject(@NotNull final Player player) throws Exception {
        Object entityPlayer = GET_HANDLE.invoke(CRAFT_PLAYER.cast(player));
        Object connection = CONNECTION.get(entityPlayer);
        Object networkManager = NETWORK_MANAGER.get(connection);
        Channel ch = (Channel) CHANNEL.get(networkManager);
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get("ModernDisguise") != null) {
                ch.pipeline().remove("ModernDisguise");
            }
        });
    }

}