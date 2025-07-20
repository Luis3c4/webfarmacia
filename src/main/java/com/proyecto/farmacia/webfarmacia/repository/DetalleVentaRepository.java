package com.proyecto.farmacia.webfarmacia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    @Query("SELECT SUM((dv.precioUnitario - dv.costeUnitario) * dv.cantidad) - SUM(dv.descuentos) " +
           "FROM DetalleVenta dv JOIN dv.venta v")
    Double calcularTotalProfit();

    @Query("SELECT SUM(dv.precioUnitario * dv.cantidad) FROM DetalleVenta dv")
    Double calcularTotalRevenue();

    @Query("SELECT SUM(dv.precioUnitario * dv.cantidad) - SUM(dv.descuentos) FROM DetalleVenta dv")
    Double calcularNetSalesValue();

    @Query(value = """
        WITH ventas_categoria AS (
          SELECT 
            DATE_TRUNC('month', v.fecha) AS mes,
            p.categoria AS category,
            SUM(dv.precio_unitario * dv.cantidad) AS turn_over
          FROM detalle_venta dv
          JOIN ventas v ON dv.venta_id = v.venta_id
          JOIN productos p ON dv.producto_id = p.producto_id
          GROUP BY DATE_TRUNC('month', v.fecha), p.categoria
        ),
        ventas_con_variacion AS (
          SELECT 
            category,
            mes,
            turn_over,
            LAG(turn_over) OVER (PARTITION BY category ORDER BY mes) AS turn_over_prev,
            ROUND(
              CASE 
                WHEN LAG(turn_over) OVER (PARTITION BY category ORDER BY mes) IS NULL THEN 0
                ELSE ((turn_over - LAG(turn_over) OVER (PARTITION BY category ORDER BY mes)) / NULLIF(LAG(turn_over) OVER (PARTITION BY category ORDER BY mes), 0)) * 100
              END, 2
            ) AS increase_by
          FROM ventas_categoria
        )
        SELECT category, turn_over, increase_by
        FROM ventas_con_variacion
        WHERE mes = DATE_TRUNC('month', CURRENT_DATE)
        ORDER BY turn_over DESC
        LIMIT 3
        """, nativeQuery = true)
    List<Object[]> getMejoresCategoriasVentasNativo();

    @Query(value = """
        WITH ventas_producto AS (
          SELECT 
            DATE_TRUNC('month', v.fecha) AS mes,
            p.producto_id,
            p.nombre AS producto,
            p.categoria AS category,
            SUM(dv.precio_unitario * dv.cantidad) AS turn_over
          FROM detalle_venta dv
          JOIN ventas v ON dv.venta_id = v.venta_id
          JOIN productos p ON dv.producto_id = p.producto_id
          GROUP BY DATE_TRUNC('month', v.fecha), p.producto_id, p.nombre, p.categoria
        ),
        ventas_con_variacion AS (
          SELECT 
            producto_id,
            producto,
            category,
            mes,
            turn_over,
            LAG(turn_over) OVER (PARTITION BY producto_id ORDER BY mes) AS turn_over_prev,
            ROUND(
              CASE 
                WHEN LAG(turn_over) OVER (PARTITION BY producto_id ORDER BY mes) IS NULL THEN 0
                WHEN LAG(turn_over) OVER (PARTITION BY producto_id ORDER BY mes) = 0 THEN 100
                ELSE ((turn_over - LAG(turn_over) OVER (PARTITION BY producto_id ORDER BY mes)) / LAG(turn_over) OVER (PARTITION BY producto_id ORDER BY mes)) * 100
              END, 2
            ) AS increase_by
          FROM ventas_producto
        )
        SELECT 
          producto_id,
          producto,
          category,
          turn_over,
          increase_by
        FROM ventas_con_variacion
        WHERE mes = DATE_TRUNC('month', CURRENT_DATE)
        ORDER BY turn_over DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getBestSellingProductsNativo();

    // Método para obtener detalles de venta por ID de venta
    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.ventaId = :ventaId")
    List<DetalleVenta> findByVentaId(@Param("ventaId") Long ventaId);
    
    // Nueva consulta para productos más vendidos por cantidad (no por valor)
    @Query(value = """
        SELECT 
          p.producto_id,
          p.nombre AS producto,
          p.categoria AS category,
          SUM(dv.cantidad) AS cantidad_vendida,
          SUM(dv.precio_unitario * dv.cantidad) AS valor_total
        FROM detalle_venta dv
        JOIN ventas v ON dv.venta_id = v.venta_id
        JOIN productos p ON dv.producto_id = p.producto_id
        GROUP BY p.producto_id, p.nombre, p.categoria
        ORDER BY cantidad_vendida DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getProductosMasVendidosPorCantidad();
} 