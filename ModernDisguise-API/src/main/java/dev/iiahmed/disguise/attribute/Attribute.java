package dev.iiahmed.disguise.attribute;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents an attribute that can be associated with an entity or object.
 * Attributes can be used to modify the behavior or characteristics of the entity.
 */
public class Attribute {

    private final String key;
    private final boolean supported;
    private final double def;

    /**
     * Constructs an Attribute with a key, a default value, and a support check.
     *
     * @param supported A supplier that checks if the attribute is supported.
     * @param key       The key associated with the attribute.
     * @param def       The default value for the attribute.
     */
    protected Attribute(
            final Supplier<Boolean> supported,
            @NotNull final String key,
            final double def
    ) {
        this.def = def;
        this.key = key;
        this.supported = supported.get();
    }

    /*
     * Retrieves the key associated with the attribute.
     */
    public String getKey() {
        return key;
    }

    /**
     * Checks if the attribute is supported in the current environment.
     *
     * @return true if the attribute is supported, false otherwise.
     */
    public boolean isSupported() {
        return supported;
    }

    /**
     * Retrieves the default value of the attribute.
     *
     * @return The default value associated with this attribute.
     */
    public double getDefaultValue() {
        return def;
    }

}
