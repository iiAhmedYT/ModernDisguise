package dev.iiahmed.mexmpl;

import dev.iiahmed.disguise.DisguiseResponse;
import dev.iiahmed.disguise.UndisguiseResponse;
import dev.iiahmed.mexmpl.command.DisguiseCommand;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.process.ResponseHandler;

public final class ModernExample extends JavaPlugin {

    private static ModernExample instance;

    @Override
    public void onEnable() {
        instance = this;
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.registerResponseHandler(DisguiseResponse.class, ResponseHandler::reply);
        handler.registerResponseHandler(UndisguiseResponse.class, ResponseHandler::reply);
        handler.register(new DisguiseCommand());
        handler.registerBrigadier();
    }

    public static ModernExample getInstance() {
        return instance;
    }

}
