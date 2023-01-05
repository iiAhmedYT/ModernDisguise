package dev.iiahmed.disguise;

public enum SkinAPI {

    /**
     * Official Mojang Skin API
     */
    MOJANG_UUID("https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false"),
    /**
     * MineToolseu Skin API
     */
    MINETOOLS_UUID("https://api.minetools.eu/profile/%uuid%"),
    /**
     * MineSkin Skin API
     */
    MINESKIN_UUID("https://api.mineskin.org/get/uuid/%uuid%"),

    ;

    private final String url;

    SkinAPI(String url) {
        this.url = url;
    }

    public String format(String replacement) {
        if (url.contains("%uuid%")) {
            return url.replace("%uuid%", replacement.replaceAll("-", ""));
        }
        if (url.contains("%name%")) {
            return url.replace("%name%", replacement);
        }
        if (url.contains("%id%")) {
            return url.replace("%id%", replacement);
        }
        return url + replacement.replaceAll("-", "");
    }

}
