package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class KillsResponse {
    List<KillEntry> kills;
    int total;
    int pageCount;

    public List<KillEntry> getKills() {
        return kills;
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
