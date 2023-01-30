package dev.iiahmed.disguise;

public class Skin {

    private final String textures, signature;

    public Skin(final String textures, final String signature) {
        this.textures = textures;
        this.signature = signature;
    }

    public String getTextures() {
        return textures;
    }

    public String getSignature() {
        return signature;
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's skin
     */
    public boolean isValid() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

}
