<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Decision del jefe</p>
    <h1 class="mb-2">Compras por aceptar</h1>
    <p class="text-muted">El vendedor ya confirmo que el dinero entro. Falta tu visto bueno final
        antes de que bodega lo alista. Cada decision queda con tu nombre, la fecha y el motivo en
        la auditoria; si rechazas, el motivo lo ve el cliente.</p>

    <div class="cinta cinta-fina my-4"></div>

    <c:if test="${empty pendientes}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1">No hay compras esperando tu aceptacion.</p>
            <p class="mb-0 small">Cuando el vendedor confirme un pago, aparece aqui y te llega el aviso.</p>
        </div>
    </c:if>

    <c:forEach var="p" items="${pendientes}">
        <div class="ficha p-4 mb-3">
            <div class="row g-4">
                <div class="col-lg-7">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <h2 class="fs-5 mb-1">${p.numero}</h2>
                            <p class="dato text-muted mb-0">
                                ${p.cliente.nombreCompleto} &middot; ${p.cliente.ciudad} &middot; ${p.fechaTexto}
                            </p>
                        </div>
                        <span class="cifra acento">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></span>
                    </div>

                    <table class="table table-sm tabla-taller mb-2">
                        <thead><tr><th>Producto</th><th>Talla</th><th>Cant.</th><th class="text-end">Subtotal</th></tr></thead>
                        <tbody>
                        <c:forEach var="it" items="${p.items}">
                            <tr>
                                <td>${it.nombreProducto}</td>
                                <td class="dato">${empty it.talla ? '—' : it.talla}</td>
                                <td class="dato">${it.cantidad}</td>
                                <td class="dato text-end">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>

                    <p class="dato text-muted mb-0">Entrega: ${p.direccionEntrega}</p>
                    <p class="dato text-muted mb-0">Pago: ${p.medioPago}
                        &middot; referencia ${p.referenciaPago}</p>
                    <c:if test="${p.tieneComprobante}">
                        <p class="dato mb-0"><a href="${ctx}/imagenes/${p.comprobantePago}" target="_blank">
                            Ver comprobante</a></p>
                    </c:if>
                    <c:if test="${not empty p.pagoVerificadoPor}">
                        <p class="dato text-muted mb-0">Pago verificado por
                            ${p.pagoVerificadoPor.nombreUsuario}</p>
                    </c:if>
                    <c:if test="${not empty p.observaciones}">
                        <p class="small mt-2 mb-0"><strong>Observaciones:</strong> ${p.observaciones}</p>
                    </c:if>
                </div>

                <div class="col-lg-5">
                    <form method="post" action="${ctx}/panel/aprobaciones/${p.id}/aprobar" class="mb-3">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <label class="form-label small" for="nota-${p.id}">Nota interna (opcional)</label>
                        <input id="nota-${p.id}" name="nota" class="form-control form-control-sm mb-2">
                        <button class="btn btn-hilo w-100">Aceptar y pasar a bodega</button>
                    </form>

                    <form method="post" action="${ctx}/panel/aprobaciones/${p.id}/rechazar">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <label class="form-label small" for="motivo-${p.id}">Motivo del rechazo</label>
                        <textarea id="motivo-${p.id}" name="motivo" rows="2" required
                                  class="form-control form-control-sm mb-2"
                                  placeholder="Comprobante falso, monto no coincide..."></textarea>
                        <button class="btn btn-contorno w-100">Rechazar</button>
                        <p class="dato text-muted mt-2 mb-0">El motivo es obligatorio: el cliente lo va a leer.</p>
                    </form>
                </div>
            </div>
        </div>
    </c:forEach>

    <c:if test="${not empty avisos}">
        <div class="cinta cinta-fina my-4"></div>
        <h2 class="fs-5 mb-3">Avisos recientes</h2>
        <c:forEach var="a" items="${avisos}" end="9">
            <div class="d-flex justify-content-between border-bottom py-2">
                <div>
                    <div class="small fw-bold">${a.titulo}</div>
                    <div class="small text-muted">${a.mensaje}</div>
                </div>
                <span class="dato text-muted">${a.fechaTexto}</span>
            </div>
        </c:forEach>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
