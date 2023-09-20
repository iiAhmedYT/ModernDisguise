package dev.iiahmed.mexmpl.command;

import dev.iiahmed.disguise.*;

import dev.iiahmed.mexmpl.ModernExample;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DisguiseCommand implements CommandExecutor {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public DisguiseCommand() {
        DisguiseManager.setPlugin(ModernExample.getInstance());
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

        Disguise disguise = Disguise.builder()
                .setName("BillPoopyPoo", false)
                .setSkin(SkinAPI.MOJANG_UUID, "427110da-51ab-4032-8672-6faf50872543")
                .build();
        long time = System.currentTimeMillis();
        DisguiseResponse response = this.provider.disguise(player, disguise);
        player.sendMessage("Response is: " + response.name() + ", took " + (
                System.currentTimeMillis() - time) + "ms");
        return true;
    }

}
