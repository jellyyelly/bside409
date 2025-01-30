package site.radio.reply;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoTypeMessageExtractor {

    private static final Pattern pattern = Pattern.compile("F.*:\\s*(.*?)\\s*---\\s*T.*:\\s*(.*)", Pattern.MULTILINE);
    private static final int F_INDEX = 1;
    private static final int T_INDEX = 2;

    public static TwoTypeMessage extract(String message) {
        Matcher replyMatcher = pattern.matcher(message);

        if (!replyMatcher.find()) {
            throw new IllegalArgumentException("패턴을 찾지 못했습니다. 답변받은 문장: " + message);
        }

        String messageForF = replyMatcher.group(F_INDEX);
        String messageForT = replyMatcher.group(T_INDEX);

        return TwoTypeMessage.of(messageForF, messageForT);
    }
}
