package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.stream.Stream;

public class PlayerListener implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();
    private final boolean supportsChat = Stream.of("1_19", "1_20")
            .anyMatch(v -> v.startsWith(DisguiseManager.VERSION));

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        if (supportsChat) {
            event.setMessage(event.getMessage() + ChatColor.RESET);
        }
    }

}
