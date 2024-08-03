package dev.iiahmed.disguise.exception;

public class IDNotFoundException extends RuntimeException {

    private final String name;

    public IDNotFoundException(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
