package dev.iiahmed.mexmpl.command;

import dev.iiahmed.disguise.*;

import dev.iiahmed.disguise.attribute.RangedAttribute;
import dev.iiahmed.disguise.util.Version;
import dev.iiahmed.mexmpl.ModernExample;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.command.AttachmentMode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("disguise")
@SuppressWarnings("unused")
public class DisguiseCommand {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public DisguiseCommand() {
        DisguiseManager.initialize(ModernExample.getInstance(), true);
        provider.allowOverrideChat(false);
    }

    @SubCommand(value = "player", attachment = AttachmentMode.EMPTY)
    public @NotNull String asPlayer(
            final Player player,
            @Default("BillBobbyBob") String name,
            @Optional String skin
    ) {
        final long time = System.currentTimeMillis();
        final Disguise.Builder builder = Disguise.builder().setName(name);

        if (skin != null) {
            builder.setSkin(SkinAPI.MOJANG, Bukkit.getOfflinePlayer(skin).getUniqueId());
        }

        return provider.disguise(player, builder.build()) + " (done in " + (System.currentTimeMillis() - time) + "ms)";
    }

    @SubCommand(value = "entity", attachment = AttachmentMode.EMPTY)
    public @NotNull String asEntity(
            final Player player,
            final @Default("ZOMBIE") EntityType type,
            final @Flag("scale") @Default("1.0") double scale
    ) {
        final long time = System.currentTimeMillis();
        final Disguise disguise = Disguise.builder()
                .setEntity(builder -> builder.setType(type).setAttribute(RangedAttribute.SCALE, scale))
                .build();
        return provider.disguise(player, disguise) + " (done in " + (System.currentTimeMillis() - time) + "ms)";
    }

    @SubCommand(value = "info", attachment = AttachmentMode.EMPTY)
    public void info(final BukkitSource source) {
        final EntityProvider entityProvider = provider.getEntityProvider();
        String foundColor = entityProvider.foundEntities() < 56? "&c" : "&a"; // 56 is amount of entities detected on 1.8.8
        source.reply(translate("Found enitities: " + foundColor + entityProvider.foundEntities()));

        String livingColor = entityProvider.foundLivingEntities() < 33? "&c" : "&a"; // 33 is amount of entities living in 1.8.8
        source.reply(translate("Extends LivingEntity: " + livingColor + entityProvider.foundLivingEntities()));

        String registeredColor = entityProvider.supportedEntities() < 32? "&c" : "&a"; // 32 is amount of entities registered in 1.8.8
        source.reply(translate("Has a Constructor (aka. Registered): " + registeredColor + entityProvider.supportedEntities()));
    }

    @Command("undisguise")
    public @NotNull String undisguise(final Player player) {
        final long time = System.currentTimeMillis();
        return provider.undisguise(player) + " (done in " + (System.currentTimeMillis() - time) + "ms)";
    }

    @Command("modernversion")
    public String isVersion(
            final BukkitSource source,
            final int major,
            final int minor,
            final int patch
    ) {
        final boolean is = Version.is(major, minor, patch);
        final boolean over = Version.isOver(major, minor, patch);
        final boolean below = Version.isBelow(major, minor, patch);

        return translate("&eIs Exact: " + color(is) + "&e, Is Over: " + color(over) + "&e, Is Below: " + color(below));
    }

    private String translate(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String color(final boolean bool) {
        return (bool ? "&a" : "&c") + bool;
    }

}
