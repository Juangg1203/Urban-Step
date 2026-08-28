package com.tiendaropa.controller;

import com.tiendaropa.service.CarritoService;
import com.tiendaropa.service.ChatbotService;
import com.tiendaropa.service.NotificacionService;
import com.tiendaropa.service.WompiService;
import com.tiendaropa.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Datos que necesitan todas las vistas (encabezado, widget de chat, rol). */
@ControllerAdvice
public class AtributosGlobales {

    private final UsuarioService usuarioService;
    private final ChatbotService chatbotService;
    private final CarritoService carritoService;
    private final NotificacionService notificacionService;
    private final WompiService wompiService;

    @Value("${app.empresa.nombre}")
    private String nombreEmpresa;

    public AtributosGlobales(UsuarioService usuarioService, ChatbotService chatbotService,
                             CarritoService carritoService, NotificacionService notificacionService,
                             WompiService wompiService) {
        this.usuarioService = usuarioService;
        this.chatbotService = chatbotService;
        this.carritoService = carritoService;
        this.notificacionService = notificacionService;
        this.wompiService = wompiService;
    }

    @ModelAttribute
    public void comunes(Model modelo, Authentication autenticacion) {
        modelo.addAttribute("empresa", nombreEmpresa);
        modelo.addAttribute("iaChatActiva", chatbotService.iaActiva());
        modelo.addAttribute("unidadesCarrito", carritoService.getTotalUnidades());
        modelo.addAttribute("pasarelaActiva", wompiService.estaHabilitado());
        if (autenticacion != null && autenticacion.isAuthenticated()
                && !"anonymousUser".equals(autenticacion.getPrincipal())) {
            usuarioService.actual(autenticacion).ifPresent(u -> {
                modelo.addAttribute("usuarioActual", u);
                modelo.addAttribute("rolActual", u.getRol());
                modelo.addAttribute("rolEtiqueta", u.getRolTexto());
                // Version que filtra por subtipo: el vendedor no cuenta los
                // avisos de bodega, ni al reves.
                modelo.addAttribute("avisosSinLeer", notificacionService.cuantasSinLeer(u));
            });
        }
    }
}
