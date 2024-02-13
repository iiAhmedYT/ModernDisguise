package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public final class PacketListener extends ChannelDuplexHandler {

    private static final String PACKET_NAME;
    private static final Field PLAYER_ID;

    static {
        try {
            PACKET_NAME = DisguiseUtil.IS_20_R2_PLUS ? "PacketPlayOutSpawnEntity" : "PacketPlayOutNamedEntitySpawn";
            final Class<?> namedEntitySpawn = Class.forName((DisguiseUtil.INT_VER >= 17 ?
                    "net.minecraft.network.protocol.game." : DisguiseUtil.PREFIX)
                    + PACKET_NAME);
            PLAYER_ID = namedEntitySpawn.getDeclaredField(DisguiseUtil.IS_20_R2_PLUS ? "d" : "b");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        PLAYER_ID.setAccessible(true);
    }

    private final DisguiseProvider provider = DisguiseManager.getProvider();
    private final Player player;

    public PacketListener(Player player) {
        this.player = player;
    }

    @Override
    public void write(final ChannelHandlerContext context, final Object packet, final ChannelPromise promise) throws Exception {
        if (packet == null || !PACKET_NAME.equals(packet.getClass().getSimpleName())) {
            super.write(context, packet, promise);
            return;
        }

        UUID playerID;
        try {
            playerID = (UUID) PLAYER_ID.get(packet);
        } catch (final Exception exception) {
            provider.getPlugin().getLogger().log(
                    Level.SEVERE,
                    "[ModernDisguise] Couldn't get a player's UUID, please report if this ever happens to you.\n"
                            + "Version: " + DisguiseUtil.VERSION + " (" + DisguiseUtil.INT_VER + ")\n"
                            + "Packet Name: " + PACKET_NAME + "\n"
                            + "This error is not supposed to happen however it is harmless & won't block any packet from being sent.",
                    exception
            );
            playerID = null;
        }

        if (playerID == null) {
            super.write(context, packet, promise);
            return;
        }

        final Player refreshed = Bukkit.getPlayer(playerID);
        if (refreshed != null && provider.isDisguisedAsEntity(refreshed)) {
            provider.refreshAsEntity(refreshed, false, player);
            return;
        }

        super.write(context, packet, promise);
    }

}
