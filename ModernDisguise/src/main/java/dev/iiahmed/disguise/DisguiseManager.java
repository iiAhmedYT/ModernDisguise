package dev.iiahmed.disguise;

import dev.iiahmed.disguise.placeholder.PAPIExpantion;
import dev.iiahmed.mvs.MVS1_8_R3;
import dev.iiahmed.mvs.MVS_ProtocolLib;
import dev.iiahmed.mvs.MVS_Unavailable;
import org.jetbrains.annotations.NotNull;

public class DisguiseManager {

    private static final DisguiseProvider PROVIDER;
    private static boolean expantionRegistered = false;

    static {
        if(versionExists("v1_8_R3")) {
            PROVIDER = new MVS1_8_R3();
        } else if(classExists("com.pheonix.protocol.ProtocolLib")
        && classExists("dev.iiahmed.mvs.MVS_ProtocolLib")) {
            PROVIDER = new MVS_ProtocolLib();
        } else {
            PROVIDER = new MVS_Unavailable();
        }
    }

    private static boolean versionExists(final String version) {
        return classExists("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
    }

    private static boolean classExists(final String path) {
        try {
            Class.forName(path);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * @return the available DisguiseProvider for current version
     */
    @NotNull
    public static DisguiseProvider getProvider() {
        return PROVIDER;
    }

    public static void registerExpantion() {
        if(!classExists("me.clip.papi.PlaceholderAPI")) {
            return;
        }
        if(!expantionRegistered) {
            new PAPIExpantion().register();
            expantionRegistered = true;
        }
    }

}
