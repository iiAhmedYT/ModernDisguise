package dev.iiahmed.disguise.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class DisguiseUtil {

    private static final String HANDLER_NAME = "ModernDisguise";
    public static final String PREFIX = "net.minecraft.server." + (Version.isBelow(17) ? "v" + Version.NMS + "." : "");

    public static final Field PROFILE_NAME;
    public static final boolean PRIMARY, INJECTION;

    public static FieldAccessor<?> CONNECTION;
    public static FieldAccessor<?> NETWORK_MANAGER;
    public static FieldAccessor<Channel> NETWORK_CHANNEL;

    private static final Method GET_PROFILE, GET_HANDLE;
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