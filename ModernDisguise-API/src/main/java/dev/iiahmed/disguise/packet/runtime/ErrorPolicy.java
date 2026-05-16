package dev.iiahmed.disguise.packet.runtime;

import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketRewriter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy invoked when a {@link PacketRewriter} throws.
 *
 * <p>The pipeline is fail-soft: a throwing rewriter never breaks the
 * Netty channel, and the packet continues through the remaining
 * rewriters unchanged by the offending one. This policy decides what
 * (if anything) gets logged or otherwise observed.</p>
 *
 * <p>Implementations must be safe to call from a Netty I/O thread, must
 * not block, and must not throw. The pipeline guards itself against
 * a misbehaving policy but a quiet, non-blocking implementation is
 * still required for correctness.</p>
 */
@FunctionalInterface
public interface ErrorPolicy {

    /**
     * Notification that a rewriter threw.
     *
     * @param thrown   the exception thrown by the rewriter
     * @param rewriter the offending rewriter
     * @param type     the logical packet being rewritten when the
     *                 exception was thrown
     */
    void onException(@NotNull Throwable thrown,
                     @NotNull PacketRewriter rewriter,
                     @NotNull LogicalPacket type);

    /** Discard the exception silently. */
    static @NotNull ErrorPolicy ignore() {
        return (t, r, p) -> { };
    }

    /**
     * Log the first occurrence of each (rewriter class, exception
     * class) pair at {@code SEVERE}; suppress further occurrences.
     *
     * <p>Suitable as a default in production: surfaces bugs without
     * flooding logs when a rewriter throws on every packet.</p>
     */
    static @NotNull ErrorPolicy logOnce(@NotNull final Logger logger) {
        final Set<String> seen = ConcurrentHashMap.newKeySet();
        return (thrown, rewriter, type) -> {
            final String key = rewriter.getClass().getName() + ':' + thrown.getClass().getName();
            if (seen.add(key)) {
                logger.log(Level.SEVERE,
                        "[ModernDisguise] Rewriter " + rewriter.getClass().getName()
                                + " threw on " + type + "; further occurrences suppressed",
                        thrown);
            }
        };
    }
}
