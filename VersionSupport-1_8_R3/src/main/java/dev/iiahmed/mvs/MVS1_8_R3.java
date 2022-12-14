package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Objects;

public class MVS1_8_R3 extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        Location location = player.getLocation();
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        // synchronizing this process, other tasks can be async just fine
        Bukkit.getScheduler().runTask(plugin, () -> {
            ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    ep));
            ep.playerConnection.sendPacket(new PacketPlayOutRespawn(ep.dimension, ep.getWorld().getDifficulty(),
                    ep.getWorld().getWorldData().getType(), ep.playerInteractManager.getGameMode()));
            player.teleport(location);
            ep.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                    ep));
        });
        for (Player serverPlayer : Bukkit.getOnlinePlayers()) {
            if (serverPlayer == player) continue;
            serverPlayer.hidePlayer(player);
            serverPlayer.showPlayer(player);
        }
    }

    @Override
    public void refreshAsEntity(Player refreshed, Player target) {
        if (!isDisguised(refreshed)) {
            return;
        }
        EntityPlayer ep = ((CraftPlayer) target).getHandle();
        EntityPlayer rfep = ((CraftPlayer) refreshed).getHandle();
        EntityType type = Objects.requireNonNull(getInfo(refreshed)).getEntityType();
        PacketPlayOutSpawnEntityLiving spawn;
        try {
            EntityLiving entity = (EntityLiving) DisguiseUtil.getEntity(type).getDeclaredConstructor(World.class).newInstance(rfep.getWorld());
            spawn = new PacketPlayOutSpawnEntityLiving(entity);
            Field id = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
            id.setAccessible(true);
            id.set(spawn, refreshed.getEntityId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(refreshed.getEntityId());
        ep.playerConnection.sendPacket(destroy);
        ep.playerConnection.sendPacket(spawn);
    }

}
