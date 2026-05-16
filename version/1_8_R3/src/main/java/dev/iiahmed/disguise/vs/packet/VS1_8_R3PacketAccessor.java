package dev.iiahmed.disguise.vs.packet;

import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketAccessor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link PacketAccessor} implementation for Minecraft 1.8.8 (NMS
 * {@code v1_8_R3}).
 *
 * <p>1.8 predates the bundle packet (added in 1.20.2), so
 * {@link #supportsBundles()} returns {@code false} and the unbundle /
 * bundle methods refuse to run.</p>
 *
 * <p>1.8 also predates the 1.19.3 split of
 * {@code PacketPlayOutPlayerInfo} into update / remove packets. The
 * accessor maps {@link PacketPlayOutPlayerInfo} to
 * {@link LogicalPacket#PLAYER_INFO_UPDATE} regardless of its
 * {@code action} field; rewriters inspect the action themselves.</p>
 *
 * <p>1.8's {@link PacketPlayOutChat} is used for both server-formatted
 * player chat and system messages; the {@link LogicalPacket#PLAYER_CHAT}
 * / {@link LogicalPacket#SYSTEM_CHAT} split exists only on 1.19+. We
 * map 1.8 chat to {@link LogicalPacket#SYSTEM_CHAT} on the principle
 * that the server is the producer.</p>
 *
 * <p>Lookups are O(1): a single {@link IdentityHashMap#get} keyed on
 * the packet's runtime class.</p>
 */
public final class VS1_8_R3PacketAccessor implements PacketAccessor {

    private static final Map<Class<?>, LogicalPacket> TYPES;
    static {
        final Map<Class<?>, LogicalPacket> map = new IdentityHashMap<>(8);
        map.put(PacketPlayOutPlayerInfo.class, LogicalPacket.PLAYER_INFO_UPDATE);
        map.put(PacketPlayOutNamedEntitySpawn.class, LogicalPacket.ADD_ENTITY);
        map.put(PacketPlayOutScoreboardTeam.class, LogicalPacket.SET_PLAYER_TEAM);
        map.put(PacketPlayOutChat.class, LogicalPacket.SYSTEM_CHAT);
        TYPES = map;
    }

    @Override
    public @Nullable LogicalPacket identify(@NotNull final Object packet) {
        return TYPES.get(packet.getClass());
    }

    @Override
    public boolean supportsBundles() {
        return false;
    }

    @Override
    public @NotNull List<Object> unbundle(@NotNull final Object bundle) {
        throw new UnsupportedOperationException("Bundles do not exist on 1.8");
    }

    @Override
    public @NotNull Object bundle(@NotNull final List<Object> packets) {
        throw new UnsupportedOperationException("Bundles do not exist on 1.8");
    }
}
