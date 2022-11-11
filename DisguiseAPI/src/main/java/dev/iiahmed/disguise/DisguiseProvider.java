package dev.iiahmed.disguise;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DisguiseProvider {

    protected Plugin plugin;

    /**
     * @param player is the disguising player
     * @param disguise the disguise that the player should use
     * @return the response of the disguise action (like reasons of failure or so)
     */
    abstract public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise);

    /**
     * @param player is the undisguising player
     * @return the response of the undisguise action (like reasons of failure or so)
     */
    abstract public @NotNull UndisguiseResponse unDisguise(@NotNull Player player);

    /**
     * @param player is the player being checked
     * @return true if the player is disguised, false if the player is not.
     */
    abstract public boolean isDisguised(@NotNull Player player);

    /**
     * @param player is the player you're grabbing info about
     * @return null if not disguised
     */
    abstract public @Nullable PlayerInfo getInfo(@NotNull Player player);

    /**
     * This sends packets to players to show changes like name and skin
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

}
