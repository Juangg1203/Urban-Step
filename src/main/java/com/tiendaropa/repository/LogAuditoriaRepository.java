package com.tiendaropa.repository;

import java.util.List;
import com.tiendaropa.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findTop200ByOrderByFechaDesc();
}
