package de.kybe.KybesUtils.utils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class RandomSentenceGenerator {
    private static final int BUFFER_SIZE = 50;
    private static final Queue<String> sentenceBuffer = new LinkedList<>();

    public static synchronized String getRandomSentence() {
        try {
            if (sentenceBuffer.isEmpty()) {
                refillBuffer();
            }
            return sentenceBuffer.poll();
        } catch (Exception e) {
            return "";
        }
    }

    private static void refillBuffer() throws Exception {
        String apiUrl = "https://api.tatoeba.org/unstable/sentences?lang=eng&word_count=5-15&sort=random&limit=" + BUFFER_SIZE + "&showtrans=eng";

        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + status);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        con.disconnect();

        JSONObject jsonResponse = (JSONObject) JSONValue.parse(content.toString());
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