package com.tiendaropa.service;

import java.util.List;

import com.tiendaropa.model.Notificacion;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.model.Producto;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Avisos internos por rol. Se guardan en la base y se muestran en el panel,
 * en vez de enviarse por correo: asi la notificacion queda con trazabilidad
 * y no depende de un servidor de correo configurado.
 *
 * Cada aviso va dirigido a un rol y, cuando aplica, a un SUBTIPO concreto de
 * empleado. Es lo que mantiene separadas las bandejas de vendedor y
 * bodeguero: cada uno ve solo lo suyo.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    /** El vendedor confirma que un pago entro; le llega al vendedor. */
    @Transactional
    public void avisarPagoPorVerificar(Pedido pedido) {
        Notificacion n = new Notificacion(Rol.EMPLEADO,
                "Pago por verificar",
                "El pedido " + pedido.getNumero() + " reporta un pago"
                        + (pedido.isTieneComprobante() ? " con comprobante adjunto" : "")
                        + ". Falta confirmarlo.",
                "/panel/pedidos");
        n.setSubtipoDestino(SubtipoEmpleado.VENDEDOR);
        n.setPedido(pedido);
        repo.save(n);
    }

    /** El vendedor ya confirmo el pago: le llega al jefe para el visto bueno final. */
    @Transactional
    public void avisarAceptacionPendiente(Pedido pedido) {
        Notificacion n = new Notificacion(Rol.JEFE,
                "Pago verificado, falta tu visto bueno",
                "El pedido " + pedido.getNumero() + " de "
                        + pedido.getCliente().getNombreCompleto() + " por $"
                        + pedido.getTotal().toBigInteger()
                        + " ya tiene el pago confirmado por "
                        + (pedido.getPagoVerificadoPor() == null ? "el vendedor"
                           : pedido.getPagoVerificadoPor().getNombreUsuario())
                        + ". Falta tu aceptacion para pasar a bodega.",
                "/panel/aprobaciones");
        n.setPedido(pedido);
        repo.save(n);
    }

    /** El jefe acepto: le llega a bodega. */
    @Transactional
    public void avisarListoParaDespachar(Pedido pedido) {
        Notificacion n = new Notificacion(Rol.EMPLEADO,
                "Pedido listo para despachar",
                "El pedido " + pedido.getNumero() + " tiene el visto bueno del jefe y "
                        + pedido.getTotalUnidades() + " unidades por alistar.",
                "/panel/pedidos");
        n.setSubtipoDestino(SubtipoEmpleado.BODEGUERO);
        n.setPedido(pedido);
        repo.save(n);
    }

    /** Aviso a bodega cuando un producto cruza su nivel minimo de existencias. */
    @Transactional
    public void avisarStockBajo(Producto producto) {
        Notificacion n = new Notificacion(Rol.EMPLEADO,
                "Existencias bajas: " + producto.getNombre(),
                "Quedan " + producto.getStock() + " unidades de " + producto.getSku()
                        + " (minimo definido: " + producto.getStockMinimo() + "). Conviene reponer.",
                "/panel/productos");
        n.setSubtipoDestino(SubtipoEmpleado.BODEGUERO);
        repo.save(n);
    }

    public List<Notificacion> paraRol(Rol rol) {
        return repo.findByRolDestinoOrderByFechaDesc(rol);
    }

    public long cuantasSinLeer(Rol rol) {
        return rol == null ? 0 : repo.countByRolDestinoAndLeida(rol, false);
    }

    /** Version que filtra por subtipo: la que se usa para el globito del menu. */
    public long cuantasSinLeer(Usuario usuario) {
        if (usuario == null) return 0;
        if (usuario.getRol() == Rol.EMPLEADO) {
            return repo.countByRolDestinoAndSubtipoDestinoAndLeida(
                    Rol.EMPLEADO, usuario.getSubtipo(), false);
        }
        return cuantasSinLeer(usuario.getRol());
    }

    @Transactional
    public void marcarTodasLeidas(Rol rol) {
        List<Notificacion> pendientes = repo.findByRolDestinoAndLeidaOrderByFechaDesc(rol, false);
        pendientes.forEach(n -> n.setLeida(true));
        repo.saveAll(pendientes);
    }
}
