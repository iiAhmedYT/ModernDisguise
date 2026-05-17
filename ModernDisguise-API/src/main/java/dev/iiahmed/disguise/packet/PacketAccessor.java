package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bridge between version-specific NMS packet objects and the
 * version-agnostic rewriter layer.
 *
 * <p>Every place where Minecraft version differences leak in — class
 * identity, field offsets, obfuscated field names, packet structural
 * changes — lives behind this interface. Rewriters never touch NMS
 * classes directly; they ask the accessor for typed view objects (to be
 * added incrementally as concrete rewriters are introduced).</p>
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>must resolve all reflective lookups at construction time and
 *       cache the resulting {@code MethodHandle}/{@code Field} objects;
 *       {@link #identify(Object)} is called for every outbound packet
 *       and must not perform per-call reflection;</li>
 *   <li>must be safe to call from a Netty I/O thread; no blocking, no
 *       Bukkit API access;</li>
 *   <li>must treat NMS packet instances as immutable — if a field
 *       needs to change, allocate a new packet via the appropriate
 *       view method rather than mutating in place.</li>
 * </ul>
 */
public interface PacketAccessor {

    /**
     * Map an NMS packet to its {@link LogicalPacket}, or {@code null}
     * if the disguise system does not rewrite it.
     *
     * <p>Called on every outbound packet — must be O(1) (typically an
     * {@link java.util.IdentityHashMap} lookup on {@code packet.getClass()}).</p>
     *
     * @param packet a non-null NMS packet object
     * @return the corresponding logical packet, or {@code null} for
     *         packets the pipeline ignores
     */
    @Nullable LogicalPacket identify(@NotNull Object packet);

    /**
     * @return {@code true} if this Minecraft version supports
     *         {@code ClientboundBundlePacket} (1.20.2+)
     */
    boolean supportsBundles();

    /**
     * Unwrap a bundle packet into its constituent packets, in order.
     *
     * <p>The returned list is mutable; the pipeline rewrites individual
     * entries and re-bundles them via {@link #bundle(List)}.</p>
     *
     * @param bundle a bundle packet (as identified by
     *               {@link LogicalPacket#BUNDLE})
     * @return the contained packets, in transmission order
     * @throws UnsupportedOperationException if this version does not
     *         support bundles
     */
    @NotNull List<Object> unbundle(@NotNull Object bundle);

    /**
     * Wrap a list of packets in a new bundle packet.
     *
     * @param packets the packets to bundle, in transmission order
     * @return a new {@code ClientboundBundlePacket} instance
     * @throws UnsupportedOperationException if this version does not
     *         support bundles
     */
    @NotNull Object bundle(@NotNull List<Object> packets);

    /**
     * Open a typed view over a {@link LogicalPacket#PLAYER_INFO_UPDATE}
     * packet.
     *
     * @param packet a packet whose {@link #identify(Object)} is
     *               {@link LogicalPacket#PLAYER_INFO_UPDATE}
     * @return a view that exposes the action and entries of the packet
     * @throws UnsupportedOperationException if this version does not
     *         support reading {@code PLAYER_INFO_UPDATE} packets (the
     *         class was missing on the runtime classpath)
     */
    @NotNull PlayerInfoView playerInfoView(@NotNull Object packet);

    /**
     * Open a typed view over a {@link LogicalPacket#ADD_ENTITY} packet
     * that carries a player UUID.
     *
     * @param packet a packet whose {@link #identify(Object)} is
     *               {@link LogicalPacket#ADD_ENTITY}
     * @return a view exposing the spawning entity's UUID
     * @throws UnsupportedOperationException if this version does not
     *         support reading the packet
     */
    @NotNull NamedEntitySpawnView namedEntitySpawnView(@NotNull Object packet);

    /**
     * Open a typed view over a {@link LogicalPacket#SET_PLAYER_TEAM}
     * packet.
     *
     * @param packet a packet whose {@link #identify(Object)} is
     *               {@link LogicalPacket#SET_PLAYER_TEAM}
     * @return a view exposing the team-member name collection
     * @throws UnsupportedOperationException if this version does not
     *         support reading the packet
     */
    @NotNull ScoreboardTeamView scoreboardTeamView(@NotNull Object packet);

    /**
     * Open a typed view over a chat-carrying packet
     * ({@link LogicalPacket#PLAYER_CHAT} or
     * {@link LogicalPacket#SYSTEM_CHAT}).
     *
     * @param packet a chat packet
     * @return a view exposing the chat content as JSON
     * @throws UnsupportedOperationException if this version does not
     *         support reading the packet
     */
    @NotNull ChatView chatView(@NotNull Object packet);
}
