"""
Genera el diagrama entidad-relacion del proyecto Trazo y Suela.

Se dibuja con Graphviz a partir del esquema real de la base de datos, no a
mano: si manana cambia una tabla, se corrige aqui y el diagrama se regenera
igual en vez de quedar desactualizado, que es lo que le pasa a los diagramas
dibujados en una herramienta grafica.

Convenciones del dibujo:
  PK  clave primaria
  FK  clave foranea
  UQ  unica
  *   obligatoria (NOT NULL)
"""
import graphviz

# Paleta del proyecto, para que el diagrama no parezca de otro trabajo
TINTA = "#16233A"
HILO = "#C9821F"
PAPEL = "#F3EFE7"
TIZA = "#FCFBF8"
LINEA = "#D9D3C7"
VERDE = "#3F6B4F"
INDIGO = "#2C4A7C"

# (tabla, color del encabezado, [(campo, tipo, marca)])
TABLAS = [
    ("USUARIO", TINTA, [
        ("id", "BIGINT", "PK"),
        ("nombre_usuario", "VARCHAR(60)", "UQ *"),
        ("correo", "VARCHAR(120)", "UQ *"),
        ("clave", "VARCHAR(120)", "* BCrypt"),
        ("rol", "VARCHAR(20)", "*"),
        ("subtipo", "VARCHAR(20)", ""),
        ("activo", "BOOLEAN", "*"),
        ("fecha_creacion", "DATETIME", "*"),
    ]),
    ("CLIENTE", TINTA, [
        ("id", "BIGINT", "PK"),
        ("usuario_id", "BIGINT", "FK UQ"),
        ("nombres", "VARCHAR(80)", "*"),
        ("apellidos", "VARCHAR(80)", "*"),
        ("ciudad", "VARCHAR(60)", ""),
        ("departamento", "VARCHAR(60)", ""),
        ("ocupacion", "VARCHAR(80)", ""),
        ("acepta_tratamiento", "BOOLEAN", "*"),
        ("autoriza_sensibles", "BOOLEAN", "*"),
        ("autoriza_marketing", "BOOLEAN", "*"),
        ("fecha_registro", "DATETIME", "*"),
    ]),
    ("CATEGORIA", VERDE, [
        ("id", "BIGINT", "PK"),
        ("nombre", "VARCHAR(60)", "*"),
        ("linea", "VARCHAR(20)", "* ROPA/CALZADO"),
    ]),
    ("PRODUCTO", VERDE, [
        ("id", "BIGINT", "PK"),
        ("sku", "VARCHAR(30)", "UQ *"),
        ("nombre", "VARCHAR(120)", "*"),
        ("descripcion", "VARCHAR(500)", ""),
        ("categoria_id", "BIGINT", "FK *"),
        ("precio", "DECIMAL(12,2)", "*"),
        ("imagen", "VARCHAR(300)", ""),
        ("tallas", "VARCHAR(120)", ""),
        ("color", "VARCHAR(60)", ""),
        ("material", "VARCHAR(80)", ""),
        ("stock", "INT", "*"),
        ("stock_minimo", "INT", "*"),
        ("activo", "BOOLEAN", "*"),
    ]),
    ("PEDIDO", HILO, [
        ("id", "BIGINT", "PK"),
        ("numero", "VARCHAR(20)", "UQ *"),
        ("cliente_id", "BIGINT", "FK *"),
        ("estado", "VARCHAR(30)", "*"),
        ("fecha", "DATETIME", "*"),
        ("subtotal", "DECIMAL(12,2)", "*"),
        ("costo_envio", "DECIMAL(12,2)", "*"),
        ("total", "DECIMAL(12,2)", "*"),
        ("medio_pago", "VARCHAR(30)", ""),
        ("direccion_entrega", "VARCHAR(400)", "cifrado"),
        ("aprobado_por_id", "BIGINT", "FK"),
        ("pago_verificado_por_id", "BIGINT", "FK"),
        ("despachado_por_id", "BIGINT", "FK"),
        ("referencia_pasarela", "VARCHAR(60)", ""),
        ("transaccion_pasarela", "VARCHAR(60)", ""),
        ("estado_pasarela", "VARCHAR(20)", ""),
        ("numero_guia", "VARCHAR(60)", ""),
    ]),
    ("ITEM_PEDIDO", HILO, [
        ("id", "BIGINT", "PK"),
        ("pedido_id", "BIGINT", "FK *"),
        ("producto_id", "BIGINT", "FK"),
        ("nombre_producto", "VARCHAR(120)", "*"),
        ("talla", "VARCHAR(10)", ""),
        ("cantidad", "INT", "*"),
        ("precio_unitario", "DECIMAL(12,2)", "* congelado"),
    ]),
    ("DATO_PRIVADO_CLIENTE", INDIGO, [
        ("id", "BIGINT", "PK"),
        ("cliente_id", "BIGINT", "FK UQ *"),
        ("tipo_documento", "VARCHAR(10)", ""),
        ("numero_documento", "VARCHAR(400)", "cifrado"),
        ("direccion", "VARCHAR(400)", "cifrado"),
        ("telefono", "VARCHAR(400)", "cifrado"),
        ("correo_personal", "VARCHAR(400)", "cifrado"),
        ("fecha_nacimiento", "VARCHAR(400)", "cifrado"),
    ]),
    ("DATO_SENSIBLE_CLIENTE", INDIGO, [
        ("id", "BIGINT", "PK"),
        ("cliente_id", "BIGINT", "FK UQ *"),
        ("medidas_corporales", "VARCHAR(600)", "cifrado"),
        ("alergias_materiales", "VARCHAR(600)", "cifrado"),
        ("condicion_movilidad", "VARCHAR(600)", "cifrado"),
        ("autorizado", "BOOLEAN", "*"),
    ]),
    ("ATENCION", "#7A6A9B", [
        ("id", "BIGINT", "PK"),
        ("cliente_id", "BIGINT", "FK"),
        ("agente_id", "BIGINT", "FK"),
        ("canal", "VARCHAR(20)", "*"),
        ("tema", "VARCHAR(30)", ""),
        ("estado", "VARCHAR(20)", "*"),
        ("calificacion", "INT", ""),
        ("recomendacion", "VARCHAR(600)", ""),
        ("fecha_inicio", "DATETIME", "*"),
    ]),
    ("CONVERSACION", "#7A6A9B", [
        ("id", "BIGINT", "PK"),
        ("sesion", "VARCHAR(60)", "UQ *"),
        ("cliente_id", "BIGINT", "FK"),
        ("atencion_id", "BIGINT", "FK"),
        ("escalada", "BOOLEAN", "*"),
    ]),
    ("MENSAJE_CHAT", "#7A6A9B", [
        ("id", "BIGINT", "PK"),
        ("conversacion_id", "BIGINT", "FK *"),
        ("emisor", "VARCHAR(10)", "*"),
        ("texto", "VARCHAR(2000)", "*"),
        ("intencion", "VARCHAR(40)", ""),
        ("fecha", "DATETIME", "*"),
    ]),
    ("NOTIFICACION", "#8A8378", [
        ("id", "BIGINT", "PK"),
        ("rol_destino", "VARCHAR(20)", "*"),
        ("subtipo_destino", "VARCHAR(20)", ""),
        ("titulo", "VARCHAR(120)", "*"),
        ("pedido_id", "BIGINT", "FK"),
        ("leida", "BOOLEAN", "*"),
    ]),
    ("COMPRA_CLIENTE", "#8A8378", [
        ("id", "BIGINT", "PK"),
        ("cliente_id", "BIGINT", "FK *"),
        ("producto_id", "BIGINT", "FK"),
        ("fecha", "DATETIME", "*"),
        ("monto", "DECIMAL(12,2)", "*"),
    ]),
    ("LOG_AUDITORIA", "#A63A2E", [
        ("id", "BIGINT", "PK"),
        ("fecha", "DATETIME", "*"),
        ("usuario", "VARCHAR(60)", ""),
        ("rol", "VARCHAR(20)", ""),
        ("accion", "VARCHAR(40)", "*"),
        ("nivel_dato", "VARCHAR(15)", ""),
        ("entidad", "VARCHAR(40)", ""),
        ("registro_id", "BIGINT", ""),
        ("ip", "VARCHAR(45)", ""),
    ]),
    ("REPORTE_MENSUAL", "#A63A2E", [
        ("id", "BIGINT", "PK"),
        ("anio", "INT", "*"),
        ("mes", "INT", "*"),
        ("personas_atendidas", "INT", "*"),
        ("promedio_calificacion", "DOUBLE", "*"),
        ("satisfaccion_pct", "DOUBLE", "*"),
        ("sugerencias", "TEXT", ""),
    ]),
]

