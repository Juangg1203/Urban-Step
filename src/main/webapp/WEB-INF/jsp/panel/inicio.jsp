<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <p class="rotulo mb-1"><spring:message code="panel.rotulo" /> &middot; ${rolEtiqueta}</p>
    <h1 class="mb-1"><spring:message code="panel.hola" />, ${usuarioActual.nombreUsuario}</h1>
    <p class="text-muted"><spring:message code="panel.subtitulo" /></p>

    <!-- Los accesos van primero: es a lo que se entra a hacer algo.
         Los indicadores se leen despues. -->
    <div class="acciones-panel mb-4">
<div class="d-flex flex-wrap gap-2">
                    <sec:authorize access="hasAnyRole('EMPLEADO','ADMIN','JEFE')">
                        <a href="${ctx}/panel/clientes" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.clientes" /></a>
                        <a href="${ctx}/panel/atenciones" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.atenciones" /></a>
                        <a href="${ctx}/panel/pedidos" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.pedidos" /></a>
                        <a href="${ctx}/panel/historial" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.historial" /></a>
                    </sec:authorize>
                    <sec:authorize access="hasRole('JEFE')">
                        <a href="${ctx}/panel/productos" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.productos" /></a>
                        <a href="${ctx}/panel/aprobaciones" class="btn btn-hilo btn-sm">
                            <spring:message code="panel.aprobaciones" />
                            <c:if test="${porAprobar > 0}"><span class="globo">${porAprobar}</span></c:if>
                        </a>
                    </sec:authorize>
                    <sec:authorize access="hasRole('ADMIN')">
                        <a href="${ctx}/panel/usuarios" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.usuarios2" /></a>
                    </sec:authorize>
                    <sec:authorize access="hasAnyRole('ADMIN','JEFE')">
                        <a href="${ctx}/panel/reportes?anio=${anio}&amp;mes=${mes}" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.reporte" /></a>
                        <a href="${ctx}/panel/historico" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.reportesGuardados" /></a>
                        <a href="${ctx}/panel/auditoria" class="btn btn-contorno btn-sm">
                            <spring:message code="panel.auditoria" /></a>
                    </sec:authorize>
                </div>
    </div>

    <div class="cinta cinta-fina mb-4"></div>

    <!-- ============ los seis indicadores del negocio ============ -->
    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.usuarios" /></p>
                <p class="cifra mb-1">${totalUsuarios}</p>
                <p class="dato text-muted mb-0">
                    ${usuariosCliente} <spring:message code="panel.clientesRol" /> &middot;
                    ${usuariosInternos} <spring:message code="panel.internos" />
                </p>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.productosTotal" /></p>
                <p class="cifra mb-1">${totalProductos}</p>
                <p class="dato text-muted mb-0">
                    ${productosActivos} <spring:message code="panel.publicados" />
                </p>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.pedidosTotal" /></p>
                <p class="cifra acento mb-0">${totalPedidos}</p>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.inventarioBajo" /></p>
                <p class="cifra ${fn:length(productosBajos) + fn:length(productosAgotados) > 0 ? 'acento' : ''} mb-1">
                    ${fn:length(productosBajos) + fn:length(productosAgotados)}
                </p>
                <p class="dato text-muted mb-0">
                    ${fn:length(productosAgotados)} <spring:message code="panel.agotado" />
                </p>
            </div>
        </div>
    </div>

    <!-- ============ alerta de inventario ============ -->
    <c:if test="${not empty productosBajos or not empty productosAgotados}">
        <div class="aviso-inventario p-3 mb-4">
            <p class="rotulo mb-2"><spring:message code="panel.inventarioAtencion" /></p>
            <c:forEach var="pr" items="${productosAgotados}">
                <div class="d-flex justify-content-between border-bottom py-1">
                    <span class="small">${pr.nombre} <span class="dato text-muted">${pr.sku}</span></span>
                    <span class="chip-alerta"><spring:message code="panel.agotado" /></span>
                </div>
            </c:forEach>
            <c:forEach var="pr" items="${productosBajos}">
                <div class="d-flex justify-content-between border-bottom py-1">
                    <span class="small">${pr.nombre} <span class="dato text-muted">${pr.sku}</span></span>
                    <span class="dato">
                        <spring:message code="panel.quedan" /> ${pr.stock} &middot;
                        <spring:message code="panel.minimo" /> ${pr.stockMinimo}
                    </span>
                </div>
            </c:forEach>
            <sec:authorize access="hasRole('JEFE')">
                <a href="${ctx}/panel/productos" class="btn btn-contorno btn-sm mt-3">
                    <spring:message code="panel.reponer" />
                </a>
            </sec:authorize>
        </div>
    </c:if>

    <!-- ============ cola de trabajo ============ -->
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.porAprobar" /></p>
                <p class="cifra ${porAprobar > 0 ? 'acento' : ''} mb-0">${porAprobar}</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.porVerificar" /></p>
                <p class="cifra mb-0">${porVerificar}</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1"><spring:message code="panel.porDespachar" /></p>
                <p class="cifra mb-0">${porDespachar}</p>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <!-- ============ ultimos pedidos ============ -->
        <div class="col-lg-7">
            <div class="ficha p-4 h-100">
                <div class="d-flex justify-content-between align-items-baseline mb-3">
                    <h2 class="fs-5 mb-0"><spring:message code="panel.ultimosPedidos" /></h2>
                    <a href="${ctx}/panel/pedidos" class="dato text-decoration-none">
                        <spring:message code="panel.verTodos" /> &rarr;
                    </a>
                </div>

                <c:if test="${empty ultimosPedidos}">
                    <p class="small text-muted mb-0"><spring:message code="panel.sinPedidos" /></p>
                </c:if>

                <c:forEach var="pd" items="${ultimosPedidos}">
                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                        <div>
                            <a href="${ctx}/panel/pedidos/${pd.id}" class="fw-bold small text-decoration-none">
                                ${pd.numero}
                            </a>
                            <div class="dato text-muted">
                                ${pd.cliente.nombreCompleto} &middot; ${pd.fechaTexto}
                            </div>
                        </div>
                        <div class="text-end">
                            <div class="dato fw-bold">
                                $<fmt:formatNumber value="${pd.total}" maxFractionDigits="0" />
                            </div>
                            <span class="estado estado-${fn:toLowerCase(pd.estado)}">${pd.estado.etiqueta}</span>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- ============ estado de los pedidos ============ -->
        <div class="col-lg-5">
            <div class="ficha p-4 h-100">
                <h2 class="fs-5 mb-3"><spring:message code="panel.estadoPedidos" /></h2>

                <c:if test="${empty estadosPedido}">
                    <p class="small text-muted mb-0"><spring:message code="panel.sinPedidos" /></p>
                </c:if>

                <c:forEach var="e" items="${estadosPedido}">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>${e.etiqueta}</span>
                        <span class="dato">${e.cantidad} &middot; ${e.porcentaje}%</span>
                    </div>
                    <div class="barra-medida mb-3"><span style="width:${e.porcentaje}%"></span></div>
                </c:forEach>
            </div>
        </div>
    </div>

</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
