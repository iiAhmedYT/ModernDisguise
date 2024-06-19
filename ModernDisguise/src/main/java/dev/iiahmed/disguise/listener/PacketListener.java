package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.util.DisguiseUtil;
import dev.iiahmed.disguise.util.Version;
import dev.iiahmed.disguise.util.reflection.FieldAccessor;
import dev.iiahmed.disguise.util.reflection.Reflections;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

public final class PacketListener extends ChannelDuplexHandler {

    private static final String BUNDLE_PACKET_NAME = "ClientboundBundlePacket";
    private static final String PACKET_NAME;
    private static final FieldAccessor<UUID> PLAYER_ID;
    private static FieldAccessor<?> PACKET_LIST;

    static {
        try {
            PACKET_NAME = Version.IS_20_R2_PLUS ? "PacketPlayOutSpawnEntity" : "PacketPlayOutNamedEntitySpawn";
            final Class<?> namedEntitySpawn = Class.forName((Version.isOrOver(17) ?
                    "net.minecraft.network.protocol.game." : DisguiseUtil.PREFIX)
                    + PACKET_NAME);
            PLAYER_ID = Reflections.getField(namedEntitySpawn, UUID.class);
            if (Version.IS_20_R2_PLUS) {
                PACKET_LIST = Reflections.getField(Class.forName("net.minecraft.network.protocol.BundlePacket"), Iterable.class);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final DisguiseProvider provider = DisguiseManager.getProvider();
    private final Player player;

    public PacketListener(Player player) {
        this.player = player;
    }

    @Override
    public void write(
            final ChannelHandlerContext context,
            final Object packet,
            final ChannelPromise promise
    ) throws Exception {
        if (packet == null) {
            super.write(context, null, promise);
            return;
        }

        final String name = packet.getClass().getSimpleName();
        if (PACKET_NAME.equals(name)) {
            this.handleSpawnPacket(context, packet, packet, promise);
            return;
        } else if (BUNDLE_PACKET_NAME.equals(name)) {
            final Iterable<?> iterable = (Iterable<?>) PACKET_LIST.get(packet);
            for (final Object bundlePacket : iterable) {
                final String packetName = bundlePacket.getClass().getSimpleName();
                if (packetName.equals(PACKET_NAME)) {
                    this.handleSpawnPacket(context, bundlePacket, packet, promise);
                    return;
                }
            }
        }

        super.write(context, packet, promise);
    }

    private void handleSpawnPacket(
            final ChannelHandlerContext context,
            final Object spawnPacket,
            final Object passPacket,
            final ChannelPromise promise
    ) throws Exception {
        UUID playerID;
        try {
            playerID = PLAYER_ID.get(spawnPacket);
        } catch (final Exception exception) {
            provider.getPlugin().getLogger().log(
                    Level.SEVERE,
                    "[ModernDisguise] Couldn't get a player's UUID, please report if this ever happens to you.\n"
                            + "Version: " + Version.NMS + " (" + Version.VERSION_EXACT + ")\n"
                            + "Packet Name: " + PACKET_NAME + "\n"
                            + "This error is not supposed to happen however it is harmless & won't block any packet from being sent.",
                    exception
            );
            playerID = null;
        }

        if (playerID == null) {
            super.write(context, passPacket, promise);
            return;
        }

        final Player refreshed = Bukkit.getPlayer(playerID);
        if (refreshed != null && provider.isDisguisedAsEntity(refreshed)) {
            provider.refreshAsEntity(refreshed, false, player);
            return;
        }

        super.write(context, passPacket, promise);
    }

}