# (origen, destino, etiqueta, campo origen)
RELACIONES = [
    ("CLIENTE", "USUARIO", "1:1", "usuario_id"),
    ("DATO_PRIVADO_CLIENTE", "CLIENTE", "1:1", "cliente_id"),
    ("DATO_SENSIBLE_CLIENTE", "CLIENTE", "1:1", "cliente_id"),
    ("PRODUCTO", "CATEGORIA", "N:1", "categoria_id"),
    ("PEDIDO", "CLIENTE", "N:1", "cliente_id"),
    ("PEDIDO", "USUARIO", "N:1", "aprobado_por_id"),
    ("ITEM_PEDIDO", "PEDIDO", "N:1", "pedido_id"),
    ("ITEM_PEDIDO", "PRODUCTO", "N:1", "producto_id"),
    ("COMPRA_CLIENTE", "CLIENTE", "N:1", "cliente_id"),
    ("COMPRA_CLIENTE", "PRODUCTO", "N:1", "producto_id"),
    ("ATENCION", "CLIENTE", "N:1", "cliente_id"),
    ("ATENCION", "USUARIO", "N:1", "agente_id"),
    ("CONVERSACION", "CLIENTE", "N:1", "cliente_id"),
    ("CONVERSACION", "ATENCION", "1:1", "atencion_id"),
    ("MENSAJE_CHAT", "CONVERSACION", "N:1", "conversacion_id"),
    ("NOTIFICACION", "PEDIDO", "N:1", "pedido_id"),
]


