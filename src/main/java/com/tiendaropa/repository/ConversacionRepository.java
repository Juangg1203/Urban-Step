package com.tiendaropa.repository;

import java.util.Optional;
import com.tiendaropa.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    Optional<Conversacion> findBySesion(String sesion);
}
