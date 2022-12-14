package dev.iiahmed.disguise.placeholder;

import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.PlayerInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIExpantion extends PlaceholderExpansion {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    @Override
    public @NotNull String getIdentifier() {
        return "nick";
    }

    @Override
    public @NotNull String getAuthor() {
        return "iiAhmedYT";
    }

    @Override
    public @NotNull String getVersion() {
        return "ModernDisguise";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (player == null) {
            return "";
        }

        String request = params.toLowerCase();
        PlayerInfo info = provider.getInfo(player);

        switch (request) {
            case "name":
                if (info != null) {
                    return info.getNickname();
                }
                return player.getName();
            case "realname":
                if (provider.isDisguised(player) && info != null) {
                    return info.getName();
                }
                return player.getName();
            case "is_nicked":
            case "is_disguised":
                return String.valueOf(provider.isDisguised(player));

        }

        return null;
    }

}
