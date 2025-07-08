package dev.iiahmed.disguise.vs;

import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.Entity;
import dev.iiahmed.disguise.attribute.Attribute;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("all")
public final class VS1_21_R5 extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        if (!player.isOnline()) {
            return;
        }
        final Location location = player.getLocation();
        final ServerPlayer ep = ((CraftPlayer) player).getHandle();
        ep.connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(ep.getUUID())));
        ep.connection.send(
                new ClientboundRespawnPacket(
                        ep.createCommonSpawnInfo(ep.level()),
                        ClientboundRespawnPacket.KEEP_ALL_DATA
                )
        );
        player.teleport(location);
        ep.getServer().getPlayerList().sendLevelInfo(ep, ep.level());
        ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                ep));
        ep.connection.send(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                ep));
        ep.containerMenu.sendAllDataToRemote(); // originally player.updateInventory();
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
        final Entity entity = getInfo(refreshed).getEntity();

        final ClientboundAddEntityPacket spawn;
        final Collection<AttributeInstance> attributesSet;
        try {
            final LivingEntity living = (LivingEntity) this.entityProvider.create(entity.getType(), handle.level());

            for (final Map.Entry<Attribute, Double> entry : entity.getAttributes().entrySet()) {
                final String name = entry.getKey().getKey();
                final Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder = CraftRegistry.getMinecraftRegistry(Registries.ATTRIBUTE).wrapAsHolder(
                        CraftRegistry.getMinecraftRegistry(Registries.ATTRIBUTE).get(ResourceLocation.parse(name)).get().value()
                );
                living.getAttribute(holder).setBaseValue(entry.getValue());
            }

            attributesSet = living.getAttributes().getAttributesToUpdate();
            spawn = new ClientboundAddEntityPacket(
                    handle.getId(),
                    living.getUUID(),
                    handle.getX(),
                    handle.getY(),
                    handle.getZ(),
                    handle.getXRot(),
                    handle.getYRot(),
                    living.getType(),
                    0,
                    handle.getDeltaMovement(),
                    handle.getYHeadRot()
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final ClientboundTeleportEntityPacket tp = new ClientboundTeleportEntityPacket(
                handle.getId(),
                PositionMoveRotation.of(handle),
                Collections.emptySet(),
                handle.onGround
        );
        final ClientboundUpdateAttributesPacket attributes = new ClientboundUpdateAttributesPacket(refreshed.getEntityId(), attributesSet);

        final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<Packet<? super ClientGamePacketListener>>() {
            {
                if (remove) {
                    add(new ClientboundRemoveEntitiesPacket(refreshed.getEntityId()));
                }
                add(spawn);
                add(tp);
                add(attributes);
            }
        };

        final ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        for (final Player player : targets) {
            if (player == refreshed) continue;
            final ServerPlayer ep = ((CraftPlayer) player).getHandle();
            ep.connection.send(bundlePacket);
        }
    }

}