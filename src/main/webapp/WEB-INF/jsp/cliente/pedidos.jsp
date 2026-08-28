<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/mi-cuenta" class="enlace-volver">&larr; <spring:message code="comun.volverCuenta" /></a>
    <p class="rotulo mb-1"><spring:message code="pedidos.rotulo" /></p>
    <h1 class="mb-3"><spring:message code="pedidos.titulo" /></h1>

    <c:if test="${not empty cotizaciones}">
        <h2 class="fs-5 mt-4 mb-2"><spring:message code="pedidos.cotizaciones" /></h2>
        <p class="small text-muted">Precios congelados. Envialas a aprobacion cuando quieras.</p>
        <table class="table tabla-taller align-middle">
            <thead><tr><th><spring:message code="pedidos.numero" /></th><th><spring:message code="comun.fecha" /></th><th><spring:message code="comun.unidades" /></th><th class="text-end"><spring:message code="comun.total" /></th><th></th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${cotizaciones}">
                <tr>
                    <td class="fw-bold">${p.numero}</td>
                    <td class="dato">${p.fechaTexto}</td>
                    <td class="dato">${p.totalUnidades}</td>
                    <td class="dato text-end">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td>
                    <td class="text-end">
                        <a href="${ctx}/pedidos/${p.id}" class="btn btn-contorno btn-sm"><spring:message code="comun.ver" /></a>
                        <form method="post" action="${ctx}/pedidos/${p.id}/enviar" class="d-inline">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button class="btn btn-hilo btn-sm"><spring:message code="pedidos.enviarAprobacion" /></button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <div class="cinta cinta-fina my-4"></div>

    <h2 class="fs-5 mb-3"><spring:message code="pedidos.todos" /></h2>
    <table class="table tabla-taller align-middle">
        <thead><tr><th><spring:message code="pedidos.numero" /></th><th><spring:message code="comun.fecha" /></th><th><spring:message code="comun.estado" /></th><th><spring:message code="comun.unidades" /></th>
                   <th class="text-end"><spring:message code="comun.total" /></th><th></th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pedidos}">
            <tr>
                <td class="fw-bold">${p.numero}</td>
                <td class="dato">${p.fechaTexto}</td>
                <td><span class="estado estado-${fn:toLowerCase(p.estado)}">${p.estado.etiqueta}</span></td>
                <td class="dato">${p.totalUnidades}</td>
                <td class="dato text-end">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td>
                <td class="text-end"><a href="${ctx}/pedidos/${p.id}" class="btn btn-contorno btn-sm"><spring:message code="pedidos.seguir" /></a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty pedidos}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1"><spring:message code="pedidos.vacio" /></p>
            <a href="${ctx}/catalogo" class="btn btn-tinta btn-sm mt-2"><spring:message code="inicio.verCatalogo" /></a>
        </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
