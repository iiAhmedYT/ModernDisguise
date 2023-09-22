package dev.iiahmed.disguise.vs;

import com.mojang.authlib.properties.Property;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import dev.iiahmed.disguise.Skin;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;

@SuppressWarnings("all")
public final class VS1_20_R2 extends DisguiseProvider {

    private final Field id;

    {
        try {
            id = ClientboundAddEntityPacket.class.getDeclaredField("c");
            id.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        if (!player.isOnline()) {
            return;
        }
        final Location location = player.getLocation();
        final ServerPlayer ep = ((CraftPlayer) player).getHandle();
        ep.connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(ep.getUUID())));
        ep.connection.send(new ClientboundRespawnPacket(ep.createCommonSpawnInfo(ep.serverLevel()), ClientboundRespawnPacket.KEEP_ALL_DATA));
        player.teleport(location);
        ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                ep));
        ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                ep));
        player.updateInventory();
        for (final Player serverPlayer : Bukkit.getOnlinePlayers()) {
            serverPlayer.hidePlayer(plugin, player);
            serverPlayer.showPlayer(plugin, player);
        }
    }

    @Override
    public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets) {
        if (!isDisguised(refreshed) || targets.length == 0 || !getInfo(refreshed).hasEntity()) {
            return;
        }
        final ServerPlayer rfep = ((CraftPlayer) refreshed).getHandle();
        final org.bukkit.entity.EntityType type = getInfo(refreshed).getEntityType();
        final ClientboundAddEntityPacket spawn;
        try {
            final Entity entity = (Entity) DisguiseUtil.createEntity(type, rfep.level());
            spawn = new ClientboundAddEntityPacket(entity);
            id.set(spawn, refreshed.getEntityId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(refreshed.getEntityId());
        final ClientboundTeleportEntityPacket tp = new ClientboundTeleportEntityPacket(rfep);
        final ClientboundUpdateAttributesPacket attributes = new ClientboundUpdateAttributesPacket(refreshed.getEntityId(), rfep.getAttributes().getDirtyAttributes());
        for (final Player player : targets) {
            if (player == refreshed) continue;
            final ServerPlayer ep = ((CraftPlayer) player).getHandle();
            if (remove) {
                ep.connection.send(destroy);
            }
            ep.connection.send(spawn);
            ep.connection.send(tp);
            ep.connection.send(attributes);
        }
    }

    @Override
    public Skin getSkin(Property property) {
        return new Skin(property.value(), property.signature());
    }

}
