package bsise.server.error;

import jakarta.persistence.EntityNotFoundException;

public class LetterNotFoundException extends EntityNotFoundException {

    public LetterNotFoundException(String message) {
        super(message);
    }
}
