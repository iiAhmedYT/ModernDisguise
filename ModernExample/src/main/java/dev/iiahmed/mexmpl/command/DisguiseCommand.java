package dev.iiahmed.mexmpl.command;

import dev.iiahmed.disguise.*;

import dev.iiahmed.mexmpl.ModernExample;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.*;
import revxrsal.commands.command.CommandActor;

@Command("disguise")
@SuppressWarnings("unused")
public class DisguiseCommand {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public DisguiseCommand() {
        DisguiseManager.setPlugin(ModernExample.getInstance());
        provider.allowOverrideChat(false);
    }

    @Subcommand("info")
    public void info(
            final CommandActor actor
            )
    {
        actor.reply("Found enitities: " + DisguiseUtil.found);
        actor.reply("Extends LivingEntity: " + DisguiseUtil.living);
        actor.reply("Has a Constructor (aka. Registered): " + DisguiseUtil.registered);
    }

    @Subcommand("player")
    public @NotNull DisguiseResponse asPlayer(
            final Player player,
            @Flag("name") @Default("BillBobbyBob") String name,
            @Flag("skin") @Default("StraightSexual") String skin,
            @Flag("api") @Default("MOJANG") SkinAPI api
    )
    {
        Disguise disguise = Disguise.builder()
                .setName(name, false)
                .setSkin(skin, api)
                .build();
        return provider.disguise(player, disguise);
    }

    @Subcommand("entity")
    public @NotNull DisguiseResponse asEntity(
            final Player player,
            @Flag("entity") @Default("ZOMBIE") EntityType type
    )
    {
        Disguise disguise = Disguise.builder()
                .setEntityType(type)
                .build();
        return provider.disguise(player, disguise);
    }

    @Command("undisguise")
    @Subcommand("entity")
    public @NotNull UndisguiseResponse undisguise(
            final Player player
    )
    {
        return provider.undisguise(player);
    }

}
