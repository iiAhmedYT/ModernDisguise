package dev.iiahmed.disguise;

import dev.iiahmed.disguise.attribute.Attributable;
import dev.iiahmed.disguise.attribute.Attribute;
import dev.iiahmed.disguise.attribute.Validatable;
import dev.iiahmed.disguise.util.DisguiseUtil;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Entity implements Attributable {

    private final EntityType type;
    private final Map<Attribute, Double> attributes;

    public Entity(
            final EntityType type,
            final Map<Attribute, Double> attributes
    ) {
        this.type = type;
        this.attributes = attributes;
    }

    public EntityType getType() {
        return type;
    }

    public Map<Attribute, Double> getAttributes() {
        return attributes;
    }

    public boolean isValid() {
        return this.type != null && this.type != EntityType.PLAYER && DisguiseUtil.isEntitySupported(this.type);
    }

    @Override
    public double getAttribute(@NotNull Attribute attribute) {
        return attributes.getOrDefault(attribute, attribute.getDefaultValue());
    }

    @Override
    public void setAttribute(@NotNull final Attribute attribute, final double value) {
        if (attribute.isSupported()) {
            if (attribute instanceof Validatable && ((Validatable) attribute).isInvalid(value)) {
                return;
            }

            this.attributes.put(attribute, value);
        }
    }

    public static class Builder {

        private EntityType type;
        private final Map<Attribute, Double> attributes = new ConcurrentHashMap<>();

        Builder() {}

        /**
         * @param type the entity type the player should look like
         */
        public Builder setType(final EntityType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the value for a specific attribute associated with the entity.
         *
         * @param attribute The attribute for which the value is being set.
         *                  This must be an instance of {@link Attribute} with the appropriate type.
         * @param value     The value to associate with the given attribute.
         *                  The type of this value must match the type parameter of the attribute.
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

        public Entity build() {
            return new Entity(this.type, this.attributes);
        }

    }

}
