<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Panel</p>
    <h1 class="mb-3">Atenciones</h1>

    <div class="mb-4">
        <a href="${ctx}/panel/atenciones" class="btn btn-sm ${empty filtro ? 'btn-tinta' : 'btn-contorno'}">Recientes</a>
        <a href="${ctx}/panel/atenciones?filtro=pendientes"
           class="btn btn-sm ${filtro eq 'pendientes' ? 'btn-tinta' : 'btn-contorno'}">Pendientes y escaladas</a>
    </div>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th>Fecha</th><th>Cliente</th><th>Canal</th><th>Tema</th><th>Estado</th>
                <th>Calificacion</th><th>Recomendacion</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="a" items="${atenciones}">
            <tr>
                <td class="dato">${a.fechaInicioTexto}</td>
                <td>${a.nombreCliente}</td>
                <td class="small">${a.canal}</td>
                <td class="small">${a.tema.etiqueta}</td>
                <td>
                    <span class="nivel ${a.estado eq 'ESCALADA' ? 'nivel-sensible' :
                        (a.estado eq 'ABIERTA' ? 'nivel-privado' : 'nivel-publico')}">${a.estado}</span>
                </td>
                <td class="estrellas">
                    <c:choose>
                        <c:when test="${empty a.calificacion}"><span class="dato text-muted">sin calificar</span></c:when>
                        <c:otherwise><c:forEach begin="1" end="${a.calificacion}">&#9733;</c:forEach></c:otherwise>
                    </c:choose>
                </td>
                <td class="small" style="max-width:260px;">${a.recomendacion}</td>
                <td class="text-end">
                    <c:if test="${a.estado ne 'CERRADA'}">
                        <form method="post" action="${ctx}/panel/atenciones/${a.id}/tomar" class="d-inline">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button class="btn btn-contorno btn-sm">Tomar</button>
                        </form>
                        <form method="post" action="${ctx}/panel/atenciones/${a.id}/cerrar" class="d-inline">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <input type="hidden" name="resuelta" value="true">
                            <button class="btn btn-tinta btn-sm">Cerrar</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty atenciones}">
        <div class="bloque-dato p-4"><p class="mb-0">No hay atenciones en este filtro.</p></div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
