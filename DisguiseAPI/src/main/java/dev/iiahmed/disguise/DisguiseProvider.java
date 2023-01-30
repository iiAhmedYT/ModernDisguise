package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public abstract class DisguiseProvider {

    protected final HashMap<UUID, PlayerInfo> playerInfo = new HashMap<>();
    protected Plugin plugin;

    /**
     * Disguises a {@link Player} with a valid {@link Disguise}
     *
     * @param player   the disguising player
     * @param disguise the disguise that the player should use
     * @return the response of the disguise action (like reasons of failure or so)
     * @see DisguiseProvider#undisguise(Player)
     */
    public @NotNull DisguiseResponse disguise(@NotNull final Player player, @NotNull final Disguise disguise) {
        if (plugin == null || !plugin.isEnabled()) {
            return DisguiseResponse.FAIL_PLUGIN_NOT_INITIALIZED;
        }

        if (disguise.isEmpty()) {
            return DisguiseResponse.FAIL_EMPTY_DISGUISE;
        }

        if (disguise.hasEntity() && DisguiseUtil.getEntity(disguise.getEntityType()) == null) {
            return DisguiseResponse.FAIL_ENTITY_NOT_SUPPORTED;
        }

        String oldName = player.getName();
        final GameProfile profile = DisguiseUtil.getProfile(player);
        if (!player.isOnline() || profile == null) {
            return DisguiseResponse.FAIL_PROFILE_NOT_FOUND;
        }

        if (disguise.hasName() && !disguise.getName().equals(player.getName())) {
            String name = disguise.getName();
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }

            if (DisguiseUtil.isPlayerOnline(name)) {
                return DisguiseResponse.FAIL_NAME_ALREADY_ONLINE;
            }

            try {
                DisguiseUtil.PROFILE_NAME.set(profile, name);
                DisguiseUtil.register(name);
            } catch (IllegalAccessException e) {
                return DisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        String oldTextures = null, oldSignature = null;
        if (disguise.hasSkin()) {
            final Optional<Property> optional = profile.getProperties().get("textures").stream().findFirst();
            if (optional.isPresent()) {
                oldTextures = optional.get().getValue();
                oldSignature = optional.get().getSignature();
                profile.getProperties().removeAll("textures");
            }
            profile.getProperties().put("textures", new Property("textures", disguise.getTextures(), disguise.getSignature()));
        }

        if (isDisguised(player)) {
            final PlayerInfo info = playerInfo.remove(player.getUniqueId());
            if (info.hasName()) {
                DisguiseUtil.unregister(info.getNickname());
            }
            if (info.hasSkin()) {
                oldTextures = info.getTextures();
                oldSignature = info.getSignature();
            }
            oldName = info.getName();
        }
        DisguiseUtil.register(oldName);
        playerInfo.put(player.getUniqueId(), new PlayerInfo(oldName, disguise.getName(),
                new Skin(oldTextures, oldSignature), disguise.getEntityType()));

        if (disguise.hasName() || disguise.hasSkin()) {
            final boolean flying = player.isFlying();
            refreshAsPlayer(player);
            player.teleport(player.getLocation());
            player.setFlying(flying);
        }

        if (disguise.hasEntity()) {
            refreshAsEntity(player, true, player.getWorld().getPlayers().toArray(new Player[0]));
        }

        return DisguiseResponse.SUCCESS;
    }

    /**
     * Unisguises a disguised {@link Player}
     *
     * @param player the undisguising {@link Player}
     * @return the response of the undisguise action (like reasons of failure or so)
     * @see DisguiseProvider#disguise(Player, Disguise)
     */
    public @NotNull UndisguiseResponse undisguise(@NotNull final Player player) {
        if (!isDisguised(player)) {
            return UndisguiseResponse.FAIL_ALREADY_UNDISGUISED;
        }

        if (!player.isOnline()) {
            final PlayerInfo info = playerInfo.remove(player.getUniqueId());
            DisguiseUtil.unregister(info.getName());
            DisguiseUtil.unregister(info.getNickname());
            return UndisguiseResponse.SUCCESS;
        }

        final GameProfile profile = DisguiseUtil.getProfile(player);
        if (profile == null) {
            return UndisguiseResponse.FAIL_PROFILE_NOT_FOUND;
        }

        final PlayerInfo info = playerInfo.get(player.getUniqueId());

        if (info.hasName()) {
            try {
                DisguiseUtil.PROFILE_NAME.set(profile, info.getName());
                DisguiseUtil.unregister(info.getNickname());
            } catch (IllegalAccessException e) {
                return UndisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        if (info.hasSkin()) {
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", info.getTextures(), info.getSignature()));
        }

        playerInfo.remove(player.getUniqueId());
        refreshAsPlayer(player);
        player.teleport(player.getLocation());

        return UndisguiseResponse.SUCCESS;
    }

    /**
     * @param player the {@link Player} being checked
     * @return true if the {@link Player} is disguised, false if the {@link Player} is not.
     */
    public boolean isDisguised(@NotNull final Player player) {
        return playerInfo.containsKey(player.getUniqueId());
    }

    /**
     * @param player the {@link Player} being checked
     * @return true if the {@link Player} is disguised as an entity, false if the {@link Player} is not.
     */
    public boolean isDisguisedAsEntity(@NotNull final Player player) {
        return playerInfo.containsKey(player.getUniqueId()) && getInfo(player).hasEntity();
    }

    /**
     * @param player the {@link Player} you're grabbing info about
     * @return the known info about a {@link Player}
     */
    public @NotNull PlayerInfo getInfo(@NotNull final Player player) {
        if (playerInfo.containsKey(player.getUniqueId())) {
            return playerInfo.get(player.getUniqueId());
        }
        return new PlayerInfo(player.getName(), null, null, null);
    }

    /**
     * This sends packets to {@link Player}s to show changes like name and skin
     *
     * @param player the refreshed {@link Player}
     */
    abstract public void refreshAsPlayer(@NotNull final Player player);

    /**
     * @param refreshed the refreshed {@link Player}
     * @param targets   the needed {@link Player}s to receive packets
     */
    abstract public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets);

    /**
     * @return the plugin used plugin to register listeners & refresh {@link Player}s
     */
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * @param plugin the needed plugin to register listeners & refresh {@link Player}s
     * @deprecated see {@link DisguiseManager#setPlugin}
     */
    @SuppressWarnings("all")
    @Deprecated
    protected void setPlugin(@NotNull final Plugin plugin) {
        this.plugin = plugin;
    }

}
