package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
 *       action field)</li>
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
 * {@link Class#forName(String)} and cached in an
 * {@link IdentityHashMap}; {@link #identify(Object)} is then an O(1)
 * lookup safe to call from the Netty I/O thread.</p>
 *
 * <p>Versions where one of the expected packet classes is missing on
 * the runtime classpath are skipped silently — a small map of
 * mappings, not a fatal error.</p>
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
                // This version does not have this packet; leave it out
                // and let identify() return null for it.
            }
        }
        this.classes = resolved;
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
}
