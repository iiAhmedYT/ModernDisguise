package dev.iiahmed.disguise;

import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class DisguiseUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(24);
    public static final int INT_VER = Integer.parseInt(VERSION.split("_")[1]);
    private static final String PREFIX = "net.minecraft.server." + (INT_VER<17? "v" + VERSION + "." : "");

    public static final HashMap<EntityType, Class<?>> ENTITIES = new HashMap<>();

    private static Class<?> CRAFT_PLAYER, LIVING_ENTITY;
    private static Method GET_PROFILE;
    public static Field PROFILE_NAME;

    static {
        try {
            CRAFT_PLAYER = Class.forName("org.bukkit.craftbukkit.v" + VERSION + ".entity.CraftPlayer");
            GET_PROFILE = CRAFT_PLAYER.getMethod("getProfile");
            PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            PROFILE_NAME.setAccessible(true);
            LIVING_ENTITY = Class.forName(PREFIX + "EntityLiving");
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
            String className = PREFIX + name;
            Class<?> clazz = getClass(className);
            if (clazz == null) {
                continue;
            }
            if (!LIVING_ENTITY.isAssignableFrom(clazz)) {
                continue;
            }
            ENTITIES.put(type, clazz);
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
