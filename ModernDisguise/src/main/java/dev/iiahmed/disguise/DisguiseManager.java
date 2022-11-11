package dev.iiahmed.disguise;

import dev.iiahmed.disguise.listener.PlayerListener;
import dev.iiahmed.disguise.placeholder.PAPIExpantion;
import dev.iiahmed.mvs.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisguiseManager {

    private static final DisguiseProvider PROVIDER;
    private static boolean expantionRegistered = false;

    static {
        final Pattern pattern = Pattern.compile("\\d\\.\\d(\\d)?(.\\d)?");
        final Matcher matcher = pattern.matcher(Bukkit.getBukkitVersion());

        String version = null;
        while (matcher.find()) {
            version = Bukkit.getBukkitVersion().substring(matcher.start(), matcher.end());
        }

        if(version == null) {
            PROVIDER = new MVS_Unavailable();
        } else {
            switch (version) {
                case "1.8.8":
                    PROVIDER = new MVS1_8_R3();
                    break;
                case "1.9.4":
                    PROVIDER = new MVS1_9_R2();
                    break;
                case "1.10":
                case "1.10.1":
                case "1.10.2":
                    PROVIDER = new MVS1_10_R1();
                    break;
                case "1.11":
                case "1.11.1":
                case "1.11.2":
                    PROVIDER = new MVS1_11_R1();
                    break;
                case "1.12":
                case "1.12.1":
                case "1.12.2":
                    PROVIDER = new MVS1_12_R1();
                    break;
                case "1.13.2":
                    PROVIDER = new MVS1_13_R2();
                    break;
                case "1.14":
                case "1.14.1":
                case "1.14.2":
                case "1.14.3":
                case "1.14.4":
                    PROVIDER = new MVS1_14_R1();
                    break;
                case "1.15":
                case "1.15.1":
                case "1.15.2":
                    PROVIDER = new MVS1_15_R1();
                    break;
                case "1.16.5":
                    PROVIDER = new MVS1_16_R3();
                    break;
                case "1.17":
                case "1.17.1":
                    PROVIDER = new MVS1_17_R1();
                    break;
                case "1.18.2":
                    PROVIDER = new MVS1_18_R2();
                    break;
                default:
                    if(classExists("com.pheonix.protocol.ProtocolLib")) {
                        PROVIDER = new MVS_ProtocolLib();
                    } else {
                        PROVIDER = new MVS_Unavailable();
                    }
                    break;
            }
        }
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
     * sets the plugin for the provider and registers the litsners
     */
    public static void setPlugin(Plugin plugin) {
        if(PROVIDER.getPlugin() == null) {
            PROVIDER.setPlugin(plugin);
            plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        }
    }

    /**
     * @return the available DisguiseProvider for current version
     */
    @NotNull
    public static DisguiseProvider getProvider() {
        return PROVIDER;
    }

    @SuppressWarnings("unused")
    public static void registerExpantion() {
        if(!classExists("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
            return;
        }

        if(!expantionRegistered) {
            new PAPIExpantion().register();
            expantionRegistered = true;
        }
    }

}
