package de.kybe.KybesUtils.VcAPI.model;

import java.time.OffsetDateTime;

public class KillEntry {
    OffsetDateTime time;
    String deathMessage;
    String victimPlayerName;
    String victimPlayerUuid;
    String killerPlayerName;
    String killerPlayerUuid;
    String weaponName;
    String killerMob;

    public OffsetDateTime getTime() {
        return time;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public String getVictimPlayerName() {
        return victimPlayerName;
    }

    public String getVictimPlayerUuid() {
        return victimPlayerUuid;
    }

    public String getKillerPlayerName() {
        return killerPlayerName;
    }

    public String getKillerPlayerUuid() {
        return killerPlayerUuid;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getKillerMob() {
        return killerMob;
    }
}
