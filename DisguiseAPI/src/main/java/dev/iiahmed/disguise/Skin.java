package dev.iiahmed.disguise;

public final class Skin {

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

}
