package de.kybe.KybesUtils.utils;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.rusherhack.client.api.utils.ChatUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class TermbinUploader {
    public static String upload(String content) {
        try (Socket socket = new Socket("termbin.com", 9999);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.write(content);
            writer.flush();
            socket.shutdownOutput();

            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload to termbin: " + e.getMessage();
        }
    }

    public static CompletableFuture<String> uploadAsync(String content) {
        return CompletableFuture.supplyAsync(() -> upload(content));
    }

    public static void uploadAsyncChat(String content) {
        uploadAsync(content).thenAccept(s -> {
            ChatUtils.print(
                    Component.literal(s).withStyle(
                                    Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, s))
                    )
            );
        });
    }
}