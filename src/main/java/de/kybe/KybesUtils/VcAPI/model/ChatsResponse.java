package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class ChatsResponse {
    List<ChatEntry> chats;
    int total;
    int pageCount;

    public List<ChatEntry> getChats() {
        return chats;
    }

    @SuppressWarnings("unused")
    public int getTotal() {
        return total;
    }

    @SuppressWarnings("unused")
    public int getPageCount() {
        return pageCount;
    }
}