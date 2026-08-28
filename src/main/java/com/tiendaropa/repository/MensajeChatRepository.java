package com.tiendaropa.repository;

import java.util.List;
import com.tiendaropa.model.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {
    List<MensajeChat> findByConversacionIdOrderByFechaAsc(Long conversacionId);
}
