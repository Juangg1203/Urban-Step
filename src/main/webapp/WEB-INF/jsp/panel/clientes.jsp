<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Panel</p>
    <h1 class="mb-3">Directorio de clientes</h1>

    <form class="row g-2 mb-4" method="get" action="${ctx}/panel/clientes">
        <div class="col-md-5">
            <label class="visually-hidden" for="q">Buscar cliente</label>
            <input id="q" name="q" value="${busqueda}" class="form-control"
                   placeholder="Buscar por nombre o ciudad">
        </div>
        <div class="col-md-2"><button class="btn btn-tinta w-100">Buscar</button></div>
    </form>

    <p class="small text-muted">Este listado muestra unicamente datos de nivel publico. Para ver la ficha
        completa entra al detalle: alli se aplica la politica de acceso de tu rol y queda registrada la consulta.</p>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th>Cliente</th><th>Ciudad</th><th>Ocupacion</th><th>Registro</th><th>Autorizaciones</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="cl" items="${clientes}">
            <tr>
                <td class="fw-bold">${cl.nombreCompleto}</td>
                <td>${cl.ciudad}</td>
                <td>${cl.ocupacion}</td>
                <td class="dato">${cl.fechaRegistroTexto}</td>
                <td>
                    <c:if test="${cl.autorizaSensibles}"><span class="nivel nivel-sensible">Sensibles</span></c:if>
                    <c:if test="${cl.autorizaMarketing}"><span class="nivel nivel-semiprivado">Marketing</span></c:if>
                </td>
                <td class="text-end">
                    <a href="${ctx}/panel/clientes/${cl.id}" class="btn btn-contorno btn-sm">Ver ficha</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty clientes}">
        <div class="bloque-dato p-4">
            <p class="mb-0">No hay clientes que coincidan con la busqueda.</p>
        </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
