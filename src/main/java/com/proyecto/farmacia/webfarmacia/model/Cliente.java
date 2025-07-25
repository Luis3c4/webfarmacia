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
    private Long clienteId;
    private String nombre;
    private String producto;
    private String numeroContacto;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private ClienteStatus estado;
    
    // Constructor por defecto
    public Cliente() {
    }

    // Constructor con parámetros
    public Cliente(String nombre, String producto, String numeroContacto, String email, ClienteStatus estado) {
        this.nombre = nombre;
        this.producto = producto;
        this.numeroContacto = numeroContacto;
        this.email = email;
        this.estado = estado;
    }

    // Getters and Setters
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClienteStatus getEstado() {
        return estado;
    }

    public void setEstado(ClienteStatus estado) {
        this.estado = estado;
    }

} 