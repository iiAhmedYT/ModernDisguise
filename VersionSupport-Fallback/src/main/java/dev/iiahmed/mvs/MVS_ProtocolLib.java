package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MVS_ProtocolLib extends DisguiseProvider {

    @Override
    public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise) {
        if (plugin == null
                || !plugin.isEnabled()) {
            return DisguiseResponse.FAIL_PLUGIN_NOT_INITIALIZED;
        }
        if (isDisguised(player)) {
            return DisguiseResponse.FAIL_ALREADY_DISGUISED;
        }
        return DisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public @NotNull UndisguiseResponse unDisguise(@NotNull Player player) {
        return UndisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public void refreshPlayer(Player player) {

    }
}
