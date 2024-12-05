package bsise.server.clova.service;

import static bsise.server.user.domain.Preference.F;

import bsise.server.user.domain.Preference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageExtractor {

    private static final Pattern pattern = Pattern.compile("F.*:\\s*(.*?)\\s*---\\s*T.*:\\s*(.*)", Pattern.MULTILINE);
    private static final int F_INDEX = 1;
    private static final int T_INDEX = 2;

    public static String extract(String message, Preference preference) {
        Matcher replyMatcher = pattern.matcher(message);

        if (!replyMatcher.find()) {
            throw new IllegalArgumentException("패턴을 찾지 못했습니다. 답변받은 문장: " + message);
        }

        if (preference == F) {
            return replyMatcher.group(F_INDEX);
        }
        return replyMatcher.group(T_INDEX);
    }
}
