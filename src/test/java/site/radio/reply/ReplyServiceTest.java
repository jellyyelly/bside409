package site.radio.reply;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import site.radio.reply.domain.Letter;
import site.radio.reply.domain.Reply;
import site.radio.reply.dto.ReplyResponseDto;
import site.radio.reply.repository.ReplyRepository;
import site.radio.reply.service.ReplyService;
import site.radio.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ReplyServiceTest {

    @InjectMocks
    private ReplyService replyService;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ReplyRepository mockReplyRepository;

    @DisplayName("published 조건에 따라 해당하는 편지(Letter)의 답장(Reply) DTO 를 응답한다")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findMyLetterAndReply(boolean published) {
        // given
        /* Arrange */
        UUID userId = UUID.randomUUID();
        int year = 2024;
        PageRequest pageable = PageRequest.of(0, 10);

        /* Mocking Letter 객체 */
        Letter mockLetter = mock(Letter.class);
        given(mockLetter.isPublished()).willReturn(published);

        /* Mocking Reply 객체 */
        Reply mockReply = mock(Reply.class);
        UUID replyId = UUID.randomUUID();
        given(mockReply.getId()).willReturn(replyId);
        given(mockReply.getLetter()).willReturn(mockLetter);
        given(mockReply.getCreatedAt()).willReturn(LocalDateTime.of(year, 1, 1, 0, 0));
        PageImpl<Reply> replies = new PageImpl<>(List.of(mockReply));

        doReturn(true).when(mockUserRepository).existsUserById(userId);
        doReturn(replies).when(mockReplyRepository)
                .findLatestRepliesBy(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class),
                        eq(published), eq(pageable));

        // when
        Page<ReplyResponseDto> response = replyService.findMyLetterAndReply(userId, year, published, pageable);

        // then
        then(mockUserRepository).should().existsUserById(userId);
        then(mockReplyRepository).should().findLatestRepliesBy(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), eq(published), eq(pageable));

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).isPublished()).isEqualTo(published);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo(userId);
        assertThat(response.getContent().get(0).getReplyId()).isEqualTo(replyId);
        assertThat(response.getContent().get(0).getCreatedAt().getYear()).isEqualTo(year);

        // verify
        verify(mockUserRepository, times(1)).existsUserById(any(UUID.class));
        verify(mockReplyRepository, times(1))
                .findLatestRepliesBy(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class),
                        any(boolean.class), any(Pageable.class));
    }
}