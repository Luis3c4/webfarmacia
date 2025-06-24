package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.repository.VentaRepository;
import java.util.List;

@Service
public class VentaService {
    @Autowired
    private VentaRepository ventaRepository;

    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    public Venta saveVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    public Venta getVentaById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public void deleteVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    public Long contarTotalVentas() {
        return ventaRepository.contarTotalVentas();
    }
} 