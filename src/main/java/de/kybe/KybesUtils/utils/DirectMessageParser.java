package de.kybe.KybesUtils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DirectMessageParser(Pattern pattern) {
    public DirectMessageParser(String pattern) {
        this(Pattern.compile(pattern));
    }

    public DirectMessage parse(String raw) {
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.matches()) return null;

        return new DirectMessage(matcher.group(1), matcher.group(2));
    }
}
