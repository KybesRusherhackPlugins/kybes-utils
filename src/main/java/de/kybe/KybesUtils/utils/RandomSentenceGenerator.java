package de.kybe.KybesUtils.utils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RandomSentenceGenerator {
    private static final int BUFFER_SIZE = 50;
    private static final Queue<String> sentenceBuffer = new ConcurrentLinkedQueue<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<String> getRandomSentenceAsync() {
        if (!sentenceBuffer.isEmpty()) {
            return CompletableFuture.completedFuture(sentenceBuffer.poll());
        }

        return refillBufferAsync().thenApply(v -> sentenceBuffer.poll());
    }

    private static CompletableFuture<Void> refillBufferAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildApiUrl()))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(RandomSentenceGenerator::parseAndFill);
    }

    private static String buildApiUrl() {
        return "https://api.tatoeba.org/unstable/sentences?lang=eng&word_count=5-15&sort=random&limit=" + BUFFER_SIZE + "&showtrans=eng";
    }

    private static void parseAndFill(String body) {
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(body);
        JSONArray dataArray = (JSONArray) jsonResponse.get("data");

        for (Object obj : dataArray) {
            JSONObject sentenceObj = (JSONObject) obj;
            String sentence = (String) sentenceObj.get("text");
            if (sentence != null && !sentence.isEmpty()) {
                sentenceBuffer.add(sentence);
            }
        }
    }
}