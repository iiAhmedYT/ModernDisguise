package dev.iiahmed.mvs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.iiahmed.disguise.*;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class MVS1_12_R1 extends DisguiseProvider {

    @Override
    public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise) {
        if (plugin == null || !plugin.isEnabled()) {
            return DisguiseResponse.FAIL_PLUGIN_NOT_INITIALIZED;
        }

        if (isDisguised(player)) {
            return DisguiseResponse.FAIL_ALREADY_DISGUISED;
        }

        if (disguise.isEmpty()) {
            return DisguiseResponse.FAIL_EMPTY_DISGUISE;
        }

        final String realname = player.getName();

        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile profile = craftPlayer.getProfile();

        if (Bukkit.getPlayer(disguise.getName()) != null) {
            return DisguiseResponse.FAIL_NAME_ALREADY_ONLINE;
        }

        if (disguise.hasName() && !Objects.equals(disguise.getName(), player.getName())) {
            String name = disguise.getName();

            if (name.length() > 16) {
                name = name.substring(0, 16);
            }

            try {
                nameField.set(profile, name);
            } catch (IllegalAccessException e) {
                return DisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        String oldTextures = null, oldSignature = null;

        if (disguise.hasSkin()) {
            Optional<Property> optional = profile.getProperties().get("textures").stream().findFirst();

            if (optional.isPresent()) {
                oldTextures = optional.get().getValue();
                oldSignature = optional.get().getSignature();
                profile.getProperties().removeAll("textures");
            }

            profile.getProperties().put("textures", new Property("textures", disguise.getTextures(), disguise.getSignature()));
        }

        playerInfo.put(player.getUniqueId(), new PlayerInfo(realname, disguise.hasName() ? disguise.getName() : realname, oldTextures, oldSignature));
        refreshPlayer(player);

        return DisguiseResponse.SUCCESS;
    }

    @Override
    public @NotNull UndisguiseResponse unDisguise(@NotNull Player player) {
        if (!isDisguised(player)) {
            return UndisguiseResponse.FAIL_ALREADY_UNDISGUISED;
        }

        if (!player.isOnline()) {
            playerInfo.remove(player.getUniqueId());
            return UndisguiseResponse.SUCCESS;
        }

        PlayerInfo info = playerInfo.get(player.getUniqueId());

        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile profile = craftPlayer.getProfile();

        if (!Objects.equals(info.getName(), player.getName())) {
            String name = info.getName();
            try {
                nameField.set(profile, name);
            } catch (IllegalAccessException e) {
                return UndisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", info.getTextures(), info.getSignature()));

        playerInfo.remove(player.getUniqueId());

        refreshPlayer(player);
        return UndisguiseResponse.SUCCESS;
    }

    @Override
    public void refreshPlayer(Player player) {
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
            serverPlayer.hidePlayer(plugin, player);
            serverPlayer.showPlayer(plugin, player);
        }
    }

}
