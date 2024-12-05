package bsise.server.clova.weekly;

import static bsise.server.clova.dto.ClovaRole.ASSISTANT;
import static bsise.server.clova.dto.ClovaRole.SYSTEM;
import static bsise.server.clova.dto.ClovaRole.USER;

import bsise.server.clova.dto.ClovaMessageFormat;
import bsise.server.clova.dto.ClovaRequestDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClovaWeeklyReportRequestDto implements ClovaRequestDto {

    private List<ClovaMessageFormat> messages;

    private final Double temperature = 0.5;
    private final Integer topK = 0;
    private final Double topP = 0.8;
    private final Double repeatPenalty = 5.0;
    private final List<String> stopBefore = new ArrayList<>();
    private final Integer maxTokens = 256;
    private final boolean includeAiFilters = true;
    private final Integer seed = 340418298;

    private static final String SYSTEM_PROMPT = """
            시스템 목표:
            입력받은 최대 7개의 일일 분석들을 분석하여 1주일간 고생한 사용자에게 위로의 한마디를 건네줘
                        
            분석 내용:
            1. result:
            - 반드시 공백 포함 100자 이내로 작성해야 함
            - 문장들을 복사하지 않고, 문장들을 종합하여 하나의 감정 통찰과 행동 지침을 제공
                        
            응답 형식:
            - 응답 형식은 반드시 JSON 타입이어야 합니다. '}'로 끝내세요
            응답 예시:
            {
                "result": "반복되는 일상과 끊임없는 공부, 코딩으로 인해 조금씩 지쳐가는 모습이 보여요. 이런 상황에서는 지속적인 노력과 열정만으로는 극복하기 어려울 때도 있죠. 가끔은 숨을 돌리고 여유를 가지며 마음을 편안하게 해주는 건 어떨까요? 짧은 휴식이나 취미활동을 통해 머리를 식히고 활력을 되찾을 수 있을 거에요."
            }
                        
            주의사항:
            1. 답변에 "오늘" 이란 단어가 들어가면 안됨.    
            2. 의문문으로 끝나지 않고, 부드러운 어투로 권유하기
            3. 없는 말은 지어내면 안돼
            """;
    private static final String USER_PROMPT = "다양한 경험을 하면서 여러 감정을 느낀 하루였네요. 팀 프로젝트에서는 충돌로 인해 분노와 슬픔을 겪으셨겠지만, 소통의 중요성을 배우는 계기가 되었을 것 입니다. 주말에는 가족과 즐거운 시간을 보내며 연대감을 다지셨다니 정말 좋으셨겠어요. 거기다 새로 시작한 취미로부터 삶의 열망과 열정을 얻으시다니, 매일이 새롭게 느껴지실 것 같네요., 여러 감정들이 오고간 하루였습니다. 비 때문에 조금 우울해질 뻔 했지만 책 한권이 큰 위로가 되어주었고, 친구들과 만나며 즐거움을 느꼈지만 미래에 대한 고민 또한 피할 수 없었습니다. 지하철에서는 불쾌한 상황을 목격하고 분노하였지만, 정의로운 행동을 보며 아직 세상엔 좋은 사람이 많다는 것을 느끼기도 하였습니다. 이런 감정들을 잘 정리하고 소화한다면, 성장의 기회로 삼을 수 있을 것입니다., 당신의 하루는 밝음과 어둠이 교차하는 순간들로 가득 차 있었습니다. 비 오는 날의 차분함 속에서 책을 읽으며 우울함을 느끼기도 했지만, 그 안에서 위로와 희망을 발견하기도 했습니다. 친구들과의 소중한 시간 속에서는 즐거움과 동시에 미래에 대한 고민이 고개를 들었지만, 현재의 소중함을 느끼며 그 무게를 덜어놓기도 했죠. 지하철에서는 무례한 행동에 분노하면서도, 용기 있는 행동 덕분에 세상의 따뜻함을 느낄 수도 있었습니다. 이런 다양한 감정들을 받아들이고 인정하며, 그 안에서 의미를 찾아보세요.";
    private static final String ASSISTANT_RESPONSE = """
            {
                "result": "밝음과 어둠이 교차하는 순간들로 가득한 하루하루를 보내고 계시네요. 때로는 우울함과 고민이 찾아올 때도 있겠지만, 그 안에서 위로와 희망을 찾고 현재의 소중함을 느끼는 모습이 인상적이에요. 이렇게 다양한 감정들을 받아들이고 인정하다 보면, 한층 더 성장할 수 있을 거예요. 그러니 너무 걱정하지 마시고, 자신을 믿고 계속해서 나아가 보세요."
            }
            """;

    private static final List<ClovaMessageFormat> PROMPT_TEMPLATE = List.of(
            ClovaMessageFormat.of(SYSTEM, SYSTEM_PROMPT),
            ClovaMessageFormat.of(USER, USER_PROMPT),
            ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE)
    );

    public static ClovaWeeklyReportRequestDto from(String descriptions) {
        List<ClovaMessageFormat> promptTemplate = promptTemplate();
        promptTemplate.add(ClovaMessageFormat.of(USER, descriptions));

        return new ClovaWeeklyReportRequestDto(promptTemplate);
    }

    private static List<ClovaMessageFormat> promptTemplate() {
        return new ArrayList<>(PROMPT_TEMPLATE);
    }
}
