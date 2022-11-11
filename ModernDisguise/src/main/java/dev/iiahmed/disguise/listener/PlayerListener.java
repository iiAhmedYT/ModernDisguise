package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (provider.isDisguised(player)) {
            provider.unDisguise(player);
        }
    }

    @EventHandler
    public void onSwitch(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (provider.isDisguised(player)) {
            provider.refreshPlayer(player);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (provider.isDisguised(player)) {
            provider.refreshPlayer(player);
        }
    }

}
