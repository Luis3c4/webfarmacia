package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import com.proyecto.farmacia.webfarmacia.repository.DetalleVentaRepository;
import com.proyecto.farmacia.webfarmacia.dto.CategoriaVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.ProductoVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.DetalleVentaDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetalleVentaService {
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public List<DetalleVenta> getAllDetalleVentas() {
        return detalleVentaRepository.findAll();
    }

    public List<DetalleVentaDTO> getDetalleVentaDTOByVentaId(Long ventaId) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        return detalles.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
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

    public Double calcularNetSalesValue() {
        Double netSalesValue = detalleVentaRepository.calcularNetSalesValue();
        return netSalesValue != null ? netSalesValue : 0.0;
    }

    public List<CategoriaVentaDTO> getMejoresCategoriasVentas() {
        List<Object[]> resultados = detalleVentaRepository.getMejoresCategoriasVentasNativo();
        return resultados.stream()
            .map(row -> new CategoriaVentaDTO(
                (String) row[0],           // category
                ((Number) row[1]).doubleValue(), // turn_over
                ((Number) row[2]).doubleValue()  // increase_by
            ))
            .toList();
    }

    public List<ProductoVentaDTO> getBestSellingProducts() {
        List<Object[]> resultados = detalleVentaRepository.getBestSellingProductsNativo();
        return resultados.stream()
            .map(row -> new ProductoVentaDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).doubleValue()
            ))
            .toList();
    }

    // Método para obtener detalles de venta por ID de venta (mantener compatibilidad)
    public List<DetalleVenta> getDetalleVentaByVentaId(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    // Método para convertir DetalleVenta a DetalleVentaDTO
    private DetalleVentaDTO convertToDTO(DetalleVenta detalle) {
        DetalleVentaDTO dto = new DetalleVentaDTO();
        dto.setDetalleId(detalle.getDetalleId());
        dto.setVentaId(detalle.getVenta().getVentaId());
        dto.setProductoNombre(detalle.getProducto() != null ? detalle.getProducto().getNombre() : "Producto no encontrado");
        dto.setProductoId(detalle.getProducto() != null ? detalle.getProducto().getProductoId() : null);
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setCosteUnitario(detalle.getCosteUnitario());
        dto.setDescuentos(detalle.getDescuentos());
        return dto;
    }
} 