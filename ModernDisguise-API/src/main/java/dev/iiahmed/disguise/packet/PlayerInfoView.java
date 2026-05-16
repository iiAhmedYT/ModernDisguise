package dev.iiahmed.disguise.packet;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Typed view over {@link LogicalPacket#PLAYER_INFO_UPDATE} packets.
 *
 * <p>Returned by
 * {@link PacketAccessor#playerInfoView(Object)}. Implementations are
 * version-specific; the interface they expose to rewriters is not.</p>
 *
 * <p>On pre-1.19.3 versions the wire format is a single
 * {@code PacketPlayOutPlayerInfo} whose action field discriminates add
 * / update / remove operations; this view surfaces that as {@link #action()}
 * so rewriters that care about it can branch.</p>
 *
 * <p>The list returned by {@link #entries()} is a list of view objects
 * over the packet's internal entry list; modifications to individual
 * entries (via {@link Entry#setProfile(GameProfile)}) are reflected in
 * the underlying packet, but the list itself is not mutable — adding,
 * removing, or reordering entries is out of scope for this initial
 * view and will require a future {@code withEntries} method.</p>
 */
public interface PlayerInfoView {

    /** Action conveyed by the packet. */
    enum Action {

        /** Add a new tab-list entry. */
        ADD_PLAYER,

        /** Update an existing entry's gamemode. */
        UPDATE_GAMEMODE,

        /** Update an existing entry's latency / ping. */
        UPDATE_LATENCY,

        /** Update an existing entry's display name. */
        UPDATE_DISPLAY_NAME,

        /** Remove entries from the tab list. */
        REMOVE_PLAYER,

        /** An action the view does not recognize. */
        OTHER
    }

    /** @return the action this packet performs */
    @NotNull Action action();

    /**
     * @return view objects wrapping the packet's contained
     *         {@code PlayerInfoData} entries, in transmission order
     */
    @NotNull List<Entry> entries();

    /** A single tab-list entry within the packet. */
    interface Entry {

        /** @return the entry's {@link GameProfile} */
        @NotNull GameProfile profile();

        /**
         * Replace this entry's {@link GameProfile}.
         *
         * <p>On versions where the underlying NMS packet has no copy
         * constructor (1.8 – 1.16 era), this mutates the packet in
         * place. A rewriter that calls this method therefore must
         * return {@link RewriteResult#replace(Object) replace(packet)} —
         * returning {@link RewriteResult#pass() pass()} after a
         * mutation is a bug.</p>
         */
        void setProfile(@NotNull GameProfile profile);
    }
}
