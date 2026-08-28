package com.tiendaropa.repository;

import java.util.List;
import com.tiendaropa.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByLinea(String linea);
}
