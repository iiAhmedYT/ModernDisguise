package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This will always return a FAIL_VERSION_NOT_SUPPORTED in every response
 */
public class MVS_Unavailable extends DisguiseProvider {

    @Override
    public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise) {
        return DisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public @NotNull UndisguiseResponse unDisguise(@NotNull Player player) {
        return UndisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public void refreshPlayer(Player player) {
        // do nothing
    }

}