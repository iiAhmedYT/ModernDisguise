package dev.iiahmed.disguise.attribute;

import org.jetbrains.annotations.NotNull;

public interface Attributable {

    /**
     * Retrieves the value associated with a specific attribute for the entity.
     *
     * @param attribute The attribute whose associated value is to be retrieved.
     *                  This must be an instance of {@link Attribute} with the appropriate type.
     * @return The value associated with the specified attribute.
     * If no value is set, returns the default value defined by {@link Attribute#getDefaultValue()}.
     */
    double getAttribute(@NotNull Attribute attribute);

    /**
     * Sets the value for a specific attribute associated with the entity.
     *
     * @param attribute The attribute for which the value is being set.
     *                  This must be an instance of {@link Attribute} with the appropriate type.
     * @param value     The value to associate with the given attribute.
     *                  The type of this value must match the type parameter of the attribute.
     */
    void setAttribute(@NotNull final Attribute attribute, final double value);

}
