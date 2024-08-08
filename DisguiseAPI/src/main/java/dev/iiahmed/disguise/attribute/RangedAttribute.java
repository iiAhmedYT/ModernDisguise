package dev.iiahmed.disguise.attribute;

import dev.iiahmed.disguise.util.Version;

import java.util.function.Supplier;

public class RangedAttribute extends Attribute implements Validatable {

    public static final RangedAttribute SCALE = new RangedAttribute(
            () -> Version.isOrOver(1, 20, 5),
            "generic.scale", 0.0625, 16.0, 1.0
    );

    private final double min, max;

    protected RangedAttribute(
            final Supplier<Boolean> supported,
            final String key,
            final double min,
            final double max,
            final double def
    ) {
        super(supported, key, def);
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public boolean isValid(final double value) {
        return value >= min && value <= max;
    }

}
