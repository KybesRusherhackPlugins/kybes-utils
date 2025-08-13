package de.kybe.KybesUtils.VcAPI.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ChatEntry {
    String playerName;
    UUID uuid;
    OffsetDateTime time;
    String chat;

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public String getChat() {
        return chat;
    }
}
