package bsise.server.letter;

import bsise.server.reply.ReplyResponseDto;
import bsise.server.reply.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "letter", description = "편지 API")
@RestController
@RequestMapping(path = "/api/v1/letters", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;
    private final ReplyService replyService;

    @Operation(summary = "유저의 사연이 담긴 편지를 접수받는 API", description = "유저의 편지로부터 CLOVA를 이용해 답장을 제공합니다.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ReplyResponseDto receiveLetter(@Valid @RequestBody LetterRequestDto letterRequestDto) {
        return replyService.makeAndSaveReply(letterRequestDto);
    }

    @Operation(summary = "편지 삭제 요청 API", description = "요청한 편지 ID에 해당하는 편지를 제거합니다.")
    @DeleteMapping("/{letterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLetter(@PathVariable("letterId") String letterId) {
        letterService.deleteLetter(UUID.fromString(letterId));
    }

    // FIXME: 커뮤니티 => 구현 나중에
    @Operation(summary = "유저들이 작성한 편지 목록을 반환하는 API", description = "최근 작성된 편지 10개를 제공합니다.")
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<LetterResponseDto> getTopNLettersForCommunity() {
        return letterService.getLatestLetters();
    }
}
