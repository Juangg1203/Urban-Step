"""
Diagrama de arquitectura de Trazo y Suela.

Muestra el recorrido de una peticion: navegador -> seguridad -> controlador ->
servicio -> repositorio -> MySQL, y de vuelta. Se marcan tambien los dos
servicios externos opcionales (pasarela de pagos y modelo de lenguaje) porque
el sistema debe funcionar sin ellos.
"""
import graphviz

TINTA = "#16233A"
HILO = "#C9821F"
PAPEL = "#F3EFE7"
TIZA = "#FCFBF8"
LINEA = "#D9D3C7"
VERDE = "#3F6B4F"
INDIGO = "#2C4A7C"
CARMIN = "#A63A2E"
GRIS = "#6F6A61"


def caja(titulo, detalle, color):
    return (
        f'<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="7">'
        f'<TR><TD ALIGN="CENTER"><FONT POINT-SIZE="13" COLOR="{TIZA}"><B>{titulo}</B></FONT></TD></TR>'
        f'<TR><TD ALIGN="CENTER"><FONT POINT-SIZE="9" COLOR="{TIZA}">{detalle}</FONT></TD></TR>'
        f'</TABLE>>'
    )


def construir():
    g = graphviz.Digraph("Arquitectura", format="png")
    g.attr(rankdir="TB", bgcolor=PAPEL, fontname="Helvetica", pad="0.6",
           nodesep="0.45", ranksep="0.75", labelloc="t", labeljust="l",
           label=('<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="8">'
                  f'<TR><TD ALIGN="LEFT"><FONT POINT-SIZE="24" COLOR="{TINTA}"><B>'
                  'Trazo y Suela &#183; Arquitectura</B></FONT></TD></TR>'
                  '<TR><TD ALIGN="LEFT"><FONT POINT-SIZE="11" COLOR="#6F6A61">'
                  'Monolito en capas sobre Spring Boot &#183; MVC con vistas JSP renderizadas '
                  'en el servidor</FONT></TD></TR></TABLE>>'))
    g.attr("node", shape="box", style="filled,rounded", fontname="Helvetica",
           color=LINEA, penwidth="0")
    g.attr("edge", color="#5A6577", fontname="Helvetica", fontsize="9",
           arrowsize="0.7", penwidth="1.2")

    # ---------- capa 1: cliente ----------
    with g.subgraph(name="cluster_cliente") as c:
        c.attr(label="  1. Presentacion (navegador)  ", style="filled,rounded",
               fillcolor=TIZA, color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("navegador", caja("Navegador",
               "HTML + Bootstrap 5 + CSS propio<br/>chat.js &#183; seguridad-clave.js", TINTA),
               fillcolor=TINTA)

    # ---------- capa 2: web ----------
    with g.subgraph(name="cluster_web") as c:
        c.attr(label="  2. Capa web (Spring MVC)  ", style="filled,rounded",
               fillcolor=TIZA, color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("seguridad", caja("Spring Security",
               "autenticacion &#183; roles<br/>CSRF &#183; BCrypt", CARMIN), fillcolor=CARMIN)
        c.node("controlador", caja("Controladores",
               "Home &#183; Auth &#183; Carrito &#183; Pedido<br/>Panel &#183; Producto &#183; Pago &#183; ChatApi",
               INDIGO), fillcolor=INDIGO)
        c.node("vistas", caja("Vistas JSP + JSTL",
               "se renderizan en el servidor<br/>i18n con messages_es / messages_en",
               INDIGO), fillcolor=INDIGO)

    # ---------- capa 3: negocio ----------
    with g.subgraph(name="cluster_negocio") as c:
        c.attr(label="  3. Logica de negocio (servicios)  ", style="filled,rounded",
               fillcolor=TIZA, color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("servicios", caja("Servicios de dominio",
               "PedidoService &#183; InventarioService &#183; ProductoService<br/>"
               "CarritoService (sesion) &#183; ReporteService &#183; ChatbotService",
               HILO), fillcolor=HILO)
        c.node("transversal", caja("Servicios transversales",
               "PoliticaAccesoService (matriz de acceso)<br/>"
               "AuditoriaService &#183; SeguridadClaveService &#183; CifradoAes",
               VERDE), fillcolor=VERDE)

    # ---------- capa 4: datos ----------
    with g.subgraph(name="cluster_datos") as c:
        c.attr(label="  4. Acceso a datos  ", style="filled,rounded",
               fillcolor=TIZA, color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("repositorios", caja("Repositorios (Spring Data JPA)",
               "13 interfaces &#183; consultas derivadas y JPQL<br/>@EntityGraph para evitar N+1",
               "#7A6A9B"), fillcolor="#7A6A9B")
        c.node("hibernate", caja("Hibernate / JPA",
               "mapeo objeto-relacional<br/>@Convert cifra los campos sensibles",
               "#7A6A9B"), fillcolor="#7A6A9B")

    # ---------- capa 5: persistencia ----------
    with g.subgraph(name="cluster_bd") as c:
        c.attr(label="  5. Persistencia  ", style="filled,rounded",
               fillcolor=TIZA, color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("mysql", caja("MySQL / MariaDB",
               "15 tablas &#183; claves foraneas<br/>datos privados y sensibles cifrados",
               TINTA), fillcolor=TINTA)
        c.node("disco", caja("Carpeta de imagenes",
               "fuera del war<br/>en la BD solo el nombre del archivo",
               GRIS), fillcolor=GRIS)

    # ---------- externos ----------
    with g.subgraph(name="cluster_externos") as c:
        c.attr(label="  Servicios externos (opcionales)  ", style="filled,rounded",
               fillcolor="#EFEAE0", color=LINEA, penwidth="1",
               fontname="Helvetica-Bold", fontsize="12", fontcolor=GRIS, margin="14")
        c.node("wompi", caja("Pasarela de pagos",
               "Wompi &#183; modo real o simulado<br/>los datos de tarjeta no pasan por aqui",
               CARMIN), fillcolor=CARMIN)
        c.node("ia", caja("Modelo de lenguaje",
               "Gemini o Ollama local<br/>si falla, responden las reglas",
               CARMIN), fillcolor=CARMIN)

    # ---------- flujo principal ----------
    g.edge("navegador", "seguridad", label="  HTTP")
    g.edge("seguridad", "controlador", label="  peticion autorizada")
    g.edge("controlador", "servicios", label="  invoca")
    g.edge("servicios", "transversal", label="  valida y audita", style="dashed")
    g.edge("servicios", "repositorios", label="  consulta")
    g.edge("repositorios", "hibernate")
    g.edge("hibernate", "mysql", label="  JDBC / SQL")
    g.edge("controlador", "vistas", label="  modelo")
    g.edge("vistas", "navegador", label="  HTML", style="dashed", constraint="false")
    g.edge("servicios", "disco", label="  imagenes", style="dotted")
    g.edge("servicios", "wompi", label="  HTTPS", style="dotted", constraint="false")
    g.edge("servicios", "ia", label="  HTTPS", style="dotted", constraint="false")

    return g


if __name__ == "__main__":
    d = construir()
    d.render("/mnt/user-data/outputs/diagrama-arquitectura", format="png", cleanup=True)
    d.render("/mnt/user-data/outputs/diagrama-arquitectura", format="svg", cleanup=True)
    print("Diagrama de arquitectura generado")
