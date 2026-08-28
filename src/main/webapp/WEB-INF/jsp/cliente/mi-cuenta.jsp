<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="c" value="${vista.cliente}" />

<section class="container py-5">
    <a href="${ctx}/" class="enlace-volver">&larr; Volver al inicio</a>
    <p class="rotulo mb-1">Titular de los datos</p>
    <h1 class="mb-1">Hola, ${c.nombres}</h1>
    <p class="text-muted">Aqui ves todo lo que la tienda tiene sobre ti, organizado por nivel. Puedes
        corregirlo, autorizarlo o borrarlo sin pedirle permiso a nadie.</p>

    <div class="cinta cinta-fina my-4"></div>

    <div class="row g-4">
        <!-- ============ PUBLICO ============ -->
        <div class="col-lg-6">
            <div class="bloque-dato publico p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos publicos</h2>
                <form method="post" action="${ctx}/mi-cuenta/publicos" class="row g-2">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="col-6">
                        <label class="form-label small" for="nombres">Nombres</label>
                        <input id="nombres" name="nombres" class="form-control form-control-sm"
                               value="${c.nombres}" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="apellidos">Apellidos</label>
                        <input id="apellidos" name="apellidos" class="form-control form-control-sm"
                               value="${c.apellidos}" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="ciudad">Ciudad</label>
                        <input id="ciudad" name="ciudad" class="form-control form-control-sm" value="${c.ciudad}">
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="departamento">Departamento</label>
                        <input id="departamento" name="departamento" class="form-control form-control-sm"
                               value="${c.departamento}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="ocupacion">Ocupacion</label>
                        <input id="ocupacion" name="ocupacion" class="form-control form-control-sm"
                               value="${c.ocupacion}">
                    </div>
                    <div class="col-12 mt-3">
                        <button class="btn btn-tinta btn-sm">Guardar cambios</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ============ PRIVADO ============ -->
        <div class="col-lg-6">
            <div class="bloque-dato privado p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos privados</h2>
                <p class="small text-muted">Guardados con AES-256. El agente los ve enmascarados solo para
                    verificar tu identidad; el administrador no los ve.</p>
                <form method="post" action="${ctx}/mi-cuenta/privados" class="row g-2">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="col-4">
                        <label class="form-label small" for="tipoDocumento">Documento</label>
                        <select id="tipoDocumento" name="tipoDocumento" class="form-select form-select-sm">
                            <option ${vista.tipoDocumento eq 'CC' ? 'selected' : ''}>CC</option>
                            <option ${vista.tipoDocumento eq 'CE' ? 'selected' : ''}>CE</option>
                            <option ${vista.tipoDocumento eq 'TI' ? 'selected' : ''}>TI</option>
                            <option ${vista.tipoDocumento eq 'PA' ? 'selected' : ''}>PA</option>
                        </select>
                    </div>
                    <div class="col-8">
                        <label class="form-label small" for="numeroDocumento">Numero</label>
                        <input id="numeroDocumento" name="numeroDocumento" class="form-control form-control-sm"
                               value="${vista.numeroDocumento}">
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="telefono">Telefono</label>
                        <input id="telefono" name="telefono" class="form-control form-control-sm"
                               value="${vista.telefono}">
                    </div>
                    <div class="col-6">
                        <label class="form-label small" for="fechaNacimiento">Nacimiento</label>
                        <input id="fechaNacimiento" name="fechaNacimiento" type="date"
                               class="form-control form-control-sm" value="${vista.fechaNacimiento}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="correoPersonal">Correo personal</label>
                        <input id="correoPersonal" name="correoPersonal" class="form-control form-control-sm"
                               value="${vista.correoPersonal}">
                    </div>
                    <div class="col-12">
                        <label class="form-label small" for="direccion">Direccion</label>
                        <input id="direccion" name="direccion" class="form-control form-control-sm"
                               value="${vista.direccion}">
                    </div>
                    <div class="col-12 mt-3">
                        <button class="btn btn-tinta btn-sm">Guardar cifrado</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ============ SENSIBLE ============ -->
        <div class="col-lg-7">
            <div class="bloque-dato sensible p-4 h-100">
                                    <h2 class="fs-5 mb-0">Datos sensibles</h2>

                <c:choose>
                    <c:when test="${not c.autorizaSensibles}">
                        <p class="small">Son opcionales. Sirven para recomendarte tallas y avisarte si una
                            prenda tiene un material al que eres alergico. Si no los das, no pierdes ningun
                            servicio.</p>
                        <form method="post" action="${ctx}/mi-cuenta/sensibles/autorizacion">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <input type="hidden" name="autoriza" value="true">
                            <button class="btn btn-hilo btn-sm">Autorizar y agregar mis datos</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${ctx}/mi-cuenta/sensibles" class="row g-2">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <div class="col-12">
                                <label class="form-label small" for="medidasCorporales">Medidas corporales</label>
                                <input id="medidasCorporales" name="medidasCorporales"
                                       class="form-control form-control-sm"
                                       placeholder="Pecho 96 cm, cintura 80 cm, calzado 40"
                                       value="${vista.medidasCorporales}">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small" for="alergiasMateriales">Alergias a materiales</label>
                                <input id="alergiasMateriales" name="alergiasMateriales"
                                       class="form-control form-control-sm" value="${vista.alergiasMateriales}">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small" for="condicionMovilidad">Condiciones de movilidad</label>
                                <input id="condicionMovilidad" name="condicionMovilidad"
                                       class="form-control form-control-sm" value="${vista.condicionMovilidad}">
                            </div>
                            <div class="col-12">
                                <label class="form-label small" for="restriccionVestimenta">Restricciones de vestimenta</label>
                                <input id="restriccionVestimenta" name="restriccionVestimenta"
                                       class="form-control form-control-sm" value="${vista.restriccionVestimenta}">
                            </div>
                            <div class="col-12 mt-3">
                                <button class="btn btn-tinta btn-sm">Guardar</button>
                            </div>
                        </form>

                        <div class="cinta cinta-fina my-3"></div>
                        <form method="post" action="${ctx}/mi-cuenta/sensibles/autorizacion"
                              onsubmit="return confirm('Al revocar se eliminan tus datos sensibles. Continuar?');">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <input type="hidden" name="autoriza" value="false">
                            <button class="btn btn-outline-danger btn-sm">Revocar autorizacion y borrar</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- ============ AUTORIZACIONES ============ -->
        <div class="col-lg-5">
            <div class="ficha p-4 h-100">
                <h2 class="fs-5 mb-3">Tus autorizaciones</h2>
                <table class="table table-sm tabla-taller mb-3">
                    <tr>
                        <td>Tratamiento de datos</td>
                        <td class="dato">${c.aceptaTratamiento ? 'Autorizado' : 'Pendiente'}</td>
                    </tr>
                    <tr>
                        <td>Datos sensibles</td>
                        <td class="dato">${c.autorizaSensibles ? 'Autorizado' : 'No autorizado'}</td>
                    </tr>
                    <tr>
                        <td>Comunicaciones comerciales</td>
                        <td class="dato">${c.autorizaMarketing ? 'Autorizado' : 'No autorizado'}</td>
                    </tr>
                    <tr>
                        <td>Version de la politica</td>
                        <td class="dato">${c.versionPolitica}</td>
                    </tr>
                </table>

                <form method="post" action="${ctx}/mi-cuenta/marketing">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="autoriza" value="${c.autorizaMarketing ? 'false' : 'true'}">
                    <button class="btn btn-contorno btn-sm w-100">
                        ${c.autorizaMarketing ? 'Dejar de recibir comunicaciones' : 'Quiero recibir novedades'}
                    </button>
                </form>
            </div>
        </div>

        <!-- ============ SEMIPRIVADO ============ -->
        <div class="col-lg-7">
            <div class="bloque-dato semiprivado p-4 h-100">
                                    <h2 class="fs-5 mb-0">Tus compras</h2>
                <c:if test="${empty vista.compras}">
                    <p class="small text-muted mb-0">Todavia no tienes compras registradas.</p>
                </c:if>
                <c:if test="${not empty vista.compras}">
                    <table class="table table-sm tabla-taller mb-0">
                        <thead><tr><th>Fecha</th><th>Producto</th><th>Monto</th><th>Pago</th></tr></thead>
                        <tbody>
                        <c:forEach var="compra" items="${vista.compras}">
                            <tr>
                                <td class="dato">${compra.fechaTexto}</td>
                                <td>${compra.producto.nombre}</td>
                                <td class="dato">$<fmt:formatNumber value="${compra.monto}" maxFractionDigits="0" /></td>
                                <td class="small">${compra.medioPago} &middot; ${compra.estadoPago}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>

        <!-- ============ ATENCIONES ============ -->
        <div class="col-lg-5">
            <div class="ficha p-4 h-100">
                <h2 class="fs-5 mb-3">Tus atenciones</h2>
                <c:if test="${empty atenciones}">
                    <p class="small text-muted mb-0">Aun no has escrito al chat ni abierto un caso.</p>
                </c:if>
                <c:forEach var="a" items="${atenciones}" end="5">
                    <div class="d-flex justify-content-between border-bottom py-2">
                        <div>
                            <div class="small fw-bold">${a.tema.etiqueta}</div>
                            <div class="dato text-muted">${a.canal} &middot; ${a.estado}</div>
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
