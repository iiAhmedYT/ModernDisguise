package dev.iiahmed.disguise;

import java.util.UUID;

public enum SkinAPI {

    /**
     * Official Mojang Skin API (RECOMMENDED)
     */
    MOJANG("https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false"),

    /**
     * MineSkin Skin API
     */
    MINESKIN("https://api.mineskin.org/get/uuid/%uuid%"),

    /**
     * MineTools Skin API
     */
    MINETOOLS("https://api.minetools.eu/profile/%uuid%"),
    ;

    private final String url;

    SkinAPI(final String url) {
        this.url = url;
    }

    public String format(final UUID uuid) {
        final String replacement = String.valueOf(uuid).replaceAll("-", "");
        if (url.contains("%uuid%")) {
            return url.replace("%uuid%", replacement);
        }
        return url + replacement;
    }

}
