package site.radio.clova.prompt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PromptRole {

    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String role;
}
