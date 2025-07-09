package dev.iiahmed.disguise;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * The EntityProvider interface defines the contract for classes that
 * interact with Minecraft's internal (NMS) entities. It provides methods
 * for checking the availability of entity creation, creating entities based on
 * Bukkit's {@link EntityType}, and verifying support for specific entity types.
 */
public interface EntityProvider {

    /**
     * Checks if this EntityProvider is available for use.
     * This might involve checking if the necessary dependencies or resources
     * are present and correctly initialized.
     *
     * @return true if the EntityProvider is available, false otherwise.
     */
    boolean isAvailable();

    /**
     * Creates an instance of an entity of the given {@link EntityType} within the specified world.
     *
     * @param type  The type of the entity to create, represented by a {@link EntityType}.
     * @param world The world object (NMS or equivalent) where the entity will be spawned.
     * @return The created entity instance, typically as an NMS object.
     * @throws Exception if the entity cannot be created due to any reason.
     */
    Object create(final EntityType type, @NotNull final Object world) throws Exception;

    /**
     * Checks if the provided entity is valid and supported by this EntityProvider.
     *
     * @param entity The entity to check.
     * @return true if the entity is valid and supported, false otherwise.
     */
    default boolean isSupported(final Entity entity) {
        if (entity == null) {
            return false;
        }

        return entity.isValid() && this.isSupported(entity.getType());
    }

    /**
     * Checks if the specified {@link EntityType} is supported by this EntityProvider.
     *
     * @param type The type of the entity to check.
     * @return true if the entity type is supported, false otherwise.
     */
    boolean isSupported(final EntityType type);

    /**
     * Returns the number of entities found by this provider.
     *
     * @return The total number of entities found, default is 0.
     */
    default int foundEntities() {
        return 0;
    }

    /**
     * Returns the number of living entities found by this provider.
     *
     * @return The total number of living entities found, default is 0.
     */
    default int foundLivingEntities() {
        return 0;
    }

    /**
     * Returns the number of supported entities by this provider.
     *
     * @return The total number of supported entities, default is 0.
     */
    default int supportedEntities() {
        return 0;
    }

}
