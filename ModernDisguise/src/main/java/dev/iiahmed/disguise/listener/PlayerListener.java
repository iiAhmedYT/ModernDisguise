package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import dev.iiahmed.disguise.UndisguiseResponse;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();
    private final boolean supportsChat = DisguiseUtil.INT_VER > 18;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        DisguiseUtil.register(player.getName());
        try {
            DisguiseUtil.inject(player, new PacketListener(player));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onLeave(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        try {
            DisguiseUtil.uninject(player);
        } catch (Exception ignored) {
        }
        if (!provider.isDisguised(player)) {
            return;
        }
        final UndisguiseResponse response = provider.undisguise(player);
        if (!response.name().equals("SUCCESS")) {
            provider.getPlugin().getLogger().info("Undisguise failed on leave");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(final AsyncPlayerChatEvent event) {
        if (supportsChat) {
            event.setMessage(event.getMessage() + ChatColor.RESET);
        }
    }

}
