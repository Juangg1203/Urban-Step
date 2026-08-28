<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Control</p>
    <h1 class="mb-2">Auditoria de datos personales</h1>
    <p class="text-muted">Ultimos 200 eventos. Aqui queda constancia de quien consulto o modifico cada
        nivel de informacion, incluidos los intentos denegados.</p>

    <div class="cinta cinta-fina my-4"></div>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th>Fecha</th><th>Usuario</th><th>Rol</th><th>Accion</th><th>Nivel</th>
                <th>Detalle</th><th>IP</th></tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${registros}">
            <tr>
                <td class="dato">${r.fechaTexto}</td>
                <td>${r.usuario}</td>
                <td class="small">${r.rol}</td>
                <td>
                    <span class="dato ${r.accion eq 'ACCESO_DENEGADO' ? 'text-danger fw-bold' : ''}">${r.accion}</span>
                </td>
                <td>
                    <c:if test="${not empty r.nivelDato}">
                        <span class="nivel nivel-${r.nivelDato eq 'PUBLICO' ? 'publico' :
                            (r.nivelDato eq 'SEMIPRIVADO' ? 'semiprivado' :
                            (r.nivelDato eq 'PRIVADO' ? 'privado' : 'sensible'))}">${r.nivelDato}</span>
                    </c:if>
                </td>
                <td class="small">${r.detalle}</td>
                <td class="dato text-muted">${r.ip}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty registros}">
        <div class="bloque-dato p-4"><p class="mb-0">Todavia no hay eventos registrados.</p></div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
