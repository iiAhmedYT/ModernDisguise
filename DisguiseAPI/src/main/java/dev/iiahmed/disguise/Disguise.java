package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@SuppressWarnings("unused")
public final class Disguise {

    private final String name, textures, signature;
    private final boolean fakename;
    private final EntityType entityType;

    private Disguise(String name, String textures, String signature, boolean fakename, EntityType entityType) {
        this.name = name;
        this.textures = textures;
        this.signature = signature;
        this.fakename = fakename;
        this.entityType = entityType;
    }

    public boolean isEmpty() {
        return !hasName() && !hasSkin() && !hasEntity();
    }

    public boolean hasEntity() {
        return entityType != null && entityType != EntityType.PLAYER;
    }

    public boolean hasName() {
        return name != null && !fakename;
    }

    public boolean hasSkin() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

    public String getName() {
        return name;
    }

    public String getTextures() {
        return textures;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isFakename() {
        return fakename;
    }

    public EntityType getEntityType() {
        return entityType == null? EntityType.PLAYER : entityType;
    }

    /**
     * Returns a new instance of the Disguise.Builder class
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder class for the disguise system
     */
    public static class Builder {

        String name, texture, signature;
        boolean fakename = false;
        private EntityType entityType;

        /* we don't allow constructors from outside */
        private Builder() {}

        /**
         * This method sets the new name of the nicked player
         *
         * @param name the replacement of the actual player name
         * @param fake whether should the plugin replace the name or only replace it with a specific placeholder
         * @return the disguise builder
         */
        public Builder setName(String name, boolean fake) {
            this.name = name;
            this.fakename = fake;
            return this;
        }

        /**
         * @param skinAPI     determines the SkinAPI type
         * @param replacement this is either the UUID or the Name of the needed player's skin
         * @return the disguise builder
         */
        public Builder setSkin(SkinAPI skinAPI, String replacement) {
            final String urlString = skinAPI.format(replacement);
            JSONObject object;
            try {
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "ModernDisguiseAPI/v1.0");
                connection.setRequestMethod("GET");
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("The used URL doesn't seem to be working (the api is down?) " + urlString);
                }

                Scanner scanner = new Scanner(url.openStream());
                StringBuilder builder = new StringBuilder();

                while (scanner.hasNext()) {
                    builder.append(scanner.next());
                }

                JSONParser parser = new JSONParser();
                object = (JSONObject) parser.parse(builder.toString());
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }

            if (object == null || object.isEmpty()) {
                return this;
            }

            String texture = null, signature = null;
            switch (skinAPI) {
                case MOJANG_UUID:
                    JSONArray mojangArray = (JSONArray) object.get("properties");
                    for (Object o : mojangArray) {
                        JSONObject jsonObject = (JSONObject) o;
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
                    JSONObject raw = (JSONObject) object.get("raw");
                    JSONArray array = (JSONArray) raw.get("properties");
                    for (Object o : array) {
                        JSONObject jsonObject = (JSONObject) o;
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
                    JSONObject dataObject = (JSONObject) object.get("data");
                    JSONObject texturesObject = (JSONObject) dataObject.get("texture");
                    texture = (String) texturesObject.get("value");
                    signature = (String) texturesObject.get("signature");
                    break;
            }
            return setSkin(texture, signature);
        }

        /**
         * Sets the skin based on a texture and a signature
         * @return the disguise builder
         */
        public Builder setSkin(String texture, String signature) {
            this.texture = texture;
            this.signature = signature;
            return this;
        }

        /**
         * @param entityType the entity type the player should look like
         * @return the disguise builder
         */
        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Disguise build() {
            return new Disguise(name, texture, signature, fakename, entityType);
        }

    }


}
