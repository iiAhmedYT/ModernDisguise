package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class DisguiseProvider {

    protected final HashMap<UUID, PlayerInfo> playerInfo = new HashMap<>();
    protected Plugin plugin;

    /**
     * @param player   is the disguising player
     * @param disguise the disguise that the player should use
     * @return the response of the disguise action (like reasons of failure or so)
     */
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

        if (disguise.hasEntity() && DisguiseUtil.getEntity(disguise.getEntityType()) == null) {
            return DisguiseResponse.FAIL_ENTITY_UNSUPPORTED;
        }

        final String realname = player.getName();
        GameProfile profile = DisguiseUtil.getProfile(player);
        if (profile == null) {
            return DisguiseResponse.FAIL_PROFILE_NOT_FOUND;
        }

        if (Bukkit.getPlayer(disguise.getName()) != null) {
            return DisguiseResponse.FAIL_NAME_ALREADY_ONLINE;
        }

        if (disguise.hasName() && !Objects.equals(disguise.getName(), player.getName())) {
            String name = disguise.getName();

            if (name.length() > 16) {
                name = name.substring(0, 16);
            }

            try {
                DisguiseUtil.PROFILE_NAME.set(profile, name);
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

        playerInfo.put(player.getUniqueId(), new PlayerInfo(realname,
                disguise.hasName()? disguise.getName():realname,
                oldTextures, oldSignature, disguise.getEntityType()));
        if(disguise.hasName() || disguise.hasSkin()) {
            refreshPlayer(player);
        }
        if(disguise.hasEntity()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refreshEntity(player, p);
            }
        }
        return DisguiseResponse.SUCCESS;
    }

    /**
     * @param player is the undisguising player
     * @return the response of the undisguise action (like reasons of failure or so)
     */
    public @NotNull UndisguiseResponse unDisguise(@NotNull Player player) {
        if (!isDisguised(player)) {
            return UndisguiseResponse.FAIL_ALREADY_UNDISGUISED;
        }

        if (!player.isOnline()) {
            playerInfo.remove(player.getUniqueId());
            return UndisguiseResponse.SUCCESS;
        }

        PlayerInfo info = playerInfo.get(player.getUniqueId());

        GameProfile profile = DisguiseUtil.getProfile(player);
        if (profile == null) {
            return UndisguiseResponse.FAIL_PROFILE_NOT_FOUND;
        }
        if (!Objects.equals(info.getName(), player.getName())) {
            String name = info.getName();
            try {
                DisguiseUtil.PROFILE_NAME.set(profile, name);
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

    /**
     * @param player is the player being checked
     * @return true if the player is disguised, false if the player is not.
     */
    public boolean isDisguised(@NotNull Player player) {
        return playerInfo.containsKey(player.getUniqueId());
    }

    /**
     * @param player is the player you're grabbing info about
     * @return null if not disguised
     */
    public @Nullable PlayerInfo getInfo(@NotNull Player player) {
        return playerInfo.get(player.getUniqueId());
    }

    /**
     * This sends packets to players to show changes like name and skin
     *
     * @param player is the refreshed player
     */
    abstract public void refreshPlayer(Player player);

    /**
     * @param plugin the neeeded plugin to register listeners / hide players
     * @deprecated see DisguiseManager#setPlugin
     */
    @SuppressWarnings("all")
    @Deprecated
    public void setPlugin(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * returns the plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @param refreshed the needed player to be refreshed
     * @param target the needed player to receive packets
     */
    abstract public void refreshEntity(Player refreshed, Player target);

}
