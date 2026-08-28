package com.tiendaropa.service;

import java.util.List;

import com.tiendaropa.model.Resena;
import com.tiendaropa.repository.ResenaRepository;
import org.springframework.stereotype.Service;

/**
 * Lectura de resenas para el catalogo. La escritura vive en
 * PedidoService.dejarResena, porque una resena solo tiene sentido atada al
 * pedido que la origino (no se puede resenar sin haber comprado y recibido).
 */
@Service
public class ResenaService {

    private final ResenaRepository repo;

    public ResenaService(ResenaRepository repo) {
        this.repo = repo;
    }

    public List<Resena> deProducto(Long productoId) {
        return repo.findByProductoIdOrderByFechaDesc(productoId);
    }

    /** Redondeado a un decimal; 0 si el producto todavia no tiene resenas. */
    public double promedioDe(Long productoId) {
        double promedio = repo.promedioDe(productoId);
        return Math.round(promedio * 10.0) / 10.0;
    }

    public long cuantasTiene(Long productoId) {
        return repo.countByProductoId(productoId);
    }
}
