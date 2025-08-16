package de.kybe.KybesUtils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ChatMessageParser(Pattern pattern) {
    public ChatMessageParser(String pattern) {
        this(Pattern.compile(pattern));
    }

    public ChatMessage parse(String raw) {
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.matches()) return null;

        return new ChatMessage(matcher.group(1), matcher.group(2));
    }
}