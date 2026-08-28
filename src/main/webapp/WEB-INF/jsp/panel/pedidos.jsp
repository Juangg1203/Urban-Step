<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Operacion</p>
    <h1 class="mb-2">Pedidos</h1>
    <p class="text-muted">Cada rol ve solo su bandeja. Cada accion queda con tu nombre.</p>

    <div class="cinta cinta-fina my-4"></div>

    <!-- ============================================================
         VENDEDOR (y administrador, en modo consulta): pagos por verificar
         ============================================================ -->
    <c:if test="${not empty porVerificar or esVendedor}">
        <h2 class="fs-5 mb-2">Pagos por verificar
            <c:if test="${not empty porVerificar}"><span class="globo">${fn:length(porVerificar)}</span></c:if>
        </h2>
        <p class="small text-muted">Confirma solo cuando el dinero este realmente en la cuenta.
            De ahi pasa al jefe para el visto bueno final; tu no decides eso.</p>

        <c:if test="${empty porVerificar}">
            <div class="bloque-dato p-3 mb-4"><p class="mb-0 small">Nada pendiente por verificar.</p></div>
        </c:if>
        <c:if test="${not empty porVerificar}">
            <table class="table tabla-taller align-middle mb-4">
                <thead><tr><th>Pedido</th><th>Cliente</th><th>Medio</th><th>Referencia</th>
                           <th>Comprobante</th><th class="text-end">Total</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${porVerificar}">
                    <tr>
                        <td class="fw-bold"><a href="${ctx}/panel/pedidos/${p.id}">${p.numero}</a></td>
                        <td>${p.cliente.nombreCompleto}</td>
                        <td class="small">${p.medioPago}</td>
                        <td class="dato">${p.referenciaPago}</td>
                        <td class="small">
                            <c:choose>
                                <c:when test="${p.tieneComprobante}">
                                    <a href="${ctx}/imagenes/${p.comprobantePago}" target="_blank">Ver adjunto</a>
                                </c:when>
                                <c:otherwise><span class="text-muted">Sin adjunto</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="dato text-end">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td>
                        <td class="text-end">
                            <c:if test="${esVendedor}">
                                <form method="post" action="${ctx}/panel/pedidos/${p.id}/confirmar-pago">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button class="btn btn-hilo btn-sm">Confirmar pago</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </c:if>

    <!-- ============================================================
         BODEGUERO (y administrador, en modo consulta): despacho
         ============================================================ -->
    <c:if test="${not empty porDespachar or esBodeguero}">
        <h2 class="fs-5 mb-2">Por alistar y despachar
            <c:if test="${not empty porDespachar}"><span class="globo">${fn:length(porDespachar)}</span></c:if>
        </h2>

        <c:if test="${empty porDespachar}">
            <div class="bloque-dato p-3 mb-4"><p class="mb-0 small">Bodega al dia.</p></div>
        </c:if>
        <c:if test="${not empty porDespachar}">
            <table class="table tabla-taller align-middle mb-4">
                <thead><tr><th>Pedido</th><th>Cliente</th><th>Estado</th><th>Unidades</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${porDespachar}">
                    <tr>
                        <td class="fw-bold"><a href="${ctx}/panel/pedidos/${p.id}">${p.numero}</a></td>
                        <td>${p.cliente.nombreCompleto}</td>
                        <td><span class="estado estado-${fn:toLowerCase(p.estado)}">${p.estado.etiqueta}</span></td>
                        <td class="dato">${p.totalUnidades}</td>
                        <td class="text-end">
                            <c:if test="${esBodeguero}">
                                <c:if test="${p.estado eq 'PAGADO'}">
                                    <form method="post" action="${ctx}/panel/pedidos/${p.id}/alistar" class="d-inline">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="btn btn-contorno btn-sm">Empezar alistamiento</button>
                                    </form>
                                </c:if>
                                <form method="post" action="${ctx}/panel/pedidos/${p.id}/despachar"
                                      class="d-inline-flex gap-1 align-items-center">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <!-- Numero de rastreo de la transportadora (Servientrega, Coordinadora...).
                                         Se deja un valor de ejemplo puesto: en un proyecto academico no hay
                                         transportadora real, asi que inventar el codigo es lo esperado. -->
                                    <input name="guia" class="form-control form-control-sm" style="width:130px;"
                                           value="GU${p.id}${fn:substring(p.numero, fn:length(p.numero)-3, fn:length(p.numero))}"
                                           title="Numero de rastreo de la transportadora. Para el proyecto, cualquier codigo sirve."
                                           required>
                                    <button class="btn btn-tinta btn-sm">Despachar</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>

        <c:if test="${esBodeguero and not empty porConfirmarEntrega}">
            <h2 class="fs-6 mb-2">Despachados, esperando que el cliente confirme</h2>
            <p class="small text-muted">El cliente confirma solo desde su cuenta y eso habilita su
                resena. Usa esto unicamente si pasa mucho tiempo y no confirma.</p>
            <table class="table tabla-taller align-middle mb-4">
                <thead><tr><th>Pedido</th><th>Cliente</th><th>Guia</th><th>Despachado</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${porConfirmarEntrega}">
                    <tr>
                        <td class="fw-bold"><a href="${ctx}/panel/pedidos/${p.id}">${p.numero}</a></td>
                        <td>${p.cliente.nombreCompleto}</td>
                        <td class="dato">${p.numeroGuia}</td>
                        <td class="dato">${p.fechaDespachoTexto}</td>
                        <td class="text-end">
                            <form method="post" action="${ctx}/panel/pedidos/${p.id}/entregado" class="d-inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-contorno btn-sm">Cerrar como entregado</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </c:if>

    <!-- ============================================================
         ADMINISTRADOR: vision completa de supervision
         ============================================================ -->
    <c:if test="${esAdmin}">
        <div class="cinta cinta-fina my-4"></div>
        <h2 class="fs-5 mb-3">Ultimos pedidos (todos los estados)</h2>
        <p class="small text-muted">Como administrador puedes consultar y forzar el estado desde el
            detalle de cada pedido, pero confirmar pagos y despachar es tarea operativa del personal.</p>
        <table class="table tabla-taller align-middle">
            <thead><tr><th>Pedido</th><th>Fecha</th><th>Cliente</th><th>Estado</th>
                       <th class="text-end">Total</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${recientes}">
                <tr>
                    <td class="fw-bold">${p.numero}</td>
                    <td class="dato">${p.fechaTexto}</td>
                    <td>${p.cliente.nombreCompleto}</td>
                    <td><span class="estado estado-${fn:toLowerCase(p.estado)}">${p.estado.etiqueta}</span></td>
                    <td class="dato text-end">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td>
                    <td class="text-end">
                        <a href="${ctx}/panel/pedidos/${p.id}" class="btn btn-contorno btn-sm">Ver</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
