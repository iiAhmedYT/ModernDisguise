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

public class PacketListener extends ChannelDuplexHandler {

    private static final Field PLAYER_ID;

    static {
        try {
            final Class<?> namedEntitySpawn = Class.forName((DisguiseUtil.INT_VER >= 17 ?
                    "net.minecraft.network.protocol.game." : DisguiseUtil.PREFIX)
                    + "PacketPlayOutNamedEntitySpawn");
            PLAYER_ID = namedEntitySpawn.getDeclaredField("b");
        } catch (Exception e) {
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
        if (packet == null || !"PacketPlayOutNamedEntitySpawn".equals(packet.getClass().getSimpleName())) {
            super.write(context, packet, promise);
            return;
        }

        UUID playerID;
        try {
            playerID = (UUID) PLAYER_ID.get(packet);
        } catch (Exception exception) {
            exception.printStackTrace();
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
