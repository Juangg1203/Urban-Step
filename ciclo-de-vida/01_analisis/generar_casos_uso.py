"""Diagrama de casos de uso de UrbanStep, generado con Graphviz."""
import graphviz

TINTA = "#0B0E14"; LIMA = "#6E8B1E"; VIOLETA = "#5C3FA6"
CIAN = "#1D7A70"; MAGENTA = "#A31A55"; PAPEL = "#F3EFE7"; LINEA = "#D9DEE5"

ACTORES = [
    ("Cliente", TINTA, 0.15),
    ("Empleado\n(Vendedor)", VIOLETA, 0.42),
    ("Empleado\n(Bodeguero)", VIOLETA, 0.68),
    ("Jefe", CIAN, 0.55),
    ("Administrador", MAGENTA, 0.85),
]

CASOS = {
    "Registrarse / iniciar sesion": [0],
    "Explorar catalogo": [0],
    "Agregar al carrito": [0],
    "Pagar el pedido": [0],
    "Reportar pago manual": [0],
    "Confirmar recepcion": [0],
    "Dejar resena": [0],
    "Usar el chatbot": [0],
    "Confirmar pago recibido": [1],
    "Alistar pedido": [2],
    "Despachar pedido": [2],
    "Gestionar catalogo (CRUD)": [3],
    "Aceptar / rechazar compra": [3],
    "Ver reporte mensual": [3, 4],
    "Auditar accesos a datos": [3, 4],
    "Gestionar cuentas de personal": [4],
    "Forzar estado de un pedido": [4],
}

def construir():
    g = graphviz.Digraph("CasosDeUso", format="png")
    g.attr(rankdir="LR", bgcolor=PAPEL, fontname="Helvetica-Bold", pad="0.5",
           nodesep="0.35", ranksep="1.4", labelloc="t", labeljust="l",
           fontsize="22", fontcolor=TINTA,
           label="UrbanStep - Casos de uso principales\n ")

    with g.subgraph(name="cluster_actores") as c:
        c.attr(style="invis")
        for nombre, color, _ in ACTORES:
            c.node(nombre, shape="plaintext", fontname="Helvetica-Bold",
                   fontsize="12", fontcolor=color,
                   label=(f'<<TABLE BORDER="1" CELLBORDER="0" CELLPADDING="8" '
                          f'COLOR="{color}" BGCOLOR="white" STYLE="ROUNDED">'
                          f'<TR><TD><FONT COLOR="{color}">{nombre}</FONT></TD></TR>'
                          f'</TABLE>>'))

    with g.subgraph(name="cluster_sistema") as c:
        c.attr(label="Sistema UrbanStep", style="filled,rounded", fillcolor="white",
               color=LINEA, fontname="Helvetica-Bold", fontsize="13", fontcolor="#5A6472")
        c.attr("node", shape="ellipse", style="filled", fillcolor="#F4F6F9",
               color=LINEA, fontname="Helvetica", fontsize="10")
        for caso in CASOS:
            c.node(caso)

    g.attr("edge", color="#8C99AE", arrowsize="0.6")
    for caso, actores in CASOS.items():
        for i in actores:
            g.edge(ACTORES[i][0], caso)

    return g

if __name__ == "__main__":
    d = construir()
    d.render("/mnt/user-data/outputs/casos-de-uso", format="png", cleanup=True)
    d.render("/mnt/user-data/outputs/casos-de-uso", format="svg", cleanup=True)
    print("Diagrama de casos de uso generado")
