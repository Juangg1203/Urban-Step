package com.tiendaropa.controller;

import java.util.Optional;

import com.tiendaropa.dto.FuerzaClaveDTO;
import com.tiendaropa.model.NivelDato;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.service.AuditoriaService;
import com.tiendaropa.service.SeguridadClaveService;
import com.tiendaropa.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Cuentas del personal interno: administrador, jefe, vendedor, bodeguero.
 *
 * Es justo el trabajo del administrador segun el enunciado: mantener el
 * sitio funcional y seguro, lo que incluye quien tiene acceso y con que
 * rol — no vender ni tocar el catalogo, eso es del jefe.
 *
 * No se administran clientes desde aqui: ellos se registran solos en
 * /registro y no tiene sentido que el administrador les asigne un rol.
 */
@Controller
@RequestMapping("/panel/usuarios")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;
    private final SeguridadClaveService seguridadClave;
    private final AuditoriaService auditoria;

    public UsuarioAdminController(UsuarioService usuarioService, SeguridadClaveService seguridadClave,
                                  AuditoriaService auditoria) {
        this.usuarioService = usuarioService;
        this.seguridadClave = seguridadClave;
        this.auditoria = auditoria;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("titulo", "Usuarios");
        modelo.addAttribute("usuarios", usuarioService.listar().stream()
                .filter(u -> u.getRol() != Rol.CLIENTE)
                .toList());
        return "panel/usuarios";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model modelo) {
        modelo.addAttribute("titulo", "Nuevo usuario");
        modelo.addAttribute("usuario", new Usuario());
        modelo.addAttribute("esNuevo", true);
        return "panel/usuario-form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model modelo, RedirectAttributes flash) {
        Optional<Usuario> usuario = usuarioService.porId(id);
        if (usuario.isEmpty() || usuario.get().getRol() == Rol.CLIENTE) {
            flash.addFlashAttribute("mensaje", "Ese usuario no existe.");
            return "redirect:/panel/usuarios";
        }
        modelo.addAttribute("titulo", "Editar " + usuario.get().getNombreUsuario());
        modelo.addAttribute("usuario", usuario.get());
        modelo.addAttribute("esNuevo", false);
        return "panel/usuario-form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombreUsuario,
                          @RequestParam String correo,
                          @RequestParam Rol rol,
                          @RequestParam(required = false) SubtipoEmpleado subtipo,
                          @RequestParam(required = false) String clave,
                          RedirectAttributes flash) {

        if (rol == Rol.CLIENTE) {
            flash.addFlashAttribute("mensaje", "Los clientes se registran solos en /registro; "
                    + "aqui solo se administra el personal interno.");
            return volverAlFormulario(id);
        }
        if (rol == Rol.EMPLEADO && subtipo == null) {
            flash.addFlashAttribute("mensaje", "Un empleado necesita subtipo: vendedor o bodeguero.");
            return volverAlFormulario(id);
        }
        if (usuarioService.existeNombreUsuario(nombreUsuario, id)) {
            flash.addFlashAttribute("mensaje", "Ya existe otra cuenta con ese nombre de usuario.");
            return volverAlFormulario(id);
        }
        if (usuarioService.existeCorreo(correo, id)) {
            flash.addFlashAttribute("mensaje", "Ya existe otra cuenta con ese correo.");
            return volverAlFormulario(id);
        }

        if (id == null) {
            // Cuenta nueva: la clave es obligatoria y se valida igual que en
            // el registro publico. No hay una clave por defecto insegura.
            if (clave == null || clave.isBlank()) {
                flash.addFlashAttribute("mensaje", "Define una clave para la cuenta nueva.");
                return volverAlFormulario(null);
            }
            FuerzaClaveDTO fuerza = seguridadClave.evaluar(clave, nombreUsuario, correo);
            if (!fuerza.isAceptable()) {
                flash.addFlashAttribute("mensaje", "Clave " + fuerza.getEtiqueta() + ". "
                        + String.join(". ", fuerza.getAvisos()));
                return volverAlFormulario(null);
            }
            Usuario creado = usuarioService.crearInterno(nombreUsuario, correo, clave, rol, subtipo);
            auditoria.registrar("USUARIO_CREADO", NivelDato.PUBLICO, "Usuario", creado.getId(),
                    creado.getNombreUsuario() + " (" + creado.getRolTexto() + ")");
            flash.addFlashAttribute("mensaje", "Cuenta de " + creado.getNombreUsuario() + " creada.");
        } else {
            boolean ok = usuarioService.editar(id, nombreUsuario, correo, rol, subtipo);
            if (!ok) {
                flash.addFlashAttribute("mensaje", "Ese usuario ya no existe.");
                return "redirect:/panel/usuarios";
            }
            auditoria.registrar("USUARIO_EDITADO", NivelDato.PUBLICO, "Usuario", id,
                    nombreUsuario + " (" + rol.getEtiqueta() + ")");

            // Cambiar la clave es un paso aparte: solo si se escribio algo.
            if (clave != null && !clave.isBlank()) {
                FuerzaClaveDTO fuerza = seguridadClave.evaluar(clave, nombreUsuario, correo);
                if (!fuerza.isAceptable()) {
                    flash.addFlashAttribute("mensaje", "Los datos se guardaron, pero la clave nueva "
                            + "es " + fuerza.getEtiqueta().toLowerCase() + " y no se cambio.");
                    return "redirect:/panel/usuarios";
                }
                usuarioService.cambiarClave(id, clave);
                auditoria.registrar("USUARIO_CLAVE_CAMBIADA", NivelDato.PUBLICO, "Usuario", id,
                        "Clave restablecida por el administrador");
            }
            flash.addFlashAttribute("mensaje", "Cuenta de " + nombreUsuario + " actualizada.");
        }
        return "redirect:/panel/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        usuarioService.activar(id, false);
        auditoria.registrar("USUARIO_DESACTIVADO", NivelDato.PUBLICO, "Usuario", id, "Acceso revocado");
        flash.addFlashAttribute("mensaje", "Cuenta desactivada: ya no puede iniciar sesion.");
        return "redirect:/panel/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        usuarioService.activar(id, true);
        auditoria.registrar("USUARIO_ACTIVADO", NivelDato.PUBLICO, "Usuario", id, "Acceso restablecido");
        flash.addFlashAttribute("mensaje", "Cuenta activada de nuevo.");
        return "redirect:/panel/usuarios";
    }

    private String volverAlFormulario(Long id) {
        return id == null ? "redirect:/panel/usuarios/nuevo" : "redirect:/panel/usuarios/" + id + "/editar";
    }
}
