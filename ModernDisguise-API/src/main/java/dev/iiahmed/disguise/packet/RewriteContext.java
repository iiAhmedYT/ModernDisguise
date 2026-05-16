package dev.iiahmed.disguise.packet;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Per-call context passed to
 * {@link PacketRewriter#rewrite(Object, RewriteContext)}.
 *
 * <p>Bundles the recipient, the version-specific {@link PacketAccessor},
 * and a snapshot of the {@link DisguiseRegistry}. Instances are
 * short-lived — one per outbound packet — and must not be stored or
 * shared across rewriter invocations.</p>
 */
public interface RewriteContext {

    /** The player the packet is being sent to. */
    @NotNull Player recipient();

    /** Version-specific packet field access. */
    @NotNull PacketAccessor accessor();

    /** The current set of active disguises. */
    @NotNull DisguiseRegistry registry();
}
