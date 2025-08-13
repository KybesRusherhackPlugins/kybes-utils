package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class ChatsResponse {
    List<ChatEntry> chats;
    int total;
    int pageCount;

    public List<ChatEntry> getChats() {
        return chats;
    }

    public int getTotal() {
        return total;
    }

    public int getPageCount() {
        return pageCount;
    }
}