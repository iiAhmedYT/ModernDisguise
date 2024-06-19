package dev.iiahmed.disguise.vs;

import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;

public final class VS1_16_R3 extends DisguiseProvider {

    private final Field id;

    {
        try {
            id = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
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
        final long seed = player.getWorld().getSeed();
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                ep));
        ep.playerConnection.sendPacket(
                new PacketPlayOutRespawn(
                        ep.world.getDimensionManager(),
                        ep.world.getDimensionKey(),
                        player.getWorld().getSeed(),
                        ep.playerInteractManager.getGameMode(),
                        ep.playerInteractManager.getGameMode(),
                        false, false, true
                )
        );
        player.teleport(location);
        ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
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
        final EntityPlayer p = ((CraftPlayer) refreshed).getHandle();
        final EntityType type = getInfo(refreshed).getEntityType();
        final PacketPlayOutSpawnEntityLiving spawn;
        final Collection<AttributeModifiable> attributeMapBase;
        try {
            final EntityLiving entity = (EntityLiving) DisguiseUtil.createEntity(type, p.world);
            attributeMapBase = entity.getAttributeMap().getAttributes();

            spawn = new PacketPlayOutSpawnEntityLiving(entity);
            id.set(spawn, refreshed.getEntityId());
        } catch (final Exception e) {
            throw new RuntimeException("Couldn't change entityID for " + refreshed.getName(), e);
        }
        final PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(refreshed.getEntityId());
        final PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(p);
        final PacketPlayOutUpdateAttributes attributes = new PacketPlayOutUpdateAttributes(refreshed.getEntityId(), attributeMapBase);
        for (final Player player : targets) {
            if (player == refreshed) continue;
            final EntityPlayer ep = ((CraftPlayer) player).getHandle();
            if (remove) {
                ep.playerConnection.sendPacket(destroy);
            }
            ep.playerConnection.sendPacket(spawn);
            ep.playerConnection.sendPacket(tp);
            ep.playerConnection.sendPacket(attributes);
        }
    }

}
