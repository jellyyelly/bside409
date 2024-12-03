package bsise.server.reply;

import bsise.server.error.LetterNotFoundException;
import bsise.server.error.UserNotFoundException;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRequestDto;
import bsise.server.letter.LetterService;
import bsise.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReplyService {

    private final LetterService letterService;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final LetterReplyService letterReplyService;

    public ReplyResponseDto makeAndSaveReply(LetterRequestDto letterRequestDto) {
        return letterReplyService.makeAndSaveReply(letterRequestDto);
    }

    public ReplyResponseDto findReply(UUID letterId) {
        Letter letter = letterService.findLetter(letterId);
        Reply reply = replyRepository.findByLetter(letter)
                .orElseThrow(() -> new LetterNotFoundException("letter not found: " + letterId.toString()));

        return ReplyResponseDto.of(reply);
    }

    public List<ReplyResponseDto> findTopNLetterAndReply(Integer size) {
        size = correctSize(size);
        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNReplies(pageable);
        return replies.stream()
                .map(ReplyResponseDto::of)
                .toList();
    }

    public List<ReplyResponseDto> findMyLetterAndReply(UUID userId, Integer size) {
        validateUserId(userId);
        size = correctSize(size);

        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNRepliesByUserId(userId, pageable);
        return replies.stream()
                .map(reply -> ReplyResponseDto.ofByUserId(reply, userId))
                .toList();
    }

    public Page<ReplyResponseDto> findMyLetterAndReply(UUID userId, Pageable pageable) {
        validateUserId(userId);

        Page<Reply> replies = replyRepository.findRepliesByOrderByCreatedAt(userId, pageable);
        List<ReplyResponseDto> replyDto = replies.stream()
                .map(reply -> ReplyResponseDto.ofByUserId(reply, userId))
                .toList();

        return new PageImpl<>(replyDto, pageable, replies.getTotalElements());
    }

    private void validateUserId(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new UserNotFoundException("user not found");
        }
    }

    private Integer correctSize(Integer size) {
        if (size == null) {
            return 10;
        } else if (size > 10) {
            return 10;
        }
        return size;
    }
}
