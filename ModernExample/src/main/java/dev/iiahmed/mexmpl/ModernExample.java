package dev.iiahmed.mexmpl;

import dev.iiahmed.mexmpl.command.DisguiseCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModernExample extends JavaPlugin {

    private static ModernExample instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginCommand("disguise").setExecutor(new DisguiseCommand());
    }

    public static ModernExample getInstance() {
        return instance;
    }

}
