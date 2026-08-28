<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3">
        <div>
            <p class="rotulo mb-1">Administracion</p>
            <h1 class="mb-1">Usuarios</h1>
            <p class="text-muted mb-0">Cuentas del personal interno. Los clientes se registran
                solos en /registro y no aparecen aqui.</p>
        </div>
        <a href="${ctx}/panel/usuarios/nuevo" class="btn btn-hilo">Nuevo usuario</a>
    </div>

    <div class="cinta cinta-fina my-4"></div>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th>Usuario</th><th>Correo</th><th>Rol</th><th>Estado</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${usuarios}">
            <tr class="${u.activo ? '' : 'fila-inactiva'}">
                <td class="fw-bold">${u.nombreUsuario}</td>
                <td class="dato">${u.correo}</td>
                <td>
                    <span class="nivel ${u.rol eq 'ADMIN' ? 'nivel-sensible' :
                        (u.rol eq 'JEFE' ? 'nivel-privado' : 'nivel-semiprivado')}">${u.rolTexto}</span>
                </td>
                <td class="dato">${u.activo ? 'Activo' : 'Inactivo'}</td>
                <td class="text-end" style="white-space:nowrap;">
                    <a href="${ctx}/panel/usuarios/${u.id}/editar" class="btn btn-contorno btn-sm">Editar</a>
                    <c:choose>
                        <c:when test="${u.activo}">
                            <form method="post" action="${ctx}/panel/usuarios/${u.id}/desactivar" class="d-inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-contorno btn-sm">Desactivar</button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${ctx}/panel/usuarios/${u.id}/activar" class="d-inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-hilo btn-sm">Activar</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty usuarios}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1">Todavia no hay personal registrado.</p>
            <a href="${ctx}/panel/usuarios/nuevo" class="btn btn-tinta btn-sm mt-2">Crear el primero</a>
        </div>
    </c:if>

    <div class="bloque-dato p-3 mt-4">
        <p class="rotulo mb-1">Desactivar no es lo mismo que eliminar</p>
        <p class="small mb-0">Desactivar bloquea el inicio de sesion pero conserva la cuenta, para
            que los pedidos y decisiones que quedaron a su nombre sigan teniendo sentido en el
            historial. No hay borrado definitivo de usuarios: la trazabilidad importa mas.</p>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
