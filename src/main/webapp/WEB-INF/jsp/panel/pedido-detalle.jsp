<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="p" value="${pedido}" />

<section class="container py-5">
    <a href="${ctx}/panel/pedidos" class="enlace-volver">&larr; Volver a pedidos</a>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mt-3 mb-2">
        <div>
            <p class="rotulo mb-1">Pedido interno</p>
            <h1 class="mb-1">${p.numero}</h1>
            <p class="dato text-muted mb-0">${p.cliente.nombreCompleto} &middot; ${p.fechaTexto}</p>
        </div>
        <div class="text-end">
            <span class="estado estado-${fn:toLowerCase(p.estado)} fs-6">${p.estado.etiqueta}</span>
            <div class="mt-2">
                <a href="${ctx}/panel/pedidos/${p.id}/pdf" class="btn btn-tinta btn-sm">Descargar PDF</a>
            </div>
        </div>
    </div>

    <div class="cinta my-4"></div>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="ficha p-4">
                <h2 class="fs-5 mb-3">Productos</h2>
                <table class="table table-sm tabla-taller mb-0">
                    <thead><tr><th>SKU</th><th>Producto</th><th>Talla</th><th>Cant.</th>
                               <th class="text-end">Subtotal</th></tr></thead>
                    <tbody>
                    <c:forEach var="it" items="${p.items}">
                        <tr>
                            <td class="dato">${it.producto.sku}</td>
                            <td>${it.nombreProducto}</td>
                            <td class="dato">${empty it.talla ? '—' : it.talla}</td>
                            <td class="dato">${it.cantidad}</td>
                            <td class="dato text-end">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="bloque-dato privado p-4 mb-3">
                                    <h2 class="fs-6 mb-0">Datos de entrega</h2>
                <p class="dato mb-1">${p.direccionEntrega}</p>
                <p class="dato text-muted mb-0">${p.cliente.ciudad}, ${p.cliente.departamento}</p>
                <p class="small text-muted mt-2 mb-0">Esta direccion se guarda cifrada y tu consulta
                    quedo registrada en la auditoria.</p>
            </div>

            <div class="ficha p-4">
                <h2 class="fs-6 mb-3">Trazabilidad</h2>
                <table class="table table-sm tabla-taller mb-0">
                    <tr><th>Total</th><td class="dato">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td></tr>
                    <tr><th>Medio de pago</th><td class="dato">${p.medioPago}</td></tr>
                    <c:if test="${not empty p.referenciaPago}">
                        <tr><th>Referencia</th><td class="dato">${p.referenciaPago}</td></tr>
                    </c:if>
                    <c:if test="${p.tieneComprobante}">
                        <tr><th>Comprobante</th><td class="dato">
                            <a href="${ctx}/imagenes/${p.comprobantePago}" target="_blank">Ver adjunto</a>
                        </td></tr>
                    </c:if>
                    <c:if test="${not empty p.pagoVerificadoPor}">
                        <tr><th>Pago verificado</th><td class="dato">${p.pagoVerificadoPor.nombreUsuario}</td></tr>
                    </c:if>
                    <c:if test="${not empty p.aprobadoPor}">
                        <tr><th>Aceptado por el jefe</th>
                            <td class="dato">${p.aprobadoPor.nombreUsuario}<br>${p.fechaAprobacionTexto}</td></tr>
                    </c:if>
                    <c:if test="${not empty p.motivoDecision}">
                        <tr><th>Nota</th><td class="small">${p.motivoDecision}</td></tr>
                    </c:if>
                    <c:if test="${not empty p.despachadoPor}">
                        <tr><th>Despachado por</th><td class="dato">${p.despachadoPor.nombreUsuario}<br>${p.fechaDespachoTexto}</td></tr>
                    </c:if>
                    <c:if test="${not empty p.numeroGuia}">
                        <tr><th>Guia</th><td class="dato">${p.numeroGuia}</td></tr>
                    </c:if>
                    <c:if test="${not empty p.fechaEntregaTexto}">
                        <tr><th>Entregado</th><td class="dato">${p.fechaEntregaTexto}</td></tr>
                    </c:if>
                    <c:if test="${p.tieneFotoEntrega}">
                        <tr><th>Foto del cliente</th><td class="dato">
                            <a href="${ctx}/imagenes/${p.fotoEntrega}" target="_blank">Ver foto</a>
                        </td></tr>
                    </c:if>
                </table>
            </div>

            <sec:authorize access="hasRole('ADMIN')">
                <form method="post" action="${ctx}/panel/pedidos/${p.id}/estado" class="ficha p-4 mt-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <h2 class="fs-6 mb-2">Cambiar el estado manualmente</h2>
                    <p class="small text-muted">Salta el flujo normal. Uselo solo para casos que el
                        proceso no previo; el cambio queda en la auditoria con su nombre y el motivo.</p>

                    <label class="form-label small" for="estado">Nuevo estado</label>
                    <select id="estado" name="estado" class="form-select form-select-sm mb-2" required>
                        <c:forEach var="e" items="${estadosPosibles}">
                            <option value="${e}" ${e eq p.estado ? 'disabled' : ''}>${e.etiqueta}</option>
                        </c:forEach>
                    </select>

                    <label class="form-label small" for="motivo">Motivo</label>
                    <input id="motivo" name="motivo" class="form-control form-control-sm mb-3"
                           placeholder="Pago recibido en tienda, devolucion acordada..." required>

                    <button class="btn btn-contorno w-100">Cambiar estado</button>
                </form>
            </sec:authorize>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
