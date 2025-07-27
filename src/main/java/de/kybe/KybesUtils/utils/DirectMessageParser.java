package de.kybe.KybesUtils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectMessageParser {
    private final Pattern pattern;

    public DirectMessageParser(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public DirectMessage parse(String raw) {
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.matches()) return null;

        return new DirectMessage(matcher.group(1), matcher.group(2));
    }
}
