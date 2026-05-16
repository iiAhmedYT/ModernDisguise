package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;

/**
 * A pluggable rewriter for a single {@link LogicalPacket}.
 *
 * <p>Multiple rewriters may target the same logical packet; the
 * pipeline runs them in the order implied by {@link #phase()} (no
 * ordering guarantees within a phase). Rewriters in the same phase must
 * therefore be independent.</p>
 *
 * <p>Contract:</p>
 * <ul>
 *   <li>Implementations must be stateless. All per-call state lives on
 *       the supplied {@link RewriteContext}; all cross-call state lives
 *       on the {@link DisguiseRegistry}.</li>
 *   <li>Implementations are invoked on a Netty I/O thread. No blocking,
 *       no Bukkit API access, no logging on the hot path.</li>
 *   <li>Implementations must not mutate the input packet. Allocate a
 *       replacement via the {@link PacketAccessor} and return it via
 *       {@link RewriteResult#replace(Object)}.</li>
 * </ul>
 */
public interface PacketRewriter {

    /** The logical packet type this rewriter targets. */
    @NotNull LogicalPacket type();

    /**
     * The phase this rewriter runs in. Determines ordering relative to
     * other rewriters acting on the same packet.
     */
    @NotNull RewritePhase phase();

    /**
     * Rewrite (or pass through, expand, or drop) an outbound packet.
     *
     * @param packet  the packet, of the NMS class associated with
     *                {@link #type()} on the current server version
     * @param context per-call context: recipient, accessor, registry
     * @return the rewriter's decision
     */
    @NotNull RewriteResult rewrite(@NotNull Object packet, @NotNull RewriteContext context);
}
