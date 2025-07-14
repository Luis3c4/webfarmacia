package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.repository.VentaRepository;
import com.proyecto.farmacia.webfarmacia.dto.VentaDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {
    @Autowired
    private VentaRepository ventaRepository;

    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    public List<VentaDTO> getAllVentasDTO() {
        List<Venta> ventas = ventaRepository.findAll();
        return ventas.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public Venta saveVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    public Venta getVentaById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public VentaDTO getVentaDTOById(Long id) {
        Venta venta = ventaRepository.findById(id).orElse(null);
        return venta != null ? convertToDTO(venta) : null;
    }

    public void deleteVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    public Long contarTotalVentas() {
        return ventaRepository.contarTotalVentas();
    }

    // Método para convertir Venta a VentaDTO
    private VentaDTO convertToDTO(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setVentaId(venta.getVentaId());
        dto.setClienteNombre(venta.getUsuario() != null ? venta.getUsuario().getNombre() : "Cliente");
        dto.setFecha(venta.getFecha());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setEstado(venta.getEstado());
        dto.setMetodoEntrega(venta.getMetodoEntrega());
        dto.setObservaciones(venta.getObservaciones());
        dto.setDireccionEntrega(venta.getDireccionEntrega());
        dto.setTelefonoContacto(venta.getTelefonoContacto());
        return dto;
    }
} 