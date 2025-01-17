package site.radio.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.radio.letter.Letter;
import site.radio.letter.LetterCreationEvent;
import site.radio.letter.LetterService;
import site.radio.reply.ReplyResponseDto;
import site.radio.reply.ReplyService;

@Slf4j
@RequiredArgsConstructor
@Component
public class LetterReplyEventListener {

    private final LetterService letterService;
    private final ReplyService replyService;

    @Transactional
    @EventListener
    public void handleCreationLetter(LetterCreationEvent event) {
        // event status => PROCESSING
        event.process();

        // letter 저장
        Letter letter = letterService.save(UUID.fromString(event.getUserId()),
                event.getMessage(),
                event.getPreference(),
                event.isPublished());

        // reply 저장
        ReplyResponseDto replyResponse = replyService.save(letter, event.getTwoTypeMessage());

        // future complete
        if (!event.getFuture().complete(replyResponse)) {
            log.warn("Failed to complete future for letter {}", letter.getId());
        }

        // event status => COMPLETE
        event.complete();
    }
}
