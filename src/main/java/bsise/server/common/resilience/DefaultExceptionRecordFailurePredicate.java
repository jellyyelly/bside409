package bsise.server.common.resilience;

import feign.FeignException;
import feign.RetryableException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class DefaultExceptionRecordFailurePredicate implements Predicate<Throwable> {

    /**
     * circuitBreaker 가 어떤 예외를 Fail 처리할 것인지 결정하는 Predicate 입니다. FeignServerException 은 기본적으로 Fail 처리 됩니다.
     *
     * @param throwable the input argument
     * @return 반환 값이 true => Fail 로 처리
     */
    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            return true;
        }

        if (throwable instanceof RetryableException) {
            return true;
        }

        return throwable instanceof FeignException.FeignServerException; // Fail 처리 (true)
    }
}
