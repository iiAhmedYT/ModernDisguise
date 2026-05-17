package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Typed view over {@link LogicalPacket#SET_PLAYER_TEAM} packets.
 *
 * <p>Scoreboard team packets reference team members by their player
 * name (not UUID), so when a disguise replaces a player's visible
 * name the team membership references must be rewritten too. This
 * view exposes the player-name collection for the rewriter to
 * substitute real names with nicknames.</p>
 *
 * <p>The collection is empty when the packet's action does not
 * involve player membership (action = update info / remove team).
 * Rewriters that find an empty collection should leave the packet
 * unchanged.</p>
 */
public interface ScoreboardTeamView {

    /**
     * @return the team-member name collection, or an empty collection
     *         when this packet's action does not carry membership
     */
    @NotNull Collection<String> players();

    /**
     * Replace the team-member name collection.
     *
     * <p>Mutates the underlying packet on legacy versions; a rewriter
     * that calls this must return
     * {@link RewriteResult#replace(Object) replace(packet)}.</p>
     */
    void setPlayers(@NotNull Collection<String> players);
}
