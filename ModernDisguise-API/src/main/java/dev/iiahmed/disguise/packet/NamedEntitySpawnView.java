package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Typed view over {@link LogicalPacket#ADD_ENTITY} packets that carry
 * a player UUID (i.e. {@code PacketPlayOutNamedEntitySpawn} on
 * 1.8 – 1.16, {@code ClientboundAddPlayerPacket} / {@code ClientboundAddEntityPacket}
 * on later versions).
 *
 * <p>No built-in rewriter consumes this view yet — the visible
 * appearance of a spawning player is driven by the tab-list entry
 * (rewritten by {@link dev.iiahmed.disguise.packet.rewriter.PlayerInfoNickRewriter}),
 * so the spawn packet itself needs no rewriting for nick disguises.
 * The view exists so plugins can implement extras like UUID spoofing
 * without re-implementing the reflection plumbing.</p>
 */
public interface NamedEntitySpawnView {

    /** @return the spawning entity's UUID */
    @NotNull UUID uuid();

    /**
     * Replace the spawning entity's UUID.
     *
     * <p>Mutates the underlying packet on legacy versions; a rewriter
     * that calls this must return
     * {@link RewriteResult#replace(Object) replace(packet)}.</p>
     */
    void setUuid(@NotNull UUID uuid);
}
