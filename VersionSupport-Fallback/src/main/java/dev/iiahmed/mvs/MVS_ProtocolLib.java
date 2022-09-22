package dev.iiahmed.mvs;

import dev.iiahmed.disguise.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MVS_ProtocolLib extends DisguiseProvider {

    @Override
    public @NotNull DisguiseResponse disguise(@NotNull Player player, @NotNull Disguise disguise) {
        if(plugin == null
        || !plugin.isEnabled()) {
            return DisguiseResponse.FAIL_PLUGIN_NOT_INITIALIZED;
        }
        if(isDisguised(player)) {
            return DisguiseResponse.FAIL_ALREADY_DISGUISED;
        }
        return DisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public @NotNull UndisguiseResponse unDisguise(@NotNull Player player) {
        return UndisguiseResponse.FAIL_VERSION_NOT_SUPPORTED;
    }

    @Override
    public boolean isDisguised(@NotNull Player player) {
        return false;
    }

    @Override
    public @Nullable PlayerInfo getInfo(@NotNull Player player) {
        return null;
    }

    @Override
    public void refreshPlayer(Player player) {

    }
}
