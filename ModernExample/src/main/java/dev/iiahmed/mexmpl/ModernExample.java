package dev.iiahmed.mexmpl;

import dev.iiahmed.mexmpl.command.DisguiseCommand;
import dev.iiahmed.mexmpl.hook.PAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.process.ResponseHandler;

public final class ModernExample extends JavaPlugin {

    private static ModernExample instance;

    @Override
    public void onEnable() {
        instance = this;
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.registerResponseHandler(String.class, ResponseHandler::reply);
        handler.register(new DisguiseCommand());
        handler.registerBrigadier();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion().register();
        }
    }

    public static ModernExample getInstance() {
        return instance;
    }

}
