package com.tiendaropa.controller;

import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.Rol;
import com.tiendaropa.service.AtencionService;
import com.tiendaropa.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Area del titular de los datos. Aqui el cliente ejerce sus derechos:
 * conocer, actualizar, autorizar y revocar.
 */
@Controller
@RequestMapping("/mi-cuenta")
public class MiCuentaController {

    private final ClienteService clienteService;
    private final AtencionService atencionService;

    public MiCuentaController(ClienteService clienteService, AtencionService atencionService) {
        this.clienteService = clienteService;
        this.atencionService = atencionService;
    }

    @GetMapping
    public String miCuenta(Authentication autenticacion, Model modelo) {
        Cliente cliente = clienteService.porNombreUsuario(autenticacion.getName()).orElse(null);
        if (cliente == null) return "redirect:/";

        modelo.addAttribute("vista", clienteService.armarVista(cliente, Rol.CLIENTE, true));
        modelo.addAttribute("atenciones", atencionService.deCliente(cliente.getId()));
        modelo.addAttribute("titulo", "Mi cuenta");
        return "cliente/mi-cuenta";
    }

    @PostMapping("/publicos")
    public String publicos(Authentication autenticacion,
                           @RequestParam String nombres, @RequestParam String apellidos,
                           @RequestParam(required = false) String ciudad,
                           @RequestParam(required = false) String departamento,
                           @RequestParam(required = false) String ocupacion,
                           RedirectAttributes flash) {
        clienteService.porNombreUsuario(autenticacion.getName()).ifPresent(cliente ->
                clienteService.actualizarPublicos(cliente, nombres, apellidos, ciudad, departamento, ocupacion));
        flash.addFlashAttribute("mensaje", "Datos publicos actualizados.");
        return "redirect:/mi-cuenta";
    }

    @PostMapping("/privados")
    public String privados(Authentication autenticacion,
                           @RequestParam(required = false) String tipoDocumento,
                           @RequestParam(required = false) String numeroDocumento,
                           @RequestParam(required = false) String direccion,
                           @RequestParam(required = false) String telefono,
                           @RequestParam(required = false) String correoPersonal,
                           @RequestParam(required = false) String fechaNacimiento,
                           RedirectAttributes flash) {
        clienteService.porNombreUsuario(autenticacion.getName()).ifPresent(cliente ->
                clienteService.actualizarPrivados(cliente, tipoDocumento, numeroDocumento, direccion,
                        telefono, correoPersonal, fechaNacimiento));
        flash.addFlashAttribute("mensaje", "Datos privados guardados cifrados.");
        return "redirect:/mi-cuenta";
    }

    @PostMapping("/sensibles/autorizacion")
    public String autorizacionSensibles(Authentication autenticacion,
                                        @RequestParam(defaultValue = "false") boolean autoriza,
                                        RedirectAttributes flash) {
        clienteService.porNombreUsuario(autenticacion.getName()).ifPresent(cliente ->
                clienteService.cambiarAutorizacionSensibles(cliente, autoriza));
        flash.addFlashAttribute("mensaje", autoriza
                ? "Autorizacion registrada. Puedes guardar tus datos sensibles."
                : "Autorizacion revocada. Los datos sensibles fueron eliminados.");
        return "redirect:/mi-cuenta";
    }

    @PostMapping("/sensibles")
    public String sensibles(Authentication autenticacion,
                            @RequestParam(required = false) String medidasCorporales,
                            @RequestParam(required = false) String alergiasMateriales,
                            @RequestParam(required = false) String condicionMovilidad,
                            @RequestParam(required = false) String restriccionVestimenta,
                            RedirectAttributes flash) {
        try {
            clienteService.porNombreUsuario(autenticacion.getName()).ifPresent(cliente ->
                    clienteService.guardarSensibles(cliente, medidasCorporales, alergiasMateriales,
                            condicionMovilidad, restriccionVestimenta));
            flash.addFlashAttribute("mensaje", "Datos sensibles guardados. Solo tu puedes verlos.");
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-cuenta";
    }

    @PostMapping("/marketing")
    public String marketing(Authentication autenticacion,
                            @RequestParam(defaultValue = "false") boolean autoriza,
                            RedirectAttributes flash) {
        clienteService.porNombreUsuario(autenticacion.getName()).ifPresent(cliente ->
                clienteService.cambiarAutorizacionMarketing(cliente, autoriza));
        flash.addFlashAttribute("mensaje", "Preferencia de comunicaciones actualizada.");
        return "redirect:/mi-cuenta";
    }
}
