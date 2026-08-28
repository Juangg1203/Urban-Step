<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/" class="enlace-volver">&larr; <spring:message code="comun.volverInicio" /></a>
    <p class="rotulo mb-1"><spring:message code="catalogo.titulo" /></p>
    <h1 class="mb-4"><spring:message code="catalogo.subtitulo" /></h1>

    <form class="row g-2 align-items-end mb-4" method="get" action="${ctx}/catalogo">
        <div class="col-md-4">
            <label class="rotulo" for="q"><spring:message code="comun.buscar" /></label>
            <input id="q" name="q" value="${busqueda}" class="form-control" placeholder="camiseta, cuero, bota...">
        </div>
        <div class="col-md-3">
            <label class="rotulo" for="linea"><spring:message code="catalogo.linea" /></label>
            <select id="linea" name="linea" class="form-select">
                <option value=""><spring:message code="comun.todas" /></option>
                <option value="ROPA" ${lineaActiva eq 'ROPA' ? 'selected' : ''}><spring:message code="nav.ropa" /></option>
                <option value="CALZADO" ${lineaActiva eq 'CALZADO' ? 'selected' : ''}><spring:message code="nav.calzado" /></option>
            </select>
        </div>
        <div class="col-md-3">
            <label class="rotulo" for="categoria"><spring:message code="comun.categoria" /></label>
            <select id="categoria" name="categoria" class="form-select">
                <option value=""><spring:message code="comun.todas" /></option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" ${categoriaActiva eq cat.id ? 'selected' : ''}>${cat.nombre}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2">
            <button class="btn btn-tinta w-100"><spring:message code="comun.filtrar" /></button>
        </div>
    </form>

    <div class="cinta cinta-fina mb-4"></div>

    <c:if test="${empty productos}">
        <div class="bloque-dato p-4">
            <p class="mb-1 fw-bold"><spring:message code="catalogo.vacio" /></p>
            <p class="mb-0 small text-muted">Prueba con otra palabra o quita los filtros para ver todo el catalogo.</p>
        </div>
    </c:if>

    <div class="row g-4">
        <c:forEach var="p" items="${productos}">
            <div class="col-6 col-lg-3">
                <article class="ficha">
                    <c:choose>
                        <c:when test="${p.tieneImagen}">
                            <img src="${ctx}${p.rutaImagen}" alt="${p.nombre}" class="ficha-foto">
                        </c:when>
                        <c:otherwise>
                            <%-- Sin foto cargada: se muestra el SKU sobre el bloque de color --%>
                            <div class="ficha-imagen ${p.categoria.linea eq 'CALZADO' ? 'calzado' : ''}">${p.sku}</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="p-3">
                        <p class="rotulo mb-1">${p.categoria.nombre}</p>
                        <h2 class="fs-6 mb-1">${p.nombre}</h2>
                        <p class="dato text-muted mb-1">Tallas ${p.tallas}</p>
                        <p class="precio mb-2">
                            $<fmt:formatNumber value="${p.precio}" type="number" maxFractionDigits="0" />
                        </p>
                        <a href="${ctx}/producto/${p.id}" class="btn btn-contorno btn-sm w-100 mb-2"><spring:message code="comun.verFicha" /></a>
                        <c:if test="${p.stock > 0}">
                            <form method="post" action="${ctx}/carrito/agregar">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="productoId" value="${p.id}">
                                <input type="hidden" name="cantidad" value="1">
                                <input type="hidden" name="volverA" value="catalogo">
                                <button class="btn btn-hilo btn-sm w-100"><spring:message code="catalogo.agregar" /></button>
                            </form>
                        </c:if>
                        <c:if test="${p.stock <= 0}">
                            <span class="dato text-muted d-block text-center"><spring:message code="comun.agotado" /></span>
                        </c:if>
                    </div>
                </article>
            </div>
        </c:forEach>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
