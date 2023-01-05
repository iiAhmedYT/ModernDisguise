package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public final class DisguiseUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(24);
    public static final int INT_VER = Integer.parseInt(VERSION.split("_")[1]);
    private static final String PREFIX = "net.minecraft.server." + (INT_VER<17? "v" + VERSION + "." : "");
    private static Class<?> CRAFT_PLAYER, ENTITY_LIVING, ENTITY_TYPES, WORLD;
    private static Method GET_PROFILE;
    public static Field PROFILE_NAME;

    private static final HashMap<EntityType, Class<?>> ENTITIES = new HashMap<>();
    public static int found, living, registered;

    static {
        try {
            CRAFT_PLAYER = Class.forName("org.bukkit.craftbukkit.v" + VERSION + ".entity.CraftPlayer");
            GET_PROFILE = CRAFT_PLAYER.getMethod("getProfile");
            PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            PROFILE_NAME.setAccessible(true);
            ENTITY_LIVING = Class.forName((INT_VER >= 17?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityLiving");
            WORLD = Class.forName((INT_VER >= 17?
                    "net.minecraft.world.level." : PREFIX)
                    + "World");
            ENTITY_TYPES = Class.forName((INT_VER >= 17?
                    "net.minecraft.world.entity." : PREFIX)
                    + "EntityTypes");
        } catch (Exception ignored) {}
        for (EntityType type : EntityType.values()) {
            StringBuilder builder = new StringBuilder("Entity");
            boolean cap = true;
            for (char c : type.name().toCharArray()) {
                if (c == '_') {
                    cap = true;
                    continue;
                }
                builder.append(cap? c : String.valueOf(c).toLowerCase());
                cap = false;
            }
            String name = builder.toString();
            Class<?> clazz = findEntity(name);
            if (clazz == null) {
                continue;
            }
            found++;
            if (!ENTITY_LIVING.isAssignableFrom(clazz)) {
                continue;
            }
            living++;
            if (hasConstructor(clazz, WORLD) || hasConstructor(clazz, ENTITY_TYPES, WORLD)) {
                registered++;
                ENTITIES.put(type, clazz);
            }
        }
    }

    private static Class<?> findEntity(String name) {
        if (INT_VER < 17) {
            return getClass(PREFIX + name);
        }
        for (String path : Arrays.asList("animal", "monster", "ambient", "npc", "raid",
                "monster.warden", "monster.piglin", "monster.hoglin", "boss.wither",
                "boss.enderdragon", "animal.allay", "animal.axolotl", "animal.camel",
                "animal.frog", "animal.goat", "animal.horse")) {
            Class<?> clazz = getClass("net.minecraft.world.entity." + path + "." + name);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    public static boolean hasConstructor(Class<?> clazz, Class<?>... classes) {
        try {
            clazz.getDeclaredConstructor(classes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static Class<?> getClass(String path) {
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getEntity(EntityType type) {
        return ENTITIES.get(type);
    }

    public static GameProfile getProfile(Player player) {
        try {
            return (GameProfile) GET_PROFILE.invoke(CRAFT_PLAYER.cast(player));
        } catch (Exception e) {
            return null;
        }
    }

}
