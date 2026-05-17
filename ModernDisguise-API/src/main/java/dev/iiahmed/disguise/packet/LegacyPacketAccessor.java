package dev.iiahmed.disguise.packet;

import com.mojang.authlib.GameProfile;
import dev.iiahmed.disguise.util.reflection.FieldAccessor;
import dev.iiahmed.disguise.util.reflection.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared {@link PacketAccessor} for the pre-1.17 NMS era
 * ({@code net.minecraft.server.v1_8_R3} through
 * {@code net.minecraft.server.v1_16_R3}).
 *
 * <p>Spigot kept its public NMS class names and the
 * {@code net.minecraft.server.v1_X_RY} package layout stable across this
 * range, so the same implementation serves every legacy version — only
 * the package prefix changes. The plugin instantiates this with the
 * detected NMS version string, e.g.
 * {@code new LegacyPacketAccessor("v1_8_R3")}.</p>
 *
 * <p>The current rewrite surface (the four packets selected for the
 * initial milestone) is:</p>
 *
 * <ul>
 *   <li>{@code PacketPlayOutPlayerInfo} → {@link LogicalPacket#PLAYER_INFO_UPDATE}
 *       (1.8 has no update/remove split; the rewriter inspects the
 *       action field via {@link PlayerInfoView#action()})</li>
 *   <li>{@code PacketPlayOutNamedEntitySpawn} → {@link LogicalPacket#ADD_ENTITY}</li>
 *   <li>{@code PacketPlayOutScoreboardTeam} → {@link LogicalPacket#SET_PLAYER_TEAM}</li>
 *   <li>{@code PacketPlayOutChat} → {@link LogicalPacket#SYSTEM_CHAT}
 *       (1.8–1.18 use one chat packet for everything; the 1.19+ split
 *       does not exist here)</li>
 * </ul>
 *
 * <p>Bundles do not exist before 1.20.2; {@link #supportsBundles()}
 * returns {@code false} and the bundle / unbundle methods refuse to
 * run.</p>
 *
 * <p>Classes are resolved once at construction via
 * {@link Class#forName(String)} and field handles are resolved by
 * <em>type</em> (not by obfuscated name) so the same code keeps working
 * across 1.8 – 1.16 where Spigot's field names drifted but field
 * <em>types</em> stayed stable. Reflection happens at construction
 * only; {@link #identify(Object)} and {@link #playerInfoView(Object)}
 * are O(1) on the Netty I/O thread.</p>
 *
 * <p>Versions where one of the expected packet classes is missing on
 * the runtime classpath are skipped silently; {@link #identify(Object)}
 * returns {@code null} for them and {@link #playerInfoView(Object)}
 * throws {@link UnsupportedOperationException} if its target class did
 * not resolve.</p>
 */
public final class LegacyPacketAccessor implements PacketAccessor {

    private static final String[] PACKETS = {
            "PacketPlayOutPlayerInfo",
            "PacketPlayOutNamedEntitySpawn",
            "PacketPlayOutScoreboardTeam",
            "PacketPlayOutChat"
    };

    private static final LogicalPacket[] TYPES = {
            LogicalPacket.PLAYER_INFO_UPDATE,
            LogicalPacket.ADD_ENTITY,
            LogicalPacket.SET_PLAYER_TEAM,
            LogicalPacket.SYSTEM_CHAT
    };

    private final String nmsVersion;
    private final Map<Class<?>, LogicalPacket> classes;

    /** Resolved at construction; {@code null} if the version lacks the packet. */
    private final FieldAccessor<?> piActionField;
    private final FieldAccessor<?> piEntriesField;
    private final FieldAccessor<?> piDataProfileField;

    private final FieldAccessor<UUID> spawnUuidField;

    private final FieldAccessor<?> teamPlayersField;

    private final FieldAccessor<?> chatComponentField;
    private final Method chatSerializeMethod;
    private final Method chatDeserializeMethod;

    /**
     * @param nmsVersion the NMS package suffix, e.g. {@code "v1_8_R3"},
     *                   matching the {@code net.minecraft.server.<suffix>}
     *                   package
     */
    public LegacyPacketAccessor(@NotNull final String nmsVersion) {
        this.nmsVersion = Objects.requireNonNull(nmsVersion, "nmsVersion");
        final Map<Class<?>, LogicalPacket> resolved = new IdentityHashMap<>(PACKETS.length);
        final String prefix = "net.minecraft.server." + nmsVersion + ".";
        for (int i = 0; i < PACKETS.length; i++) {
            try {
                resolved.put(Class.forName(prefix + PACKETS[i]), TYPES[i]);
            } catch (final ClassNotFoundException ignored) {
                // This version does not have this packet; identify()
                // simply returns null for instances of the missing class.
            }
        }
        this.classes = resolved;

        FieldAccessor<?> action = null, entries = null, dataProfile = null;
        try {
            final Class<?> piPacket = Class.forName(prefix + "PacketPlayOutPlayerInfo");
            final Class<?> actionEnum = firstNested(piPacket, true);
            final Class<?> dataClass = firstNested(piPacket, false);
            action = Reflections.getField(piPacket, actionEnum);
            entries = Reflections.getField(piPacket, List.class);
            dataProfile = Reflections.getField(dataClass, GameProfile.class);
        } catch (final Throwable ignored) {
            // Leave fields null; playerInfoView() will refuse.
        }
        this.piActionField = action;
        this.piEntriesField = entries;
        this.piDataProfileField = dataProfile;

        FieldAccessor<UUID> spawnUuid = null;
        try {
            final Class<?> spawnPacket = Class.forName(prefix + "PacketPlayOutNamedEntitySpawn");
            spawnUuid = Reflections.getField(spawnPacket, UUID.class);
        } catch (final Throwable ignored) {
            // Leave null; namedEntitySpawnView() will refuse.
        }
        this.spawnUuidField = spawnUuid;

        FieldAccessor<?> teamPlayers = null;
        try {
            final Class<?> teamPacket = Class.forName(prefix + "PacketPlayOutScoreboardTeam");
            teamPlayers = Reflections.getField(teamPacket, Collection.class);
        } catch (final Throwable ignored) {
            // Leave null; scoreboardTeamView() will refuse.
        }
        this.teamPlayersField = teamPlayers;

        FieldAccessor<?> chatComponent = null;
        Method chatSerialize = null, chatDeserialize = null;
        try {
            final Class<?> chatPacket = Class.forName(prefix + "PacketPlayOutChat");
            final Class<?> componentClass = Class.forName(prefix + "IChatBaseComponent");
            chatComponent = Reflections.getField(chatPacket, componentClass);
            // ChatSerializer is a nested class of IChatBaseComponent on
            // every supported legacy version (v1_8_R2 and later). Its
            // serialize / deserialize methods are both named "a",
            // distinguished by argument type.
            Class<?> serializer = null;
            for (final Class<?> nested : componentClass.getDeclaredClasses()) {
                if ("ChatSerializer".equals(nested.getSimpleName())) {
                    serializer = nested;
                    break;
                }
            }
            if (serializer == null) {
                throw new ClassNotFoundException("ChatSerializer nested in " + componentClass.getName());
            }
            chatSerialize = serializer.getMethod("a", componentClass);
            chatDeserialize = serializer.getMethod("a", String.class);
        } catch (final Throwable ignored) {
            // Leave fields null; chatView() will refuse.
        }
        this.chatComponentField = chatComponent;
        this.chatSerializeMethod = chatSerialize;
        this.chatDeserializeMethod = chatDeserialize;
    }

    /** @return the NMS version this accessor was constructed for */
    public @NotNull String nmsVersion() {
        return nmsVersion;
    }

    @Override
    public @Nullable LogicalPacket identify(@NotNull final Object packet) {
        return classes.get(packet.getClass());
    }

    @Override
    public boolean supportsBundles() {
        return false;
    }

    @Override
    public @NotNull List<Object> unbundle(@NotNull final Object bundle) {
        throw new UnsupportedOperationException("Bundles do not exist before 1.20.2");
    }

    @Override
    public @NotNull Object bundle(@NotNull final List<Object> packets) {
        throw new UnsupportedOperationException("Bundles do not exist before 1.20.2");
    }

    @Override
    public @NotNull PlayerInfoView playerInfoView(@NotNull final Object packet) {
        if (piActionField == null) {
            throw new UnsupportedOperationException(
                    "PlayerInfo view not available on NMS " + nmsVersion);
        }
        return new LegacyPlayerInfoView(packet);
    }

    @Override
    public @NotNull NamedEntitySpawnView namedEntitySpawnView(@NotNull final Object packet) {
        if (spawnUuidField == null) {
            throw new UnsupportedOperationException(
                    "NamedEntitySpawn view not available on NMS " + nmsVersion);
        }
        return new LegacyNamedEntitySpawnView(packet);
    }

    @Override
    public @NotNull ScoreboardTeamView scoreboardTeamView(@NotNull final Object packet) {
        if (teamPlayersField == null) {
            throw new UnsupportedOperationException(
                    "ScoreboardTeam view not available on NMS " + nmsVersion);
        }
        return new LegacyScoreboardTeamView(packet);
    }

    @Override
    public @NotNull ChatView chatView(@NotNull final Object packet) {
        if (chatComponentField == null) {
            throw new UnsupportedOperationException(
                    "Chat view not available on NMS " + nmsVersion);
        }
        return new LegacyChatView(packet);
    }

    /**
     * Returns the first nested class of {@code outer} that is or is
     * not an enum. The packet contains exactly one nested enum (the
     * action) and one nested non-enum class (the entry data type)
     * across the entire 1.8 – 1.16 range, so this scan is sufficient
     * without name-matching.
     */
    private static @NotNull Class<?> firstNested(@NotNull final Class<?> outer, final boolean isEnum) {
        for (final Class<?> nested : outer.getDeclaredClasses()) {
            if (nested.isEnum() == isEnum && !nested.isInterface()) {
                return nested;
            }
        }
        throw new IllegalStateException(
                "Expected " + (isEnum ? "an enum" : "a non-enum class")
                        + " nested in " + outer.getName());
    }

    private static @NotNull PlayerInfoView.Action mapAction(@NotNull final Object nmsEnum) {
        final String name = ((Enum<?>) nmsEnum).name();
        switch (name) {
            case "ADD_PLAYER":
                return PlayerInfoView.Action.ADD_PLAYER;
            case "UPDATE_GAMEMODE":
                return PlayerInfoView.Action.UPDATE_GAMEMODE;
            case "UPDATE_LATENCY":
                return PlayerInfoView.Action.UPDATE_LATENCY;
            case "UPDATE_DISPLAY_NAME":
                return PlayerInfoView.Action.UPDATE_DISPLAY_NAME;
            case "REMOVE_PLAYER":
                return PlayerInfoView.Action.REMOVE_PLAYER;
            default:
                return PlayerInfoView.Action.OTHER;
        }
    }

    private final class LegacyPlayerInfoView implements PlayerInfoView {

        private final Object packet;

        LegacyPlayerInfoView(@NotNull final Object packet) {
            this.packet = packet;
        }

        @Override
        public @NotNull Action action() {
            final Object nmsEnum = piActionField.get(packet);
            return mapAction(nmsEnum);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull List<Entry> entries() {
            final List<Object> raw = (List<Object>) piEntriesField.get(packet);
            if (raw == null || raw.isEmpty()) {
                return Collections.emptyList();
            }
            final List<Entry> wrapped = new ArrayList<>(raw.size());
            for (int i = 0, n = raw.size(); i < n; i++) {
                wrapped.add(new LegacyEntry(raw.get(i)));
            }
            return Collections.unmodifiableList(wrapped);
        }
    }

    private final class LegacyEntry implements PlayerInfoView.Entry {

        private final Object data;

        LegacyEntry(@NotNull final Object data) {
            this.data = data;
        }

        @Override
        public @NotNull GameProfile profile() {
            return (GameProfile) piDataProfileField.get(data);
        }

        @Override
        public void setProfile(@NotNull final GameProfile profile) {
            piDataProfileField.set(data, Objects.requireNonNull(profile, "profile"));
        }
    }

    private final class LegacyNamedEntitySpawnView implements NamedEntitySpawnView {

        private final Object packet;

        LegacyNamedEntitySpawnView(@NotNull final Object packet) {
            this.packet = packet;
        }

        @Override
        public @NotNull UUID uuid() {
            return spawnUuidField.get(packet);
        }

        @Override
        public void setUuid(@NotNull final UUID uuid) {
            spawnUuidField.set(packet, Objects.requireNonNull(uuid, "uuid"));
        }
    }

    private final class LegacyScoreboardTeamView implements ScoreboardTeamView {

        private final Object packet;

        LegacyScoreboardTeamView(@NotNull final Object packet) {
            this.packet = packet;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Collection<String> players() {
            final Collection<String> raw = (Collection<String>) teamPlayersField.get(packet);
            return raw == null ? Collections.emptyList() : raw;
        }

        @Override
        public void setPlayers(@NotNull final Collection<String> players) {
            teamPlayersField.set(packet, Objects.requireNonNull(players, "players"));
        }
    }

    private final class LegacyChatView implements ChatView {

        private final Object packet;

        LegacyChatView(@NotNull final Object packet) {
            this.packet = packet;
        }

        @Override
        public String json() {
            final Object component = chatComponentField.get(packet);
            if (component == null) {
                return null;
            }
            try {
                return (String) chatSerializeMethod.invoke(null, component);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Failed to serialize chat component on " + nmsVersion, e);
            }
        }

        @Override
        public void setJson(@NotNull final String json) {
            Objects.requireNonNull(json, "json");
            try {
                final Object component = chatDeserializeMethod.invoke(null, json);
                chatComponentField.set(packet, component);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Failed to deserialize chat component on " + nmsVersion, e);
            }
        }
    }
}
