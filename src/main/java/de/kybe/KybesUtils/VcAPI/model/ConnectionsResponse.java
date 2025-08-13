package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class ConnectionsResponse {
    List<ConnectionEntry> connections;
    int total;
    int pageCount;

    public List<ConnectionEntry> getConnections() {
        return connections;
    }

    public int getTotal() {
        return total;
    }

    public int getPageCount() {
        return pageCount;
    }
}
