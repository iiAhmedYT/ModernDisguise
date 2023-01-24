package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;

public class PlayerInfo {

    private final String name, nickname, textures, signature;
    private final EntityType entityType;

    protected PlayerInfo(final String name, final String nickname, final String textures, final String signature, final EntityType entityType) {
        if (name == null) {
            throw new IllegalArgumentException("Input real name can't be null.");
        }
        this.name = name;
        this.nickname = nickname;
        this.textures = textures;
        this.signature = signature;
        this.entityType = entityType;
    }

    /**
     * @return the real name of the disguised Player
     */
    public String getName() {
        return name;
    }

    /**
     * @return the fake name of the disguised Player, will return
     * the real one if there's no fake
     */
    public String getNickname() {
        return nickname == null ? name : nickname;
    }

    /**
     * @return the original textures of the disguised player's skin
     */
    public String getTextures() {
        return textures;
    }

    /**
     * @return the original signature of the disguised player's skin
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return the {@link EntityType} of the disguised Player
     */
    public EntityType getEntityType() {
        return entityType == null ? EntityType.PLAYER : entityType;
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's name
     */
    public boolean hasName() {
        return nickname != null && !nickname.equals(name);
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's skin
     */
    public boolean hasSkin() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's entity
     */
    public boolean hasEntity() {
        return entityType != null && entityType != EntityType.PLAYER;
    }

}
