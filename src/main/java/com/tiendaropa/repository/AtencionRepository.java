package com.tiendaropa.repository;

import java.time.LocalDateTime;
import java.util.List;
import com.tiendaropa.model.Atencion;
import com.tiendaropa.model.EstadoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    List<Atencion> findByFechaInicioBetweenOrderByFechaInicioDesc(LocalDateTime desde, LocalDateTime hasta);

    List<Atencion> findByEstadoOrderByFechaInicioDesc(EstadoAtencion estado);

    List<Atencion> findByClienteIdOrderByFechaInicioDesc(Long clienteId);

    List<Atencion> findTop50ByOrderByFechaInicioDesc();

    /** Personas distintas atendidas en el rango (clientes registrados). */
    @Query("SELECT COUNT(DISTINCT a.cliente.id) FROM Atencion a "
         + "WHERE a.cliente IS NOT NULL AND a.fechaInicio BETWEEN :desde AND :hasta")
    long contarClientesDistintos(LocalDateTime desde, LocalDateTime hasta);

    /** Atenciones a visitantes sin registro dentro del rango. */
    @Query("SELECT COUNT(a) FROM Atencion a "
         + "WHERE a.cliente IS NULL AND a.fechaInicio BETWEEN :desde AND :hasta")
    long contarAnonimos(LocalDateTime desde, LocalDateTime hasta);
}
