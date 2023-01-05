package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Objects;

public class MVS1_19_R2 extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(Player player) {
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
            ep.connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(ep.getUUID())));
            ep.connection.send(new ClientboundRespawnPacket(ep.getLevel().dimensionTypeId(),
                    ep.getLevel().dimension(),
                    seed, ep.gameMode.getGameModeForPlayer(),
                    ep.gameMode.getGameModeForPlayer(), false, false, ClientboundRespawnPacket.KEEP_ALL_DATA,
                    ep.getLastDeathLocation()));
            player.teleport(location);
            ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                    ep));
            ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    ep));
        });
        for (Player serverPlayer : Bukkit.getOnlinePlayers()) {
            if (serverPlayer == player) continue;
            serverPlayer.hidePlayer(plugin, player);
            serverPlayer.showPlayer(plugin, player);
        }
    }

    @Override
    @SuppressWarnings("all")
    public void refreshAsEntity(Player refreshed, Player target) {
        if (!isDisguised(refreshed)) {
            return;
        }
        ServerPlayer ep = ((CraftPlayer) target).getHandle();
        ServerPlayer rfep = ((CraftPlayer) refreshed).getHandle();
        org.bukkit.entity.EntityType type = Objects.requireNonNull(getInfo(refreshed)).getEntityType();
        ClientboundAddEntityPacket spawn;
        try {
            Class<?> clazz = DisguiseUtil.getEntity(type);
            Entity entity;
            if (DisguiseUtil.hasConstructor(clazz, Level.class)) {
                entity = (Entity) clazz.getDeclaredConstructor(Level.class).newInstance(rfep.getLevel());
            } else {
                EntityType t = (EntityType) EntityType.class.getField(type.name()).get(null);
                entity = (Entity) clazz.getDeclaredConstructor(EntityType.class, Level.class).newInstance(t, rfep.getLevel());
            }
            spawn = new ClientboundAddEntityPacket(entity);
            Field id = ClientboundAddEntityPacket.class.getDeclaredField("c");
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
