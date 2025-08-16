package de.kybe.KybesUtils.VcAPI.model;

import java.time.OffsetDateTime;

public class ConnectionEntry {
    OffsetDateTime time;
    ConnectionType connection;

    public OffsetDateTime getTime() {
        return time;
    }

    public ConnectionType getConnection() {
        return connection;
    }

    public enum ConnectionType {
        JOIN,
        LEAVE
    }
}
