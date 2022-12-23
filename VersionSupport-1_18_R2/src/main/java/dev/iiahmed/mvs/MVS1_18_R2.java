package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Objects;

public class MVS1_18_R2 extends DisguiseProvider {

    @Override
    public void refreshPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        Location location = player.getLocation();
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        final long seed = player.getWorld().getSeed();
        ServerPlayer ep = ((CraftPlayer) player).getHandle();
        // synchronizing this process, other tasks can be async just fine
        Bukkit.getScheduler().runTask(plugin, () -> {
            ep.connection.send(new ClientboundPlayerInfoPacket(
                    ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER,
                    ep));
            ep.connection.send(new ClientboundRespawnPacket(ep.getLevel().dimensionTypeRegistration(),
                    ep.getLevel().dimension(),
                    seed, ep.gameMode.getGameModeForPlayer(),
                    ep.gameMode.getGameModeForPlayer(), false, false, true));
            player.teleport(location);
            ep.connection.send(new ClientboundPlayerInfoPacket(
                    ClientboundPlayerInfoPacket.Action.ADD_PLAYER,
                    ep));
        });
        for (Player serverPlayer : Bukkit.getOnlinePlayers()) {
            if (serverPlayer == player) continue;
            serverPlayer.hidePlayer(plugin, player);
            serverPlayer.showPlayer(plugin, player);
        }
    }

    @Override
    public void refreshEntity(Player refreshed, Player target) {
        if (!isDisguised(refreshed)) {
            return;
        }
        ServerPlayer ep = ((CraftPlayer) target).getHandle();
        ServerPlayer rfep = ((CraftPlayer) refreshed).getHandle();
        EntityType type = Objects.requireNonNull(getInfo(refreshed)).getEntityType();
        ClientboundAddEntityPacket spawn;
        try {
            Entity entity = (Entity) DisguiseUtil.getEntity(type).getDeclaredConstructor(Level.class).newInstance(rfep.getLevel());
            spawn = new ClientboundAddEntityPacket(entity);
            Field id = ClientboundAddEntityPacket.class.getDeclaredField("a");
            id.setAccessible(true);
            id.set(spawn, refreshed.getEntityId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(refreshed.getEntityId());
        ep.connection.send(destroy);
        ep.connection.send(spawn);
    }

}
