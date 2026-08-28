package com.tiendaropa.repository;

import java.util.List;

import com.tiendaropa.model.Resena;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    @EntityGraph(attributePaths = {"cliente"})
    List<Resena> findByProductoIdOrderByFechaDesc(Long productoId);

    boolean existsByPedidoIdAndProductoId(Long pedidoId, Long productoId);

    @Query("SELECT COALESCE(AVG(r.calificacion), 0) FROM Resena r WHERE r.producto.id = :productoId")
    double promedioDe(@Param("productoId") Long productoId);

    long countByProductoId(Long productoId);
}
