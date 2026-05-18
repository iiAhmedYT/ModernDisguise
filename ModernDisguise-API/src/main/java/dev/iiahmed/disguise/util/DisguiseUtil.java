package dev.iiahmed.disguise.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.iiahmed.disguise.Skin;
import dev.iiahmed.disguise.util.reflection.FieldAccessor;
import dev.iiahmed.disguise.util.reflection.Reflections;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public static final String PREFIX = "net.minecraft.server." + (Version.isBelow(1, 17, 0) ? "v" + Version.NMS + "." : "");

    public static final Field PROFILE_NAME;
    public static final boolean PRIMARY, INJECTION;

    private static final Object UNSAFE;
    private static final Method UNSAFE_PUT_OBJECT;
    private static final boolean GP_IS_RECORD;
    private static final long GAME_PROFILE_OFFSET;
    private static final Constructor<?> GP_CTOR;
    private static final boolean GP_CTOR_INCLUDES_PROPERTIES;
    private static final Method GP_GET_ID;
    private static final Method GP_GET_PROPERTIES;
    private static final Method GP_GET_NAME;

    private static final Method GET_PLAYER_PROFILE;
    private static final Method SET_PLAYER_PROFILE;
    private static final Method PLAYER_PROFILE_SET_NAME;
    private static final Method PLAYER_PROFILE_REMOVE_PROPERTY;
    private static final Method PLAYER_PROFILE_SET_PROPERTY;
    private static final Constructor<?> PROFILE_PROPERTY_CTOR;

    public static FieldAccessor<?> CONNECTION;
    public static FieldAccessor<?> NETWORK_MANAGER;
    public static FieldAccessor<Channel> NETWORK_CHANNEL;

    private static final Method GET_PROFILE, GET_HANDLE;
    private static final Map PLAYERS_MAP;

    static {
        final boolean obf = Version.isOrOver(1, 17, 0);
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
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);
            UNSAFE_PUT_OBJECT = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
            boolean gpIsRecord = false;
            try {
                gpIsRecord = (Boolean) Class.class.getMethod("isRecord").invoke(GameProfile.class);
            } catch (final Exception ignored) {}
            GP_IS_RECORD = gpIsRecord;
            long gameProfileOffset = 0;
            Constructor<?> gpCtor = null;
            boolean gpCtorIncludesProperties = false;
            if (GP_IS_RECORD) {
                Field gpField = null;
                for (Class<?> c = GET_HANDLE.getReturnType(); c != null && gpField == null; c = c.getSuperclass()) {
                    for (final Field f : c.getDeclaredFields()) {
                        if (f.getType() == GameProfile.class) {
                            gpField = f;
                            break;
                        }
                    }
                }
                if (gpField == null) throw new RuntimeException("Could not find GameProfile field in EntityPlayer hierarchy");
                gameProfileOffset = (long) unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(UNSAFE, gpField);
                // prefer 3-arg canonical record constructor (UUID, String, PropertyMap)
                for (final Constructor<?> ctor : GameProfile.class.getDeclaredConstructors()) {
                    final Class<?>[] params = ctor.getParameterTypes();
                    if (params.length == 3 && params[0] == UUID.class && params[1] == String.class) {
                        ctor.setAccessible(true);
                        gpCtor = ctor;
                        gpCtorIncludesProperties = true;
                        break;
                    }
                }
                if (gpCtor == null) {
                    for (final Constructor<?> ctor : GameProfile.class.getDeclaredConstructors()) {
                        final Class<?>[] params = ctor.getParameterTypes();
                        if (params.length == 2 && params[0] == UUID.class && params[1] == String.class) {
                            ctor.setAccessible(true);
                            gpCtor = ctor;
                            break;
                        }
                    }
                }
                if (gpCtor == null) throw new RuntimeException("Could not find GameProfile constructor");
            }
            GAME_PROFILE_OFFSET = gameProfileOffset;
            GP_CTOR = gpCtor;
            GP_CTOR_INCLUDES_PROPERTIES = gpCtorIncludesProperties;
            // accessor names changed from getId/getProperties/getName to id/properties/name in authlib 7+
            Method gpGetId;
            try {
                gpGetId = GameProfile.class.getMethod("id");
            } catch (final NoSuchMethodException ignored) {
                gpGetId = GameProfile.class.getMethod("getId");
            }
            GP_GET_ID = gpGetId;
            Method gpGetProperties;
            try {
                gpGetProperties = GameProfile.class.getMethod("properties");
            } catch (final NoSuchMethodException ignored) {
                gpGetProperties = GameProfile.class.getMethod("getProperties");
            }
            GP_GET_PROPERTIES = gpGetProperties;
            Method gpGetName;
            try {
                gpGetName = GameProfile.class.getMethod("name");
            } catch (final NoSuchMethodException ignored) {
                gpGetName = GameProfile.class.getMethod("getName");
            }
            GP_GET_NAME = gpGetName;
            Method getPlayerProfile = null;
            Method setPlayerProfile = null;
            Method playerProfileSetName = null;
            Method playerProfileRemoveProperty = null;
            Method playerProfileSetProperty = null;
            Constructor<?> profilePropertyCtor = null;
            try {
                getPlayerProfile = craftPlayer.getMethod("getPlayerProfile");
                for (final Method m : craftPlayer.getMethods()) {
                    if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) {
                        setPlayerProfile = m;
                        break;
                    }
                }
                if (setPlayerProfile != null) {
                    final Class<?> ppClass = setPlayerProfile.getParameterTypes()[0];
                    playerProfileSetName = ppClass.getMethod("setName", String.class);
                    for (final Method m : ppClass.getMethods()) {
                        if (m.getName().equals("removeProperty") && m.getParameterCount() == 1
                                && m.getParameterTypes()[0] == String.class) {
                            playerProfileRemoveProperty = m;
                            break;
                        }
                    }
                    for (final Method m : ppClass.getMethods()) {
                        if (m.getName().equals("setProperty") && m.getParameterCount() == 1) {
                            playerProfileSetProperty = m;
                            final Class<?> propClass = m.getParameterTypes()[0];
                            try {
                                profilePropertyCtor = propClass.getConstructor(String.class, String.class, String.class);
                            } catch (final NoSuchMethodException ignored) {}
                            break;
                        }
                    }
                }
            } catch (final Exception ignored) {}
            GET_PLAYER_PROFILE = getPlayerProfile;
            SET_PLAYER_PROFILE = setPlayerProfile;
            PLAYER_PROFILE_SET_NAME = playerProfileSetName;
            PLAYER_PROFILE_REMOVE_PROPERTY = playerProfileRemoveProperty;
            PLAYER_PROFILE_SET_PROPERTY = playerProfileSetProperty;
            PROFILE_PROPERTY_CTOR = profilePropertyCtor;
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
        boolean injection;
        try {
            final Class<?> entityPlayer = Class.forName(
                    (obf ? PREFIX + "level." : PREFIX) + "EntityPlayer"
            );
            final Class<?> playerConnection = Class.forName(
                    (obf ? PREFIX + "network." : PREFIX) + (Version.IS_20_R2_PLUS ? "ServerCommonPacketListenerImpl" : "PlayerConnection")
            );
            final Class<?> networkManager = Class.forName(
                    (obf ? "net.minecraft.network." : PREFIX) + "NetworkManager"
            );

            CONNECTION = Reflections.getField(entityPlayer, playerConnection);
            NETWORK_CHANNEL = Reflections.getField(networkManager, Channel.class);
            NETWORK_MANAGER = Reflections.getField(playerConnection, networkManager);
            injection = true;
        } catch (final Throwable exception) {
            injection = false;
            Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to load ModernDisguise's secondary features (disguising as entities)", exception);
        }

        INJECTION = injection;
    }

    public static UUID getProfileId(@NotNull final GameProfile profile) {
        try {
            return (UUID) GP_GET_ID.invoke(profile);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get GameProfile id", e);
        }
    }

    public static PropertyMap getProfileProperties(@NotNull final GameProfile profile) {
        try {
            return (PropertyMap) GP_GET_PROPERTIES.invoke(profile);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get GameProfile properties", e);
        }
    }

    public static String getProfileName(@NotNull final GameProfile profile) {
        try {
            return (String) GP_GET_NAME.invoke(profile);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get GameProfile name", e);
        }
    }

    /**
     * Sets the skin textures on a player's profile.
     * On Paper uses the {@code PlayerProfile} API. On other servers mutates the existing (mutable) {@link PropertyMap} directly.
     * Pass {@code null} textures to remove the texture property entirely.
     */
    public static void setSkin(@NotNull final Player player, @Nullable final GameProfile profile,
                               @Nullable final String textures, @Nullable final String signature) throws Exception {
        if (GET_PLAYER_PROFILE != null && SET_PLAYER_PROFILE != null) {
            final Object playerProfile = GET_PLAYER_PROFILE.invoke(player);
            if (PLAYER_PROFILE_REMOVE_PROPERTY != null) {
                PLAYER_PROFILE_REMOVE_PROPERTY.invoke(playerProfile, "textures");
            }
            if (textures != null && PLAYER_PROFILE_SET_PROPERTY != null && PROFILE_PROPERTY_CTOR != null) {
                final Object prop = PROFILE_PROPERTY_CTOR.newInstance("textures", textures, signature);
                PLAYER_PROFILE_SET_PROPERTY.invoke(playerProfile, prop);
            }
            SET_PLAYER_PROFILE.invoke(player, playerProfile);
            return;
        }
        if (profile == null) return;
        final PropertyMap props = getProfileProperties(profile);
        props.removeAll("textures");
        if (textures != null) {
            props.put("textures", new Property("textures", textures, signature));
        }
    }

    /**
     * Sets the name on a player's {@link GameProfile}.
     * When GameProfile is a record (immutable), creates a new instance and replaces it in the EntityPlayer via Unsafe.
     * Returns the active profile to use for any subsequent property mutations.
     */
    public static GameProfile setProfileName(@NotNull final Player player, @NotNull final String name) throws Exception {
        // Paper: use getPlayerProfile()/setPlayerProfile() to avoid injecting a raw record GameProfile
        if (GET_PLAYER_PROFILE != null && SET_PLAYER_PROFILE != null && PLAYER_PROFILE_SET_NAME != null) {
            final Object playerProfile = GET_PLAYER_PROFILE.invoke(player);
            PLAYER_PROFILE_SET_NAME.invoke(playerProfile, name);
            SET_PLAYER_PROFILE.invoke(player, playerProfile);
            return getProfile(player);
        }

        final GameProfile old = (GameProfile) GET_PROFILE.invoke(player);
        if (!GP_IS_RECORD) {
            PROFILE_NAME.set(old, name);
            return old;
        }
        // Non-Paper record case: reuse existing PropertyMap reference (skin handled separately via setSkin)
        final Object entityPlayer = GET_HANDLE.invoke(player);
        final PropertyMap existingProps = getProfileProperties(old);
        final GameProfile newProfile = (GameProfile) (GP_CTOR_INCLUDES_PROPERTIES
                ? GP_CTOR.newInstance(getProfileId(old), name, existingProps)
                : GP_CTOR.newInstance(getProfileId(old), name));
        UNSAFE_PUT_OBJECT.invoke(UNSAFE, entityPlayer, GAME_PROFILE_OFFSET, newProfile);
        return newProfile;
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
     * Finds an online player by their name.
     *
     * @param name the name of the player to look for, must not be null
     * @return the player with the specified name, or null if not found
     */
    @Nullable
    public static Player getPlayer(@NotNull final String name) {
        final Player direct = Bukkit.getPlayerExact(name);
        if (direct != null) {
            return direct;
        }

        final String lowercase = name.toLowerCase(Locale.ENGLISH);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ENGLISH).equals(lowercase)) return player;
        }
        return null;
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
     * @param name    the name of the injected handler
     * @param handler the {@link ChannelHandler} injected into the channel
     */
    public static void inject(@NotNull final Player player, final String name, @NotNull final ChannelHandler handler) {
        final Channel ch = getChannel(player);
        if (ch == null) {
            return;
        }
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get(name) == null) {
                ch.pipeline().addBefore("packet_handler", name, handler);
            }
        });
    }

    /**
     * Un-injects out of the {@link Player}'s netty channel
     *
     * @param player the player getting un-injected out of
     * @param name   the name of the un-injected handler
     */
    public static void uninject(@NotNull final Player player, final String name) {
        final Channel ch = getChannel(player);
        if (ch == null) {
            return;
        }
        ch.eventLoop().submit(() -> {
            if (ch.pipeline().get(name) != null) {
                ch.pipeline().remove(name);
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
            return NETWORK_CHANNEL.get(networkManager);
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
        final Optional<Property> optional = getProfileProperties(profile).get("textures").stream().findFirst();
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