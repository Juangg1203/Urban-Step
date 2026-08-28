package com.tiendaropa.controller;

import com.tiendaropa.dto.FuerzaClaveDTO;
import com.tiendaropa.dto.RegistroForm;
import com.tiendaropa.service.ClienteService;
import com.tiendaropa.service.SeguridadClaveService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final ClienteService clienteService;
    private final SeguridadClaveService seguridadClave;

    public AuthController(ClienteService clienteService, SeguridadClaveService seguridadClave) {
        this.clienteService = clienteService;
        this.seguridadClave = seguridadClave;
    }

    @GetMapping("/login")
    public String login(Model modelo) {
        modelo.addAttribute("titulo", "Iniciar sesion");
        return "auth/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model modelo) {
        if (!modelo.containsAttribute("registroForm")) {
            modelo.addAttribute("registroForm", new RegistroForm());
        }
        modelo.addAttribute("titulo", "Crear cuenta");
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult errores, Model modelo, RedirectAttributes flash) {

        if (clienteService.existeUsuario(form.getNombreUsuario())) {
            errores.rejectValue("nombreUsuario", "duplicado", "Ese nombre de usuario ya esta tomado");
        }
        if (clienteService.existeCorreo(form.getCorreo())) {
            errores.rejectValue("correo", "duplicado", "Ese correo ya tiene una cuenta");
        }

        // La validacion del navegador es comodidad; esta es la que decide.
        FuerzaClaveDTO fuerza = seguridadClave.evaluar(
                form.getClave(), form.getNombreUsuario(), form.getCorreo());
        if (!fuerza.isAceptable()) {
            String detalle = fuerza.getAvisos().isEmpty()
                    ? "Elige una clave mas segura"
                    : String.join(". ", fuerza.getAvisos());
            errores.rejectValue("clave", "debil", "Clave " + fuerza.getEtiqueta() + ". " + detalle);
        }
        if (errores.hasErrors()) {
            modelo.addAttribute("titulo", "Crear cuenta");
            return "auth/registro";
        }

        clienteService.registrar(form);
        flash.addFlashAttribute("mensaje", "Cuenta creada. Inicia sesion para entrar a Mi cuenta.");
        return "redirect:/login";
    }
}
