package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;

public final class PlayerInfo {

    private final String name, nickname;
    private final Skin skin;
    private final EntityType entityType;

    PlayerInfo(final String name, final String nickname, final Skin skin, final EntityType entityType) {
        if (name == null) {
            throw new IllegalArgumentException("Input real name can't be null.");
        }
        this.name = name;
        this.nickname = nickname;
        this.skin = skin;
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
     * @return the skin info of the player
     */
    public Skin getSkin() {
        return skin;
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
        return nickname != null && !nickname.isEmpty() && !nickname.equals(name);
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's skin
     */
    public boolean hasSkin() {
        return skin != null && skin.isValid();
    }

    /**
     * @return a {@link Boolean} that indicates whether the disguise had changed the player's entity
     */
    public boolean hasEntity() {
        return entityType != null && entityType != EntityType.PLAYER;
    }

}
