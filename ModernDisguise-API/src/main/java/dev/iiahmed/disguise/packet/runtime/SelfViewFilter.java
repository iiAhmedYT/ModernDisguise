package dev.iiahmed.disguise.packet.runtime;

import dev.iiahmed.disguise.packet.PacketRewriter;
import dev.iiahmed.disguise.packet.RewriteContext;
import org.jetbrains.annotations.NotNull;

/**
 * Predicate consulted by the pipeline before each rewriter call.
 *
 * <p>Implementations can suppress rewriters in scenarios where the
 * default behaviour would be wrong — most commonly when a disguised
 * player is the recipient of a packet about themselves ("self-view")
 * and their own client must continue to see their real identity.</p>
 *
 * <p>The pipeline does not know which player a packet is about — that
 * is the rewriter's domain knowledge. Per-rewriter self-view policy
 * therefore typically lives inside the rewriter; this filter is the
 * cross-cutting hook for policy that spans multiple rewriters
 * (dev-mode toggles, per-rewriter kill switches, A/B rollouts).</p>
 */
@FunctionalInterface
public interface SelfViewFilter {

    /** Default: every rewriter runs for every packet. */
    SelfViewFilter ALWAYS = (rewriter, context) -> true;

    /**
     * @return {@code true} to run the rewriter, {@code false} to skip
     *         it (the packet continues through the rest of the chain
     *         unchanged from this rewriter's perspective)
     */
    boolean shouldRun(@NotNull PacketRewriter rewriter,
                      @NotNull RewriteContext context);
}
