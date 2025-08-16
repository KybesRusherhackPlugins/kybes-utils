package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class ConnectionsResponse {
    List<ConnectionEntry> connections;
    int total;
    int pageCount;

    public List<ConnectionEntry> getConnections() {
        return connections;
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
