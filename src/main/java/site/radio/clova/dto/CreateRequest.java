package site.radio.clova.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import site.radio.clova.prompt.PromptTemplate;

/**
 * 이 객체는 HTTP 요청 시 {@link org.springframework.web.bind.annotation.RequestBody}가 되는 DTO 입니다. 생성형 AI에 대한 요청으로 사용되며, 요청은
 * {@link PromptTemplate}을 요구합니다. 생성자에 템플릿 메서드 패턴을 적용이 되었기 때문에 System, Assistant, User 프롬프트의 순서를 별도로 지정할 필요없이 사용할 수
 * 있습니다.
 */
@Data
@RequiredArgsConstructor
public class CreateRequest {

    @JsonUnwrapped
    private final PromptTemplate promptTemplate;

    @JsonProperty("messages")
    private final List<CreateRequestFormat> messages = new ArrayList<>();

    private CreateRequest(PromptTemplate promptTemplate, String userMessage) {
        this.promptTemplate = promptTemplate;
        messages.add(CreateRequestFormat.createSystemPrompt(promptTemplate.getSystemPrompt()));
        messages.addAll(CreateRequestFormat.createAssistantPrompt(promptTemplate.getAssistantPrompts()));
        messages.add(CreateRequestFormat.createUserMessage(userMessage));
    }

    public static CreateRequest of(PromptTemplate promptTemplate, String userMessage) {
        return new CreateRequest(promptTemplate, userMessage);
    }
}