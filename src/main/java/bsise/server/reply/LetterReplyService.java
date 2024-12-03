package bsise.server.reply;

import bsise.server.letter.LetterRequestDto;

public interface LetterReplyService {

    ReplyResponseDto makeAndSaveReply(LetterRequestDto letterRequestDto);
}