def escapar(texto):
    return (texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))


def tabla_html(nombre, color, campos):
    filas = [
        f'<TR><TD BGCOLOR="{color}" COLSPAN="3" ALIGN="CENTER">'
        f'<FONT COLOR="{TIZA}" POINT-SIZE="12"><B>{nombre}</B></FONT></TD></TR>'
    ]
    for campo, tipo, marca in campos:
        negrita = "PK" in marca
        nombre_campo = f"<B>{escapar(campo)}</B>" if negrita else escapar(campo)
        color_marca = "#A63A2E" if "cifrado" in marca else "#6F6A61"
        # Graphviz no acepta una etiqueta HTML vacia: se pone un espacio duro.
        texto_marca = escapar(marca) if marca else "&#160;"
        filas.append(
            f'<TR>'
            f'<TD ALIGN="LEFT" PORT="{campo}"><FONT POINT-SIZE="10">{nombre_campo}</FONT></TD>'
            f'<TD ALIGN="LEFT"><FONT POINT-SIZE="9" COLOR="#6F6A61">{escapar(tipo)}</FONT></TD>'
            f'<TD ALIGN="LEFT"><FONT POINT-SIZE="9" COLOR="{color_marca}">'
            f'{texto_marca}</FONT></TD>'
            f'</TR>'
        )
    cuerpo = "".join(filas)
    return (f'<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4" '
            f'BGCOLOR="{TIZA}" COLOR="{LINEA}">{cuerpo}</TABLE>>')


def construir():
    """
    Las tablas se agrupan en bloques para que el diagrama se lea. Un ER de 15
    tablas sin agrupar es una maraña de lineas donde no se distingue el nucleo
    del negocio de lo accesorio.
    """
    BLOQUES = [
        ("Nucleo del negocio", ["CATEGORIA", "PRODUCTO", "PEDIDO", "ITEM_PEDIDO"], "#FFFFFF"),
        ("Usuarios y datos personales",
         ["USUARIO", "CLIENTE", "DATO_PRIVADO_CLIENTE", "DATO_SENSIBLE_CLIENTE"], "#F7F4EE"),
        ("Atencion al cliente", ["ATENCION", "CONVERSACION", "MENSAJE_CHAT"], "#FFFFFF"),
        ("Apoyo y control",
         ["COMPRA_CLIENTE", "NOTIFICACION", "LOG_AUDITORIA", "REPORTE_MENSUAL"], "#F7F4EE"),
    ]
    por_nombre = {n: (n, c, campos) for n, c, campos in TABLAS}

    g = graphviz.Digraph("ER", format="png")
    g.attr(rankdir="LR", splines="spline", nodesep="0.5", ranksep="1.9",
           bgcolor=PAPEL, fontname="Helvetica", pad="0.6", compound="true",
           labelloc="t", labeljust="l",
           label=("<<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"8\">"
                  f"<TR><TD ALIGN=\"LEFT\"><FONT POINT-SIZE=\"26\" COLOR=\"{TINTA}\"><B>"
                  "Trazo y Suela &#183; Modelo entidad-relacion</B></FONT></TD></TR>"
                  "<TR><TD ALIGN=\"LEFT\"><FONT POINT-SIZE=\"12\" COLOR=\"#6F6A61\">"
                  "PK clave primaria &#183; FK clave foranea &#183; UQ unica &#183; "
                  "* obligatoria &#183; cifrado = AES-256-GCM"
                  "</FONT></TD></TR></TABLE>>"))
    g.attr("node", shape="plaintext", fontname="Helvetica")
    g.attr("edge", color="#5A6577", fontname="Helvetica", fontsize="10",
           arrowhead="normal", arrowsize="0.7", penwidth="1.1")

    for indice, (titulo_bloque, tablas, fondo) in enumerate(BLOQUES):
        with g.subgraph(name=f"cluster_{indice}") as bloque:
            bloque.attr(label=titulo_bloque, style="filled,rounded", color=LINEA,
                        fillcolor=fondo, fontname="Helvetica-Bold", fontsize="13",
                        fontcolor="#6F6A61", margin="16")
            for nombre in tablas:
                n, color, campos = por_nombre[nombre]
                bloque.node(n, label=tabla_html(n, color, campos))

    for origen, destino, cardinalidad, campo in RELACIONES:
        # Las relaciones 1:1 se dibujan punteadas: se distinguen de un vistazo.
        estilo = "dashed" if cardinalidad == "1:1" else "solid"
        g.edge(f"{origen}:{campo}", f"{destino}:id",
               label=cardinalidad, style=estilo)

    return g


if __name__ == "__main__":
    diagrama = construir()
    diagrama.render("/mnt/user-data/outputs/diagrama-entidad-relacion",
                    format="png", cleanup=True)
    diagrama.render("/mnt/user-data/outputs/diagrama-entidad-relacion",
                    format="svg", cleanup=True)
    print("Diagrama ER generado")
