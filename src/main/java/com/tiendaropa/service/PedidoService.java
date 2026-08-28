package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tiendaropa.dto.ConteoDTO;
import com.tiendaropa.dto.ItemCarrito;
import com.tiendaropa.model.*;
import com.tiendaropa.repository.PedidoRepository;
import com.tiendaropa.repository.ProductoRepository;
import com.tiendaropa.repository.ResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo de vida del pedido.
 *
 * El cliente paga directo, sin esperar una aprobacion previa. Lo unico que
 * el sistema controla antes del pago es que no compre mas de lo que hay en
 * inventario (se valida en el checkout, ver PedidoController). Despues del
 * pago hay dos filtros humanos en cascada, cada uno responsable de una sola
 * cosa y sin ver el trabajo del otro:
 *
 *   1. El VENDEDOR confirma que el dinero realmente entro.
 *   2. El JEFE da el visto bueno final antes de que pase a bodega.
 *
 * Un pedido nunca cambia de estado solo. Cada paso lo ejecuta una persona
 * con el rol adecuado, queda con nombre y fecha, y se registra en la
 * auditoria.
 */
@Service
public class PedidoService {

    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyMMdd");

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final ResenaRepository resenaRepo;
    private final NotificacionService notificaciones;
    private final AuditoriaService auditoria;
    private final InventarioService inventario;
    private final com.tiendaropa.repository.UsuarioRepository usuarioRepo;

    public PedidoService(PedidoRepository pedidoRepo, ProductoRepository productoRepo,
                         ResenaRepository resenaRepo, NotificacionService notificaciones,
                         AuditoriaService auditoria, InventarioService inventario,
                         com.tiendaropa.repository.UsuarioRepository usuarioRepo) {
        this.pedidoRepo = pedidoRepo;
        this.productoRepo = productoRepo;
        this.resenaRepo = resenaRepo;
        this.notificaciones = notificaciones;
        this.auditoria = auditoria;
        this.inventario = inventario;
        this.usuarioRepo = usuarioRepo;
    }

    // ==================================================================
    //  Del carrito al pedido
    // ==================================================================

    /**
     * Convierte el carrito en un pedido. Si esCotizacion, queda guardado sin
     * enviar; si no, el pedido nace en PENDIENTE_PAGO, listo para que el
     * cliente pague de inmediato. No hay aprobacion previa: el unico control
     * antes de esto es la disponibilidad de inventario, que ya se valido en
     * el controlador.
     */
    @Transactional
    public Pedido crearDesdeCarrito(Cliente cliente, List<ItemCarrito> lineas, boolean esCotizacion,
                                    String direccion, String medioPago, String observaciones) {
        return crearDesdeCarrito(cliente, lineas, esCotizacion, direccion, medioPago, observaciones, null);
    }

    /**
     * Version completa: permite asociar un vendedor al pedido para la comision de
     * venta. vendedorId puede ser null (nadie lo atendio, o es una cotizacion).
     */
    @Transactional
    public Pedido crearDesdeCarrito(Cliente cliente, List<ItemCarrito> lineas, boolean esCotizacion,
                                    String direccion, String medioPago, String observaciones,
                                    Long vendedorId) {

        Pedido pedido = new Pedido(cliente);
        pedido.setNumero(siguienteNumero(esCotizacion));
        pedido.setDireccionEntrega(direccion);
        pedido.setMedioPago(medioPago);
        pedido.setObservaciones(observaciones);

        if (vendedorId != null) {
            usuarioRepo.findById(vendedorId)
                    .filter(u -> u.getRol() == Rol.EMPLEADO && u.isVendedor() && u.isActivo())
                    .ifPresent(pedido::setVendedor);
        }

        for (ItemCarrito linea : lineas) {
            Producto producto = productoRepo.findById(linea.getProductoId()).orElse(null);
            if (producto == null) continue;
            ItemPedido item = new ItemPedido(producto, linea.getTalla(), linea.getCantidad());
            item.setPrecioUnitario(linea.getPrecioUnitario());
            pedido.agregar(item);
        }
        pedido.recalcular();
        pedido.setEstado(esCotizacion ? EstadoPedido.COTIZACION : EstadoPedido.PENDIENTE_PAGO);
        pedidoRepo.save(pedido);

        auditoria.registrar(esCotizacion ? "COTIZACION_CREADA" : "PEDIDO_GENERADO",
                NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " con " + pedido.getTotalUnidades() + " unidades"
                        + (pedido.isTieneVendedor() ? ", referido por " + pedido.getVendedor().getNombreUsuario() : ""));

        if (!esCotizacion) avisarSiQuedaronBajos(inventario.descontar(pedido));
        return pedido;
    }

