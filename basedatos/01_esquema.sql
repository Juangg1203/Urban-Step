-- =====================================================================
--  TRAZO Y SUELA  |  Base de datos MySQL local
--  Clasificacion de datos personales segun Ley 1581 de 2012 (Colombia)
--  PUBLICO / SEMIPRIVADO / PRIVADO / SENSIBLE
-- =====================================================================
DROP DATABASE IF EXISTS tienda_ropa;
CREATE DATABASE tienda_ropa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tienda_ropa;

-- ---------------------------------------------------------------------
-- 1. USUARIOS (acceso al sistema)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario  VARCHAR(60)  NOT NULL UNIQUE,
    correo          VARCHAR(120) NOT NULL UNIQUE,
    clave           VARCHAR(120) NOT NULL,               -- BCrypt
    rol             VARCHAR(20)  NOT NULL,               -- CLIENTE, AGENTE, MARKETING, OFICIAL_DATOS, ADMIN
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  DATETIME     NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. CLIENTE  ->  NIVEL PUBLICO
--    Se puede consultar por cualquier rol interno. Queda auditado.
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id            BIGINT UNIQUE,
    nombres               VARCHAR(80) NOT NULL,
    apellidos             VARCHAR(80) NOT NULL,
    ciudad                VARCHAR(60),
    departamento          VARCHAR(60),
    ocupacion             VARCHAR(80),
    fecha_registro        DATETIME NOT NULL,
    -- Autorizaciones de habeas data
    acepta_tratamiento    BOOLEAN NOT NULL DEFAULT FALSE,
    autoriza_sensibles    BOOLEAN NOT NULL DEFAULT FALSE,
    autoriza_marketing    BOOLEAN NOT NULL DEFAULT FALSE,
    version_politica      VARCHAR(20),
    fecha_autorizacion    DATETIME,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. DATOS PRIVADOS  ->  NIVEL PRIVADO  (cifrados AES-256-GCM)
