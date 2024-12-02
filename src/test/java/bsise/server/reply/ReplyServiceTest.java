package bsise.server.reply;

import bsise.server.auth.OAuth2Provider;
import bsise.server.clova.service.ClovaService;
import bsise.server.letter.LetterRepository;
import bsise.server.letter.LetterRequestDto;
import bsise.server.limiter.RateLimitService;
import bsise.server.user.domain.Preference;
import bsise.server.user.domain.Role;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LetterRepository letterRepository;

    @MockBean
    private RateLimitService rateLimitService;

    @Autowired
    private ClovaService clovaService;

    @DisplayName("letter에 대한 clova의 응답이 잘못되었을 때 트랜잭션이 롤백되어야 한다.")
    @Test
    void makeAndSaveReply() {
        // given
        User testUser = createTestUser();
        LetterRequestDto letterRequestDto = new LetterRequestDto(
                testUser.getId().toString(), "testMsg", testUser.getPreference(), true
        );

        when(rateLimitService.isRequestAllowed(letterRequestDto.getUserId()))
                .thenReturn(true);

        // when
        assertThrows(NullPointerException.class, () -> replyService.makeAndSaveReply(letterRequestDto));

        // then
        assertThat(letterRepository.findAll().isEmpty()).isEqualTo(true);
    }

    private User createTestUser() {
        return userRepository.save(User.builder()
                .username("testUser")
                .nickname("tester")
                .email("test@test.mail")
                .preference(Preference.F)
                .provider(OAuth2Provider.KAKAO)
                .role(Role.OAUTH)
                .build());
    }
}
