package site.radio.clova.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
public abstract class PromptTemplate {

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    @JsonProperty("temperature")
    private final double temperature;

    @Range(min = 0, max = 120)
    @JsonProperty("topK")
    private final int topK;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    @JsonProperty("topP")
    private final double topP;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "10.0", inclusive = true)
    @JsonProperty("repeatPenalty")
    private final double repeatPenalty;

    @JsonProperty("stopBefore")
    private final List<String> stopBefore;

    @JsonProperty("includeAiFilters")
    private final boolean includeAiFilters;

    @Range(min = 500, max = 2000)
    @JsonProperty("maxTokens")
    private final int maxTokens;

    @JsonProperty("seed")
    private final long seed;

    @NotEmpty
    private final String systemPrompt;

    private final List<String> assistantPrompts;

    public PromptTemplate(@DefaultValue("0.5") double temperature,
                          @DefaultValue("0") int topK,
                          @DefaultValue("0.8") double topP,
                          @DefaultValue("true") boolean includeAiFilters,
                          @DefaultValue("6.0") double repeatPenalty,
                          List<String> stopBefore,
                          @DefaultValue("500") int maxTokens,
                          long seed,
                          String systemPrompt,
                          List<String> assistantPrompts) {
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
        this.includeAiFilters = includeAiFilters;
        this.repeatPenalty = repeatPenalty;
        this.stopBefore = stopBefore != null ? stopBefore : Collections.emptyList();
        this.maxTokens = maxTokens;
        this.seed = seed;
        this.systemPrompt = systemPrompt;
        this.assistantPrompts = assistantPrompts != null ? assistantPrompts : Collections.emptyList();
    }
}
