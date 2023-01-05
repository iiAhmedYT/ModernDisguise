package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;

public class PlayerInfo {

    private final String name, nickname, textures, signature;
    private final EntityType entityType;

    public PlayerInfo(String name, String nickname, String textures, String signature, EntityType entityType) {
        this.name = name;
        this.nickname = nickname;
        this.textures = textures;
        this.signature = signature;
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTextures() {
        return textures;
    }

    public String getSignature() {
        return signature;
    }

    public EntityType getEntityType() {
        return entityType == null? EntityType.PLAYER : entityType;
    }

    public boolean hasSkin() {
        return textures != null && !textures.isEmpty() && signature != null && !signature.isEmpty();
    }

}
