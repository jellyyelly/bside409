package site.radio.clova.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.radio.clova.client.ClovaFeignClient;
import site.radio.clova.dto.CreateRequest;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.prompt.PromptTemplate;

@Slf4j
@Profile({"prod", "dev"})
@Service
@RequiredArgsConstructor
public class ClovaService {

    protected final ClovaFeignClient client;

    public CreateResponse sendWithPromptTemplate(PromptTemplate promptTemplate, String userMessage) {
        return client.sendToClova(CreateRequest.of(promptTemplate, userMessage));
    }

    @Async("httpRequestExecutor")
    public CompletableFuture<CreateResponse> sendAsyncWithPromptTemplate(PromptTemplate promptTemplate,
                                                                         String userMessage) {
        return CompletableFuture.completedFuture(client.sendToClova(CreateRequest.of(promptTemplate, userMessage)))
                .exceptionally(t -> {
                    log.error("외부 API 호출 중 예외 발생", t);
                    throw new CompletionException(t);
                })
                .orTimeout(20, TimeUnit.SECONDS);
    }
}
