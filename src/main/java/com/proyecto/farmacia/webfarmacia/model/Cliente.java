package com.proyecto.farmacia.webfarmacia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String producto;
    private String numeroContacto;
    private String correo;
    
    @Enumerated(EnumType.STRING)
    private ClienteStatus estado;
    
    // Constructor por defecto
    public Cliente() {
    }

    // Constructor con parámetros
    public Cliente(String nombre, String producto, String numeroContacto, String correo, ClienteStatus estado) {
        this.nombre = nombre;
        this.producto = producto;
        this.numeroContacto = numeroContacto;
        this.correo = correo;
        this.estado = estado;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getNumeroContacto() {
        return numeroContacto;
    }

    public void setNumeroContacto(String numeroContacto) {
        this.numeroContacto = numeroContacto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public ClienteStatus getEstado() {
        return estado;
    }

    public void setEstado(ClienteStatus estado) {
        this.estado = estado;
    }

} 