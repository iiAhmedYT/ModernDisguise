package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class Disguise {

    private final String name;
    private final Skin skin;
    private final Entity entity;

    private Disguise(final String name, final Skin skin, final Entity entity) {
        this.name = name;
        this.skin = skin;
        this.entity = entity;
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
        return entity != null && entity.isValid();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise will change the player's name
     */
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise will change the player's skin
     */
    public boolean hasSkin() {
        return skin != null && skin.isValid();
    }

    /**
     * @return the name that the disguised player's name going to be changed for
     */
    public String getName() {
        return name;
    }

    /**
     * @return the textures that the disguised player's skin going to be changed for
     */
    public String getTextures() {
        if (skin == null) {
            return null;
        }
        return skin.getTextures();
    }

    /**
     * @return the signature that the disguised player's skin going to be changed for
     */
    public String getSignature() {
        if (skin == null) {
            return null;
        }
        return skin.getSignature();
    }

    /**
     * @return the entity that the disguised player's entity going to be changed for
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * The builder class for the disguise system
     */
    public static class Builder {

        private String name;
        private Skin skin;
        private Entity entity;

        /* we don't allow constructors from outside */
        private Builder() {
        }

        /**
         * This method sets the new name of the nicked player
         *
         * @param name the replacement of the actual player name
         * @return the disguise builder
         */
        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @param uuid this is the UUID of the needed player's skin
         * @return the disguise builder
         */
        public Builder setSkin(final UUID uuid) {
            return setSkin(SkinAPI.MOJANG, uuid);
        }

        /**
         * @deprecated now that values are generic it's easier for your IDE to use the SkinAPI provided first
         *
         * @param value   this is the value required by the skin api
         * @param api     determines the SkinAPI type
         * @return the disguise builder
         * @see Disguise.Builder#setSkin(SkinAPI, Object)
         */
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        public <V> Builder setSkin(final V value, final SkinAPI<V> api) {
            return setSkin(api.of(value));
        }

        /**
         * @param value   this is the value required by the skin api
         * @param api     determines the SkinAPI type
         * @return the disguise builder
         */
        public <V> Builder setSkin(final SkinAPI<V> api, final V value) {
            return setSkin(api.of(value));
        }

        /**
         * @param context   this is the context required by the skin api
         * @param api       determines the SkinAPI type
         * @return the disguise builder
         */
        public <V> Builder setSkin(final SkinAPI<V> api, final SkinAPI.Context<V> context) {
            return setSkin(api.of(context));
        }

        /**
         * Sets the skin based on a texture and a signature
         *
         * @return the disguise builder
         */
        public Builder setSkin(final String textures, final String signature) {
            return setSkin(new Skin(textures, signature));
        }

        /**
         * Sets the skin based on a {@link Skin}
         *
         * @return the disguise builder
         */
        public Builder setSkin(final Skin skin) {
            this.skin = skin;
            return this;
        }

        /**
         * @deprecated There is now a custom entity system
         * @see #setEntity(Function)
         *
         * @param type the entity type the player should look like
         * @return the disguise builder
         */
        public Builder setEntityType(final EntityType type) {
            this.entity = new Entity.Builder().setType(type).build();
            return this;
        }

        /**
         * @param builder the entity builder the player should look like
         * @return        the disguise builder
         */
        public Builder setEntity(final Function<Entity.Builder, Entity.Builder> builder) {
            builder.apply(new Entity.Builder());
            return this;
        }

        /**
         * @return a new instance of {@link Disguise} with the collected info
         */
        public Disguise build() {
            return new Disguise(name, skin, entity);
        }

    }

}
