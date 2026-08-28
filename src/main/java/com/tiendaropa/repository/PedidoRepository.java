package com.tiendaropa.repository;

import java.util.List;
import java.util.Optional;

import com.tiendaropa.model.EstadoComision;
import com.tiendaropa.model.EstadoPedido;
import com.tiendaropa.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Las consultas que alimentan vistas o PDF traen los items en el mismo viaje
 * (@EntityGraph). Sin eso, con open-in-view=false la sesion ya esta cerrada
 * cuando la JSP recorre pedido.items y salta LazyInitializationException.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Trae el pedido con sus lineas y el producto de cada linea. */
    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    Optional<Pedido> findById(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    Optional<Pedido> findByNumero(String numero);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    Optional<Pedido> findByReferenciaPasarela(String referenciaPasarela);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    List<Pedido> findByClienteIdOrderByFechaDesc(Long clienteId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    List<Pedido> findByClienteIdAndEstadoOrderByFechaDesc(Long clienteId, EstadoPedido estado);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    List<Pedido> findByEstadoOrderByFechaAsc(EstadoPedido estado);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    List<Pedido> findByEstadoInOrderByFechaAsc(List<EstadoPedido> estados);

    /** Historial de comisiones de un vendedor: todos los pedidos que le asociaron, mas recientes primero. */
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"items", "items.producto", "cliente"})
    List<Pedido> findByVendedorIdOrderByFechaDesc(Long vendedorId);

    @Query(
        "SELECT COALESCE(SUM(p.comisionMonto), 0) FROM Pedido p "
      + "WHERE p.vendedor.id = :vendedorId AND p.comisionEstado = :estado")
    java.math.BigDecimal sumaComisionPor(@Param("vendedorId") Long vendedorId,
                                         @Param("estado") EstadoComision estado);

    long countByEstado(EstadoPedido estado);

    @Query("select count(p) from Pedido p where p.estado <> com.tiendaropa.model.EstadoPedido.COTIZACION")
    long contarPedidosReales();

    // Sin los items a proposito: el historial no los muestra, y un "top 50"
    // con join a la coleccion obliga a Hibernate a paginar en memoria.
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD,
                 attributePaths = {"cliente", "aprobadoPor", "despachadoPor"})
    List<Pedido> findTop50ByOrderByFechaDesc();
}
