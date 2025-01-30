package site.radio.clova.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.radio.clova.client.ClovaFeignClient;
import site.radio.clova.dto.CreateRequest;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.prompt.PromptTemplate;

@Profile({"prod", "dev"})
@Service
@RequiredArgsConstructor
public class ClovaService {

    protected final ClovaFeignClient client;

    public CreateResponse sendWithPromptTemplate(PromptTemplate promptTemplate, String userMessage) {
        return client.sendToClova(CreateRequest.of(promptTemplate, userMessage));
    }
}
