package de.kybe.KybesUtils.VcAPI.model;

import java.util.List;

public class DeathsResponse {
    List<DeathEntry> deaths;
    int total;
    int pageCount;

    public List<DeathEntry> getDeaths() {
        return deaths;
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
