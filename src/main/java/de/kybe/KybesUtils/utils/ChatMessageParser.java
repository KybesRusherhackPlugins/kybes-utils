package de.kybe.KybesUtils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageParser {
    private final Pattern pattern;

    public ChatMessageParser(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public ChatMessage parse(String raw) {
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.matches()) return null;

        return new ChatMessage(matcher.group(1), matcher.group(2));
    }
}