package com.tiendaropa.controller;

import com.tiendaropa.model.Tema;
import com.tiendaropa.service.AtencionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/panel/atenciones")
public class AtencionController {

    private final AtencionService atencionService;

    public AtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro, Model modelo) {
        modelo.addAttribute("atenciones",
                "pendientes".equals(filtro) ? atencionService.pendientes() : atencionService.recientes());
        modelo.addAttribute("filtro", filtro);
        modelo.addAttribute("temas", Tema.values());
        modelo.addAttribute("titulo", "Atenciones");
        return "panel/atenciones";
    }

    @PostMapping("/{id}/tomar")
    public String tomar(@PathVariable Long id, Authentication autenticacion, RedirectAttributes flash) {
        atencionService.tomar(id, autenticacion.getName());
        flash.addFlashAttribute("mensaje", "Caso asignado.");
        return "redirect:/panel/atenciones?filtro=pendientes";
    }

    @PostMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id,
                         @RequestParam(defaultValue = "true") boolean resuelta,
                         @RequestParam(required = false) Tema tema,
                         RedirectAttributes flash) {
        atencionService.cerrar(id, resuelta, tema);
        flash.addFlashAttribute("mensaje", "Caso cerrado.");
        return "redirect:/panel/atenciones";
    }
}
