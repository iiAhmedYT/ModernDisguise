package dev.iiahmed.disguise.listener;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseUtil;
import dev.iiahmed.disguise.UndisguiseResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        DisguiseUtil.inject(player, new PacketListener(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        DisguiseUtil.uninject(player);
        if (!provider.isDisguised(player)) {
            return;
        }
        final UndisguiseResponse response = provider.undisguise(player);
        if (!"SUCCESS".equals(response.name())) {
            provider.getPlugin().getLogger().info("Undisguise failed on leave");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChat(final AsyncPlayerChatEvent event) {
        if (!provider.shouldOverrideChat()) {
            return;
        }
        event.setCancelled(true);
        Player sender = event.getPlayer();
        for (Player receiver : event.getRecipients()) {
            receiver.sendMessage(event.getFormat()
                    .replace("%1$s", sender.getName())
                    .replace("%2$s", event.getMessage())
            );
        }
    }

}
