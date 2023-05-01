package dev.iiahmed.disguise.vs;

import dev.iiahmed.disguise.DisguiseProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VS_Unavailable extends DisguiseProvider {

    @Override
    public void refreshAsPlayer(@NotNull final Player player) {
        // do nothing
    }

    @Override
    public void refreshAsEntity(@NotNull final Player refreshed, final boolean remove, final Player... targets) {
        // do nothing
    }

    @Override
    public boolean isVersionSupported() {
        return false;
    }

}