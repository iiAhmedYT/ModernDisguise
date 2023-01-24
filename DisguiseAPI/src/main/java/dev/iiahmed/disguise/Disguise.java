package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unused")
public final class Disguise {

    private final String name, textures, signature;
    private final boolean fakename;
    private final EntityType entityType;

    private Disguise(final String name, final String textures, final String signature, final boolean fakename, final EntityType entityType) {
        this.name = name;
        this.textures = textures;
        this.signature = signature;
        this.fakename = fakename;
        this.entityType = entityType;
    }

    /**
     * Returns a new instance of the Disguise.Builder class
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise is empty or not
     */
    public boolean isEmpty() {
        return !hasName() && !hasSkin() && !hasEntity();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise will change the player's entity
     */
    public boolean hasEntity() {
        return entityType != null && entityType != EntityType.PLAYER;
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise will change the player's name
     */
    public boolean hasName() {
        return name != null && !fakename;
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise will change the player's skin
     */
    public boolean hasSkin() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

    /**
     * @return the name that the disguised player's name going to be changed for
     */
    public String getName() {
        return name;
    }

    /**
     * @return a {@link Boolean} that indicates whether the nickname is fake or not
     */
    public boolean isFakename() {
        return fakename;
    }

    /**
     * @return the textures that the disguised player's skin going to be changed for
     */
    public String getTextures() {
        return textures;
    }

    /**
     * @return the signature that the disguised player's skin going to be changed for
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return the entitytype that the disguised player's entity going to be changed for
     */
    public EntityType getEntityType() {
        return entityType == null ? EntityType.PLAYER : entityType;
    }

    /**
     * The builder class for the disguise system
     */
    public static class Builder {

        String name, texture, signature;
        boolean fakename = false;
        private EntityType entityType;

        /* we don't allow constructors from outside */
        private Builder() {
        }

        /**
         * This method sets the new name of the nicked player
         *
         * @param name the replacement of the actual player name
         * @param fake whether should the plugin replace the name or only replace it with a specific placeholder
         * @return the disguise builder
         */
        public Builder setName(final String name, final boolean fake) {
            this.name = name;
            this.fakename = fake;
            return this;
        }

        /**
         * @param skinAPI     determines the SkinAPI type
         * @param replacement this is either the UUID or the Name of the needed player's skin
         * @return the disguise builder
         */
        public Builder setSkin(final SkinAPI skinAPI, final String replacement) {
            final String urlString = skinAPI.format(replacement);
            final JSONObject object = DisguiseUtil.getJSONObject(urlString);

            if (object == null || object.isEmpty()) {
                return this;
            }

            String texture = null, signature = null;
            switch (skinAPI) {
                case MOJANG_UUID:
                    final JSONArray mojangArray = (JSONArray) object.get("properties");
                    for (Object o : mojangArray) {
                        final JSONObject jsonObject = (JSONObject) o;
                        if (jsonObject == null) continue;

                        if (jsonObject.get("value") != null) {
                            texture = (String) jsonObject.get("value");
                        }

                        if (jsonObject.get("signature") != null) {
                            signature = (String) jsonObject.get("signature");
                        }
                    }
                    break;
                case MINETOOLS_UUID:
                    final JSONObject raw = (JSONObject) object.get("raw");
                    final JSONArray array = (JSONArray) raw.get("properties");
                    for (Object o : array) {
                        final JSONObject jsonObject = (JSONObject) o;
                        if (jsonObject == null) continue;

                        if (jsonObject.get("value") != null) {
                            texture = (String) jsonObject.get("value");
                        }

                        if (jsonObject.get("signature") != null) {
                            signature = (String) jsonObject.get("signature");
                        }
                    }
                    break;
                case MINESKIN_UUID:
                    final JSONObject dataObject = (JSONObject) object.get("data");
                    final JSONObject texturesObject = (JSONObject) dataObject.get("texture");
                    texture = (String) texturesObject.get("value");
                    signature = (String) texturesObject.get("signature");
                    break;
            }
            return setSkin(texture, signature);
        }

        /**
         * Sets the skin based on a texture and a signature
         *
         * @return the disguise builder
         */
        public Builder setSkin(final String texture, final String signature) {
            this.texture = texture;
            this.signature = signature;
            return this;
        }

        /**
         * @param entityType the entity type the player should look like
         * @return the disguise builder
         */
        public Builder setEntityType(final EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        /**
         * @return a new instance of {@link Disguise} with the collected info
         */
        public Disguise build() {
            return new Disguise(name, texture, signature, fakename, entityType);
        }

    }


}
