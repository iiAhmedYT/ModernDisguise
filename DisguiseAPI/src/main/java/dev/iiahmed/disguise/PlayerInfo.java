package dev.iiahmed.disguise;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;

@RequiredArgsConstructor
@Getter
public class PlayerInfo {

    private final String name, nickname, textures, signature;
    private final EntityType entityType;

}
