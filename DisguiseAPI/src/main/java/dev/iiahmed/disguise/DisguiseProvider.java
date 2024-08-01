package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.iiahmed.disguise.util.DisguiseUtil;
import dev.iiahmed.disguise.util.Version;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public abstract class DisguiseProvider {

    private Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");
    private boolean overrideChat = Version.isOver(18);
    private int nameLength = 16;

    private final Map<UUID, PlayerInfo> playerInfo = new ConcurrentHashMap<>();

    protected Plugin plugin;
    protected boolean entityDisguises;

    /**
     * Set the username {@link Pattern} that is
     * used to validate nicknames.
     *
     * @param pattern the name pattern
     */
    @SuppressWarnings("unused")
    public DisguiseProvider setNamePattern(@NotNull final Pattern pattern) {
        this.namePattern = pattern;
        return this;
    }

    /**
     * Set the maximum username length
     * allowed for nicknames.
     *
     * @param length the max length
     */
    @SuppressWarnings("unused")
    public DisguiseProvider setNameLength(final int length) {
        this.nameLength = length;
        return this;
    }

    /**
     * Controls the ability of ModernDisguise to modify the chat behavior for servers
     * utilizing the Mojang Report feature, enabling disguised players to interact with chat.
     * This setting is designed to be compatible with existing chat plugins and should not
     * disrupt their functionality.
     *
     * @param overrideChat A boolean flag to allow or disallow the chat system override.
     * @see DisguiseProvider#shouldOverrideChat()
     */
    @SuppressWarnings("unused")
    public void allowOverrideChat(boolean overrideChat) {
        this.overrideChat = overrideChat;
    }

    /**
     * Determines whether ModernDisguise is currently configured to override the server's chat system.
     *
     * @return True if ModernDisguise is set to override the chat system, false otherwise.
     * @see DisguiseProvider#allowOverrideChat(boolean)
     */
    public boolean shouldOverrideChat() {
        return this.overrideChat;
    }

    /**
     * Disguises a {@link Player} with a valid {@link Disguise}
     *
     * @param player   the disguising player
     * @param disguise the disguise that the player should use
     * @return the response of the disguise action (like reasons of failure or so)
     * @see DisguiseProvider#undisguise(Player)
     */
    public final @NotNull DisguiseResponse disguise(@NotNull final Player player, @NotNull final Disguise disguise) {
        if (!isVersionSupported()) {
            return DisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
        }

        if (plugin == null || !plugin.isEnabled()) {
            return DisguiseResponse.FAIL_PLUGIN_NOT_INITIALIZED;
        }

        if (disguise.isEmpty()) {
            return DisguiseResponse.FAIL_EMPTY_DISGUISE;
        }

        if (disguise.hasEntity() && (!entityDisguises || !DisguiseUtil.isEntitySupported(disguise.getEntityType()))) {
            return DisguiseResponse.FAIL_ENTITY_NOT_SUPPORTED;
        }

        String realName = player.getName();
        String nickname = realName;
        final GameProfile profile = DisguiseUtil.getProfile(player);
        if (!player.isOnline() || profile == null) {
            return DisguiseResponse.FAIL_PROFILE_NOT_FOUND;
        }

        if (disguise.hasName() && !disguise.getName().equals(player.getName())) {
            final String name = disguise.getName();

            if (name.length() > nameLength) {
                return DisguiseResponse.FAIL_NAME_TOO_LONG;
            }

            if (!namePattern.matcher(name).matches()) {
                return DisguiseResponse.FAIL_NAME_INVALID;
            }

            final Player found = DisguiseUtil.getPlayer(name);
            if (found != null && found.isOnline()) {
                return DisguiseResponse.FAIL_NAME_ALREADY_ONLINE;
            }

            nickname = name;
            try {
                DisguiseUtil.PROFILE_NAME.set(profile, name);
                DisguiseUtil.register(name, player);
            } catch (final IllegalAccessException e) {
                // shouldn't happen
                return DisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        Skin realSkin = null;
        if (disguise.hasSkin()) {
            final Optional<Property> optional = profile.getProperties().get("textures").stream().findFirst();
            if (optional.isPresent()) {
                realSkin = DisguiseUtil.getSkin(optional.get());
                profile.getProperties().removeAll("textures");
            }
            profile.getProperties().put("textures", new Property("textures", disguise.getTextures(), disguise.getSignature()));
        }

        EntityType type = disguise.getEntityType();
        if (isDisguised(player)) {
            final PlayerInfo info = this.playerInfo.remove(player.getUniqueId());
            if (info.hasName()) {
                DisguiseUtil.unregister(info.getNickname());
                if (disguise.hasName()) nickname = info.getNickname();
            }
            if (info.hasSkin()) {
                realSkin = info.getSkin();
            }
            realName = info.getName();

            if (info.hasEntity() && !disguise.hasEntity()) {
                type = info.getEntityType();
            }
        }

        playerInfo.put(
                player.getUniqueId(),
                new PlayerInfo(
                        realName,
                        nickname,
                        realSkin,
                        type
                )
        );

        if (disguise.hasName() || disguise.hasSkin()) {
            final boolean flying = player.isFlying();
            final int foodLevel = player.getFoodLevel();
            final float saturation = player.getSaturation();
            final float exhaustion = player.getExhaustion();

            this.refreshAsPlayer(player);

            player.teleport(player.getLocation());
            player.setFlying(flying);
            player.setFoodLevel(foodLevel);
            player.setSaturation(saturation);
            player.setExhaustion(exhaustion);
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
    public final @NotNull UndisguiseResponse undisguise(@NotNull final Player player) {
        if (!isDisguised(player)) {
            return UndisguiseResponse.FAIL_ALREADY_UNDISGUISED;
        }

        final GameProfile profile = DisguiseUtil.getProfile(player);
        if (profile == null) {
            if (player.isOnline()) {
                // shouldn't happen
                return UndisguiseResponse.FAIL_PROFILE_NOT_FOUND;
            }
            final PlayerInfo info = this.playerInfo.remove(player.getUniqueId());
            if (info.hasName()) {
                DisguiseUtil.unregister(info.getNickname());
            }
            return UndisguiseResponse.SUCCESS;
        }

        final PlayerInfo info = this.playerInfo.get(player.getUniqueId());
        if (info.hasName()) {
            try {
                DisguiseUtil.PROFILE_NAME.set(profile, info.getName());
                DisguiseUtil.unregister(info.getNickname());
            } catch (final IllegalAccessException e) {
                return UndisguiseResponse.FAIL_NAME_CHANGE_EXCEPTION;
            }
        }

        if (info.hasSkin()) {
            final Skin skin = info.getSkin();
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", skin.getTextures(), skin.getSignature()));
        }

        this.playerInfo.remove(player.getUniqueId());
        this.refreshAsPlayer(player);
        player.teleport(player.getLocation());

        return UndisguiseResponse.SUCCESS;
    }

    /**
     * @param player the {@link Player} being checked
     * @return true if the {@link Player} is disguised, false if the {@link Player} is not.
     */
    public final boolean isDisguised(@NotNull final Player player) {
        return this.playerInfo.containsKey(player.getUniqueId());
    }

    /**
     * @param player the {@link Player} being checked
     * @return true if the {@link Player} is disguised as an entity, false if the {@link Player} is not.
     */
    public final boolean isDisguisedAsEntity(@NotNull final Player player) {
        return this.playerInfo.containsKey(player.getUniqueId()) && getInfo(player).hasEntity();
    }

    /**
     * @param player the {@link Player} you're grabbing info about
     * @return the known info about a {@link Player}
     */
    public final @NotNull PlayerInfo getInfo(@NotNull final Player player) {
        if (this.playerInfo.containsKey(player.getUniqueId())) {
            return this.playerInfo.get(player.getUniqueId());
        }
        return new PlayerInfo(player.getName(), null, null, EntityType.PLAYER);
    }

    /**
     * This sends packets to {@link Player}s to show changes like name and skin
     *
     * @param player the refreshed {@link Player}
     */
    abstract public void refreshAsPlayer(@NotNull final Player player);

    /**
     * @param refreshed the refreshed {@link Player}
     * @param targets   the needed {@link Player}s to receive refresh packets
     */
    abstract public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets);

    /**
     * @return false if version is NOT supported
     */
    public boolean isVersionSupported() {
        return DisguiseUtil.PRIMARY;
    }

    /**
     * @return the plugin used plugin to register listeners & refresh {@link Player}s
     */
    public final Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * @return whether entity disguises are allowed or not
     * depends on whether the player initialized it like that or not
     */
    public final boolean performEntityDisguises() {
        return this.entityDisguises;
    }

}
