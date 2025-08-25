package net.lucxtv.dojoclan.model;

import java.util.UUID;

public class ClanMember {

    private final UUID uuid;
    private final String name;
    private final boolean online;

    public ClanMember(UUID uuid, String name, boolean online) {
        this.uuid = uuid;
        this.name = name;
        this.online = online;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }
}