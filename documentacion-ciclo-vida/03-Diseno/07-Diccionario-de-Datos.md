# 7. Diccionario de datos

Generado a partir de `basedatos/01_esquema.sql` (esquema completo, tablas base + columnas
agregadas por ALTER TABLE en fases posteriores). 16 tablas.


## usuario

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| nombre_usuario | VARCHAR(60) | Si | UQ |  |
| correo | VARCHAR(120) | Si | UQ |  |
| clave | VARCHAR(120) | Si |  |  |
| rol | VARCHAR(20) | Si |  |  |
| activo | BOOLEAN | Si |  |  |
| fecha_creacion | DATETIME | Si |  |  |
| subtipo | VARCHAR(20) | No |  | Solo aplica al rol EMPLEADO: VENDEDOR o BODEGUERO |

## cliente

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| usuario_id | BIGINT | No | FK -> usuario / UQ |  |
| nombres | VARCHAR(80) | Si |  |  |
| apellidos | VARCHAR(80) | Si |  |  |
| ciudad | VARCHAR(60) | No |  |  |
| departamento | VARCHAR(60) | No |  |  |
| ocupacion | VARCHAR(80) | No |  |  |
| fecha_registro | DATETIME | Si |  |  |
| acepta_tratamiento | BOOLEAN | Si |  |  |
| autoriza_sensibles | BOOLEAN | Si |  |  |
| autoriza_marketing | BOOLEAN | Si |  |  |
| version_politica | VARCHAR(20) | No |  |  |
| fecha_autorizacion | DATETIME | No |  |  |

## dato_privado_cliente

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| cliente_id | BIGINT | Si | FK -> cliente / UQ |  |
| tipo_documento | VARCHAR(10) | No |  |  |
| numero_documento | VARCHAR(400) | No |  |  |
| direccion | VARCHAR(400) | No |  |  |
| telefono | VARCHAR(400) | No |  |  |
| correo_personal | VARCHAR(400) | No |  |  |
| fecha_nacimiento | VARCHAR(400) | No |  |  |
| actualizado | DATETIME | No |  |  |

## dato_sensible_cliente

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| cliente_id | BIGINT | Si | FK -> cliente / UQ |  |
| medidas_corporales | VARCHAR(600) | No |  |  |
| alergias_materiales | VARCHAR(600) | No |  |  |
| condicion_movilidad | VARCHAR(600) | No |  |  |
| restriccion_vestimenta | VARCHAR(600) | No |  |  |
| autorizado | BOOLEAN | Si |  |  |
| fecha_autorizacion | DATETIME | No |  |  |

## categoria

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| nombre | VARCHAR(60) | Si |  |  |
| linea | VARCHAR(20) | Si |  |  |

## producto

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| sku | VARCHAR(30) | Si | UQ | Referencia autogenerada por categoria (CAM-001, PANT-011...) |
| nombre | VARCHAR(120) | Si |  |  |
| descripcion | VARCHAR(500) | No |  |  |
| categoria_id | BIGINT | Si | FK -> categoria |  |
| precio | DECIMAL(12,2) | Si |  |  |
| tallas | VARCHAR(120) | No |  |  |
| color | VARCHAR(60) | No |  |  |
| material | VARCHAR(80) | No |  |  |
| stock | INT | Si |  |  |
| activo | BOOLEAN | Si |  |  |
| imagen | VARCHAR(300) | No |  | Nombre del archivo en disco, no la imagen en si |
| stock_minimo | INT | Si |  | Nivel de existencias que dispara la alerta |
| comision_pct | DECIMAL(5,2) | Si |  | Porcentaje que gana el vendedor por unidad de este producto |

## compra_cliente

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| cliente_id | BIGINT | Si | FK -> cliente |  |
| producto_id | BIGINT | No | FK -> producto |  |
| fecha | DATETIME | Si |  |  |
| cantidad | INT | Si |  |  |
| monto | DECIMAL(12,2) | Si |  |  |
| medio_pago | VARCHAR(30) | No |  |  |
| estado_pago | VARCHAR(20) | No |  |  |

## atencion

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| cliente_id | BIGINT | No | FK -> cliente |  |
| agente_id | BIGINT | No |  |  |
| canal | VARCHAR(20) | Si |  |  |
| tema | VARCHAR(30) | No |  |  |
| fecha_inicio | DATETIME | Si |  |  |
| fecha_cierre | DATETIME | No |  |  |
| estado | VARCHAR(20) | Si |  | Ver el enum correspondiente del modulo (EstadoPedido, EstadoAtencion...) |
| resuelta | BOOLEAN | Si |  |  |
| calificacion | INT | No |  | De 1 a 5 estrellas |
| recomendacion | VARCHAR(600) | No |  |  |

## conversacion

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| sesion | VARCHAR(60) | Si | UQ |  |
| cliente_id | BIGINT | No | FK -> cliente |  |
| atencion_id | BIGINT | No | FK -> atencion |  |
| fecha_inicio | DATETIME | Si |  |  |
| fecha_fin | DATETIME | No |  |  |
| escalada | BOOLEAN | Si |  |  |

