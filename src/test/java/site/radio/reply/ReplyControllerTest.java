package site.radio.reply;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import site.radio.reply.controller.ReplyController;
import site.radio.reply.domain.Letter;
import site.radio.reply.domain.Reply;
import site.radio.reply.dto.ReplyResponse;
import site.radio.reply.service.ReplyService;
import site.radio.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ReplyControllerTest {

    @InjectMocks
    private ReplyController replyController;

    @Mock
    private ReplyService replyService;

    private MockMvc mockMvc;
    private User mockUser;
    private Letter mockLetter;
    private Letter mockLetter2;
    private Reply mockReply;
    private Reply mockReply2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(replyController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        /* Mocking User 객체 */
        mockUser = mock(User.class);
        UUID userId = UUID.randomUUID();
        given(mockUser.getId()).willReturn(userId);

        /* Mocking Letter 객체 */
        mockLetter = mock(Letter.class);
        given(mockLetter.getUser()).willReturn(mockUser);

        mockLetter2 = mock(Letter.class);
        lenient().doReturn(mockUser).when(mockLetter2).getUser();

        /* Mocking Reply 객체 */
        mockReply = mock(Reply.class);
        UUID replyId = UUID.randomUUID();
        lenient().doReturn(replyId).when(mockReply).getId();
        lenient().doReturn(mockLetter).when(mockReply).getLetter();

        mockReply2 = mock(Reply.class);
        UUID replyId2 = UUID.randomUUID();
        lenient().doReturn(replyId2).when(mockReply2).getId();
        lenient().doReturn(mockLetter2).when(mockReply2).getLetter();
    }

    @DisplayName("published 값에 따른 편지와 1:1로 대응하는 답장을 페이징으로 응답할 수 있다")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void success_paging_response_when_published_is_existed(boolean published) throws Exception {
        // given
        /* Arrange */
        int year = 2024;
        PageRequest pageable = PageRequest.of(0, 15);
        given(mockLetter.isPublished()).willReturn(published); // published 조건에 따른 편지의 공개 여부 stub
        given(mockReply.getCreatedAt()).willReturn(LocalDateTime.of(year, 1, 1, 0, 0));

        PageImpl<ReplyResponse> replies = new PageImpl<>(List.of(ReplyResponse.of(mockReply)), pageable, 1);
        given(replyService.findMyLetterAndReply(mockUser.getId(), year, published, pageable)).willReturn(replies);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/replies/users/{userId}", mockUser.getId())
                        .queryParam("year", String.valueOf(year))
                        .queryParam("published", String.valueOf(published))
                        .queryParam("page", String.valueOf(pageable.getPageNumber()))
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        ReplyResponse firstContent = replies.getContent().get(0);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$..[?(@.userId == '%s')]", firstContent.getUserId()).exists())
                .andExpect(jsonPath("$..[?(@.replyId == '%s')]", firstContent.getReplyId()).exists())
                .andExpect(jsonPath("$..[?(@.published == %s)]", firstContent.isPublished()).exists())
                .andExpect(jsonPath("$..[?(@.pageNumber == %d)]", replies.getNumber()).exists())
                .andExpect(jsonPath("$..[?(@.pageSize == %d)]", replies.getSize()).exists())
                .andDo(MockMvcResultHandlers.print());
    }

    @DisplayName("published 값이 null 이면 페이징된 전체 답장을 응답한다")
    @Test
    void success_paging_response_when_published_is_null() throws Exception {
        // given
        /* Arrange */
        int year = 2024;
        PageRequest pageable = PageRequest.of(0, 15);
        given(mockLetter.isPublished()).willReturn(true); // 공개 편지
        given(mockLetter2.isPublished()).willReturn(false); // 비공개 편지
        given(mockReply.getCreatedAt()).willReturn(LocalDateTime.of(year, 1, 1, 0, 0));
        given(mockReply2.getCreatedAt()).willReturn(LocalDateTime.of(year, 1, 2, 0, 0));

        List<ReplyResponse> contents = List.of(ReplyResponse.of(mockReply), ReplyResponse.of(mockReply2));
        PageImpl<ReplyResponse> replies = new PageImpl<>(contents, pageable, 1);
        given(replyService.findMyLetterAndReply(mockUser.getId(), year, null, pageable)).willReturn(replies);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/replies/users/{userId}", mockUser.getId())
                        .queryParam("year", String.valueOf(year))
                        .queryParam("page", String.valueOf(pageable.getPageNumber()))
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        ReplyResponse firstContent = replies.getContent().get(0);
        ReplyResponse secondContent = replies.getContent().get(1);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(firstContent.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].replyId").value(firstContent.getReplyId().toString()))
                .andExpect(jsonPath("$.content[0].published").value(firstContent.isPublished()))
                .andExpect(jsonPath("$.content[1].replyId").value(secondContent.getReplyId().toString()))
                .andExpect(jsonPath("$.content[1].published").value(secondContent.isPublished()))
                .andExpect(jsonPath("$.pageable.pageNumber").value(replies.getNumber()))
                .andExpect(jsonPath("$.pageable.pageSize").value(replies.getSize()))
                .andDo(MockMvcResultHandlers.print());
    }
}