    /**
     * Venta asistida: el VENDEDOR arma el pedido a nombre de un cliente (por ejemplo,
     * alguien que compra en persona). El pedido queda asociado a el mismo, sin
     * preguntarselo a nadie, porque fue el quien hizo la venta.
     */
    @Transactional
    public Pedido crearVentaAsistida(Cliente cliente, List<ItemCarrito> lineas, Usuario vendedor,
                                     String direccion, String medioPago, String observaciones) {
        Pedido pedido = crearDesdeCarrito(cliente, lineas, false, direccion, medioPago,
                observaciones, vendedor.getId());
        auditoria.registrar("VENTA_ASISTIDA", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " creado por el vendedor "
                        + vendedor.getNombreUsuario() + " a nombre de " + cliente.getNombreCompleto());
        return pedido;
    }

    /** Una cotizacion guardada pasa a pendiente de pago sin volver a armarla. */
    @Transactional
    public boolean enviarCotizacion(Long pedidoId, Long clienteId) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (!pedido.getCliente().getId().equals(clienteId)) return false;
        if (pedido.getEstado() != EstadoPedido.COTIZACION) return false;

        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedidoRepo.save(pedido);
        avisarSiQuedaronBajos(inventario.descontar(pedido));
        auditoria.registrar("PEDIDO_GENERADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Cotizacion " + pedido.getNumero() + " lista para pagar");
        return true;
    }

    // ==================================================================
    //  Pago
    // ==================================================================

    /**
     * El cliente reporta el pago manual (transferencia, consignacion...),
     * con un comprobante opcional para agilizar la verificacion. Tambien es
     * el punto de entrada cuando la pasarela deja el pago en un estado que
     * necesita revision humana.
     */
    @Transactional
    public boolean reportarPago(Long pedidoId, Long clienteId, String referencia, String medioPago,
                                String comprobante) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (!pedido.getCliente().getId().equals(clienteId)) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) return false;

        pedido.setReferenciaPago(referencia);
        if (medioPago != null && !medioPago.isBlank()) pedido.setMedioPago(medioPago);
        if (comprobante != null && !comprobante.isBlank()) pedido.setComprobantePago(comprobante);
        pedido.setEstado(EstadoPedido.PAGO_EN_VERIFICACION);
        pedidoRepo.save(pedido);

        auditoria.registrar("PAGO_REPORTADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pago reportado para " + pedido.getNumero()
                        + (pedido.isTieneComprobante() ? " con comprobante" : ""));
        notificaciones.avisarPagoPorVerificar(pedido);
        return true;
    }

    /**
     * El VENDEDOR confirma que el dinero entro. Esto no despacha nada
     * todavia: pasa al jefe para el visto bueno final. El vendedor solo
     * responde por el pago, no decide si el pedido sigue.
     */
    @Transactional
    public boolean confirmarPago(Long pedidoId, Usuario vendedor) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.PAGO_EN_VERIFICACION) return false;

        pedido.setEstado(EstadoPedido.PENDIENTE_ACEPTACION_JEFE);
        pedido.setPagoVerificadoPor(vendedor);
        pedidoRepo.save(pedido);

        auditoria.registrar("PAGO_CONFIRMADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pago de " + pedido.getNumero() + " confirmado por " + vendedor.getNombreUsuario());
        notificaciones.avisarAceptacionPendiente(pedido);
        return true;
    }

    // ==================================================================
    //  Visto bueno final del jefe (despues del pago, no antes)
    // ==================================================================

    @Transactional
    public boolean aceptar(Long pedidoId, Usuario jefe, String nota) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_ACEPTACION_JEFE) return false;

        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setAprobadoPor(jefe);
        pedido.setFechaAprobacion(LocalDateTime.now());
        pedido.setMotivoDecision(nota);
        calcularComision(pedido);
        pedidoRepo.save(pedido);

        auditoria.registrar("PEDIDO_ACEPTADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " aceptado por " + jefe.getNombreUsuario());
        if (pedido.isTieneVendedor() && pedido.getComisionMonto().signum() > 0) {
            auditoria.registrar("COMISION_CALCULADA", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                    "Comision de $" + pedido.getComisionMonto().toBigInteger() + " para "
                            + pedido.getVendedor().getNombreUsuario());
        }
        notificaciones.avisarListoParaDespachar(pedido);
        return true;
    }

    /**
     * Suma, linea por linea, cuanto gana el vendedor asociado al pedido. Se calcula
     * una sola vez, cuando el jefe acepta (estado PAGADO): antes de eso el pedido
     * todavia podia no llegar a concretarse, y no tendria sentido "prometer" una
     * comision sobre una venta que no esta confirmada.
     */
    private void calcularComision(Pedido pedido) {
        if (!pedido.isTieneVendedor()) {
            pedido.setComisionEstado(EstadoComision.NO_APLICA);
            return;
        }
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            if (producto == null) continue;
            total = total.add(producto.comisionDe(item.getCantidad(), item.getPrecioUnitario()));
        }
        pedido.setComisionMonto(total);
        pedido.setComisionEstado(total.signum() > 0 ? EstadoComision.PENDIENTE : EstadoComision.NO_APLICA);
    }

    /**
     * El jefe rechaza un pedido que YA se pago. Es la salida para un fraude
     * de pago, un comprobante falso o un problema grave. Las unidades vuelven
     * al inventario; el reembolso del dinero, si aplica, se gestiona por
     * fuera del sistema.
     */
    @Transactional
    public boolean rechazar(Long pedidoId, Usuario jefe, String motivo) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_ACEPTACION_JEFE) return false;

        pedido.setEstado(EstadoPedido.RECHAZADO);
        pedido.setAprobadoPor(jefe);
        pedido.setFechaAprobacion(LocalDateTime.now());
        pedido.setMotivoDecision(motivo == null || motivo.isBlank()
                ? "Sin motivo registrado" : motivo);
        pedidoRepo.save(pedido);

        inventario.reponer(pedido);

        auditoria.registrar("PEDIDO_RECHAZADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " rechazado tras el pago: " + pedido.getMotivoDecision());
        return true;
    }

    // ==================================================================
    //  Pasarela de pagos
    // ==================================================================

    @Transactional
    public void registrarIntentoPasarela(Long pedidoId, String referencia) {
        pedidoRepo.findById(pedidoId).ifPresent(pedido -> {
            pedido.setReferenciaPasarela(referencia);
            pedido.setEstadoPasarela("PENDING");
            pedidoRepo.save(pedido);
            auditoria.registrar("PAGO_INICIADO", NivelDato.SEMIPRIVADO, "Pedido", pedidoId,
                    "Checkout de pasarela para " + pedido.getNumero() + " ref " + referencia);
        });
    }

    public Optional<Pedido> porReferenciaPasarela(String referencia) {
        if (referencia == null || referencia.isBlank()) return Optional.empty();
        return pedidoRepo.findByReferenciaPasarela(referencia);
    }

    /**
     * Aplica lo que reporta la pasarela.
     *
     * Un pago aprobado por pasarela SI necesita el mismo doble filtro que uno
     * manual: el banco confirma el dinero, pero el vendedor y el jefe siguen
     * siendo quienes autorizan que el pedido avance. La pasarela deja el
     * pedido listo en PAGO_EN_VERIFICACION, un paso antes de esos dos filtros.
     * Es idempotente: si ya se aplico esta misma transaccion, no repite nada.
     */
    @Transactional
    public boolean aplicarResultadoPasarela(Long pedidoId, String transaccionId, String estado,
                                            String metodo, long centavosCobrados) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();

        if (transaccionId != null && transaccionId.equals(pedido.getTransaccionPasarela())
                && estado != null && estado.equals(pedido.getEstadoPasarela())) {
            return true;
        }

        pedido.setTransaccionPasarela(transaccionId);
        pedido.setEstadoPasarela(estado);
        pedido.setMetodoPasarela(metodo);
        pedido.setReferenciaPago(transaccionId);
        if (metodo != null && !metodo.isBlank()) pedido.setMedioPago(metodo);

        if ("APPROVED".equals(estado) && pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO) {
            long esperado = pedido.getTotalEnCentavos();
            pedido.setEstado(EstadoPedido.PAGO_EN_VERIFICACION);
            pedidoRepo.save(pedido);
            notificaciones.avisarPagoPorVerificar(pedido);
            if (centavosCobrados > 0 && centavosCobrados != esperado) {
                auditoria.registrar("PAGO_MONTO_DESCUADRADO", NivelDato.SEMIPRIVADO, "Pedido",
                        pedidoId, "Se cobraron " + centavosCobrados + " y se esperaban " + esperado);
            }
        }
        // DECLINED, ERROR, VOIDED: el pedido sigue pendiente de pago, puede reintentar.
        // PENDING: no cambia nada todavia, se espera el resultado final.

        pedidoRepo.save(pedido);
        auditoria.registrar("PAGO_PASARELA", NivelDato.SEMIPRIVADO, "Pedido", pedidoId,
                pedido.getNumero() + " -> " + estado + " (" + metodo + ") tx " + transaccionId);
        return true;
    }

    // ==================================================================
    //  Bodega
    // ==================================================================

    @Transactional
    public boolean alistar(Long pedidoId, Usuario empleado) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.PAGADO) return false;

        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoRepo.save(pedido);
        auditoria.registrar("PEDIDO_EN_PREPARACION", NivelDato.SEMIPRIVADO, "Pedido",
                pedido.getId(), "Alistamiento iniciado por " + empleado.getNombreUsuario());
        return true;
    }

    @Transactional
    public boolean despachar(Long pedidoId, Usuario empleado, String guia) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.EN_PREPARACION
                && pedido.getEstado() != EstadoPedido.PAGADO) return false;

        pedido.setEstado(EstadoPedido.DESPACHADO);
        pedido.setDespachadoPor(empleado);
        pedido.setNumeroGuia(guia);
        pedido.setFechaDespacho(LocalDateTime.now());
        pedidoRepo.save(pedido);

        auditoria.registrar("PEDIDO_DESPACHADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " despachado con guia " + guia);
        return true;
    }

    /**
     * Confirmacion de recepcion por parte del CLIENTE, con una foto opcional
     * como prueba de entrega. Es lo que dispara ENTREGADO y habilita la
     * resena; no depende de que bodega marque nada.
     */
    @Transactional
    public boolean confirmarRecepcion(Long pedidoId, Long clienteId, String foto) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (!pedido.getCliente().getId().equals(clienteId)) return false;
        if (pedido.getEstado() != EstadoPedido.DESPACHADO) return false;

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(LocalDateTime.now());
        if (foto != null && !foto.isBlank()) pedido.setFotoEntrega(foto);
        if (pedido.getComisionEstado() == EstadoComision.PENDIENTE) {
            pedido.setComisionEstado(EstadoComision.CONFIRMADA);
        }
        pedidoRepo.save(pedido);

        auditoria.registrar("PEDIDO_ENTREGADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Entrega confirmada por el cliente" + (pedido.isTieneFotoEntrega() ? " con foto" : ""));
        return true;
    }

    /**
     * Respaldo para el personal: si el cliente no confirma, alguien del
     * equipo puede cerrar el pedido igual. No genera resena, porque esa
     * solo la deja quien recibio.
     */
    @Transactional
    public boolean marcarEntregadoPorStaff(Long pedidoId, Usuario empleado) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (pedido.getEstado() != EstadoPedido.DESPACHADO) return false;

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(LocalDateTime.now());
        if (pedido.getComisionEstado() == EstadoComision.PENDIENTE) {
            pedido.setComisionEstado(EstadoComision.CONFIRMADA);
        }
        pedidoRepo.save(pedido);
        auditoria.registrar("PEDIDO_ENTREGADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Entrega cerrada por " + empleado.getNombreUsuario() + " (el cliente no confirmo)");
        return true;
    }

    // ==================================================================
    //  Resenas
    // ==================================================================

    @Transactional
    public boolean dejarResena(Long pedidoId, Long clienteId, Long productoId, int calificacion,
                               String comentario) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (!pedido.getCliente().getId().equals(clienteId)) return false;
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) return false;
        if (resenaRepo.existsByPedidoIdAndProductoId(pedidoId, productoId)) return false;

        boolean productoEnPedido = pedido.getItems().stream()
                .anyMatch(item -> item.getProducto() != null && item.getProducto().getId().equals(productoId));
        if (!productoEnPedido) return false;

        Producto producto = productoRepo.findById(productoId).orElse(null);
        if (producto == null) return false;

        int nota = Math.max(1, Math.min(5, calificacion));
        resenaRepo.save(new Resena(producto, pedido.getCliente(), pedido, nota, comentario));

        auditoria.registrar("RESENA_CREADA", NivelDato.PUBLICO, "Producto", productoId,
                "Resena de " + nota + " estrellas en el pedido " + pedido.getNumero());
        return true;
    }

    // ==================================================================
    //  Cancelacion
    // ==================================================================

    @Transactional
    public boolean cancelar(Long pedidoId, Long clienteId, String motivo) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty()) return false;
        Pedido pedido = posible.get();
        if (clienteId != null && !pedido.getCliente().getId().equals(clienteId)) return false;
        if (!pedido.getEstado().isCancelablePorCliente()) return false;

        boolean teniaInventarioComprometido = pedido.getEstado() != EstadoPedido.COTIZACION;
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoDecision(motivo);
        pedidoRepo.save(pedido);

        if (teniaInventarioComprometido) inventario.reponer(pedido);

        auditoria.registrar("PEDIDO_CANCELADO", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                "Pedido " + pedido.getNumero() + " cancelado");
        return true;
    }

    // ==================================================================
    //  Cambio de estado administrativo
    // ==================================================================

    @Transactional
    public boolean cambiarEstado(Long pedidoId, EstadoPedido nuevo, Usuario quien, String motivo) {
        Optional<Pedido> posible = pedidoRepo.findById(pedidoId);
        if (posible.isEmpty() || nuevo == null) return false;

        Pedido pedido = posible.get();
        EstadoPedido anterior = pedido.getEstado();
        if (anterior == nuevo) return false;

        boolean comprometiaAntes = comprometeInventario(anterior);
        boolean comprometeDespues = comprometeInventario(nuevo);

        pedido.setEstado(nuevo);
        pedido.setMotivoDecision(motivo == null || motivo.isBlank()
                ? "Cambio manual de estado" : motivo);

        // La comision sigue al pedido: si se anula el pedido, se anula la comision;
        // si el pedido llega a ENTREGADO por esta via manual, se confirma igual que
        // por el camino normal.
        if (pedido.isTieneVendedor()) {
            if ((nuevo == EstadoPedido.CANCELADO || nuevo == EstadoPedido.RECHAZADO)
                    && pedido.getComisionEstado() != EstadoComision.NO_APLICA) {
                pedido.setComisionEstado(EstadoComision.ANULADA);
            } else if (nuevo == EstadoPedido.PAGADO && pedido.getComisionEstado() == EstadoComision.NO_APLICA) {
                calcularComision(pedido);
            } else if (nuevo == EstadoPedido.ENTREGADO
                    && pedido.getComisionEstado() == EstadoComision.PENDIENTE) {
                pedido.setComisionEstado(EstadoComision.CONFIRMADA);
            }
        }

        pedidoRepo.save(pedido);

        if (comprometiaAntes && !comprometeDespues) {
            inventario.reponer(pedido);
        } else if (!comprometiaAntes && comprometeDespues) {
            avisarSiQuedaronBajos(inventario.descontar(pedido));
        }

        auditoria.registrar("PEDIDO_ESTADO_MANUAL", NivelDato.SEMIPRIVADO, "Pedido", pedido.getId(),
                pedido.getNumero() + ": " + anterior + " -> " + nuevo
                        + " por " + (quien == null ? "?" : quien.getNombreUsuario())
                        + ". Motivo: " + pedido.getMotivoDecision());
        return true;
    }

    private boolean comprometeInventario(EstadoPedido estado) {
        return estado != EstadoPedido.COTIZACION
            && estado != EstadoPedido.CANCELADO
            && estado != EstadoPedido.RECHAZADO;
    }

    // ==================================================================
    //  Consultas
    // ==================================================================

    public Optional<Pedido> porId(Long id) { return pedidoRepo.findById(id); }

    public List<Pedido> deCliente(Long clienteId) {
        return pedidoRepo.findByClienteIdOrderByFechaDesc(clienteId);
    }

    public List<Pedido> cotizacionesDe(Long clienteId) {
        return pedidoRepo.findByClienteIdAndEstadoOrderByFechaDesc(clienteId, EstadoPedido.COTIZACION);
    }

    /** Bandeja del VENDEDOR: solo pagos por confirmar. El bodeguero no la ve. */
    public List<Pedido> porVerificarPago() {
        return pedidoRepo.findByEstadoOrderByFechaAsc(EstadoPedido.PAGO_EN_VERIFICACION);
    }

    /** Bandeja del JEFE: pagos ya verificados que esperan su visto bueno. */
    public List<Pedido> pendientesDeAceptacion() {
        return pedidoRepo.findByEstadoOrderByFechaAsc(EstadoPedido.PENDIENTE_ACEPTACION_JEFE);
    }

    /** Bandeja del BODEGUERO: solo despachos. El vendedor no la ve. */
    public List<Pedido> porDespachar() {
        return pedidoRepo.findByEstadoInOrderByFechaAsc(
                List.of(EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION));
    }

    /** Pedidos despachados esperando confirmacion, para el respaldo del staff. */
    public List<Pedido> porConfirmarEntrega() {
        return pedidoRepo.findByEstadoOrderByFechaAsc(EstadoPedido.DESPACHADO);
    }

    public List<Pedido> recientes() { return pedidoRepo.findTop50ByOrderByFechaDesc(); }

    public List<Pedido> ultimos(int cuantos) {
        List<Pedido> todos = pedidoRepo.findTop50ByOrderByFechaDesc();
        return todos.size() <= cuantos ? todos : todos.subList(0, cuantos);
    }

    public long totalPedidos() { return pedidoRepo.contarPedidosReales(); }

    public long totalIncluyendoCotizaciones() { return pedidoRepo.count(); }

    public List<ConteoDTO> conteoPorEstado() {
        long total = totalIncluyendoCotizaciones();
        List<ConteoDTO> conteos = new ArrayList<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            long cuantos = pedidoRepo.countByEstado(estado);
            if (cuantos == 0) continue;
            double pct = total == 0 ? 0 : Math.round(cuantos * 1000.0 / total) / 10.0;
            conteos.add(new ConteoDTO(estado.getEtiqueta(), cuantos, pct));
        }
        return conteos;
    }

    public long cuantosPorVerificar() {
        return pedidoRepo.countByEstado(EstadoPedido.PAGO_EN_VERIFICACION);
    }

    public long cuantosPendientesAceptacion() {
        return pedidoRepo.countByEstado(EstadoPedido.PENDIENTE_ACEPTACION_JEFE);
    }

    public long cuantosPorDespachar() {
        return pedidoRepo.countByEstado(EstadoPedido.PAGADO)
             + pedidoRepo.countByEstado(EstadoPedido.EN_PREPARACION);
    }

    private void avisarSiQuedaronBajos(List<Producto> bajos) {
        for (Producto p : bajos) {
            notificaciones.avisarStockBajo(p);
        }
    }

    private String siguienteNumero(boolean esCotizacion) {
        String prefijo = esCotizacion ? "COT-" : "PED-";
        String sello = LocalDateTime.now().format(SELLO);
        long consecutivo = pedidoRepo.count() + 1;
        String numero = prefijo + sello + "-" + String.format("%04d", consecutivo);
        while (pedidoRepo.findByNumero(numero).isPresent()) {
            consecutivo++;
            numero = prefijo + sello + "-" + String.format("%04d", consecutivo);
        }
        return numero;
    }
}
