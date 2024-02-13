package dev.iiahmed.disguise;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class Skin {

    public static final Skin STEVE = new Skin(
            "ewogICJ0aW1lc3RhbXAiIDogMTcwNjAwNTI5Mjk5NCwKICAicHJvZmlsZUlkIiA6ICIzYzEwM2RlOTAzZjc0ZjA4YTU3ZGY1NWE5NzQ0NGM4YiIsCiAgInByb2ZpbGVOYW1lIiA6ICIxZ3NITGtDWjJidTl6cFQyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZkM2IwNmMzODUwNGZmYzAyMjliOTQ5MjE0N2M2OWZjZjU5ZmQyZWQ3ODg1Zjc4NTAyMTUyZjc3YjRkNTBkZTEiCiAgICB9CiAgfQp9",
            "LdWvQE2c2iQ7L82urOoeHKqMjE5IMe6MoA2mi2pSc3Np0hvxDxmCgMsUKqIzF5czSgBOxSdcT9JWN21Wc0yxhT4fBhAyQ5bq3LiZmqzGlCiNQ25j0g4e/lhii+0NgFbYAhqIwx6LCWU9aAI+Bk2KrVrlwUBN5m9oYhxZXXIA+huon3+J/ZRwW0c5eiAnhmuJG8iSVzbGBl1Z8WnUVr4YUPAYSHMKvKQBLZZ0bPWeiEuT5i0nSIy26ToFjpy5lweSAIFJA2P5sYDTIu97WFBZkT5FCiwT+kqoNxNqtCX9Fd63aFd/9VXoe/dEnFV96pdisGlRAN6cgqsqzJky/iRjrLyf7Zms8HCcoTGLu1vgkK2vaaJ5PWyDtKDqHYOANzdeIHlLJMfFwKYUwsGP/NWf6kXjuznjbK9FO9QXdgrlRope0OW5NjEblV9ZrMEfPrGhLnV5ez5F1hDook2X8S6wctG3OK/HRagL2LGufGzSPf6AjbmkanRYrsEohETty+zfAOpcQQ7hfqn8h5ZqtKuH+mJDxrFwECO48gLX+I/ChJ7UzEStxruhw9kbAMVa1Bo8/1g0B4oqCpzuT1dAJ/Ck1it0O46icm9pmHdDvPXFEre6CKMVgzxRJEScKaFg0jW2VTsg8TY54AkIMlIVLNjgQTFmi3W701qQoevcfYMZwPI="
    );

    public static final Skin ALEX = new Skin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY2NjMwNzU4MDMyOSwKICAicHJvZmlsZUlkIiA6ICI0ZGEzNDFmNjgyOTg0YTgwYjE2MTIzMGUxNmJmZWRjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFZnJlZXRTUCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80NmFjZDA2ZTg0ODNiMTc2ZThlYTM5ZmMxMmZlMTA1ZWIzYTJhNDk3MGY1MTAwMDU3ZTlkODRkNGI2MGJkZmE3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            "xDoJ1XDQaiJtMPfagD/HodwiiuFbo60JHAA/ATSG6brzyHrk4N5k0U4eGFPOge/4XHLUCalQq49OPHKzVsYUvmkuoiTVDMSQnz5FUhjrHTVoL0hS1VJ5Zy8heF7bC74aTo4SCIcZzr2FiTBwkGEOoiXMWHFz8kJT5XcgNFgWsemaz5DqZuIklIlR5ZKh9FXoOLGsJaMR3Nn0c159Ud4y5cJMDqTR1N5LpSdApJWqkerlc15ioc78CFsGwAY5ObpTanIIKJvVPuWt7fdGez/ErG4BmaHaoXwkWy99+BVAroH/ajMICI32Bcrmy99x1oIjWx0M7y0YWKQGIpiO4UF8blImZlEvKXJvIr3nI5M3+22el0JTtei0Qv1JqxWfrc3oX10Io9pc5OWtsxQ51U4DXaqKB6wUpxGH3sAIeBxrVu0G3HoEP4J8Xzsqndo0+6hQkuzTcA2S55n3b6IRXU8MqBBXpw3ul8qjjNIahvfK1J6FGd1VcUny5jU7wUt5/16WY27otWEH37sd/F3p1wyoGXiuaRrc3xHum7LKU/lIiQPhUF9wlqziTchQ9qTFFEIhsg8MUPEIntshP3XrvpezIWe4qXazcJTuBS2xWU9G4/CP8yfkKU0iY68Xq0ryr/wk3yd4ZIy0jT1vRoz6ujsPSA/csWhoEYHZrN7uz1Waex0="
    );

    private final String textures, signature;

    public Skin(final String textures, final String signature) {
        this.textures = textures;
        this.signature = signature;
    }

    /**
     * @return the textures of the skin
     */
    public String getTextures() {
        return textures;
    }

    /**
     * @return the signature of the skin
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return a {@link Boolean} that indicates whether the skin is valid or not
     */
    public boolean isValid() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

    /**
     * @return a random {@link Skin} between Steve / Alex
     */
    @SuppressWarnings("unused")
    public static @NotNull Skin defaultSkin() {
        return new Random().nextBoolean() ? STEVE : ALEX;
    }

    /**
     * @return the {@link Skin} of the given {@link Player}
     */
    @SuppressWarnings("unused")
    public static @NotNull Skin of(final Player player) {
        return DisguiseUtil.getSkin(player);
    }

}
