package site.radio.clova.weekly;

import static site.radio.clova.dto.ClovaRole.ASSISTANT;
import static site.radio.clova.dto.ClovaRole.SYSTEM;
import static site.radio.clova.dto.ClovaRole.USER;

import site.radio.clova.dto.ClovaMessageFormat;
import site.radio.clova.dto.ClovaRequestDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClovaWeeklyReportRequestDto implements ClovaRequestDto {

    @JsonProperty("temperature")
    private final double temperature = 0.5;

    @JsonProperty("topK")
    private final int topK = 0;

    @JsonProperty("topP")
    private final double topP = 0.8;

    @JsonProperty("repeatPenalty")
    private final double repeatPenalty = 5.0;

    @JsonProperty("stopBefore")
    private final List<String> stopBefore = Collections.emptyList();

    @JsonProperty("maxTokens")
    private final int maxTokens = 500;

    @JsonProperty("includeAiFilters")
    private final boolean includeAiFilters = true;

    @JsonProperty("seed")
    private final long seed = 3923155011L;

    private static final String USER_PROMPT = "다양한 경험을 하면서 여러 감정을 느낀 하루였네요. 팀 프로젝트에서는 충돌로 인해 분노와 슬픔을 겪으셨겠지만, 소통의 중요성을 배우는 계기가 되었을 것 입니다. 주말에는 가족과 즐거운 시간을 보내며 연대감을 다지셨다니 정말 좋으셨겠어요. 거기다 새로 시작한 취미로부터 삶의 열망과 열정을 얻으시다니, 매일이 새롭게 느껴지실 것 같네요., 여러 감정들이 오고간 하루였습니다. 비 때문에 조금 우울해질 뻔 했지만 책 한권이 큰 위로가 되어주었고, 친구들과 만나며 즐거움을 느꼈지만 미래에 대한 고민 또한 피할 수 없었습니다. 지하철에서는 불쾌한 상황을 목격하고 분노하였지만, 정의로운 행동을 보며 아직 세상엔 좋은 사람이 많다는 것을 느끼기도 하였습니다. 이런 감정들을 잘 정리하고 소화한다면, 성장의 기회로 삼을 수 있을 것입니다., 당신의 하루는 밝음과 어둠이 교차하는 순간들로 가득 차 있었습니다. 비 오는 날의 차분함 속에서 책을 읽으며 우울함을 느끼기도 했지만, 그 안에서 위로와 희망을 발견하기도 했습니다. 친구들과의 소중한 시간 속에서는 즐거움과 동시에 미래에 대한 고민이 고개를 들었지만, 현재의 소중함을 느끼며 그 무게를 덜어놓기도 했죠. 지하철에서는 무례한 행동에 분노하면서도, 용기 있는 행동 덕분에 세상의 따뜻함을 느낄 수도 있었습니다. 이런 다양한 감정들을 받아들이고 인정하며, 그 안에서 의미를 찾아보세요.";
    private static final String ASSISTANT_RESPONSE1 = "밝음과 어둠이 교차하는 순간들로 가득한 하루하루를 보내고 계시네요. 때로는 우울함과 고민이 찾아올 때도 있겠지만, 그 안에서 위로와 희망을 찾고 현재의 소중함을 느끼는 모습이 인상적이에요. 이렇게 다양한 감정들을 받아들이고 인정하다 보면, 한층 더 성장할 수 있을 거예요. 그러니 너무 걱정하지 마시고, 자신을 믿고 계속해서 나아가 보세요.";
    private static final String ASSISTANT_RESPONSE2 = "여러 감정들이 오고 가는 하루하루를 보내고 계시는군요. 이러한 감정들을 잘 정리하고 소화한다면 성장의 기회로 삼을 수 있을거에요. 그리고 밝은 면만 보면서 긍정적으로 생각하면 좀 더 행복한 하루를 보낼 수 있을겁니다! 😊";
    private static final String ASSISTANT_RESPONSE3 = "많은 경험을 하며 여러 감정을 느끼고 계신 것 같습니다. 그런 다양한 감정들은 우리를 더욱 풍부하게 만들어 주는 요소 중 하나죠. 하지만 가끔씩은 버거울 때도 있을 거예요. 그럴 때는 잠시 쉬어가며 스스로를 돌보아 주는건 어떠신가요? 작은 여유가 큰 힘이 될 수 있답니다. 🌿";
    private static final String ASSISTANT_RESPONSE4 = "비가 내리는 날에는 기분이 가라앉기 쉽지만 독서를 통해 위로를 받으셨다니 다행이네요. 또 한편으로는 친구들과 함께하며 즐거우면서도 미래에 대한 고민을 하게 되셨군요. 자연스러운 일이지만 그로 인해 불안해 하지 않으셔도 됩니다. 지금처럼 주변 사람들과 어울리며 현재를 즐기면서 할 수 있는 일부터 차근차근 해나가시면 되니까요. 그렇게 하루하루를 소중히 보내다 보면 어느샌가 원하는 곳에 도달해 있지 않을까요? ☀️";
    private static final String ASSISTANT_RESPONSE5 = "분노와 슬픔, 즐거움과 고민 등 다양한 감정을 겪은 하루였군요. 그렇지만 그런 감정들을 통해 배우고 성장하신 것 같습니다. 부정적인 감정이 들더라도 그 안에서 긍정적인 부분을 찾아보시는 건 어떤가요? 그러면 힘든 하루를 보내도 조금은 더 나은 내일을 기대할 수 있을 거예요. 오늘도 수고하셨습니다! 🌟";
    private static final String SYSTEM_PROMPT = """
            시스템 목표:
            입력받은 일일 분석들을 분석하여 지난 한 주동안 있었던 일들을 추론해서 위로의 한마디를 생성해주세요.
            입력받은 일일 분석들은 유저가 작성한 편지에 대해 클로바가 생성한 일일 분석들입니다.
            
            응답할 내용:
            - 반드시 공백 포함 200자 이내로 생성하세요
            - 사용자가 입력한 문장들을 단순 복사하지 않고, 문장들을 종합하여 하나의 감정 통찰과 행동 지침을 제공하세요
            
            응답 예시:
            "반복되는 일상과 끊임없는 공부, 코딩으로 인해 조금씩 지쳐가는 모습이 보여요. 이런 상황에서는 지속적인 노력과 열정만으로는 극복하기 어려울 때도 있죠. 가끔은 숨을 돌리고 여유를 가지며 마음을 편안하게 해주는 건 어떨까요? 짧은 휴식이나 취미활동을 통해 머리를 식히고 활력을 되찾을 수 있을 거에요. 💪"
            
            주의사항:
            1. 답변에 "오늘" 이란 단어가 들어가면 안됩니다
            2. 의문문으로 끝나지 않고, 부드러운 어투로 권유하세요
            3. 반드시 생성한 문장에 어울리는 이모지를 사용하세요
            4. 분석할 수 없다면 '분석하기 어려운 내용이 있는 것 같아요.' 라고 응답하세요
            """;

    @JsonProperty("messages")
    private List<ClovaMessageFormat> messages;

    public static ClovaWeeklyReportRequestDto from(String descriptions) {
        List<ClovaMessageFormat> promptTemplate = new ArrayList<>();
        promptTemplate.add(ClovaMessageFormat.of(SYSTEM, SYSTEM_PROMPT));
        promptTemplate.add(ClovaMessageFormat.of(USER, USER_PROMPT));
        promptTemplate.add(ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE1));
        promptTemplate.add(ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE2));
        promptTemplate.add(ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE3));
        promptTemplate.add(ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE4));
        promptTemplate.add(ClovaMessageFormat.of(ASSISTANT, ASSISTANT_RESPONSE5));
        promptTemplate.add(ClovaMessageFormat.of(USER, descriptions));

        return new ClovaWeeklyReportRequestDto(promptTemplate);
    }
}
