package com.tiendaropa.repository;

import java.util.List;

import com.tiendaropa.model.Notificacion;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByRolDestinoOrderByFechaDesc(Rol rolDestino);

    List<Notificacion> findByRolDestinoAndLeidaOrderByFechaDesc(Rol rolDestino, boolean leida);

    long countByRolDestinoAndLeida(Rol rolDestino, boolean leida);

    /**
     * Version que ademas filtra por subtipo, para que un vendedor no vea el
     * contador de avisos de bodega ni al reves. subtipoDestino es null para
     * los avisos que van a todo el rol (por ejemplo, al jefe).
     */
    long countByRolDestinoAndSubtipoDestinoAndLeida(Rol rolDestino, SubtipoEmpleado subtipo, boolean leida);
}
