package de.kybe.KybesUtils.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathMessageParser {
    static List<Pattern> templates;

    static {
        templates = loadTemplatesFromResource();
    }

    private static List<Pattern> loadTemplatesFromResource() {
        List<Pattern> patterns = new ArrayList<>();
        try (InputStream in = DeathMessageParser.class.getClassLoader().getResourceAsStream("deaths.txt")) {
            if (in == null) {
                throw new FileNotFoundException("Could not find resource: " + "deaths.txt");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (!line.isEmpty() && !line.startsWith("#")) { // skip comments and blank lines
                        patterns.add(Pattern.compile(line));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load death message templates from " + "deaths.txt", e);
        }
        return patterns;
    }

    public static Optional<DeathMessage> parse(String message) {
        for (Pattern pattern : templates) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.matches()) {
                String v = getGroup(matcher, "v");
                String a = getGroup(matcher, "a");
                String w = getGroup(matcher, "w");
                String m = getGroup(matcher, "m");

                return Optional.of(new DeathMessage(v, a, w, m));
            }
        }
        return Optional.empty();
    }

    private static String getGroup(Matcher matcher, String name) {
        try {
            return matcher.group(name);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }
}