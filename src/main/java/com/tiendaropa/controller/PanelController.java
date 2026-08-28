package com.tiendaropa.controller;

import java.time.LocalDate;

import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.Rol;
import com.tiendaropa.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Panel interno. Lo que se ve depende del rol de quien entra. */
@Controller
@RequestMapping("/panel")
public class PanelController {

    private final ClienteService clienteService;
    private final AtencionService atencionService;
    private final ProductoService productoService;
    private final ReporteService reporteService;
    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final InventarioService inventarioService;

    public PanelController(ClienteService clienteService, AtencionService atencionService,
                           ProductoService productoService, ReporteService reporteService,
                           AuditoriaService auditoriaService, UsuarioService usuarioService,
                           PedidoService pedidoService,
                           InventarioService inventarioService) {
        this.clienteService = clienteService;
        this.atencionService = atencionService;
        this.productoService = productoService;
        this.reporteService = reporteService;
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String panel(Authentication autenticacion, Model modelo) {
        LocalDate hoy = LocalDate.now();
        modelo.addAttribute("totalClientes", clienteService.total());
        modelo.addAttribute("totalProductos", productoService.total());
        modelo.addAttribute("atencionesMes", atencionService.totalDelMes(hoy.getYear(), hoy.getMonthValue()));
        modelo.addAttribute("pendientes", atencionService.pendientes().size());
        modelo.addAttribute("conSensibles", clienteService.conAutorizacionSensibles());
        modelo.addAttribute("conMarketing", clienteService.conAutorizacionMarketing());
        // --- los seis indicadores que pide el enunciado ---
        modelo.addAttribute("totalUsuarios", usuarioService.total());
        modelo.addAttribute("usuariosCliente", usuarioService.cuantosPorRol(Rol.CLIENTE));
        modelo.addAttribute("usuariosInternos",
                usuarioService.total() - usuarioService.cuantosPorRol(Rol.CLIENTE));
        modelo.addAttribute("totalPedidos", pedidoService.totalPedidos());
        modelo.addAttribute("ultimosPedidos", pedidoService.ultimos(8));
        modelo.addAttribute("estadosPedido", pedidoService.conteoPorEstado());
        modelo.addAttribute("productosActivos", productoService.activos());

        modelo.addAttribute("productosBajos", inventarioService.bajoMinimo());
        modelo.addAttribute("productosAgotados", inventarioService.agotados());
        modelo.addAttribute("porAprobar", pedidoService.cuantosPendientesAceptacion());
        modelo.addAttribute("porVerificar", pedidoService.cuantosPorVerificar());
        modelo.addAttribute("porDespachar", pedidoService.cuantosPorDespachar());
        modelo.addAttribute("anio", hoy.getYear());
        modelo.addAttribute("mes", hoy.getMonthValue());
        modelo.addAttribute("titulo", "Panel");
        return "panel/inicio";
    }

    @GetMapping("/clientes")
    public String clientes(@RequestParam(required = false) String q, Model modelo) {
        modelo.addAttribute("clientes", clienteService.buscar(q));
        modelo.addAttribute("busqueda", q);
        modelo.addAttribute("titulo", "Clientes");
        return "panel/clientes";
    }

    @GetMapping("/clientes/{id}")
    public String cliente(@PathVariable Long id, Authentication autenticacion, Model modelo) {
        Cliente cliente = clienteService.porId(id).orElse(null);
        if (cliente == null) return "redirect:/panel/clientes";

        Rol rol = usuarioService.rolActual(autenticacion);
        modelo.addAttribute("vista", clienteService.armarVista(cliente, rol, false));
        modelo.addAttribute("atenciones", atencionService.deCliente(id));
        modelo.addAttribute("titulo", cliente.getNombreCompleto());
        return "panel/cliente-detalle";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model modelo) {
        modelo.addAttribute("registros", auditoriaService.ultimos());
        modelo.addAttribute("titulo", "Auditoria de datos");
        return "panel/auditoria";
    }

    @GetMapping("/historico")
    public String historico(Model modelo) {
        modelo.addAttribute("reportes", reporteService.historico());
        modelo.addAttribute("titulo", "Reportes guardados");
        return "panel/historico";
    }
}
