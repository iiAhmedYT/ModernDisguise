package dev.iiahmed.mvs;

import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public final class MVS1_10_R1 extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        if (!player.isOnline()) {
            return;
        }
        final Location location = player.getLocation();
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                ep));
        ep.playerConnection.sendPacket(new PacketPlayOutRespawn(ep.dimension, ep.getWorld().getDifficulty(),
                ep.getWorld().getWorldData().getType(), ep.playerInteractManager.getGameMode()));
        player.teleport(location);
        ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                ep));
        player.updateInventory();
        for (final Player serverPlayer : Bukkit.getOnlinePlayers()) {
            if (serverPlayer == player) continue;
            serverPlayer.hidePlayer(player);
            serverPlayer.showPlayer(player);
        }
    }


    @Override
    public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets) {
        if (!isDisguised(refreshed) || targets.length == 0 || !getInfo(refreshed).hasEntity()) {
            return;
        }
        final EntityPlayer p = ((CraftPlayer) refreshed).getHandle();
        final World world = p.getWorld();
        final EntityType type = getInfo(refreshed).getEntityType();
        final PacketPlayOutSpawnEntityLiving spawn;
        try {
            final EntityLiving entity = (EntityLiving) DisguiseUtil.getEntity(type).getDeclaredConstructor(World.class).newInstance(world);
            spawn = new PacketPlayOutSpawnEntityLiving(entity);
            final Field id = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
            id.setAccessible(true);
            id.set(spawn, refreshed.getEntityId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(refreshed.getEntityId());
        final PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(p);
        final PacketPlayOutUpdateAttributes attributes = new PacketPlayOutUpdateAttributes(refreshed.getEntityId(), p.getAttributeMap().a());
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
