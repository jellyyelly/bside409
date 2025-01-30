package site.radio.clova.dto;

import static site.radio.clova.prompt.PromptRole.ASSISTANT;
import static site.radio.clova.prompt.PromptRole.SYSTEM;
import static site.radio.clova.prompt.PromptRole.USER;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.clova.prompt.PromptRole;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateRequestFormat {

    private String role;
    private String content;

    public static CreateRequestFormat of(PromptRole role, String content) {
        return new CreateRequestFormat(role.getRole(), content);
    }

    public static CreateRequestFormat createSystemPrompt(String systemPrompt) {
        return CreateRequestFormat.of(SYSTEM, systemPrompt);
    }

    public static List<CreateRequestFormat> createAssistantPrompt(List<String> assistantPrompt) {
        return assistantPrompt.stream()
                .map(each -> CreateRequestFormat.of(ASSISTANT, each))
                .toList();
    }

    public static CreateRequestFormat createUserMessage(String userMessage) {
        return CreateRequestFormat.of(USER, userMessage);
    }
}
