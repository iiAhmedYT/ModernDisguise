package dev.iiahmed.mexmpl;

import dev.iiahmed.mexmpl.command.DisguiseCommand;
import dev.iiahmed.mexmpl.hook.PAPIExpansion;
import dev.velix.imperat.BukkitImperat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModernExample extends JavaPlugin {

    private static ModernExample instance;

    @Override
    public void onEnable() {
        instance = this;
        final BukkitImperat imperat = BukkitImperat.builder(this)
                .applyBrigadier(true)
                .build();

        imperat.registerCommands(new DisguiseCommand());
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion().register();
        }
    }

    public static ModernExample getInstance() {
        return instance;
    }

}
