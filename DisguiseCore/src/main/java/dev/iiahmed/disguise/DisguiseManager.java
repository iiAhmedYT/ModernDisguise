package dev.iiahmed.disguise;

import dev.iiahmed.mvs.MVS1_8;
import org.jetbrains.annotations.Nullable;

public class DisguiseManager {

    private static final DisguiseProvider PROVIDER;

    static {
        if(versionExists("v1_8_R3")) {
            PROVIDER = new MVS1_8();
        } else {
            PROVIDER = null;
        }
    }

    private static boolean versionExists(final String version) {
        try {
            Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @Nullable
    public static DisguiseProvider getProvider() {
        return PROVIDER;
    }

}
