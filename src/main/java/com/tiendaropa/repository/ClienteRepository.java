package com.tiendaropa.repository;

import java.util.List;
import java.util.Optional;
import com.tiendaropa.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuarioNombreUsuario(String nombreUsuario);

    @Query("SELECT c FROM Cliente c WHERE LOWER(CONCAT(c.nombres,' ',c.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "OR LOWER(c.ciudad) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Cliente> buscar(String texto);

    /** Busqueda del vendedor para armar una venta asistida: por usuario, correo o nombre. */
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.usuario.nombreUsuario) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "OR LOWER(c.usuario.correo) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "OR LOWER(CONCAT(c.nombres,' ',c.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Cliente> buscarParaVentaAsistida(String texto);

    long countByAutorizaSensiblesTrue();
    long countByAutorizaMarketingTrue();
}
