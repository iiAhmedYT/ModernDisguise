package dev.iiahmed.disguise.exception;

import java.util.UUID;

public class NameNotFoundException extends RuntimeException {

    private final UUID id;

    public NameNotFoundException(final UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return this.id;
    }

}
