package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
import com.proyecto.farmacia.webfarmacia.repository.DetalleCompraRepository;
import java.util.List;

@Service
public class DetalleCompraService {
    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    public List<DetalleCompra> getAllDetalleCompras() {
        return detalleCompraRepository.findAll();
    }

    public DetalleCompra saveDetalleCompra(DetalleCompra detalleCompra) {
        return detalleCompraRepository.save(detalleCompra);
    }

    public DetalleCompra getDetalleCompraById(Long id) {
        return detalleCompraRepository.findById(id).orElse(null);
    }

    public void deleteDetalleCompra(Long id) {
        detalleCompraRepository.deleteById(id);
    }

    public Double calcularNetPurchaseValue() {
        Double netPurchaseValue = detalleCompraRepository.calcularNetPurchaseValue();
        return netPurchaseValue != null ? netPurchaseValue : 0.0;
    }
} 