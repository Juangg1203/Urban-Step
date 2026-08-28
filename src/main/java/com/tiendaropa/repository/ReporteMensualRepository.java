package com.tiendaropa.repository;

import java.util.List;
import java.util.Optional;
import com.tiendaropa.model.ReporteMensual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteMensualRepository extends JpaRepository<ReporteMensual, Long> {
    Optional<ReporteMensual> findFirstByAnioAndMesOrderByFechaGeneracionDesc(int anio, int mes);
    List<ReporteMensual> findAllByOrderByAnioDescMesDesc();
}
