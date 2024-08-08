package dev.iiahmed.disguise.attribute;

public interface Validatable {

    boolean isValid(final double value);

    default boolean isInvalid(final double value) {
        return !isValid(value);
    }

}
