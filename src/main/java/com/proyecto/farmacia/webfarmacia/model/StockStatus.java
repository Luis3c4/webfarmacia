package com.proyecto.farmacia.webfarmacia.model;

public enum StockStatus {
    IN_STOCK("In-stock"),
    OUT_OF_STOCK("Out of stock"),
    LOW_STOCK("Low stock");

    private final String displayName;

    StockStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 