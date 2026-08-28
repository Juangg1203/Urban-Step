package com.tiendaropa.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.tiendaropa.model.*;
import com.tiendaropa.repository.*;
import com.tiendaropa.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial para poder probar el sitio de una vez: catalogo, usuarios
 * internos, clientes y tres meses de atenciones. Se desactiva poniendo
 * app.datos-demo=false en application.properties.
 */
@Component
@ConditionalOnProperty(name = "app.datos-demo", havingValue = "true")
public class DatosDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);
    private final Random azar = new Random(2026);

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final AtencionRepository atencionRepo;
    private final CompraClienteRepository compraRepo;
    private final UsuarioService usuarioService;
    private final PedidoRepository pedidoRepo;

    public DatosDemo(CategoriaRepository categoriaRepo, ProductoRepository productoRepo,
                     UsuarioRepository usuarioRepo, ClienteRepository clienteRepo,
                     AtencionRepository atencionRepo, CompraClienteRepository compraRepo,
                     UsuarioService usuarioService,
                     PedidoRepository pedidoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.atencionRepo = atencionRepo;
        this.compraRepo = compraRepo;
        this.usuarioService = usuarioService;
        this.pedidoRepo = pedidoRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productoRepo.count() == 0) crearCatalogo();
        // El personal se revisa siempre, no solo con la tabla vacia: si ya
        // existe algun cliente pero falta el jefe (o cualquier otro), aqui
        // se completa sin duplicar a los que ya estan.
        crearPersonalSiFalta();
        // Los clientes de ejemplo se guian por su propia tabla, no por la de
        // usuarios: si no, nunca se crearian, porque el personal de arriba
        // ya dejo la tabla de usuarios con filas.
        if (clienteRepo.count() == 0) crearUsuarios();
        if (atencionRepo.count() == 0) crearAtenciones();
        if (pedidoRepo.count() == 0) crearPedidos();
    }

    /**
     * Pedidos de ejemplo repartidos por todo el ciclo de vida, para que el
     * panel de aprobaciones y la bandeja de bodega no arranquen vacios.
     */
    private void crearPedidos() {
        List<Cliente> clientes = clienteRepo.findAll();
        List<Producto> productos = productoRepo.findAll();
        if (clientes.isEmpty() || productos.isEmpty()) {
            log.warn("Sin clientes o productos: se omiten los pedidos de demostracion.");
            return;
        }
        Usuario jefe = primero(usuarioRepo.findByRol(Rol.JEFE));
        List<Usuario> empleados = usuarioRepo.findByRol(Rol.EMPLEADO);
        Usuario vendedor = empleados.stream()
                .filter(Usuario::isVendedor).findFirst().orElse(primero(empleados));
        Usuario bodeguero = empleados.stream()
                .filter(Usuario::isBodeguero).findFirst().orElse(primero(empleados));

        // El pedido nace listo para pagar: no hay aprobacion previa. Despues
        // del pago hay dos filtros en cascada (vendedor, luego jefe) antes
        // de que bodega lo vea.
        EstadoPedido[] guion = {
            EstadoPedido.COTIZACION,
            EstadoPedido.PENDIENTE_PAGO,
            EstadoPedido.PAGO_EN_VERIFICACION,
            EstadoPedido.PENDIENTE_ACEPTACION_JEFE,
            EstadoPedido.PAGADO,
            EstadoPedido.EN_PREPARACION,
            EstadoPedido.DESPACHADO,
            EstadoPedido.ENTREGADO,
            EstadoPedido.RECHAZADO
        };

        String[] direcciones = {
            "Calle 36 # 22-15, apto 402", "Carrera 27 # 45-08", "Avenida Quebradaseca # 30-12",
            "Calle 105 # 14-33, torre 2", "Carrera 9 # 70-25"
        };
        String[] medios = {"PSE", "TARJETA", "CONTRA_ENTREGA", "EFECTIVO"};

        for (int i = 0; i < guion.length; i++) {
            EstadoPedido destino = guion[i];
            Cliente cliente = clientes.get(i % clientes.size());

            Pedido pedido = new Pedido(cliente);
            pedido.setNumero((destino == EstadoPedido.COTIZACION ? "COT-" : "PED-")
                    + "DEMO-" + String.format("%04d", i + 1));
            pedido.setFecha(LocalDateTime.now().minusDays(guion.length - i).minusHours(azar.nextInt(10)));
            pedido.setDireccionEntrega(direcciones[i % direcciones.length]);
            pedido.setMedioPago(medios[i % medios.length]);

            int lineas = 1 + azar.nextInt(3);
            for (int j = 0; j < lineas; j++) {
                Producto producto = productos.get(azar.nextInt(productos.size()));
                pedido.agregar(new ItemPedido(producto, tallaDe(producto), 1 + azar.nextInt(2)));
            }
            pedido.recalcular();
            pedido.setEstado(destino);

            // Los estados avanzados llevan la huella de quien los movio.
            boolean yaSePago = destino.getPaso() >= 2 || destino == EstadoPedido.RECHAZADO;
            if (yaSePago) {
                pedido.setReferenciaPago("REF" + (100000 + azar.nextInt(899999)));
            }
            boolean vendedorYaVerifico = destino.getPaso() >= 3 || destino == EstadoPedido.RECHAZADO;
            if (vendedorYaVerifico && vendedor != null) {
                pedido.setPagoVerificadoPor(vendedor);
            }
            if (destino == EstadoPedido.RECHAZADO) {
                pedido.setAprobadoPor(jefe);
                pedido.setFechaAprobacion(pedido.getFecha().plusHours(3));
                pedido.setMotivoDecision("El comprobante no correspondia al monto del pedido.");
            } else if (destino.getPaso() >= 4) {
                // El jefe ya dio su visto bueno final (estado PAGADO en adelante).
                pedido.setAprobadoPor(jefe);
                pedido.setFechaAprobacion(pedido.getFecha().plusHours(4));
            }
            if (destino.getPaso() >= 6 && bodeguero != null) {
                pedido.setDespachadoPor(bodeguero);
                pedido.setNumeroGuia("GU" + (7000000 + azar.nextInt(999999)));
                pedido.setFechaDespacho(pedido.getFecha().plusDays(1));
            }
            if (destino == EstadoPedido.ENTREGADO) {
                pedido.setFechaEntrega(pedido.getFecha().plusDays(3));
            }
            pedidoRepo.save(pedido);
        }
        log.info("Pedidos de demostracion creados: {}", guion.length);
    }

    private Usuario primero(List<Usuario> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Toma la primera talla declarada en el producto. */
    private String tallaDe(Producto producto) {
        String tallas = producto.getTallas();
        if (tallas == null || tallas.isBlank()) return null;
        return tallas.split(",")[0].trim();
    }

    /**
     * Devuelve la categoria si el script SQL ya la inserto; si no, la crea.
     * Sin esto, cargar 02_datos_iniciales.sql dejaba las categorias creadas
     * pero el catalogo vacio, y la demo reventaba al buscar productos.
     */
    private Categoria categoria(String nombre, String linea) {
        return categoriaRepo.findAll().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> categoriaRepo.save(new Categoria(nombre, linea)));
    }

    // ------------------------------------------------------------------
    private void crearCatalogo() {
        Categoria camisetas  = categoria("Camisetas", "ROPA");
        Categoria pantalones = categoria("Pantalones", "ROPA");
        Categoria chaquetas  = categoria("Chaquetas", "ROPA");
        Categoria tenis      = categoria("Tenis", "CALZADO");
        Categoria botas      = categoria("Botas", "CALZADO");
        Categoria sandalias  = categoria("Sandalias", "CALZADO");

        Producto cam1 = new Producto("CAM-001", "Camiseta Hilo Crudo",
                "Tejido de punto en algodon peinado, cuello reforzado y costura plana.",
                camisetas, new BigDecimal("59900"), "XS,S,M,L,XL", "Crudo", "Algodon 100%", 48);
        cam1.setImagen("/recursos/img/productos/cam-001.webp");
        productoRepo.save(cam1);

        Producto cam2 = new Producto("CAM-002", "Camiseta Grid Indigo",
                "Estampado grafico sobre base blanca, silueta clasica.",
                camisetas, new BigDecimal("69900"), "S,M,L,XL", "Blanco", "Algodon 100%", 32);
        cam2.setImagen("/recursos/img/productos/cam-002.jpg");
        productoRepo.save(cam2);

        Producto cam3 = new Producto("CAM-003", "Camiseta Oversize Blanca",
                "Corte oversize en algodon suave, silueta relajada para el dia a dia.",
                camisetas, new BigDecimal("64900"), "S,M,L,XL", "Blanco", "Algodon 100%", 40);
        cam3.setImagen("/recursos/img/productos/cam-003.webp");
        productoRepo.save(cam3);

        Producto cam4 = new Producto("CAM-004", "Camiseta Boxing Club",
                "Estampado tipo universitario, algodon grueso de buena caida.",
                camisetas, new BigDecimal("69900"), "S,M,L,XL", "Blanco", "Algodon 100%", 35);
        cam4.setImagen("/recursos/img/productos/cam-004.jpg");
        productoRepo.save(cam4);

        Producto cam5 = new Producto("CAM-005", "Camiseta Retro Estampada",
                "Estampado retro con rayas en las mangas, cuello redondo.",
                camisetas, new BigDecimal("62900"), "XS,S,M,L", "Marfil", "Algodon 100%", 28);
        cam5.setImagen("/recursos/img/productos/cam-005.webp");
        productoRepo.save(cam5);
        productoRepo.save(new Producto("PANT-001", "Pantalon Patron Recto",
                "Corte recto de tiro medio en dril liviano, pensado para clima calido.",
                pantalones, new BigDecimal("129900"), "28,30,32,34,36", "Arena", "Dril 98% algodon", 26));
        productoRepo.save(new Producto("PANT-002", "Jean Costura Doble",
                "Denim de 12 onzas con doble pespunte y remaches de laton.",
                pantalones, new BigDecimal("159900"), "28,30,32,34,36,38", "Azul medio", "Denim 12 oz", 21));
        productoRepo.save(new Producto("CHA-001", "Chaqueta Bastilla",
                "Chaqueta cortavientos con forro de malla y bolsillos internos.",
                chaquetas, new BigDecimal("189900"), "S,M,L,XL", "Verde oliva", "Poliamida", 14));
        productoRepo.save(new Producto("TEN-001", "Tenis Suela Blanca",
                "Cuero flor con suela de caucho vulcanizado y plantilla acolchada.",
                tenis, new BigDecimal("219900"), "36,37,38,39,40,41,42,43", "Blanco", "Cuero", 30));
        productoRepo.save(new Producto("TEN-002", "Tenis Puntada Cruzada",
                "Malla transpirable sobre suela de EVA, livianos para caminar todo el dia.",
                tenis, new BigDecimal("179900"), "35,36,37,38,39,40,41,42", "Gris", "Malla tecnica", 27));
        productoRepo.save(new Producto("BOT-001", "Bota Horma Alta",
                "Bota de cuero graso con costura Goodyear, resuelable.",
                botas, new BigDecimal("289900"), "38,39,40,41,42,43,44", "Miel", "Cuero graso", 12));
        productoRepo.save(new Producto("SAN-001", "Sandalia Tira Ancha",
                "Tira ancha de cuero vegetal y plantilla anatomica de corcho.",
                sandalias, new BigDecimal("119900"), "35,36,37,38,39,40", "Cafe", "Cuero vegetal", 22));
        log.info("Catalogo de demostracion creado");
    }

    // ------------------------------------------------------------------
    /**
     * Crea admin, jefe, vendedor y bodeguero SOLO si cada uno todavia no
     * existe. A diferencia de crearUsuarios() (que solo corre una vez, con
     * la base recien creada), este metodo se ejecuta en cada arranque: si
     * alguien borro al jefe sin querer, o si la base ya tenia clientes
     * cuando se agrego este rol, aqui se completa sin duplicar a nadie.
     */
    private void crearPersonalSiFalta() {
        crearSiNoExiste("admin", "admin@urbanstep.com", "Admin123", Rol.ADMIN, null);
        crearSiNoExiste("jefe", "jefe@urbanstep.com", "Jefe123", Rol.JEFE, null);
        crearSiNoExiste("vendedor", "vendedor@urbanstep.com", "Vendedor123",
                Rol.EMPLEADO, SubtipoEmpleado.VENDEDOR);
        crearSiNoExiste("bodeguero", "bodeguero@urbanstep.com", "Bodeguero123",
                Rol.EMPLEADO, SubtipoEmpleado.BODEGUERO);
    }

    private void crearSiNoExiste(String usuario, String correo, String clave, Rol rol,
                                 SubtipoEmpleado subtipo) {
        if (usuarioRepo.findByNombreUsuario(usuario).isPresent()) return;
        usuarioService.crearInterno(usuario, correo, clave, rol, subtipo);
        log.info("Cuenta de personal creada: {} ({})", usuario, rol);
    }

    private void crearUsuarios() {
        String[][] datos = {
            {"laura",  "laura@correo.com",  "Laura",   "Mendoza",  "Bucaramanga", "Santander", "Disenadora"},
            {"carlos", "carlos@correo.com", "Carlos",  "Ruiz",     "Bogota",      "Cundinamarca", "Ingeniero"},
            {"sofia",  "sofia@correo.com",  "Sofia",   "Ariza",    "Medellin",    "Antioquia", "Docente"},
            {"julian", "julian@correo.com", "Julian",  "Perez",    "Cali",        "Valle", "Contador"},
            {"marcela","marcela@correo.com","Marcela", "Gomez",    "Barranquilla","Atlantico", "Enfermera"},
            {"andres", "andres@correo.com", "Andres",  "Castro",   "Bucaramanga", "Santander", "Estudiante"}
        };

        for (String[] d : datos) {
            Usuario usuario = usuarioService.crearInterno(d[0], d[1], "Cliente123", Rol.CLIENTE);

            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setNombres(d[2]);
            cliente.setApellidos(d[3]);
            cliente.setCiudad(d[4]);
            cliente.setDepartamento(d[5]);
            cliente.setOcupacion(d[6]);
            cliente.setAceptaTratamiento(true);
            cliente.setVersionPolitica("v1.0-2026");
            cliente.setFechaAutorizacion(LocalDateTime.now().minusMonths(3));
            cliente.setAutorizaMarketing(azar.nextBoolean());

            DatoPrivadoCliente privados = new DatoPrivadoCliente();
            privados.setCliente(cliente);
            privados.setTipoDocumento("CC");
            privados.setNumeroDocumento(String.valueOf(1090000000L + azar.nextInt(9000000)));
            privados.setDireccion("Calle " + (10 + azar.nextInt(80)) + " # " + (5 + azar.nextInt(40))
                    + "-" + (10 + azar.nextInt(80)));
            privados.setTelefono("31" + (10000000 + azar.nextInt(89999999)));
            privados.setCorreoPersonal(d[1]);
            privados.setFechaNacimiento((1980 + azar.nextInt(25)) + "-0"
                    + (1 + azar.nextInt(9)) + "-1" + azar.nextInt(9));
            cliente.setDatosPrivados(privados);

            // Solo algunos clientes autorizan datos sensibles: asi se nota la diferencia.
            if (azar.nextInt(3) == 0) {
                cliente.setAutorizaSensibles(true);
                DatoSensibleCliente sensibles = new DatoSensibleCliente();
                sensibles.setCliente(cliente);
                sensibles.setMedidasCorporales("Pecho " + (86 + azar.nextInt(20)) + " cm, cintura "
                        + (70 + azar.nextInt(20)) + " cm, calzado " + (36 + azar.nextInt(8)));
                sensibles.setAlergiasMateriales(azar.nextBoolean() ? "Lana virgen" : "Ninguna reportada");
                sensibles.setCondicionMovilidad("Sin restricciones");
                sensibles.setRestriccionVestimenta("Prefiere prendas de manga larga");
                sensibles.setAutorizado(true);
                sensibles.setFechaAutorizacion(LocalDateTime.now().minusMonths(2));
                cliente.setDatosSensibles(sensibles);
            }
            clienteRepo.save(cliente);
        }

        // Historial de compras (nivel semiprivado)
        List<Producto> productos = productoRepo.findAll();
        if (productos.isEmpty()) {
            log.warn("No hay productos en la base: se omite el historial de compras de la demo.");
            return;
        }
        for (Cliente cliente : clienteRepo.findAll()) {
            int compras = 1 + azar.nextInt(4);
            for (int i = 0; i < compras; i++) {
                Producto producto = productos.get(azar.nextInt(productos.size()));
                CompraCliente compra = new CompraCliente();
                compra.setCliente(cliente);
                compra.setProducto(producto);
                compra.setCantidad(1 + azar.nextInt(2));
                compra.setMonto(producto.getPrecio());
                compra.setMedioPago(azar.nextBoolean() ? "Tarjeta" : "PSE");
                compra.setEstadoPago(azar.nextInt(10) == 0 ? "Pendiente" : "Pagado");
                compra.setFecha(LocalDateTime.now().minusDays(azar.nextInt(90)));
                compraRepo.save(compra);
            }
        }
        log.info("Usuarios de demostracion creados (admin/Admin123, agente/Agente123, "
               + "marketing/Marketing123, oficial/Oficial123, laura/Cliente123)");
    }

    // ------------------------------------------------------------------
    private void crearAtenciones() {
        List<Cliente> clientes = clienteRepo.findAll();
        if (clientes.isEmpty()) return;
        List<Usuario> agentes = usuarioRepo.findByRol(Rol.EMPLEADO);

        String[] buenas = {
            "El chat resolvio la duda de talla en un minuto, muy claro.",
            "Me gusto que respondieran de noche, no tuve que esperar al otro dia.",
            "El cambio de talla fue sencillo y no me cobraron el envio.",
            "Buena atencion, aunque me gustaria ver el numero de guia en la pagina.",
            "Excelente, la guia de tallas del bot fue exacta."
        };
        String[] regulares = {
            "Me respondieron bien pero tuve que repetir el pedido dos veces.",
            "El bot no entendio mi pregunta sobre materiales, me toco escribir a un asesor.",
            "La informacion estaba bien, la demora fue el problema."
        };
        String[] malas = {
            "Espere dos dias por una respuesta sobre mi devolucion.",
            "El bot no sabia nada de mi pedido y nadie me llamo.",
            "Me llego una talla distinta y el proceso de cambio fue confuso."
        };
        Tema[] temas = Tema.values();
        Canal[] canales = {Canal.CHATBOT, Canal.CHATBOT, Canal.CHATBOT, Canal.AGENTE, Canal.CORREO, Canal.TIENDA};

        List<Atencion> lote = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (int mesAtras = 2; mesAtras >= 0; mesAtras--) {
            int cantidad = 26 + azar.nextInt(14);
            for (int i = 0; i < cantidad; i++) {
                Atencion atencion = new Atencion();
                boolean anonimo = azar.nextInt(5) == 0;
                atencion.setCliente(anonimo ? null : clientes.get(azar.nextInt(clientes.size())));
                atencion.setCanal(canales[azar.nextInt(canales.length)]);
                atencion.setTema(temas[azar.nextInt(temas.length)]);

                LocalDateTime base = ahora.minusMonths(mesAtras).withDayOfMonth(1 + azar.nextInt(26))
                        .withHour(azar.nextInt(24)).withMinute(azar.nextInt(60));
                atencion.setFechaInicio(base);
                atencion.setFechaCierre(base.plusMinutes(5 + azar.nextInt(90)));

                boolean escalada = azar.nextInt(4) == 0;
                atencion.setEstado(escalada ? EstadoAtencion.ESCALADA : EstadoAtencion.CERRADA);
                if (atencion.getCanal() != Canal.CHATBOT && !agentes.isEmpty()) {
                    atencion.setAgente(agentes.get(0));
                }

                // El mes mas reciente sale un poco mejor calificado que los anteriores.
                int sesgo = (mesAtras == 0) ? 1 : 0;
                int estrellas = Math.min(5, 2 + azar.nextInt(4) + sesgo);
                if (escalada && azar.nextBoolean()) estrellas = Math.max(1, estrellas - 2);
                atencion.setCalificacion(estrellas);
                atencion.setResuelta(estrellas >= 3 && !escalada);

                if (azar.nextInt(3) == 0) {
                    if (estrellas >= 4) atencion.setRecomendacion(buenas[azar.nextInt(buenas.length)]);
                    else if (estrellas == 3) atencion.setRecomendacion(regulares[azar.nextInt(regulares.length)]);
                    else atencion.setRecomendacion(malas[azar.nextInt(malas.length)]);
                }
                lote.add(atencion);
            }
        }
        atencionRepo.saveAll(lote);
        log.info("Atenciones de demostracion creadas: {}", lote.size());
    }
}
