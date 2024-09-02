package dev.iiahmed.disguise.util;

import dev.iiahmed.disguise.EntityProvider;
import dev.iiahmed.disguise.util.reflection.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class DefaultEntityProvider implements EntityProvider {

    private static final Map<EntityType, Constructor<?>> ENTITIES = new HashMap<>();
    private static final Map<EntityType, Object> ENTITY_FIELDS = new HashMap<>();
    private static final boolean SUPPORTED;
    private static int found, living, registered;

    private static Method GET_ENTITY;
    private static Class<?> ENTITY_TYPES, WORLD;

    static {
        final boolean obf = Version.isOrOver(17);
        Class<?> entityLiving = null;
        boolean supported;
        try {
            entityLiving = Class.forName((obf ?
                    "net.minecraft.world.entity." : DisguiseUtil.PREFIX)
                    + "EntityLiving");
            WORLD = Class.forName((obf ?
                    "net.minecraft.world.level." : DisguiseUtil.PREFIX)
                    + "World");

            if (Version.isOrOver(13)) {
                ENTITY_TYPES = Class.forName((obf ?
                        "net.minecraft.world.entity." : DisguiseUtil.PREFIX)
                        + "EntityTypes");
                GET_ENTITY = ENTITY_TYPES.getMethod("a", String.class);
            }
            supported = true;
        } catch (final Throwable exception) {
            supported = false;
            Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to load ModernDisguise's secondary features (disguising as entities)", exception);
        }

        SUPPORTED = supported;
        if (SUPPORTED) {
            final Map<String, String> overrideNames = new HashMap<String, String>() {
                {
                    put("ELDER_GUARDIAN", "GuardianElder");
                    put("WITHER_SKELETON", "SkeletonWither");
                    put("STRAY", "SkeletonStray");
                    put("HUSK", "ZombieHusk");
                    put("ZOMBIE_HORSE", "HorseZombie");
                    put("SKELETON_HORSE", "HorseSkeleton");
                    put("DONKEY", "HorseDonkey");
                    put("MULE", "HorseMule");
                    put("ILLUSIONER", "IllagerIllusioner");
                    put("GIANT", "GiantZombie");
                    put("ZOMBIFIED_PIGLIN", "PigZombie");
                    put("MOOSHROOM", "MushroomCow");
                    put("SNOW_GOLEM", "Snowman");
                    put("PUFFERFISH", "PufferFish");
                    put("TRADER_LLAMA", "LlamaTrader");
                    put("WANDERING_TRADER", "VillagerTrader");
                }
            };

            for (final EntityType type : EntityType.values()) {
                if (!type.isAlive())  {
                    continue;
                }

                final String name = type.name();
                final String className;
                if (overrideNames.containsKey(name)) {
                    className = overrideNames.get(name);
                } else {
                    final StringBuilder builder = new StringBuilder();
                    boolean cap = true;
                    for (final char c : name.toCharArray()) {
                        if (c == '_') {
                            cap = true;
                            continue;
                        }
                        builder.append(cap ? c : String.valueOf(c).toLowerCase());
                        cap = false;
                    }
                    className = builder.toString();
                }
                final Class<?> clazz = findEntity(className);
                if (clazz == null) {
                    continue;
                }
                found++;
                if (!entityLiving.isAssignableFrom(clazz)) {
                    continue;
                }
                living++;
                final Constructor<?> constructor = findConstructor(clazz, type);
                if (constructor == null) {
                    ENTITY_FIELDS.remove(type);
                    continue;
                }
                registered++;
                ENTITIES.put(type, constructor);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return SUPPORTED && registered > 0;
    }

    @Override
    public Object create(final EntityType type, final @NotNull Object world) throws Exception {
        if (!ENTITIES.containsKey(type)) {
            throw new RuntimeException("Creating a not supported entity.");
        }
        final Constructor<?> constructor = ENTITIES.get(type);
        final Object entity;
        if (constructor.getParameterCount() == 1) {
            entity = constructor.newInstance(world);
        } else {
            entity = constructor.newInstance(ENTITY_FIELDS.get(type), world);
        }
        return entity;
    }

    @Override
    public boolean isSupported(final EntityType type) {
        return ENTITIES.containsKey(type);
    }

    /**
     * Finds the {@link Class} of any NMS entity
     *
     * @param name the name of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Class<?> findEntity(final String name) {
        if (Version.isBelow(17)) {
            return Reflections.getClass(DisguiseUtil.PREFIX + name);
        }
        for (final String path : new String[]{
                // animals
                "animal", "animal.allay", "animal.armadillo",
                "animal.axolotl", "animal.camel", "animal.frog",
                "animal.horse", "animal.goat", "animal.sniffer",

                // monster
                "monster", "monster.warden", "monster.piglin", "monster.hoglin", "monster.breeze",

                // other
                "ambient", "npc", "raid", "boss.wither", "boss.enderdragon",

                // root directory (so far only GlowSquid is like that)
                ""
        }) {
            final String additon = path.isEmpty() ? "" : path + ".";
            final Class<?> firstTry = Reflections.getClass("net.minecraft.world.entity." + additon + name);
            if (firstTry != null) {
                return firstTry;
            }

            final Class<?> secondTry = Reflections.getClass("net.minecraft.world.entity." + additon + "Entity" + name);
            if (secondTry != null) {
                return secondTry;
            }
        }
        return null;
    }

    /**
     * Finds the {@link Constructor} of any NMS entity
     *
     * @param entityClass the class of the NMS entity
     * @return null if the NMS entity was NOT found
     */
    private static Constructor<?> findConstructor(@NotNull final Class<?> entityClass, final EntityType type) {
        if (Version.isBelow(13)) {
            return Reflections.getConstructor(entityClass, WORLD);
        }
        try {
            final Object obj = GET_ENTITY.invoke(null, type.name().toLowerCase(Locale.ENGLISH));
            if (obj == null) {
                return null;
            }
            if (Version.is(13)) {
                ENTITY_FIELDS.put(type, obj);
            } else {
                final Optional<?> o = (Optional<?>) obj;
                if (o.isPresent()) {
                    ENTITY_FIELDS.put(type, o.get());
                } else {
                    return null;
                }
            }
        } catch (final Exception ignored) {
            return null;
        }
        return Reflections.getConstructor(entityClass, ENTITY_TYPES, WORLD);
    }

    @Override
    public int foundEntities() {
        return found;
    }

    @Override
    public int foundLivingEntities() {
        return living;
    }

    @Override
    public int supportedEntities() {
        return registered;
    }

}
