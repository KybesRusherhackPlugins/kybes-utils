package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class DeathsResponse {
    List<DeathEntry> deaths;
    int total;
    int pageCount;

    public List<DeathEntry> getDeaths() {
        return deaths;
    }

    public int getTotal() {
        return total;
    }

    public int getPageCount() {
        return pageCount;
    }
}