## mensaje_chat

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| conversacion_id | BIGINT | Si | FK -> conversacion |  |
| emisor | VARCHAR(10) | Si |  |  |
| texto | VARCHAR(2000) | Si |  |  |
| intencion | VARCHAR(40) | No |  |  |
| respondido_ia | BOOLEAN | Si |  |  |
| fecha | DATETIME | Si |  |  |

## log_auditoria

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| fecha | DATETIME | Si |  |  |
| usuario | VARCHAR(60) | No |  |  |
| rol | VARCHAR(20) | No |  |  |
| accion | VARCHAR(40) | Si |  |  |
| nivel_dato | VARCHAR(15) | No |  |  |
| entidad | VARCHAR(40) | No |  |  |
| registro_id | BIGINT | No |  |  |
| detalle | VARCHAR(400) | No |  |  |
| ip | VARCHAR(45) | No |  |  |

## reporte_mensual

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| anio | INT | Si |  |  |
| mes | INT | Si |  |  |
| personas_atendidas | INT | Si |  |  |
| total_atenciones | INT | Si |  |  |
| promedio_calificacion | DOUBLE | Si |  |  |
| satisfaccion_pct | DOUBLE | Si |  |  |
| atenciones_chatbot | INT | Si |  |  |
| atenciones_agente | INT | Si |  |  |
| escaladas | INT | Si |  |  |
| sugerencias | TEXT | No |  |  |
| fecha_generacion | DATETIME | Si |  |  |
| generado_por | VARCHAR(60) | No |  |  |

## pedido

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| numero | VARCHAR(20) | Si | UQ |  |
| cliente_id | BIGINT | Si | FK -> cliente |  |
| estado | VARCHAR(30) | Si |  | Ver el enum correspondiente del modulo (EstadoPedido, EstadoAtencion...) |
| fecha | DATETIME | Si |  |  |
| fecha_actualizacion | DATETIME | No |  |  |
| subtotal | DECIMAL(12,2) | Si |  |  |
| costo_envio | DECIMAL(12,2) | Si |  |  |
| total | DECIMAL(12,2) | Si |  |  |
| medio_pago | VARCHAR(30) | No |  |  |
| direccion_entrega | VARCHAR(400) | No |  |  |
| observaciones | VARCHAR(400) | No |  |  |
| aprobado_por_id | BIGINT | No |  |  |
| fecha_aprobacion | DATETIME | No |  |  |
| motivo_decision | VARCHAR(400) | No |  |  |
| pago_verificado_por_id | BIGINT | No |  |  |
| referencia_pago | VARCHAR(60) | No |  |  |
| despachado_por_id | BIGINT | No |  |  |
| numero_guia | VARCHAR(60) | No |  |  |
| fecha_despacho | DATETIME | No |  |  |
| fecha_entrega | DATETIME | No |  |  |
| referencia_pasarela | VARCHAR(60) | No |  | Referencia unica enviada a la pasarela de pagos |
| transaccion_pasarela | VARCHAR(60) | No |  | Id de la transaccion que devuelve la pasarela |
| estado_pasarela | VARCHAR(20) | No |  | APPROVED / DECLINED / PENDING / VOIDED, tal como lo reporta la pasarela |
| metodo_pasarela | VARCHAR(30) | No |  | CARD, NEQUI, PSE... segun como pago el cliente |
| comprobante_pago | VARCHAR(300) | No |  | Archivo que sube el cliente para agilizar la verificacion |
| foto_entrega | VARCHAR(300) | No |  | Foto que sube el cliente al confirmar que recibio el pedido |
| vendedor_id | BIGINT | No | FK -> usuario | Vendedor asociado al pedido (comision de venta), opcional |
| comision_monto | DECIMAL(12,2) | Si |  | Monto de la comision calculada para el pedido |
| comision_estado | VARCHAR(20) | Si |  | NO_APLICA / PENDIENTE / CONFIRMADA / ANULADA |

## item_pedido

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| pedido_id | BIGINT | Si | FK -> pedido |  |
| producto_id | BIGINT | No | FK -> producto |  |
| nombre_producto | VARCHAR(120) | Si |  |  |
| talla | VARCHAR(10) | No |  |  |
| cantidad | INT | Si |  |  |
| precio_unitario | DECIMAL(12,2) | Si |  |  |

## notificacion

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| rol_destino | VARCHAR(20) | Si |  |  |
| subtipo_destino | VARCHAR(20) | No |  | Filtra el aviso a vendedores o a bodegueros |
| titulo | VARCHAR(120) | Si |  |  |
| mensaje | VARCHAR(400) | Si |  |  |
| enlace | VARCHAR(200) | No |  |  |
| pedido_id | BIGINT | No | FK -> pedido |  |
| leida | BOOLEAN | Si |  |  |
| fecha | DATETIME | Si |  |  |

## resena

| Columna | Tipo | Obligatoria | Clave | Nota |
|---|---|---|---|---|
| id | BIGINT | Si | PK | Autoincremental |
| producto_id | BIGINT | Si | FK -> producto |  |
| cliente_id | BIGINT | Si | FK -> cliente |  |
| pedido_id | BIGINT | Si | FK -> pedido |  |
| calificacion | INT | Si |  | De 1 a 5 estrellas |
| comentario | VARCHAR(600) | No |  |  |
| fecha | DATETIME | Si |  |  |