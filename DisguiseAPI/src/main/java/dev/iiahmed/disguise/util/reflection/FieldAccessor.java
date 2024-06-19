package dev.iiahmed.disguise.util.reflection;

/**
 * An interface for retrieving the field content. (Credits: TinyProtocol)
 *
 * @param <T> - field type.
 *
 * @author Kristian
 */
public interface FieldAccessor<T> {
    /**
     * Retrieve the content of a field.
     *
     * @param target - the target object, or NULL for a static field.
     * @return The value of the field.
     */
    T get(Object target);

    /**
     * Set the content of a field.
     *
     * @param target - the target object, or NULL for a static field.
     * @param value  - the new value of the field.
     */
    void set(Object target, Object value);

    /**
     * Determine if the given object has this field.
     *
     * @param target - the object to test.
     * @return TRUE if it does, FALSE otherwise.
     */
    boolean hasField(Object target);
}