package dev.iiahmed.disguise.packet.rewriter;

import dev.iiahmed.disguise.PlayerInfo;
import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketRewriter;
import dev.iiahmed.disguise.packet.RewriteContext;
import dev.iiahmed.disguise.packet.RewritePhase;
import dev.iiahmed.disguise.packet.RewriteResult;
import dev.iiahmed.disguise.packet.ScoreboardTeamView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites {@link LogicalPacket#SET_PLAYER_TEAM} packets so that
 * disguised players are referenced by their nickname in scoreboard
 * team membership instead of their real player name.
 *
 * <p>Bukkit's scoreboard API identifies team members by player name,
 * so when the server adds a disguised player to a team (whether or
 * not the rewriter system mutates {@code player.getName()}) the
 * outbound packet may carry the real name. This rewriter substitutes
 * each real name in the packet's player collection with the
 * corresponding disguise nickname.</p>
 *
 * <p>Runs in {@link RewritePhase#RELATIONS}: it depends on identities
 * established by earlier rewriters (PlayerInfo) and edits references
 * to those identities elsewhere in the protocol.</p>
 *
 * <p>The packet's action determines whether the player collection
 * carries members (create / add-players / remove-players) or is empty
 * (update-info / remove-team). The rewriter short-circuits on an
 * empty collection.</p>
 */
public final class ScoreboardTeamNickRewriter implements PacketRewriter {

    @Override
    public @NotNull LogicalPacket type() {
        return LogicalPacket.SET_PLAYER_TEAM;
    }

    @Override
    public @NotNull RewritePhase phase() {
        return RewritePhase.RELATIONS;
    }

    @Override
    public @NotNull RewriteResult rewrite(@NotNull final Object packet,
                                          @NotNull final RewriteContext context) {
        final ScoreboardTeamView view = context.accessor().scoreboardTeamView(packet);
        final Collection<String> players = view.players();
        if (players.isEmpty()) {
            return RewriteResult.pass();
        }

        final Collection<PlayerInfo> disguises = context.registry().activeDisguises();
        if (disguises.isEmpty()) {
            return RewriteResult.pass();
        }

        final Map<String, String> realToNick = buildMapping(disguises);
        if (realToNick.isEmpty() || Collections.disjoint(players, realToNick.keySet())) {
            return RewriteResult.pass();
        }

        final List<String> rewritten = new ArrayList<>(players.size());
        for (final String name : players) {
            final String nick = realToNick.get(name);
            rewritten.add(nick != null ? nick : name);
        }
        view.setPlayers(rewritten);
        return RewriteResult.replace(packet);
    }

    private static @NotNull Map<String, String> buildMapping(@NotNull final Collection<PlayerInfo> disguises) {
        final Map<String, String> map = new HashMap<>(disguises.size() * 2);
        for (final PlayerInfo info : disguises) {
            if (info.hasName()) {
                map.put(info.getName(), info.getNickname());
            }
        }
        return map;
    }
}
