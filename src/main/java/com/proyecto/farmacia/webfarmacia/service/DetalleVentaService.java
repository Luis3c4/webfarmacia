package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import com.proyecto.farmacia.webfarmacia.repository.DetalleVentaRepository;
import java.util.List;

@Service
public class DetalleVentaService {
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public List<DetalleVenta> getAllDetalleVentas() {
        return detalleVentaRepository.findAll();
    }

    public DetalleVenta saveDetalleVenta(DetalleVenta detalleVenta) {
        return detalleVentaRepository.save(detalleVenta);
    }

    public DetalleVenta getDetalleVentaById(Long id) {
        return detalleVentaRepository.findById(id).orElse(null);
    }

    public void deleteDetalleVenta(Long id) {
        detalleVentaRepository.deleteById(id);
    }

    public Double calcularTotalProfit() {
        Double profit = detalleVentaRepository.calcularTotalProfit();
        return profit != null ? profit : 0.0;
    }

    public Double calcularTotalRevenue() {
        Double revenue = detalleVentaRepository.calcularTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }
} 