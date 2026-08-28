<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Comision de venta</p>
    <h1 class="mb-2">Mis comisiones</h1>
    <p class="text-muted">Ganas comision por lo que un cliente te asigna en el checkout, y por
        toda venta que hagas por "Venta asistida". Se confirma cuando el pedido llega al cliente.</p>

    <div class="cinta cinta-fina my-4"></div>

    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Confirmada</p>
                <p class="cifra acento mb-0">$<fmt:formatNumber value="${confirmada}" maxFractionDigits="0" /></p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Pendiente (pedido en camino)</p>
                <p class="cifra mb-0">$<fmt:formatNumber value="${pendiente}" maxFractionDigits="0" /></p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Total</p>
                <p class="cifra mb-0">$<fmt:formatNumber value="${total}" maxFractionDigits="0" /></p>
            </div>
        </div>
    </div>

    <a href="${ctx}/panel/venta-asistida" class="btn btn-hilo mb-4">Hacer una venta asistida</a>

    <h2 class="fs-5 mb-3">Historial</h2>
    <table class="table tabla-taller align-middle">
        <thead><tr><th>Pedido</th><th>Cliente</th><th>Fecha</th><th>Estado del pedido</th>
                   <th class="text-end">Comision</th><th>Estado de la comision</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pedidos}">
            <tr>
                <td class="fw-bold"><a href="${ctx}/panel/pedidos/${p.id}">${p.numero}</a></td>
                <td>${p.cliente.nombreCompleto}</td>
                <td class="dato">${p.fechaTexto}</td>
                <td><span class="estado estado-${fn:toLowerCase(p.estado)}">${p.estado.etiqueta}</span></td>
                <td class="dato text-end">$<fmt:formatNumber value="${p.comisionMonto}" maxFractionDigits="0" /></td>
                <td>
                    <span class="nivel ${p.comisionEstado eq 'CONFIRMADA' ? 'nivel-publico' :
                        (p.comisionEstado eq 'ANULADA' ? 'nivel-sensible' : 'nivel-privado')}">
                        ${p.comisionEstado.etiqueta}
                    </span>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty pedidos}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1">Todavia no tienes ventas con comision.</p>
            <p class="small mb-0">Cuando un cliente te elija en el checkout, o hagas una venta
                asistida, aparecera aqui.</p>
        </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
