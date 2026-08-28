package com.tiendaropa.repository;

import com.tiendaropa.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /** Sirve para saber si un producto ya se vendio antes de borrarlo. */
    boolean existsByProductoId(Long productoId);
}
