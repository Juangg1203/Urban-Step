package com.tiendaropa.repository;

import java.util.List;
import java.util.Optional;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByCorreo(String correo);
    List<Usuario> findByRol(Rol rol);

    long countByRol(Rol rol);

    long countByActivoTrue();
}
