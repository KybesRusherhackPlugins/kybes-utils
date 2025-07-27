package de.kybe.KybesUtils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutboundDirectMessageParser {
    private final Pattern pattern;

    public OutboundDirectMessageParser(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public OutboundDirectMessage parse(String raw) {
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.matches()) return null;

        return new OutboundDirectMessage(matcher.group(1), matcher.group(2));
    }
}
