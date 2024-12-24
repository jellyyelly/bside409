package bsise.server.letter;

import java.time.LocalDateTime;

public interface FulltextSearchResult {

    String getLetterId();

    String getMessage();

    LocalDateTime getCreatedAt();
}
