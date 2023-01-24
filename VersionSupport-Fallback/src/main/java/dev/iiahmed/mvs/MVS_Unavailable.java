package dev.iiahmed.mvs;

import dev.iiahmed.disguise.Disguise;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseResponse;
import dev.iiahmed.disguise.UndisguiseResponse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This will always return a FAIL_VERSION_NOT_SUPPORTED in every response
 */
public final class MVS_Unavailable extends DisguiseProvider {

    @Override
    public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise) {
        return DisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public @NotNull UndisguiseResponse undisguise(@NotNull Player player) {
        return UndisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        // do nothing
    }

    @Override
    public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets) {
        // do nothing
    }
}