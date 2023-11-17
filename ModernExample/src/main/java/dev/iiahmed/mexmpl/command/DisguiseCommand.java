package dev.iiahmed.mexmpl.command;

import dev.iiahmed.disguise.*;

import dev.iiahmed.mexmpl.ModernExample;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;

public class DisguiseCommand implements CommandExecutor {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public DisguiseCommand() {
        DisguiseManager.setPlugin(ModernExample.getInstance());
        provider.allowOverrideChat(false);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && Objects.equals(args[0], "info")) {
            sender.sendMessage("Found: " + DisguiseUtil.found);
            sender.sendMessage("Living: " + DisguiseUtil.living);
            sender.sendMessage("Has Constructor (aka. Registered): " + DisguiseUtil.registered);
            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (this.provider.isDisguised(player)) {
            UndisguiseResponse resp = this.provider.undisguise(player);
            player.sendMessage("Undisguised with responsoe:" + resp.name());
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("/disguisetest player");
            player.sendMessage("/disguisetest mob");
            return true;
        }

        final String sub = args[0].toLowerCase(Locale.ENGLISH);
        Disguise disguise;
        switch (sub) {
            default:
                player.sendMessage("/disguisetest player");
                player.sendMessage("/disguisetest mob");
                return true;
            case "player":
                disguise = Disguise.builder()
                        .setName("BillPoopyPoo", false)
                        .setSkin("StraightSexual", SkinAPI.MOJANG)
                        .build();
                break;
            case "mob":
                disguise = Disguise.builder()
                        .setEntityType(EntityType.ZOMBIE)
                        .build();
                break;
        }

        long time = System.currentTimeMillis();
        DisguiseResponse response = this.provider.disguise(player, disguise);
        player.sendMessage("Response is: " + response.name() + ", took " + (System.currentTimeMillis() - time) + "ms");
        return true;
    }

}
