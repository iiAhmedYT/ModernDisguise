package dev.iiahmed.disguise.packet.dev;

import dev.iiahmed.disguise.packet.ChatView;
import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.NamedEntitySpawnView;
import dev.iiahmed.disguise.packet.PacketAccessor;
import dev.iiahmed.disguise.packet.PlayerInfoView;
import dev.iiahmed.disguise.packet.ScoreboardTeamView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Test- and dev-only {@link PacketAccessor} that uses a caller-supplied
 * map from concrete packet classes to logical packet identifiers.
 *
 * <p>Lets the pipeline be exercised end-to-end on a plain JVM without
 * an NMS classpath — useful for unit tests, dry runs, and example
 * code. Not intended for production use.</p>
 *
 * <p>Bundle support is opt-in. When enabled, {@link #bundle(List)}
 * returns a {@link FakeBundle} that {@link #unbundle(Object)} can
 * decompose, and the accessor automatically identifies
 * {@link FakeBundle} as {@link LogicalPacket#BUNDLE}.</p>
 */
public final class FakeAccessor implements PacketAccessor {

    /**
     * Synthetic bundle envelope. Exposed only so test code can
     * recognize the value returned by {@link FakeAccessor#bundle(List)}.
     */
    public static final class FakeBundle {

        private final List<Object> parts;

        public FakeBundle(@NotNull final List<Object> parts) {
            this.parts = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(parts, "parts")));
        }

        public @NotNull List<Object> parts() {
            return parts;
        }
    }

    private final Map<Class<?>, LogicalPacket> registry;
    private final boolean bundles;

    /**
     * @param registry caller-owned map from packet class to logical
     *                 type; copied defensively into an
     *                 {@link IdentityHashMap}
     * @param bundles  whether to support
     *                 {@link #bundle(List)} / {@link #unbundle(Object)}
     */
    public FakeAccessor(@NotNull final Map<Class<?>, LogicalPacket> registry,
                        final boolean bundles) {
        this.registry = new IdentityHashMap<>(Objects.requireNonNull(registry, "registry"));
        if (bundles) {
            this.registry.put(FakeBundle.class, LogicalPacket.BUNDLE);
        }
        this.bundles = bundles;
    }

    @Override
    public @Nullable LogicalPacket identify(@NotNull final Object packet) {
        return registry.get(packet.getClass());
    }

    @Override
    public boolean supportsBundles() {
        return bundles;
    }

    @Override
    public @NotNull List<Object> unbundle(@NotNull final Object bundle) {
        requireBundles();
        return new ArrayList<>(((FakeBundle) bundle).parts());
    }

    @Override
    public @NotNull Object bundle(@NotNull final List<Object> packets) {
        requireBundles();
        return new FakeBundle(packets);
    }

    @Override
    public @NotNull PlayerInfoView playerInfoView(@NotNull final Object packet) {
        throw views();
    }

    @Override
    public @NotNull NamedEntitySpawnView namedEntitySpawnView(@NotNull final Object packet) {
        throw views();
    }

    @Override
    public @NotNull ScoreboardTeamView scoreboardTeamView(@NotNull final Object packet) {
        throw views();
    }

    @Override
    public @NotNull ChatView chatView(@NotNull final Object packet) {
        throw views();
    }

    private static UnsupportedOperationException views() {
        return new UnsupportedOperationException(
                "FakeAccessor does not implement typed views; tests that "
                        + "exercise rewriters should provide their own PacketAccessor");
    }

    private void requireBundles() {
        if (!bundles) {
            throw new UnsupportedOperationException("Bundles disabled on this FakeAccessor");
        }
    }
}
