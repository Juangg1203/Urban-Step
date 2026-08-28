<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Consulta</p>
    <h1 class="mb-2">Historial</h1>
    <p class="text-muted">Lo que ves aqui depende de tu rol. Cada consulta queda registrada.</p>

    <div class="cinta cinta-fina my-4"></div>

    <h2 class="fs-5 mb-3">Historial de pedidos</h2>
    <table class="table tabla-taller align-middle">
        <thead><tr><th>Pedido</th><th>Fecha</th><th>Cliente</th><th>Estado</th>
                   <th>Aprobo</th><th>Despacho</th><th class="text-end">Total</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pedidos}">
            <tr>
                <td class="fw-bold">${p.numero}</td>
                <td class="dato">${p.fechaTexto}</td>
                <td>${p.cliente.nombreCompleto}</td>
                <td><span class="estado estado-${fn:toLowerCase(p.estado)}">${p.estado.etiqueta}</span></td>
                <td class="dato">${empty p.aprobadoPor ? '—' : p.aprobadoPor.nombreUsuario}</td>
                <td class="dato">${empty p.despachadoPor ? '—' : p.despachadoPor.nombreUsuario}</td>
                <td class="dato text-end">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></td>
                <td class="text-end">
                    <a href="${ctx}/panel/pedidos/${p.id}" class="btn btn-contorno btn-sm">Ver</a>
                    <a href="${ctx}/panel/pedidos/${p.id}/pdf" class="btn btn-contorno btn-sm">PDF</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty pedidos}">
        <div class="bloque-dato p-4"><p class="mb-0">Todavia no hay pedidos registrados.</p></div>
    </c:if>

    <!-- El modelo solo trae "usuarios" si el rol lo permite: no se filtra aqui -->
    <c:if test="${not empty usuarios}">
        <div class="cinta cinta-fina my-5"></div>

        <h2 class="fs-5 mb-1">Usuarios registrados</h2>
        <p class="small text-muted mb-3">Solo datos de acceso: nombre de usuario, correo y rol.
            Los datos personales del cliente se consultan desde el directorio, con su politica de acceso.</p>

        <table class="table tabla-taller align-middle">
            <thead><tr><th>Usuario</th><th>Correo</th><th>Rol</th><th>Estado</th></tr></thead>
            <tbody>
            <c:forEach var="u" items="${usuarios}">
                <tr>
                    <td class="fw-bold">${u.nombreUsuario}</td>
                    <td class="dato">${u.correo}</td>
                    <td><span class="nivel ${u.rol eq 'CLIENTE' ? 'nivel-publico' : 'nivel-privado'}">${u.rolTexto}</span></td>
                    <td class="dato">${u.activo ? 'Activo' : 'Inactivo'}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
