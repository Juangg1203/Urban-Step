<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="c" value="${vista.cliente}" />

<section class="container py-5">
    <a href="${ctx}/panel/clientes" class="enlace-volver">&larr; Volver al directorio</a>
    <p class="rotulo mt-3 mb-1">Ficha de cliente &middot; consultada como ${rolEtiqueta}</p>
    <h1 class="mb-3">${c.nombreCompleto}</h1>

    <div class="cinta cinta-fina mb-4"></div>

    <div class="row g-4">
        <div class="col-lg-4">
            <div class="bloque-dato publico p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos publicos</h2>
                <table class="table table-sm tabla-taller mb-0">
                    <tr><th>Ciudad</th><td>${c.ciudad}</td></tr>
                    <tr><th>Departamento</th><td>${c.departamento}</td></tr>
                    <tr><th>Ocupacion</th><td>${c.ocupacion}</td></tr>
                    <tr><th>Registro</th><td class="dato">${c.fechaRegistroTexto}</td></tr>
                    <tr><th>Politica</th><td class="dato">${c.versionPolitica}</td></tr>
                </table>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="bloque-dato privado p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos privados</h2>
                <c:choose>
                    <c:when test="${vista.accesoPrivado.denegado}">
                        <div class="oculto-por-politica">
                            Tu rol no tiene acceso a este nivel. El intento quedo registrado en la auditoria.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${vista.accesoPrivado.enmascarado}">
                            <p class="small text-muted">Datos enmascarados: sirven para confirmar identidad,
                                no para copiarlos a otro sistema.</p>
                        </c:if>
                        <table class="table table-sm tabla-taller mb-0">
                            <tr><th>Documento</th><td class="dato">${vista.tipoDocumento} ${vista.numeroDocumento}</td></tr>
                            <tr><th>Telefono</th><td class="dato">${vista.telefono}</td></tr>
                            <tr><th>Correo</th><td class="dato">${vista.correoPersonal}</td></tr>
                            <tr><th>Direccion</th><td class="dato">${vista.direccion}</td></tr>
                            <tr><th>Nacimiento</th><td class="dato">${vista.fechaNacimiento}</td></tr>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="bloque-dato sensible p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos sensibles</h2>
                <div class="oculto-por-politica mb-3">
                    El contenido de este nivel no es visible para ningun rol interno. Solo el titular puede
                    verlo desde Mi cuenta.
                </div>
                <c:if test="${vista.sensiblesRegistrados}">
                    <table class="table table-sm tabla-taller mb-0">
                        <tr><th>Registrados</th><td class="dato">Si</td></tr>
                        <tr><th>Autorizados</th><td class="dato">${vista.sensiblesAutorizados ? 'Si' : 'No'}</td></tr>
                        <tr><th>Desde</th><td class="dato">${vista.fechaAutorizacionSensibles}</td></tr>
                    </table>
                </c:if>
                <c:if test="${not vista.sensiblesRegistrados}">
                    <p class="small text-muted mb-0">Sin metadatos disponibles para tu rol.</p>
                </c:if>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="bloque-dato semiprivado p-4 h-100">
                                    <h2 class="fs-5 mb-0">Historial de compras</h2>
                <c:choose>
                    <c:when test="${vista.accesoSemiprivado.denegado}">
                        <div class="oculto-por-politica">Tu rol no accede al historial de compras.</div>
                    </c:when>
                    <c:when test="${empty vista.compras}">
                        <p class="small text-muted mb-0">Sin compras registradas.</p>
                    </c:when>
                    <c:otherwise>
                        <table class="table table-sm tabla-taller mb-0">
                            <thead><tr><th>Fecha</th><th>Producto</th><th>Monto</th><th>Estado</th></tr></thead>
                            <tbody>
                            <c:forEach var="compra" items="${vista.compras}">
                                <tr>
                                    <td class="dato">${compra.fechaTexto}</td>
                                    <td>${compra.producto.nombre}</td>
                                    <td class="dato">$<fmt:formatNumber value="${compra.monto}" maxFractionDigits="0" /></td>
                                    <td class="small">${compra.estadoPago}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="ficha p-4 h-100">
                <h2 class="fs-5 mb-3">Atenciones del cliente</h2>
                <c:if test="${empty atenciones}">
                    <p class="small text-muted mb-0">Sin atenciones registradas.</p>
                </c:if>
                <c:forEach var="a" items="${atenciones}" end="7">
                    <div class="d-flex justify-content-between border-bottom py-2">
                        <div>
                            <div class="small fw-bold">${a.tema.etiqueta}</div>
                            <div class="dato text-muted">${a.fechaInicioTexto} &middot; ${a.canal}</div>
                        </div>
                        <div class="estrellas">
                            <c:if test="${not empty a.calificacion}">
                                <c:forEach begin="1" end="${a.calificacion}">&#9733;</c:forEach>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
