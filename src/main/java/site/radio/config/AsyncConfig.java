package site.radio.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean
    public Executor httpRequestExecutor() {
        // 이 예시는 최대 10명의 사용자가 동시에 각각 7건씩 요청할 수 있는 상황 (총 70건 동시 요청)
        // 외부 API의 한도와 평균 응답 시간(7초)을 고려
        // FIXME: 실제 호출은 레이트 리미터로 조절하여 분당 128건을 넘지 않도록 해야 함.
        int corePoolSize = 70;    // 최대 동시 요청 수 예상 (10명 x 7)
        int maxPoolSize = 100;    // 버스트 상황을 고려하여 확장
        int queueCapacity = 100;  // 대기 큐 용량

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setThreadNamePrefix("HttpAsyncExecutor-");
        executor.setRejectedExecutionHandler(new AbortPolicy());
        executor.initialize();
        return executor;
    }
}
