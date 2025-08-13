package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class KillsResponse {
    List<KillEntry> kills;
    int total;
    int pageCount;

    public List<KillEntry> getKills() {
        return kills;
    }

    public int getTotal() {
        return total;
    }

    public int getPageCount() {
        return pageCount;
    }
}
