package com.tiendaropa.repository;

import java.util.List;
import com.tiendaropa.model.CompraCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraClienteRepository extends JpaRepository<CompraCliente, Long> {
    List<CompraCliente> findByClienteIdOrderByFechaDesc(Long clienteId);
    /** Sirve para saber si un producto ya se vendio antes de borrarlo. */
    boolean existsByProductoId(Long productoId);
}
