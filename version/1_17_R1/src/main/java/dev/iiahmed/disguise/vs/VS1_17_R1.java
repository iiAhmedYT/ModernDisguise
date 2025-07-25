package dev.iiahmed.disguise.vs;

import dev.iiahmed.disguise.DisguiseProvider;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class VS1_17_R1 extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        if (!player.isOnline()) {
            return;
        }
        final Location location = player.getLocation();
        final ServerPlayer ep = ((CraftPlayer) player).getHandle();
        ep.connection.send(new ClientboundPlayerInfoPacket(
                ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER,
                ep));
        ep.connection.send(
                new ClientboundRespawnPacket(
                        ep.level.dimensionType(),
                        ep.level.dimension(),
                        player.getWorld().getSeed(),
                        ep.gameMode.getGameModeForPlayer(),
                        ep.gameMode.getGameModeForPlayer(),
                        false, false, true
                )
        );
        player.teleport(location);
        ep.connection.send(new ClientboundPlayerInfoPacket(
                ClientboundPlayerInfoPacket.Action.ADD_PLAYER,
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
        final ServerPlayer handle = ((CraftPlayer) refreshed).getHandle();
        final org.bukkit.entity.EntityType type = getInfo(refreshed).getEntityType();
        final ClientboundAddEntityPacket spawn;
        final Collection<AttributeInstance> attributesSet;
        try {
            final LivingEntity entity = (LivingEntity) this.entityProvider.create(type, handle.getLevel());
            attributesSet = entity.getAttributes().getDirtyAttributes();
            spawn = new ClientboundAddEntityPacket(
                    handle.getId(),
                    entity.getUUID(),
                    handle.getX(),
                    handle.getY(),
                    handle.getZ(),
                    handle.getXRot(),
                    handle.getYRot(),
                    entity.getType(),
                    0,
                    handle.getDeltaMovement()
            );
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't change entityID for " + refreshed.getName(), e);
        }
        final ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(refreshed.getEntityId());
        final ClientboundTeleportEntityPacket tp = new ClientboundTeleportEntityPacket(handle);
        final ClientboundUpdateAttributesPacket attributes = new ClientboundUpdateAttributesPacket(refreshed.getEntityId(), attributesSet);
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

}
