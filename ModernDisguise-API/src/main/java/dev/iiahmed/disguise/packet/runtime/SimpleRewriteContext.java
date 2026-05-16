package dev.iiahmed.disguise.packet.runtime;

import dev.iiahmed.disguise.packet.DisguiseRegistry;
import dev.iiahmed.disguise.packet.PacketAccessor;
import dev.iiahmed.disguise.packet.RewriteContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Plain holder implementation of {@link RewriteContext}.
 *
 * <p>One instance per connection: the recipient, accessor, and registry
 * are stable for the lifetime of the player's channel, so the pipeline
 * handler allocates one context at install time and reuses it for every
 * outbound packet. This contradicts the "short-lived" hint in the
 * {@link RewriteContext} javadoc only in the sense that the handler
 * extends its lifetime to the channel's; rewriters still must not store
 * the reference.</p>
 */
public final class SimpleRewriteContext implements RewriteContext {

    private final Player recipient;
    private final PacketAccessor accessor;
    private final DisguiseRegistry registry;

    public SimpleRewriteContext(@NotNull final Player recipient,
                                @NotNull final PacketAccessor accessor,
                                @NotNull final DisguiseRegistry registry) {
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.accessor = Objects.requireNonNull(accessor, "accessor");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public @NotNull Player recipient() {
        return recipient;
    }

    @Override
    public @NotNull PacketAccessor accessor() {
        return accessor;
    }

    @Override
    public @NotNull DisguiseRegistry registry() {
        return registry;
    }
}
