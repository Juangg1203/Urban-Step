package com.tiendaropa.service;

import java.math.BigDecimal;
import java.util.List;

import com.tiendaropa.model.EstadoComision;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.repository.PedidoRepository;
import org.springframework.stereotype.Service;

/**
 * Consultas de comision. El calculo mismo vive en PedidoService (junto al
 * cambio de estado que lo dispara); aqui solo se lee lo que ya quedo
 * guardado en cada pedido.
 */
@Service
public class ComisionService {

    private final PedidoRepository pedidoRepo;

    public ComisionService(PedidoRepository pedidoRepo) {
        this.pedidoRepo = pedidoRepo;
    }

    /** Todos los pedidos que le generaron comision a un vendedor, mas recientes primero. */
    public List<Pedido> historialDe(Long vendedorId) {
        return pedidoRepo.findByVendedorIdOrderByFechaDesc(vendedorId);
    }

    /** Ya entregado: la comision es definitiva. */
    public BigDecimal confirmadaDe(Long vendedorId) {
        return pedidoRepo.sumaComisionPor(vendedorId, EstadoComision.CONFIRMADA);
    }

    /** Pago aceptado, pero el pedido todavia no llega al cliente. */
    public BigDecimal pendienteDe(Long vendedorId) {
        return pedidoRepo.sumaComisionPor(vendedorId, EstadoComision.PENDIENTE);
    }

    public BigDecimal totalDe(Long vendedorId) {
        return confirmadaDe(vendedorId).add(pendienteDe(vendedorId));
    }
}
