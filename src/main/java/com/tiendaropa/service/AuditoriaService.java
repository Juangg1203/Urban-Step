package com.tiendaropa.service;

import java.util.List;
import com.tiendaropa.model.LogAuditoria;
import com.tiendaropa.model.NivelDato;
import com.tiendaropa.repository.LogAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Deja rastro de cada consulta o cambio sobre datos personales.
 * Sin este registro no se puede demostrar quien vio que, que es
 * justamente lo que exige el principio de responsabilidad demostrada.
 */
@Service
public class AuditoriaService {

    private final LogAuditoriaRepository repositorio;

    public AuditoriaService(LogAuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrar(String accion, NivelDato nivel, String entidad,
                          Long registroId, String detalle) {
        LogAuditoria log = new LogAuditoria();
        log.setAccion(accion);
        log.setNivelDato(nivel);
        log.setEntidad(entidad);
        log.setRegistroId(registroId);
        log.setDetalle(detalle);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            log.setUsuario(auth.getName());
            log.setRol(auth.getAuthorities().stream().findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", "")).orElse("-"));
        } else {
            log.setUsuario("anonimo");
            log.setRol("-");
        }
        log.setIp(direccionIp());
        repositorio.save(log);
    }

    public List<LogAuditoria> ultimos() {
        return repositorio.findTop200ByOrderByFechaDesc();
    }

    private String direccionIp() {
        try {
            ServletRequestAttributes atributos =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (atributos == null) return "-";
            HttpServletRequest peticion = atributos.getRequest();
            String reenviada = peticion.getHeader("X-Forwarded-For");
            return (reenviada != null && !reenviada.isBlank())
                    ? reenviada.split(",")[0].trim()
                    : peticion.getRemoteAddr();
        } catch (Exception e) {
            return "-";
        }
    }
}