--    Solo el titular y el Oficial de Datos. El Administrador NO accede.
--    El Agente los ve enmascarados (10*******32).
-- ---------------------------------------------------------------------
CREATE TABLE dato_privado_cliente (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id        BIGINT NOT NULL UNIQUE,
    tipo_documento    VARCHAR(10),
    numero_documento  VARCHAR(400),   -- cifrado
    direccion         VARCHAR(400),   -- cifrado
    telefono          VARCHAR(400),   -- cifrado
    correo_personal   VARCHAR(400),   -- cifrado
    fecha_nacimiento  VARCHAR(400),   -- cifrado (yyyy-MM-dd)
    actualizado       DATETIME,
    CONSTRAINT fk_privado_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. DATOS SENSIBLES  ->  NIVEL SENSIBLE  (cifrados + autorizacion previa)
--    NINGUN rol interno los lee. Solo el titular. Se usan de forma
--    anonimizada y agregada para produccion y compras.
-- ---------------------------------------------------------------------
CREATE TABLE dato_sensible_cliente (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id            BIGINT NOT NULL UNIQUE,
    medidas_corporales    VARCHAR(600),  -- cifrado (dato biometrico)
    alergias_materiales   VARCHAR(600),  -- cifrado (dato de salud)
    condicion_movilidad   VARCHAR(600),  -- cifrado (dato de salud / discapacidad)
    restriccion_vestimenta VARCHAR(600), -- cifrado (creencias religiosas)
    autorizado            BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_autorizacion    DATETIME,
    CONSTRAINT fk_sensible_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. CATALOGO
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(60) NOT NULL,
    linea   VARCHAR(20) NOT NULL          -- ROPA | CALZADO
) ENGINE=InnoDB;

CREATE TABLE producto (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku          VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(120) NOT NULL,
    descripcion  VARCHAR(500),
    categoria_id BIGINT NOT NULL,
    precio       DECIMAL(12,2) NOT NULL,
    tallas       VARCHAR(120),
    color        VARCHAR(60),
    material     VARCHAR(80),
    stock        INT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. HISTORIAL DE COMPRAS  ->  NIVEL SEMIPRIVADO
--    Agente, Marketing y Administrador pueden consultarlo (auditado).
-- ---------------------------------------------------------------------
CREATE TABLE compra_cliente (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id   BIGINT NOT NULL,
    producto_id  BIGINT,
    fecha        DATETIME NOT NULL,
    cantidad     INT NOT NULL DEFAULT 1,
    monto        DECIMAL(12,2) NOT NULL,
    medio_pago   VARCHAR(30),
    estado_pago  VARCHAR(20),
    CONSTRAINT fk_compra_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_compra_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. ATENCION AL CLIENTE (insumo del reporte mensual)
-- ---------------------------------------------------------------------
CREATE TABLE atencion (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id     BIGINT,
    agente_id      BIGINT,
    canal          VARCHAR(20) NOT NULL,   -- CHATBOT | AGENTE | CORREO | TIENDA
    tema           VARCHAR(30),            -- TALLAS, ENVIOS, DEVOLUCIONES, PAGOS, DATOS, PRODUCTO, OTRO
    fecha_inicio   DATETIME NOT NULL,
    fecha_cierre   DATETIME,
    estado         VARCHAR(20) NOT NULL,   -- ABIERTA | CERRADA | ESCALADA
    resuelta       BOOLEAN NOT NULL DEFAULT FALSE,
    calificacion   INT,                    -- 1 a 5 estrellas
    recomendacion  VARCHAR(600),           -- comentario del cliente
    CONSTRAINT fk_atencion_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_atencion_agente  FOREIGN KEY (agente_id)  REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. CHATBOT
-- ---------------------------------------------------------------------
CREATE TABLE conversacion (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    sesion        VARCHAR(60) NOT NULL UNIQUE,
    cliente_id    BIGINT,
    atencion_id   BIGINT,
    fecha_inicio  DATETIME NOT NULL,
    fecha_fin     DATETIME,
    escalada      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_conv_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente(id),
    CONSTRAINT fk_conv_atencion FOREIGN KEY (atencion_id) REFERENCES atencion(id)
) ENGINE=InnoDB;

CREATE TABLE mensaje_chat (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    emisor          VARCHAR(10) NOT NULL,   -- CLIENTE | BOT | AGENTE
    texto           VARCHAR(2000) NOT NULL,
    intencion       VARCHAR(40),
    respondido_ia   BOOLEAN NOT NULL DEFAULT FALSE,
    fecha           DATETIME NOT NULL,
    CONSTRAINT fk_mensaje_conv FOREIGN KEY (conversacion_id) REFERENCES conversacion(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. AUDITORIA DE ACCESO A DATOS PERSONALES
-- ---------------------------------------------------------------------
CREATE TABLE log_auditoria (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATETIME NOT NULL,
    usuario      VARCHAR(60),
    rol          VARCHAR(20),
    accion       VARCHAR(40) NOT NULL,   -- CONSULTA, MODIFICACION, AUTORIZACION, REVOCACION, ACCESO_DENEGADO
    nivel_dato   VARCHAR(15),            -- PUBLICO, SEMIPRIVADO, PRIVADO, SENSIBLE
    entidad      VARCHAR(40),
    registro_id  BIGINT,
    detalle      VARCHAR(400),
    ip           VARCHAR(45)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. REPORTE MENSUAL (se guarda cada vez que se genera)
-- ---------------------------------------------------------------------
CREATE TABLE reporte_mensual (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    anio                INT NOT NULL,
    mes                 INT NOT NULL,
    personas_atendidas  INT NOT NULL,
    total_atenciones    INT NOT NULL,
    promedio_calificacion DOUBLE NOT NULL,
    satisfaccion_pct    DOUBLE NOT NULL,
    atenciones_chatbot  INT NOT NULL,
    atenciones_agente   INT NOT NULL,
    escaladas           INT NOT NULL,
    sugerencias         TEXT,
    fecha_generacion    DATETIME NOT NULL,
    generado_por        VARCHAR(60)
) ENGINE=InnoDB;

CREATE INDEX idx_atencion_fecha ON atencion (fecha_inicio);
CREATE INDEX idx_auditoria_fecha ON log_auditoria (fecha);

-- ============================================================
--  FASE 2: carrito, cotizaciones, pedidos y aprobaciones
-- ============================================================

-- El usuario ahora puede tener un subtipo (solo aplica al rol EMPLEADO)
ALTER TABLE usuario ADD COLUMN subtipo VARCHAR(20) NULL AFTER rol;

-- 11. PEDIDO (una cotizacion es un pedido en estado COTIZACION)
CREATE TABLE pedido (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero                  VARCHAR(20) NOT NULL UNIQUE,
    cliente_id              BIGINT NOT NULL,
    estado                  VARCHAR(30) NOT NULL,
    fecha                   DATETIME NOT NULL,
    fecha_actualizacion     DATETIME,
    subtotal                DECIMAL(12,2) NOT NULL DEFAULT 0,
    costo_envio             DECIMAL(12,2) NOT NULL DEFAULT 0,
    total                   DECIMAL(12,2) NOT NULL DEFAULT 0,
    medio_pago              VARCHAR(30),
    direccion_entrega       VARCHAR(400),
    observaciones           VARCHAR(400),
    aprobado_por_id         BIGINT,
    fecha_aprobacion        DATETIME,
    motivo_decision         VARCHAR(400),
    pago_verificado_por_id  BIGINT,
    referencia_pago         VARCHAR(60),
    despachado_por_id       BIGINT,
    numero_guia             VARCHAR(60),
    fecha_despacho          DATETIME,
    fecha_entrega           DATETIME,
    CONSTRAINT fk_pedido_cliente   FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_aprobo    FOREIGN KEY (aprobado_por_id) REFERENCES usuario(id),
    CONSTRAINT fk_pedido_verifico  FOREIGN KEY (pago_verificado_por_id) REFERENCES usuario(id),
    CONSTRAINT fk_pedido_despacho  FOREIGN KEY (despachado_por_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- 12. ITEM DEL PEDIDO (guarda el precio congelado, no el actual)
CREATE TABLE item_pedido (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        BIGINT NOT NULL,
    producto_id      BIGINT,
    nombre_producto  VARCHAR(120) NOT NULL,
    talla            VARCHAR(10),
    cantidad         INT NOT NULL DEFAULT 1,
    precio_unitario  DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_item_pedido   FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_item_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB;

-- 13. NOTIFICACION INTERNA (avisa al jefe y a bodega)
CREATE TABLE notificacion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_destino      VARCHAR(20) NOT NULL,
    subtipo_destino  VARCHAR(20),
    titulo           VARCHAR(120) NOT NULL,
    mensaje          VARCHAR(400) NOT NULL,
    enlace           VARCHAR(200),
    pedido_id        BIGINT,
    leida            BOOLEAN NOT NULL DEFAULT FALSE,
    fecha            DATETIME NOT NULL,
    CONSTRAINT fk_notif_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id)
) ENGINE=InnoDB;

CREATE INDEX idx_pedido_estado ON pedido (estado);
CREATE INDEX idx_pedido_cliente ON pedido (cliente_id, fecha);
CREATE INDEX idx_notif_rol ON notificacion (rol_destino, leida);

-- ============================================================
--  CRUD de productos: imagen
-- ============================================================
-- Se guarda el NOMBRE del archivo (o una URL), no la imagen: meter binarios
-- en MySQL la infla y encarece los respaldos sin ganar nada.
ALTER TABLE producto ADD COLUMN imagen VARCHAR(300) NULL AFTER material;

-- ============================================================
--  Inventario: nivel minimo de existencias
-- ============================================================
ALTER TABLE producto ADD COLUMN stock_minimo INT NOT NULL DEFAULT 5 AFTER stock;

-- ============================================================
--  Pasarela de pagos (Wompi)
-- ============================================================
-- Se guarda la referencia, el id de transaccion y el estado que reporta la
-- pasarela. Nunca datos de tarjeta: esos no pasan por esta aplicacion.
ALTER TABLE pedido ADD COLUMN referencia_pasarela  VARCHAR(60) NULL AFTER referencia_pago;
ALTER TABLE pedido ADD COLUMN transaccion_pasarela VARCHAR(60) NULL AFTER referencia_pasarela;
ALTER TABLE pedido ADD COLUMN estado_pasarela      VARCHAR(20) NULL AFTER transaccion_pasarela;
ALTER TABLE pedido ADD COLUMN metodo_pasarela      VARCHAR(30) NULL AFTER estado_pasarela;

CREATE INDEX idx_pedido_ref_pasarela ON pedido (referencia_pasarela);

-- ============================================================
--  Flujo de pago directo: comprobante y foto de entrega
-- ============================================================
-- El cliente puede adjuntar el comprobante de su pago y, al confirmar que
-- recibio el pedido, una foto como prueba desde su lado.
ALTER TABLE pedido ADD COLUMN comprobante_pago VARCHAR(300) NULL AFTER referencia_pago;
ALTER TABLE pedido ADD COLUMN foto_entrega     VARCHAR(300) NULL AFTER fecha_entrega;

-- ============================================================
--  Resenas
-- ============================================================
-- Solo se puede resenar un producto de un pedido que ya llego, una vez.
CREATE TABLE resena (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id   BIGINT NOT NULL,
    cliente_id    BIGINT NOT NULL,
    pedido_id     BIGINT NOT NULL,
    calificacion  INT NOT NULL,
    comentario    VARCHAR(600),
    fecha         DATETIME NOT NULL,
    CONSTRAINT fk_resena_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_resena_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente(id),
    CONSTRAINT fk_resena_pedido   FOREIGN KEY (pedido_id)   REFERENCES pedido(id),
    CONSTRAINT uq_resena_pedido_producto UNIQUE (pedido_id, producto_id)
) ENGINE=InnoDB;

CREATE INDEX idx_resena_producto ON resena (producto_id);

-- ============================================================
--  Comision de venta
-- ============================================================
-- Porcentaje que gana el vendedor por cada unidad de este producto que
-- venda (0 a 100). Lo define el Jefe al crear o editar el producto.
ALTER TABLE producto ADD COLUMN comision_pct DECIMAL(5,2) NOT NULL DEFAULT 0 AFTER stock_minimo;

-- Vendedor asociado al pedido (opcional). Se fija al crear el pedido, ya
-- sea porque el cliente lo eligio en el checkout o porque el vendedor hizo
-- una venta asistida por el. No se reasigna despues desde la interfaz.
ALTER TABLE pedido ADD COLUMN vendedor_id BIGINT NULL AFTER cliente_id;
ALTER TABLE pedido ADD CONSTRAINT fk_pedido_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuario(id);

-- Monto y estado de la comision. Se calcula cuando el jefe acepta el pedido
-- (PAGADO), se confirma cuando el cliente recibe (ENTREGADO), y se anula si
-- el pedido se cancela o se rechaza despues de haberse calculado.
ALTER TABLE pedido ADD COLUMN comision_monto DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER vendedor_id;
ALTER TABLE pedido ADD COLUMN comision_estado VARCHAR(20) NOT NULL DEFAULT 'NO_APLICA' AFTER comision_monto;

CREATE INDEX idx_pedido_vendedor ON pedido (vendedor_id, comision_estado);
