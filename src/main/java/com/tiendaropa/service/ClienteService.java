package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.tiendaropa.dto.Acceso;
import com.tiendaropa.dto.RegistroForm;
import com.tiendaropa.dto.VistaClienteDTO;
import com.tiendaropa.model.*;
import com.tiendaropa.repository.*;
import com.tiendaropa.util.Enmascarar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final CompraClienteRepository compraRepo;
    private final PoliticaAccesoService politica;
    private final AuditoriaService auditoria;
    private final PasswordEncoder codificador;

    @Value("${app.politica.version}")
    private String versionPolitica;

    public ClienteService(ClienteRepository clienteRepo, UsuarioRepository usuarioRepo,
                          CompraClienteRepository compraRepo, PoliticaAccesoService politica,
                          AuditoriaService auditoria, PasswordEncoder codificador) {
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
        this.compraRepo = compraRepo;
        this.politica = politica;
        this.auditoria = auditoria;
        this.codificador = codificador;
    }

    // ------------------------------------------------------------------
    //  Registro
    // ------------------------------------------------------------------
    @Transactional
    public Cliente registrar(RegistroForm form) {
        Usuario usuario = new Usuario(form.getNombreUsuario().trim(),
                form.getCorreo().trim().toLowerCase(),
                codificador.encode(form.getClave()), Rol.CLIENTE);
        usuarioRepo.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNombres(form.getNombres().trim());
        cliente.setApellidos(form.getApellidos().trim());
        cliente.setCiudad(form.getCiudad());
        cliente.setDepartamento(form.getDepartamento());
        cliente.setOcupacion(form.getOcupacion());
        cliente.setAceptaTratamiento(form.isAceptaTratamiento());
        cliente.setAutorizaMarketing(form.isAutorizaMarketing());
        cliente.setVersionPolitica(versionPolitica);
        cliente.setFechaAutorizacion(LocalDateTime.now());

        DatoPrivadoCliente privados = new DatoPrivadoCliente();
        privados.setCliente(cliente);
        privados.setTipoDocumento(form.getTipoDocumento());
        privados.setNumeroDocumento(form.getNumeroDocumento());
        privados.setDireccion(form.getDireccion());
        privados.setTelefono(form.getTelefono());
        privados.setCorreoPersonal(form.getCorreo());
        privados.setFechaNacimiento(form.getFechaNacimiento());
        cliente.setDatosPrivados(privados);

        clienteRepo.save(cliente);

        auditoria.registrar("REGISTRO_CLIENTE", NivelDato.PRIVADO, "Cliente", cliente.getId(),
                "Alta de cliente con autorizacion " + versionPolitica);
        return cliente;
    }

    public boolean existeUsuario(String nombreUsuario) { return usuarioRepo.existsByNombreUsuario(nombreUsuario); }
    public boolean existeCorreo(String correo) { return usuarioRepo.existsByCorreo(correo); }

    public Optional<Cliente> porNombreUsuario(String nombreUsuario) {
        return clienteRepo.findByUsuarioNombreUsuario(nombreUsuario);
    }

    public Optional<Cliente> porId(Long id) { return clienteRepo.findById(id); }

    public List<Cliente> listar() { return clienteRepo.findAll(); }

    public List<Cliente> buscar(String texto) {
        return (texto == null || texto.isBlank()) ? clienteRepo.findAll() : clienteRepo.buscar(texto);
    }

    // ------------------------------------------------------------------
    //  Ficha del cliente filtrada por la politica de acceso
    // ------------------------------------------------------------------
    @Transactional
    public VistaClienteDTO armarVista(Cliente cliente, Rol rolConsulta, boolean esTitular) {
        VistaClienteDTO vista = new VistaClienteDTO();
        vista.setCliente(cliente);

        Acceso semiprivado = politica.evaluar(rolConsulta, NivelDato.SEMIPRIVADO, esTitular);
        Acceso privado = politica.evaluar(rolConsulta, NivelDato.PRIVADO, esTitular);
        Acceso sensible = politica.evaluar(rolConsulta, NivelDato.SENSIBLE, esTitular);
        vista.setAccesoSemiprivado(semiprivado);
        vista.setAccesoPrivado(privado);
        vista.setAccesoSensible(sensible);

        // --- semiprivado: historial de compras
        if (semiprivado.isCompleto()) {
            vista.setCompras(compraRepo.findByClienteIdOrderByFechaDesc(cliente.getId()));
            auditoria.registrar("CONSULTA", NivelDato.SEMIPRIVADO, "CompraCliente", cliente.getId(),
                    "Historial de compras de " + cliente.getNombreCompleto());
        }

        // --- privado
        DatoPrivadoCliente p = cliente.getDatosPrivados();
        if (p != null && !privado.isDenegado()) {
            vista.setTipoDocumento(p.getTipoDocumento());
            if (privado.isCompleto()) {
                vista.setNumeroDocumento(p.getNumeroDocumento());
                vista.setDireccion(p.getDireccion());
                vista.setTelefono(p.getTelefono());
                vista.setCorreoPersonal(p.getCorreoPersonal());
                vista.setFechaNacimiento(p.getFechaNacimiento());
            } else {
                vista.setNumeroDocumento(Enmascarar.documento(p.getNumeroDocumento()));
                vista.setDireccion(Enmascarar.direccion(p.getDireccion()));
                vista.setTelefono(Enmascarar.telefono(p.getTelefono()));
                vista.setCorreoPersonal(Enmascarar.correo(p.getCorreoPersonal()));
                vista.setFechaNacimiento(Enmascarar.oculto());
            }
            auditoria.registrar("CONSULTA", NivelDato.PRIVADO, "DatoPrivadoCliente", cliente.getId(),
                    "Acceso " + privado.name() + " a datos privados de " + cliente.getNombreCompleto());
        } else if (p != null) {
            auditoria.registrar("ACCESO_DENEGADO", NivelDato.PRIVADO, "DatoPrivadoCliente", cliente.getId(),
                    "El rol " + rolConsulta + " no tiene permiso sobre datos privados");
        }

        // --- sensible
        DatoSensibleCliente s = cliente.getDatosSensibles();
        boolean verMetadato = politica.puedeVerMetadatoSensible(rolConsulta, esTitular);
        if (s != null && verMetadato) {
            vista.setSensiblesRegistrados(true);
            vista.setSensiblesAutorizados(s.isAutorizado());
            vista.setFechaAutorizacionSensibles(s.getFechaAutorizacion() != null
                    ? s.getFechaAutorizacion().format(FORMATO) : "-");
        }
        if (s != null && sensible.isCompleto()) { // solo el titular
            vista.setMedidasCorporales(s.getMedidasCorporales());
            vista.setAlergiasMateriales(s.getAlergiasMateriales());
            vista.setCondicionMovilidad(s.getCondicionMovilidad());
            vista.setRestriccionVestimenta(s.getRestriccionVestimenta());
            auditoria.registrar("CONSULTA", NivelDato.SENSIBLE, "DatoSensibleCliente", cliente.getId(),
                    "El titular consulto sus propios datos sensibles");
        } else if (s != null && !esTitular) {
            auditoria.registrar("ACCESO_DENEGADO", NivelDato.SENSIBLE, "DatoSensibleCliente", cliente.getId(),
                    "El rol " + rolConsulta + " intento ver datos sensibles");
        }
        return vista;
    }

    // ------------------------------------------------------------------
    //  Actualizaciones hechas por el propio titular
    // ------------------------------------------------------------------
    @Transactional
    public void actualizarPublicos(Cliente cliente, String nombres, String apellidos,
                                   String ciudad, String departamento, String ocupacion) {
        cliente.setNombres(nombres);
        cliente.setApellidos(apellidos);
        cliente.setCiudad(ciudad);
        cliente.setDepartamento(departamento);
        cliente.setOcupacion(ocupacion);
        clienteRepo.save(cliente);
        auditoria.registrar("MODIFICACION", NivelDato.PUBLICO, "Cliente", cliente.getId(),
                "El titular actualizo sus datos publicos");
    }

    @Transactional
    public void actualizarPrivados(Cliente cliente, String tipoDoc, String numeroDoc, String direccion,
                                   String telefono, String correoPersonal, String fechaNacimiento) {
        DatoPrivadoCliente p = cliente.getDatosPrivados();
        if (p == null) {
            p = new DatoPrivadoCliente();
            p.setCliente(cliente);
            cliente.setDatosPrivados(p);
        }
        p.setTipoDocumento(tipoDoc);
        p.setNumeroDocumento(numeroDoc);
        p.setDireccion(direccion);
        p.setTelefono(telefono);
        p.setCorreoPersonal(correoPersonal);
        p.setFechaNacimiento(fechaNacimiento);
        p.setActualizado(LocalDateTime.now());
        clienteRepo.save(cliente);
        auditoria.registrar("MODIFICACION", NivelDato.PRIVADO, "DatoPrivadoCliente", cliente.getId(),
                "El titular actualizo sus datos privados (guardados cifrados)");
    }

    @Transactional
    public void guardarSensibles(Cliente cliente, String medidas, String alergias,
                                 String movilidad, String vestimenta) {
        if (!cliente.isAutorizaSensibles()) {
            throw new IllegalStateException("Primero debes autorizar el uso de datos sensibles");
        }
        DatoSensibleCliente s = cliente.getDatosSensibles();
        if (s == null) {
            s = new DatoSensibleCliente();
            s.setCliente(cliente);
            cliente.setDatosSensibles(s);
        }
        s.setMedidasCorporales(medidas);
        s.setAlergiasMateriales(alergias);
        s.setCondicionMovilidad(movilidad);
        s.setRestriccionVestimenta(vestimenta);
        s.setAutorizado(true);
        s.setFechaAutorizacion(LocalDateTime.now());
        clienteRepo.save(cliente);
        auditoria.registrar("MODIFICACION", NivelDato.SENSIBLE, "DatoSensibleCliente", cliente.getId(),
                "El titular guardo datos sensibles cifrados");
    }

    @Transactional
    public void cambiarAutorizacionSensibles(Cliente cliente, boolean autoriza) {
        cliente.setAutorizaSensibles(autoriza);
        cliente.setFechaAutorizacion(LocalDateTime.now());
        if (!autoriza && cliente.getDatosSensibles() != null) {
            // Revocar equivale a eliminar: no se conserva el dato "por si acaso".
            cliente.setDatosSensibles(null);
        }
        clienteRepo.save(cliente);
        auditoria.registrar(autoriza ? "AUTORIZACION" : "REVOCACION", NivelDato.SENSIBLE,
                "Cliente", cliente.getId(),
                autoriza ? "El titular autorizo el uso de datos sensibles"
                         : "El titular revoco la autorizacion y se eliminaron los datos sensibles");
    }

    @Transactional
    public void cambiarAutorizacionMarketing(Cliente cliente, boolean autoriza) {
        cliente.setAutorizaMarketing(autoriza);
        clienteRepo.save(cliente);
        auditoria.registrar(autoriza ? "AUTORIZACION" : "REVOCACION", NivelDato.SEMIPRIVADO,
                "Cliente", cliente.getId(), "Preferencia de comunicaciones comerciales");
    }

    public long conAutorizacionSensibles() { return clienteRepo.countByAutorizaSensiblesTrue(); }
    public long conAutorizacionMarketing() { return clienteRepo.countByAutorizaMarketingTrue(); }
    public long total() { return clienteRepo.count(); }
}
