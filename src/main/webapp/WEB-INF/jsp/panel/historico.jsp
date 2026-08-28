<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Administracion</p>
    <h1 class="mb-3">Reportes guardados</h1>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th>Periodo</th><th>Personas</th><th>Atenciones</th><th>Promedio</th>
                <th>Satisfaccion</th><th>Escaladas</th><th>Generado</th></tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${reportes}">
            <tr>
                <td class="fw-bold text-capitalize">${r.periodoTexto}</td>
                <td class="dato">${r.personasAtendidas}</td>
                <td class="dato">${r.totalAtenciones}</td>
                <td class="dato">${r.promedioCalificacion} / 5</td>
                <td class="dato">${r.satisfaccionPct}%</td>
                <td class="dato">${r.escaladas}</td>
                <td class="small text-muted">${r.fechaGeneracionTexto} por ${r.generadoPor}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty reportes}">
        <div class="bloque-dato p-4">
            <p class="mb-1 fw-bold">Todavia no has guardado ningun reporte.</p>
            <p class="mb-0 small">Genera el del mes en curso y usa el boton "Guardar en el historico".</p>
        </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
