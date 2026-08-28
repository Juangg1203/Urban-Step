<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel/venta-asistida" class="enlace-volver">&larr; Cambiar de cliente</a>
    <p class="rotulo mb-1">Venta asistida</p>
    <h1 class="mb-2">Comprando para ${carrito.clienteObjetivoNombre}</h1>
    <p class="text-muted">Esta venta va a quedar a tu nombre para la comision.</p>

    <div class="cinta cinta-fina my-4"></div>

    <c:if test="${not empty faltantes}">
        <div class="aviso-inventario p-3 mb-4">
            <p class="rotulo mb-2">Revisa las cantidades</p>
            <ul class="mb-0 small">
                <c:forEach var="f" items="${faltantes}"><li>${f.mensaje}</li></c:forEach>
            </ul>
        </div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-7">
            <h2 class="fs-5 mb-3">Catalogo</h2>
            <div class="row g-3">
                <c:forEach var="p" items="${productos}">
                    <div class="col-md-4">
                        <div class="ficha p-2 h-100">
                            <c:choose>
                                <c:when test="${p.tieneImagen}">
                                    <img src="${ctx}${p.rutaImagen}" alt="${p.nombre}" class="ficha-foto">
                                </c:when>
                                <c:otherwise>
                                    <div class="ficha-imagen">${p.sku}</div>
                                </c:otherwise>
                            </c:choose>
                            <div class="p-2">
                                <p class="small fw-bold mb-0">${p.nombre}</p>
                                <p class="precio mb-2">$<fmt:formatNumber value="${p.precio}" maxFractionDigits="0" /></p>
                                <c:if test="${p.stock > 0}">
                                    <form method="post" action="${ctx}/panel/venta-asistida/carrito/agregar">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="productoId" value="${p.id}">
                                        <div class="d-flex gap-1">
                                            <select name="talla" class="form-select form-select-sm">
                                                <c:forTokens var="t" items="${p.tallas}" delims=",">
                                                    <option value="${fn:trim(t)}">${fn:trim(t)}</option>
                                                </c:forTokens>
                                            </select>
                                            <button class="btn btn-hilo btn-sm">+</button>
                                        </div>
                                    </form>
                                </c:if>
                                <c:if test="${p.stock <= 0}">
                                    <span class="dato text-muted">Agotado</span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="ficha p-4 mb-3">
                <h2 class="fs-5 mb-3">Carrito de ${carrito.clienteObjetivoNombre}</h2>
                <c:if test="${carrito.vacio}"><p class="small text-muted">Todavia no hay productos.</p></c:if>
                <c:forEach var="it" items="${carrito.items}">
                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                        <div>
                            <p class="small fw-bold mb-0">${it.cantidad} &times; ${it.nombre}</p>
                            <p class="dato text-muted mb-0">${it.talla}</p>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="dato">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></span>
                            <form method="post" action="${ctx}/panel/venta-asistida/carrito/quitar">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="productoId" value="${it.productoId}">
                                <input type="hidden" name="talla" value="${it.talla}">
                                <button class="btn btn-link btn-sm text-danger p-0">Quitar</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
                <div class="d-flex justify-content-between align-items-baseline mt-3">
                    <span class="rotulo">Total</span>
                    <span class="cifra acento">$<fmt:formatNumber value="${carrito.total}" maxFractionDigits="0" /></span>
                </div>
            </div>

            <c:if test="${not carrito.vacio}">
                <form method="post" action="${ctx}/panel/venta-asistida/confirmar" class="ficha p-4">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <h2 class="fs-6 mb-2">Datos de entrega</h2>
                    <input name="direccion" class="form-control mb-2" placeholder="Direccion de entrega" required>
                    <select name="medioPago" class="form-select mb-3">
                        <option value="EFECTIVO">Efectivo en tienda</option>
                        <option value="TARJETA">Tarjeta</option>
                        <option value="PSE">PSE</option>
                        <option value="CONTRA_ENTREGA">Contra entrega</option>
                    </select>
                    <textarea name="observaciones" rows="2" class="form-control mb-3"
                              placeholder="Observaciones (opcional)"></textarea>
                    <button class="btn btn-hilo w-100">Confirmar venta</button>
                </form>

                <form method="post" action="${ctx}/panel/venta-asistida/cancelar" class="mt-2">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-link btn-sm text-danger p-0">Cancelar y empezar de nuevo</button>
                </form>
            </c:if>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
