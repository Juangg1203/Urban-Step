package com.tiendaropa.repository;

import java.util.List;
import com.tiendaropa.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    List<Producto> findByActivoTrueAndCategoriaLinea(String linea);

    List<Producto> findByActivoTrueAndCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND "
         + "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Producto> buscar(String texto);

    // ---- consultas del panel: incluyen productos inactivos ----
    List<Producto> findAllByOrderByNombreAsc();

    @Query("SELECT p FROM Producto p WHERE "
         + "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :texto, '%')) "
         + "ORDER BY p.nombre")
    List<Producto> buscarTodos(String texto);

    boolean existsBySku(String sku);

    java.util.Optional<Producto> findBySku(String sku);

    /** Para armar la referencia automatica: todos los SKU que ya usan un prefijo. */
    List<Producto> findBySkuStartingWithOrderBySkuAsc(String prefijo);

    long countByActivoTrue();

    long countByActivoTrueAndStockLessThanEqual(int limite);
}
