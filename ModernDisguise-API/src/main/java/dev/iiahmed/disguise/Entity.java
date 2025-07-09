package dev.iiahmed.disguise;

import dev.iiahmed.disguise.attribute.Attributable;
import dev.iiahmed.disguise.attribute.Attribute;
import dev.iiahmed.disguise.attribute.Validatable;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code Entity} class serves as a wrapper for Bukkit's {@link EntityType},
 * providing a way to manage and manipulate entity attributes in a thread-safe manner.
 * This class is designed to work with various attributes defined in the {@link Attribute} interface,
 * allowing for flexible configuration and validation of entity properties.
 */
public class Entity implements Attributable {

    private final EntityType type;
    private final Map<Attribute, Double> attributes;

    /**
     * Constructs a new {@code Entity} with the specified {@link EntityType} and attributes.
     *
     * @param type       The type of entity, represented by a {@link EntityType}.
     * @param attributes A map of {@link Attribute} to their corresponding values.
     */
    public Entity(
            final EntityType type,
            final Map<Attribute, Double> attributes
    ) {
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * Returns the {@link EntityType} of this entity.
     *
     * @return The entity's type.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Returns the map of attributes associated with this entity.
     *
     * @return A map of {@link Attribute} to their corresponding values.
     */
    public Map<Attribute, Double> getAttributes() {
        return attributes;
    }

    /**
     * Checks if this entity is valid. An entity is considered valid if it is not null and
     * its type is not {@link EntityType#PLAYER}.
     *
     * @return true if the entity is valid, false otherwise.
     */
    public boolean isValid() {
        return this.type != null && this.type != EntityType.PLAYER;
    }

    /**
     * Retrieves the value of the specified attribute for this entity.
     * If the attribute is not present, returns the default value defined by the attribute.
     *
     * @param attribute The attribute to retrieve the value for.
     * @return The value of the attribute, or its default value if not set.
     */
    @Override
    public double getAttribute(@NotNull Attribute attribute) {
        return attributes.getOrDefault(attribute, attribute.getDefaultValue());
    }

    /**
     * Sets the value of the specified attribute for this entity.
     * If the attribute is supported and valid, it updates the value.
     *
     * @param attribute The attribute to set.
     * @param value     The value to set for the attribute.
     */
    @Override
    public void setAttribute(@NotNull final Attribute attribute, final double value) {
        if (attribute.isSupported()) {
            if (attribute instanceof Validatable && ((Validatable) attribute).isInvalid(value)) {
                return;
            }

            this.attributes.put(attribute, value);
        }
    }

    /**
     * Builder class for creating instances of {@link Entity}.
     * The builder allows for step-by-step configuration of an {@code Entity} object,
     * including setting its type and attributes.
     */
    public static class Builder {

        private EntityType type;
        private final Map<Attribute, Double> attributes = new ConcurrentHashMap<>();

        Builder() {}

        /**
         * Sets the {@link EntityType} for the entity being built.
         *
         * @param type The entity type to set.
         * @return The {@code Builder} instance for method chaining.
         */
        public Builder setType(final EntityType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the value for a specific attribute associated with the entity.
         *
         * @param attribute The attribute to set.
         * @param value     The value to associate with the attribute.
         * @return The {@code Builder} instance for method chaining.
         */
        public Builder setAttribute(
                @NotNull final Attribute attribute,
                final double value
        ) {
            if (!attribute.isSupported()) {
                return this;
            }

            if (attribute instanceof Validatable && ((Validatable) attribute).isInvalid(value)) {
                return this;
            }

            this.attributes.put(attribute, value);
            return this;
        }

        /**
         * Builds and returns a new {@code Entity} instance with the configured properties.
         *
         * @return A new {@code Entity} instance.
         */
        public Entity build() {
            return new Entity(this.type, this.attributes);
        }

    }

}
