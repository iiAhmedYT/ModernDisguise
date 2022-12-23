package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import dev.iiahmed.disguise.UndisguiseResponse;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();
    private final boolean supportsChat = DisguiseUtil.INT_VER > 18;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            if (provider.isDisguised(p) && Objects.requireNonNull(provider.getInfo(p)).getEntityType() != EntityType.PLAYER) {
                // refresh if Entity for p
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (provider.isDisguised(player)) {
            UndisguiseResponse response = provider.unDisguise(player);

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
