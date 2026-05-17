package dev.iiahmed.disguise.packet;

import dev.iiahmed.disguise.PlayerInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Read-only view of the active disguises, consulted by rewriters at
 * packet-rewrite time.
 *
 * <p>Mutations happen elsewhere (typically on the main server thread,
 * driven by the public disguise API). Implementations expose immutable
 * snapshots so that reads from Netty I/O threads are lock-free.
 * Consequently:</p>
 *
 * <ul>
 *   <li>A single lookup is internally consistent.</li>
 *   <li>Two consecutive lookups on different keys may observe state
 *       from different snapshots; rewriters that need a coherent view
 *       across multiple players in one packet should perform their
 *       lookups together at the top of the rewrite and reuse the
 *       results.</li>
 * </ul>
 *
 * <p>The registry returns {@link PlayerInfo} (the resolved active
 * state — name, skin, entity) rather than {@link dev.iiahmed.disguise.Disguise}
 * (the request that produced it), because rewriters need to know what
 * is currently active, not what was asked for.</p>
 */
public interface DisguiseRegistry {

    /**
     * @param realId the player's real {@link UUID}
     * @return the active disguise info for that player, or {@code null}
     *         if the player is not currently disguised
     */
    @Nullable PlayerInfo getInfo(@NotNull UUID realId);

    /**
     * Convenience overload.
     *
     * @param player the player to query
     * @return the active disguise info for that player, or {@code null}
     *         if the player is not currently disguised
     */
    @Nullable default PlayerInfo getInfo(@NotNull final Player player) {
        return getInfo(player.getUniqueId());
    }

    /**
     * Snapshot enumeration of every currently disguised player's
     * {@link PlayerInfo}, used by rewriters that operate on
     * cross-player state (chat name replacement, scoreboard team
     * membership rewriting).
     *
     * <p>Implementations expose a thread-safe view — most commonly the
     * values of a {@code ConcurrentHashMap}, which iterates safely from
     * the Netty I/O thread with weakly-consistent semantics.</p>
     *
     * <p>Returns an empty collection when no one is disguised.</p>
     */
    @NotNull Collection<PlayerInfo> activeDisguises();
}
