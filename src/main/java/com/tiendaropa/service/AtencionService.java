package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tiendaropa.model.*;
import com.tiendaropa.repository.AtencionRepository;
import com.tiendaropa.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtencionService {

    private final AtencionRepository atencionRepo;
    private final UsuarioRepository usuarioRepo;
    private final AuditoriaService auditoria;

    public AtencionService(AtencionRepository atencionRepo, UsuarioRepository usuarioRepo,
                           AuditoriaService auditoria) {
        this.atencionRepo = atencionRepo;
        this.usuarioRepo = usuarioRepo;
        this.auditoria = auditoria;
    }

    public List<Atencion> recientes() { return atencionRepo.findTop50ByOrderByFechaInicioDesc(); }

    public List<Atencion> pendientes() {
        List<Atencion> abiertas = new java.util.ArrayList<>(
                atencionRepo.findByEstadoOrderByFechaInicioDesc(EstadoAtencion.ESCALADA));
        abiertas.addAll(atencionRepo.findByEstadoOrderByFechaInicioDesc(EstadoAtencion.ABIERTA));
        return abiertas;
    }

    public List<Atencion> deCliente(Long clienteId) {
        return atencionRepo.findByClienteIdOrderByFechaInicioDesc(clienteId);
    }

    public Optional<Atencion> porId(Long id) { return atencionRepo.findById(id); }

    @Transactional
    public void tomar(Long atencionId, String nombreUsuarioAgente) {
        atencionRepo.findById(atencionId).ifPresent(atencion -> {
            usuarioRepo.findByNombreUsuario(nombreUsuarioAgente).ifPresent(atencion::setAgente);
            atencionRepo.save(atencion);
            auditoria.registrar("ASIGNACION", NivelDato.PUBLICO, "Atencion", atencionId,
                    "Atencion tomada por " + nombreUsuarioAgente);
        });
    }

    @Transactional
    public void cerrar(Long atencionId, boolean resuelta, Tema tema) {
        atencionRepo.findById(atencionId).ifPresent(atencion -> {
            atencion.setEstado(EstadoAtencion.CERRADA);
            atencion.setResuelta(resuelta);
            if (tema != null) atencion.setTema(tema);
            atencion.setFechaCierre(LocalDateTime.now());
            atencionRepo.save(atencion);
            auditoria.registrar("CIERRE_ATENCION", NivelDato.PUBLICO, "Atencion", atencionId,
                    "Atencion cerrada. Resuelta: " + resuelta);
        });
    }

    public long totalDelMes(int anio, int mes) {
        LocalDateTime desde = LocalDateTime.of(anio, mes, 1, 0, 0);
        return atencionRepo.findByFechaInicioBetweenOrderByFechaInicioDesc(
                desde, desde.plusMonths(1).minusSeconds(1)).size();
    }
}
