package dev.iiahmed.disguise.attribute;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Attribute {

    private final String key;
    private final boolean supported;
    private final double def;

    protected Attribute(
            final Supplier<Boolean> supported,
            @NotNull final String key,
            final double def
    ) {
        this.def = def;
        this.key = key;
        this.supported = supported.get();
    }

    public String getKey() {
        return key;
    }

    public boolean isSupported() {
        return supported;
    }

    public double getDefaultValue() {
        return def;
    }

}
