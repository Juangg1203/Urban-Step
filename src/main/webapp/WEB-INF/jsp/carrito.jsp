<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/catalogo" class="enlace-volver">&larr; <spring:message code="carrito.seguir" /></a>
    <p class="rotulo mb-1"><spring:message code="carrito.rotulo" /></p>
    <h1 class="mb-1"><spring:message code="carrito.titulo" /></h1>
    <p class="text-muted"><spring:message code="carrito.nota" /></p>

    <div class="cinta cinta-fina my-4"></div>

    <c:if test="${not empty faltantes}">
        <div class="aviso-inventario p-3 mb-4">
            <p class="rotulo mb-2">Revisa las cantidades</p>
            <ul class="mb-0 small">
                <c:forEach var="f" items="${faltantes}">
                    <li>${f.mensaje}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>


    <c:if test="${carrito.vacio}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1"><spring:message code="carrito.vacio" /></p>
            <p class="mb-3 small"><spring:message code="carrito.vacioNota" /></p>
            <a href="${ctx}/catalogo" class="btn btn-tinta"><spring:message code="inicio.verCatalogo" /></a>
        </div>
    </c:if>

    <c:if test="${not carrito.vacio}">
    <div class="row g-4">
        <div class="col-lg-8">
            <table class="table tabla-taller align-middle">
                <thead>
                    <tr><th><spring:message code="comun.producto" /></th><th><spring:message code="comun.talla" /></th><th style="width:150px;"><spring:message code="comun.cantidad" /></th>
                        <th class="text-end"><spring:message code="comun.precio" /></th><th class="text-end"><spring:message code="comun.subtotal" /></th><th></th></tr>
                </thead>
                <tbody>
                <c:forEach var="it" items="${carrito.items}">
                    <tr>
                        <td>
                            <div class="fw-bold">${it.nombre}</div>
                            <div class="dato text-muted">${it.sku}<c:if test="${not empty it.color}"> &middot; ${it.color}</c:if></div>
                        </td>
                        <td class="dato">${empty it.talla ? '—' : it.talla}</td>
                        <td>
                            <form method="post" action="${ctx}/carrito/cantidad" class="d-flex gap-1">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="productoId" value="${it.productoId}">
                                <input type="hidden" name="talla" value="${it.talla}">
                                <input type="number" name="cantidad" value="${it.cantidad}" min="1" max="20"
                                       class="form-control form-control-sm" style="width:70px;">
                                <button class="btn btn-contorno btn-sm">Ok</button>
                            </form>
                        </td>
                        <td class="dato text-end">$<fmt:formatNumber value="${it.precioUnitario}" maxFractionDigits="0" /></td>
                        <td class="dato text-end fw-bold">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                        <td class="text-end">
                            <form method="post" action="${ctx}/carrito/quitar">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="productoId" value="${it.productoId}">
                                <input type="hidden" name="talla" value="${it.talla}">
                                <button class="btn btn-link btn-sm text-danger p-0"><spring:message code="comun.quitar" /></button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="d-flex gap-2">
                <a href="${ctx}/catalogo" class="btn btn-contorno btn-sm"><spring:message code="carrito.seguir" /></a>
                <form method="post" action="${ctx}/carrito/vaciar">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-link btn-sm text-muted"><spring:message code="carrito.vaciar" /></button>
                </form>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="ficha p-4">
                <h2 class="fs-5 mb-3"><spring:message code="comun.resumen" /></h2>
                <div class="d-flex justify-content-between small mb-2">
                    <span>${carrito.totalUnidades} unidades</span>
                    <span class="dato">$<fmt:formatNumber value="${carrito.subtotal}" maxFractionDigits="0" /></span>
                </div>
                <div class="d-flex justify-content-between small mb-2">
                    <span><spring:message code="comun.envio" /></span>
                    <span class="dato">
                        <c:choose>
                            <c:when test="${carrito.envioGratis}"><span style="color:var(--verde)"><spring:message code="comun.gratis" /></span></c:when>
                            <c:otherwise>$<fmt:formatNumber value="${carrito.costoEnvio}" maxFractionDigits="0" /></c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <c:if test="${not carrito.envioGratis}">
                    <p class="dato text-muted mb-2">
                        Te faltan $<fmt:formatNumber value="${carrito.faltaParaEnvioGratis}" maxFractionDigits="0" />
                        <spring:message code="carrito.faltaEnvio" />.
                    </p>
                </c:if>

                <div class="cinta cinta-fina my-3"></div>

                <div class="d-flex justify-content-between align-items-baseline mb-3">
                    <span class="rotulo"><spring:message code="comun.total" /></span>
                    <span class="cifra acento">$<fmt:formatNumber value="${carrito.total}" maxFractionDigits="0" /></span>
                </div>

                <a href="${ctx}/checkout" class="btn btn-hilo w-100 mb-2"><spring:message code="carrito.continuar" /></a>
                <p class="small text-muted mb-0">Continuar no cobra nada: tu compra pasa primero a
                    revision del jefe y solo se paga cuando la aprueba.</p>
            </div>
        </div>
    </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
