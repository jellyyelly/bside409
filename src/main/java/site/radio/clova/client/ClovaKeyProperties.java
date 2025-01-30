package site.radio.clova.client;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@ConfigurationProperties("clova.secret")
public class ClovaKeyProperties {

    @NotEmpty
    private final String apiKey;

    @NotEmpty
    private final String apigwKey;

    private final String requestId;

    public ClovaKeyProperties(String apiKey, String apigwKey, @DefaultValue("default") String requestId) {
        this.apiKey = apiKey;
        this.apigwKey = apigwKey;
        this.requestId = requestId;
    }
}
