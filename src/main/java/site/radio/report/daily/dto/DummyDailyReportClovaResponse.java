package site.radio.report.daily.dto;

import lombok.RequiredArgsConstructor;
import site.radio.clova.dto.CreateResponse;

/**
 * 클로바 응답을 대체하는 더미 응답 DTO. 편지 개수에 따른 더미 응답을 동적으로 생성합니다.
 */
@RequiredArgsConstructor
public class DummyDailyReportClovaResponse extends CreateResponse {

    private static final String RESPONSE_BY_ONE_LETTER = """
            {
                "letterAnalyses": [
                    {
                        "coreEmotions": ["슬픔"],
                        "sensitiveEmotions": ["우울함"],
                        "topic": "비 오는 날의 독서"
                    }
                ],
                "dailyCoreEmotion": "슬픔",
                "description": "당신."
            }
            """;
    private static final String RESPONSE_BY_TWO_LETTER = """
            {
                "letterAnalyses": [
                    {
                        "coreEmotions": ["슬픔"],
                        "sensitiveEmotions": ["우울함"],
                        "topic": "비 오는 날의 독서"
                    },
                    {
                        "coreEmotions": ["기쁨", "슬픔"],
                        "sensitiveEmotions": ["고민"],
                        "topic": "친구들과의 모임과 미래 고민"
                    }
                ],
                "dailyCoreEmotion": "슬픔",
                "description": "당신."
            }
            """;
    private static final String RESPONSE_BY_THREE_LETTER = """
            {
                "letterAnalyses": [
                    {
                        "coreEmotions": ["슬픔"],
                        "sensitiveEmotions": ["우울함"],
                        "topic": "비 오는 날의 독서"
                    },
                    {
                        "coreEmotions": ["기쁨", "슬픔"],
                        "sensitiveEmotions": ["고민"],
                        "topic": "친구들과의 모임과 미래 고민"
                    },
                    {
                        "coreEmotions": ["분노"],
                        "sensitiveEmotions": ["무례함"],
                        "topic": "지하철에서의 목격"
                    }
                ],
                "dailyCoreEmotion": "슬픔",
                "description": "당신."
            }
            """;
    private final int letterCount;

    @Override
    public String getResultMessage() {
        if (letterCount == 1) {
            return RESPONSE_BY_ONE_LETTER;
        }
        if (letterCount == 2) {
            return RESPONSE_BY_TWO_LETTER;
        }
        return RESPONSE_BY_THREE_LETTER;
    }

    public static CreateResponse createDummy(int letterCount) {
        return new DummyDailyReportClovaResponse(letterCount);
    }
}
