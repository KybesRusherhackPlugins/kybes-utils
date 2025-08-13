package de.kybe.KybesUtils.windows;

import de.kybe.KybesUtils.KybesUtils;
import de.kybe.KybesUtils.VcAPI.VcApi;
import de.kybe.KybesUtils.VcAPI.model.*;
import java.util.*;

public class PlayerHistoryManager {
    private final VcApi api;
    private final Map<String, PlayerHistory> playerHistories = new HashMap<>();

    public PlayerHistoryManager() {
        this.api = KybesUtils.getInstance().getVcApi();
    }

    public void addPlayer(String nameOrUuid) {
        playerHistories.putIfAbsent(nameOrUuid, new PlayerHistory(nameOrUuid));
    }

    public void removePlayer(String nameOrUuid) {
        playerHistories.remove(nameOrUuid);
    }

    public void clearHistory(String nameOrUuid) {
        PlayerHistory ph = playerHistories.get(nameOrUuid);
        if (ph != null) ph.clear();
    }

    public void refreshPlayer(String nameOrUuid) {
        PlayerHistory ph = playerHistories.get(nameOrUuid);
        if (ph == null) return;

        ph.clear();
        ph.resetPaging();
        fetchPageChats(ph, 0);
        fetchPageDeaths(ph, 0);
        fetchPageKills(ph, 0);
        fetchPageConnections(ph, 0);
    }

    public void loadNextPage(String nameOrUuid, DataType type) {
        PlayerHistory ph = playerHistories.get(nameOrUuid);
        if (ph == null) return;

        switch (type) {
            case CHATS:
                if (ph.currentPageChats < ph.pageCountChats - 1) {
                    fetchPageChats(ph, ph.currentPageChats + 1);
                }
                break;
            case DEATHS:
                if (ph.currentPageDeaths < ph.pageCountDeaths - 1) {
                    fetchPageDeaths(ph, ph.currentPageDeaths + 1);
                }
                break;
            case KILLS:
                if (ph.currentPageKills < ph.pageCountKills - 1) {
                    fetchPageKills(ph, ph.currentPageKills + 1);
                }
                break;
            case CONNECTIONS:
                if (ph.currentPageConnections < ph.pageCountConnections - 1) {
                    fetchPageConnections(ph, ph.currentPageConnections + 1);
                }
                break;
        }
    }

    private void fetchPageChats(PlayerHistory ph, int page) {
        api.getChats(ph.nameOrUuid, page).ifPresent(response -> {
            ph.pageCountChats = response.getPageCount();
            ph.currentPageChats = page;
            ph.chats.addAll(response.getChats());
        });
    }

    private void fetchPageDeaths(PlayerHistory ph, int page) {
        api.getDeaths(ph.nameOrUuid, page).ifPresent(response -> {
            ph.pageCountDeaths = response.getPageCount();
            ph.currentPageDeaths = page;
            ph.deaths.addAll(response.getDeaths());
        });
    }

    private void fetchPageKills(PlayerHistory ph, int page) {
        api.getKills(ph.nameOrUuid, page).ifPresent(response -> {
            ph.pageCountKills = response.getPageCount();
            ph.currentPageKills = page;
            ph.kills.addAll(response.getKills());
        });
    }

    private void fetchPageConnections(PlayerHistory ph, int page) {
        api.getConnections(ph.nameOrUuid, page).ifPresent(response -> {
            ph.pageCountConnections = response.getPageCount();
            ph.currentPageConnections = page;
            ph.connections.addAll(response.getConnections());
        });
    }

    public Optional<PlayerHistory> getHistory(String nameOrUuid) {
        return Optional.ofNullable(playerHistories.get(nameOrUuid));
    }

    public enum DataType {
        CHATS,
        DEATHS,
        KILLS,
        CONNECTIONS
    }

    public static class PlayerHistory {
        public final String nameOrUuid;

        public final List<ChatEntry> chats = new ArrayList<>();
        public final List<DeathEntry> deaths = new ArrayList<>();
        public final List<KillEntry> kills = new ArrayList<>();
        public final List<ConnectionEntry> connections = new ArrayList<>();

        public int currentPageChats = -1;
        public int pageCountChats = 0;

        public int currentPageDeaths = -1;
        public int pageCountDeaths = 0;

        public int currentPageKills = -1;
        public int pageCountKills = 0;

        public int currentPageConnections = -1;
        public int pageCountConnections = 0;

        public PlayerHistory(String nameOrUuid) {
            this.nameOrUuid = nameOrUuid;
        }

        public void clear() {
            chats.clear();
            deaths.clear();
            kills.clear();
            connections.clear();
            resetPaging();
        }

        public void resetPaging() {
            currentPageChats = -1;
            pageCountChats = 0;
            currentPageDeaths = -1;
            pageCountDeaths = 0;
            currentPageKills = -1;
            pageCountKills = 0;
            currentPageConnections = -1;
            pageCountConnections = 0;
        }
    }
}