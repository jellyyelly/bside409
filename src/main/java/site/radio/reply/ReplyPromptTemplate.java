package site.radio.reply;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import site.radio.clova.prompt.PromptTemplate;

@ConfigurationProperties(prefix = "reply")
public class ReplyPromptTemplate extends PromptTemplate {

    public ReplyPromptTemplate(@DefaultValue("0.5") double temperature,
                               @DefaultValue("0") int topK,
                               @DefaultValue("0.8") double topP,
                               @DefaultValue("true") boolean includeAiFilters,
                               @DefaultValue("6.0") double repeatPenalty,
                               List<String> stopBefore,
                               @DefaultValue("500") int maxTokens,
                               long seed,
                               String systemPrompt,
                               List<String> assistantPrompts) {
        super(temperature, topK, topP, includeAiFilters, repeatPenalty, stopBefore, maxTokens, seed, systemPrompt,
                assistantPrompts);
    }
}
