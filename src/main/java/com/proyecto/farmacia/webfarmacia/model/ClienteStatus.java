package com.proyecto.farmacia.webfarmacia.model;

public enum ClienteStatus {
    TAKING_RETURN("Taking Return"),
    NOT_TAKING_RETURN("Not Taking Return");

    private final String displayName;

    ClienteStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 