package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    public Optional<Usuario> actual(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) return Optional.empty();
        return repositorio.findByNombreUsuario(autenticacion.getName());
    }

    public Rol rolActual(Authentication autenticacion) {
        return actual(autenticacion).map(Usuario::getRol).orElse(null);
    }

    public List<Usuario> listar() { return repositorio.findAll(); }
    public List<Usuario> porRol(Rol rol) { return repositorio.findByRol(rol); }

    /** Alta de personal interno. No existe registro publico para estos roles. */
    public Usuario crearInterno(String nombreUsuario, String correo, String clave, Rol rol) {
        return crearInterno(nombreUsuario, correo, clave, rol, null);
    }

    /** El subtipo solo tiene sentido para el rol EMPLEADO (vendedor o bodeguero). */
    public Usuario crearInterno(String nombreUsuario, String correo, String clave,
                                Rol rol, SubtipoEmpleado subtipo) {
        Usuario usuario = new Usuario(nombreUsuario, correo, codificador.encode(clave), rol);
        usuario.setSubtipo(rol == Rol.EMPLEADO ? subtipo : null);
        usuario.setFechaCreacion(LocalDateTime.now());
        return repositorio.save(usuario);
    }

    /** Alias de listar(): lo usa el historial del panel. */
    public List<Usuario> todos() { return listar(); }

    public Optional<Usuario> porId(Long id) { return repositorio.findById(id); }

    public boolean existeNombreUsuario(String nombreUsuario, Long idActual) {
        return repositorio.findByNombreUsuario(nombreUsuario)
                .filter(u -> idActual == null || !u.getId().equals(idActual))
                .isPresent();
    }

    public boolean existeCorreo(String correo, Long idActual) {
        return repositorio.findByCorreo(correo)
                .filter(u -> idActual == null || !u.getId().equals(idActual))
                .isPresent();
    }

    /**
     * Edita los datos de una cuenta interna. No toca la clave: eso es un
     * paso aparte y explicito, para no resetear sin querer la clave de
     * alguien por editar su correo.
     */
    public boolean editar(Long id, String nombreUsuario, String correo, Rol rol, SubtipoEmpleado subtipo) {
        return repositorio.findById(id).map(usuario -> {
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setCorreo(correo);
            usuario.setRol(rol);
            usuario.setSubtipo(rol == Rol.EMPLEADO ? subtipo : null);
            repositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    public boolean cambiarClave(Long id, String claveNueva) {
        return repositorio.findById(id).map(usuario -> {
            usuario.setClave(codificador.encode(claveNueva));
            repositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    public boolean activar(Long id, boolean activo) {
        return repositorio.findById(id).map(usuario -> {
            usuario.setActivo(activo);
            repositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    public long total() { return repositorio.count(); }

    public long cuantosPorRol(Rol rol) { return repositorio.countByRol(rol); }

    public long cuantosActivos() { return repositorio.countByActivoTrue(); }
}
