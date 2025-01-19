package site.radio.clova.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Clova Studio 로부터 응답을 받는 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
public class ClovaResponseDto {

    @Deprecated
    private static final String CLIENT_ERROR = "4";

    @Deprecated
    private static final String CLOVA_ERROR = "5";

    private static final String FALLBACK_STRING = "fallback";

    private StatusDto status;
    private ResultDto result;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PROTECTED)
    public static class StatusDto {

        private String code;
        private String message;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PROTECTED)
    public static class ResultDto {

        private ClovaMessageFormat message;
        private int inputLength;
        private int outputLength;
        private String stopReason;
        private long seed;
        private List<AiFilterDto> aiFilter;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PROTECTED)
    public static class AiFilterDto {

        private String groupName;
        private String name;
        private String score;
        private String result;
    }

    @Deprecated
    public boolean hasErrorCode() {
        return status.code.startsWith(CLIENT_ERROR) || status.code.startsWith(CLOVA_ERROR);
    }

    public String getResultMessage() {
        return result.message.getContent();
    }
}
