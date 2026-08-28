<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="u" value="${usuario}" />

<section class="container py-5" style="max-width:640px;">
    <a href="${ctx}/panel/usuarios" class="enlace-volver">&larr; Volver a usuarios</a>
    <p class="rotulo mt-3 mb-1">${esNuevo ? 'Alta' : 'Edicion'}</p>
    <h1 class="mb-3">${esNuevo ? 'Nuevo usuario' : u.nombreUsuario}</h1>

    <div class="cinta cinta-fina mb-4"></div>

    <form method="post" action="${ctx}/panel/usuarios/guardar">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <c:if test="${not esNuevo}"><input type="hidden" name="id" value="${u.id}"></c:if>

        <div class="ficha p-4 mb-3">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label small" for="nombreUsuario">Nombre de usuario</label>
                    <input id="nombreUsuario" name="nombreUsuario" value="${u.nombreUsuario}"
                           class="form-control" required maxlength="60">
                </div>
                <div class="col-md-6">
                    <label class="form-label small" for="correo">Correo</label>
                    <input id="correo" name="correo" type="email" value="${u.correo}"
                           class="form-control" required maxlength="120">
                </div>
                <div class="col-md-6">
                    <label class="form-label small" for="rol">Rol</label>
                    <select id="rol" name="rol" class="form-select" required onchange="mostrarSubtipo()">
                        <option value="EMPLEADO" ${u.rol eq 'EMPLEADO' ? 'selected' : ''}>Empleado</option>
                        <option value="JEFE" ${u.rol eq 'JEFE' ? 'selected' : ''}>Jefe</option>
                        <option value="ADMIN" ${u.rol eq 'ADMIN' ? 'selected' : ''}>Administrador</option>
                    </select>
                </div>
                <div class="col-md-6" id="campoSubtipo">
                    <label class="form-label small" for="subtipo">Subtipo</label>
                    <select id="subtipo" name="subtipo" class="form-select">
                        <option value="VENDEDOR" ${u.subtipo eq 'VENDEDOR' ? 'selected' : ''}>Vendedor</option>
                        <option value="BODEGUERO" ${u.subtipo eq 'BODEGUERO' ? 'selected' : ''}>Bodeguero</option>
                    </select>
                    <p class="dato text-muted mb-0">Solo aplica para el rol Empleado.</p>
                </div>
            </div>
        </div>

        <div class="ficha p-4 mb-3">
            <h2 class="fs-6 mb-2">${esNuevo ? 'Clave' : 'Restablecer clave (opcional)'}</h2>
            <p class="small text-muted">
                ${esNuevo ? 'Se guarda cifrada con BCrypt, igual que las demas cuentas.'
                          : 'Deja esto en blanco si no vas a cambiar la clave.'}
            </p>
            <div class="d-flex justify-content-between align-items-baseline">
                <label class="form-label small mb-0" for="clave">Clave</label>
                <span>
                    <button type="button" id="sugerirClave" class="btn btn-link btn-sm p-0 dato">Sugerir</button>
                    <span class="text-muted dato">&middot;</span>
                    <button type="button" id="verClave" class="btn btn-link btn-sm p-0 dato">Ver</button>
                </span>
            </div>
            <input type="password" id="clave" name="clave" class="form-control" autocomplete="new-password"
                   ${esNuevo ? 'required' : ''}>
            <div class="medidor mt-2"><div id="barraClave" class="medidor-relleno"></div></div>
            <span id="textoClave" class="dato medidor-texto"></span>
            <ul id="avisosClave" class="avisos-clave"></ul>
            <div id="claveSugerida" class="clave-sugerida" hidden></div>
        </div>

        <button class="btn btn-hilo w-100 mb-2">${esNuevo ? 'Crear usuario' : 'Guardar cambios'}</button>
        <a href="${ctx}/panel/usuarios" class="btn btn-contorno w-100">Cancelar</a>
    </form>
</section>

<script src="${ctx}/recursos/js/seguridad-clave.js"></script>
<script>
    function mostrarSubtipo() {
        var rol = document.getElementById('rol').value;
        document.getElementById('campoSubtipo').style.display = (rol === 'EMPLEADO') ? '' : 'none';
    }
    mostrarSubtipo();
</script>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
