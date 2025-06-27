package com.proyecto.farmacia.webfarmacia.dto;

public class CategoriaVentaDTO {
    private String category;
    private Double turnOver;
    private Double increaseBy;

    public CategoriaVentaDTO() {}

    public CategoriaVentaDTO(String category, Double turnOver, Double increaseBy) {
        this.category = category;
        this.turnOver = turnOver;
        this.increaseBy = increaseBy;
    }

    // Getters y Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getTurnOver() {
        return turnOver;
    }

    public void setTurnOver(Double turnOver) {
        this.turnOver = turnOver;
    }

    public Double getIncreaseBy() {
        return increaseBy;
    }

    public void setIncreaseBy(Double increaseBy) {
        this.increaseBy = increaseBy;
    }
} 