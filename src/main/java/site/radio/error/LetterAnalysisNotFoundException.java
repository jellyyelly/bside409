package site.radio.error;

import jakarta.persistence.EntityNotFoundException;

public class LetterAnalysisNotFoundException extends EntityNotFoundException {

    public LetterAnalysisNotFoundException(String message) {
        super(message);
    }
}
