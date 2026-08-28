<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/catalogo" class="enlace-volver">&larr; <spring:message code="comun.volverCatalogo" /></a>
    <div class="row g-5 mt-1">
        <div class="col-lg-6">
            <c:choose>
                <c:when test="${producto.tieneImagen}">
                    <img src="${ctx}${producto.rutaImagen}" alt="${producto.nombre}" class="foto-producto">
                </c:when>
                <c:otherwise>
                    <div class="ficha-imagen ${producto.categoria.linea eq 'CALZADO' ? 'calzado' : ''}"
                         style="height:340px;font-size:3.4rem;">${producto.sku}</div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="col-lg-6">
            <p class="rotulo mb-1">${producto.categoria.nombre} &middot; ${producto.categoria.linea}</p>
            <h1 class="mb-2">${producto.nombre}</h1>
            <p class="precio fs-3 mb-3">
                $<fmt:formatNumber value="${producto.precio}" type="number" maxFractionDigits="0" />
            </p>
            <p>${producto.descripcion}</p>

            <div class="cinta cinta-fina my-4"></div>

            <table class="table tabla-taller">
                <tr><th><spring:message code="comun.tallas" /></th><td class="dato">${producto.tallas}</td></tr>
                <tr><th><spring:message code="comun.color" /></th><td>${producto.color}</td></tr>
                <tr><th><spring:message code="comun.material" /></th><td>${producto.material}</td></tr>
                <tr><th><spring:message code="comun.existencias" /></th><td class="dato">${producto.stock} unidades</td></tr>
                <tr><th><spring:message code="comun.referencia" /></th><td class="dato">${producto.sku}</td></tr>
            </table>

            <!-- ---------- agregar al carrito ---------- -->
            <c:choose>
                <c:when test="${producto.stock <= 0}">
                    <div class="bloque-dato p-3 mb-3">
                        <p class="fw-bold mb-1">Agotado por ahora</p>
                        <p class="small mb-0">Escribenos por el chat y te avisamos cuando vuelva.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/carrito/agregar" class="ficha p-3 mb-3">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <input type="hidden" name="productoId" value="${producto.id}">
                        <input type="hidden" name="volverA" value="producto">
                        <div class="row g-2 align-items-end">
                            <div class="col-5">
                                <label class="form-label small" for="talla"><spring:message code="comun.talla" /></label>
                                <select id="talla" name="talla" class="form-select form-select-sm">
                                    <c:forTokens var="t" items="${producto.tallas}" delims=",">
                                        <option value="${fn:trim(t)}">${fn:trim(t)}</option>
                                    </c:forTokens>
                                </select>
                            </div>
                            <div class="col-3">
                                <label class="form-label small" for="cantidad"><spring:message code="comun.cantidad" /></label>
                                <input id="cantidad" name="cantidad" type="number" value="1"
                                       min="1" max="${producto.stock}" class="form-control form-control-sm">
                            </div>
                            <div class="col-4">
                                <button class="btn btn-hilo btn-sm w-100"><spring:message code="catalogo.agregar" /></button>
                            </div>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>

            <div class="bloque-dato sensible p-3 mt-4">
                
                <p class="small mt-2 mb-0">
                    Si guardas tus medidas en Mi cuenta, el sitio te sugiere la talla de esta prenda.
                    Esa informacion queda cifrada y no la ve ningun empleado, ni el administrador.
                </p>
            </div>
        </div>
    </div>

    <!-- ============ resenas ============ -->
    <div class="cinta cinta-fina my-5"></div>
    <div class="row">
        <div class="col-lg-8">
            <div class="d-flex align-items-baseline gap-3 mb-3">
                <h2 class="fs-5 mb-0">Resenas</h2>
                <c:if test="${totalResenas > 0}">
                    <span class="dato">
                        <span class="estrellas">
                            <c:forEach begin="1" end="5" var="i">
                                <c:choose>
                                    <c:when test="${i <= promedioResenas}">&#9733;</c:when>
                                    <c:otherwise><span style="color:var(--linea)">&#9733;</span></c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                        ${promedioResenas} / 5 &middot; ${totalResenas}
                        ${totalResenas eq 1 ? 'resena' : 'resenas'}
                    </span>
                </c:if>
            </div>

            <c:if test="${empty resenas}">
                <p class="text-muted small mb-0">Nadie ha resenado este producto todavia. Las resenas
                    solo las dejan clientes que ya lo compraron y recibieron.</p>
            </c:if>

            <c:forEach var="r" items="${resenas}">
                <div class="ficha p-3 mb-2">
                    <div class="d-flex justify-content-between align-items-start mb-1">
                        <span class="fw-bold small">${r.nombreCliente}</span>
                        <span class="dato text-muted">${r.fechaTexto}</span>
                    </div>
                    <p class="estrellas mb-1">
                        <c:forEach begin="1" end="${r.calificacion}">&#9733;</c:forEach>
                    </p>
                    <c:if test="${not empty r.comentario}">
                        <p class="small mb-0">${r.comentario}</p>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
