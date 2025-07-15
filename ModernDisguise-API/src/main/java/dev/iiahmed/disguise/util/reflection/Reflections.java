package dev.iiahmed.disguise.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

@SuppressWarnings("unused")
public final class Reflections {

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target - the target type.
     * @param name - the name of the field, or NULL to ignore.
     * @param fieldType - a compatible field type.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param name - the name of the field, or NULL to ignore.
     * @param fieldType - a compatible field type.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(String className, String name, Class<T> fieldType) {
        return getField(getClass(className), name, fieldType, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target - the target type.
     * @param fieldType - a compatible field type.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType) {
        return getField(target, null, fieldType, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target - the target type.
     * @param fieldType - a compatible field type.
     * @param index - the number of compatible fields to skip.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return getField(target, null, fieldType, index);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param fieldType - a compatible field type.
     * @param index - the number of compatible fields to skip.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(String className, Class<T> fieldType, int index) {
        return getField(getClass(className), fieldType, index);
    }

    private static <T> FieldAccessor<T> getField(
            final Class<?> target,
            final String name,
            final Class<T> fieldType,
            int index
    ) {
        if (target == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        for (final Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);

                // A function for retrieving a specific field value
                return new FieldAccessor<T>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public T get(final Object target) {
                        try {
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public void set(final Object target, final Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public boolean hasField(final Object target) {
                        // target instanceof DeclaringClass
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }
                };
            }
        }

        // Search in parent classes
        if (target.getSuperclass() != null)
            return getField(target.getSuperclass(), name, fieldType, index);

        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    /**
     * Finds any {@link Class} of the provided paths
     *
     * @param paths all possible class paths
     * @return false if the {@link Class} was NOT found
     */
    public static boolean hasClass(final String... paths) {
        for (final String path : paths) {
            if (getClass(path) != null) return true;
        }
        return false;
    }

    /**
     * Finds any {@link Class} of the provided paths
     *
     * @param paths all possible class paths
     * @return null if the {@link Class} was NOT found
     */
    public static Optional<Class<?>> findClass(final String... paths) {
        for (final String path : paths) {
            Class<?> clazz = getClass(path);
            if (clazz != null) return Optional.of(clazz);
        }
        return Optional.empty();
    }

    /**
     * A nullable {@link Class#forName(String)} instead of throwing exceptions
     *
     * @return null if the {@link Class} was NOT found
     */
    public static Class<?> getClass(@NotNull final String path) {
        try {
            return Class.forName(path);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * A nullable {@link Class#getDeclaredConstructor(Class[])} instead of throwing exceptions
     *
     * @return null if the {@link Constructor} was NOT found
     */
    public static Constructor<?> getConstructor(@NotNull final Class<?> clazz, final Class<?>... classes) {
        try {
            return clazz.getDeclaredConstructor(classes);
        } catch (final Exception ignored) {
            return null;
        }
    }

}
