<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3">
        <div>
            <p class="rotulo mb-1">Catalogo</p>
            <h1 class="mb-1">Productos</h1>
            <p class="text-muted mb-0">Todo lo que se publica aqui sale del catalogo en vivo.</p>
        </div>
        <a href="${ctx}/panel/productos/nuevo" class="btn btn-hilo">Nuevo producto</a>
    </div>

    <div class="cinta cinta-fina my-4"></div>

    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Total en la base</p>
                <p class="cifra mb-0">${totalProductos}</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Publicados</p>
                <p class="cifra mb-0">${activos}</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="tablero">
                <p class="rotulo mb-1">Con stock bajo (&le; ${limiteStockBajo})</p>
                <p class="cifra ${stockBajo > 0 ? 'acento' : ''} mb-0">${stockBajo}</p>
            </div>
        </div>
    </div>

    <form class="row g-2 mb-4" method="get" action="${ctx}/panel/productos">
        <div class="col-md-5">
            <label class="visually-hidden" for="q">Buscar producto</label>
            <input id="q" name="q" value="${busqueda}" class="form-control"
                   placeholder="Buscar por nombre o referencia">
        </div>
        <div class="col-md-2"><button class="btn btn-tinta w-100">Buscar</button></div>
        <c:if test="${not empty busqueda}">
            <div class="col-md-2">
                <a href="${ctx}/panel/productos" class="btn btn-contorno w-100">Limpiar</a>
            </div>
        </c:if>
    </form>

    <table class="table tabla-taller align-middle">
        <thead>
            <tr><th style="width:64px;"></th><th>Producto</th><th>Categoria</th>
                <th class="text-end">Precio</th><th class="text-end">Cantidad</th>
                <th>Estado</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${productos}">
            <tr class="${p.activo ? '' : 'fila-inactiva'}">
                <td>
                    <c:choose>
                        <c:when test="${p.tieneImagen}">
                            <img src="${ctx}${p.rutaImagen}" alt="${p.nombre}" class="miniatura">
                        </c:when>
                        <c:otherwise><span class="miniatura vacia">${fn:substring(p.sku,0,3)}</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <div class="fw-bold">${p.nombre}</div>
                    <div class="dato text-muted">${p.sku}
                        <c:if test="${not empty p.color}"> &middot; ${p.color}</c:if>
                    </div>
                </td>
                <td class="small">${p.categoria.nombre}<br>
                    <span class="dato text-muted">${p.categoria.linea}</span></td>
                <td class="dato text-end">$<fmt:formatNumber value="${p.precio}" maxFractionDigits="0" /></td>
                <td class="dato text-end ${p.stock <= limiteStockBajo ? 'text-danger fw-bold' : ''}">
                    ${p.stock}
                </td>
                <td>
                    <span class="estado ${p.activo ? (p.stock > 0 ? 'estado-entregado' : 'estado-pendiente_aprobacion') : 'estado-cancelado'}">
                        ${p.estado}
                    </span>
                </td>
                <td class="text-end" style="white-space:nowrap;">
                    <a href="${ctx}/panel/productos/${p.id}/editar" class="btn btn-contorno btn-sm">Editar</a>

                    <c:choose>
                        <c:when test="${p.activo}">
                            <form method="post" action="${ctx}/panel/productos/${p.id}/desactivar" class="d-inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-contorno btn-sm">Retirar</button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${ctx}/panel/productos/${p.id}/activar" class="d-inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-hilo btn-sm">Publicar</button>
                            </form>
                        </c:otherwise>
                    </c:choose>

                    <form method="post" action="${ctx}/panel/productos/${p.id}/eliminar" class="d-inline"
                          onsubmit="return confirm('Eliminar &quot;${p.nombre}&quot; de forma definitiva? Esto no se puede deshacer.');">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button class="btn btn-link btn-sm text-danger p-0 ms-1">Eliminar</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty productos}">
        <div class="bloque-dato p-4">
            <p class="fw-bold mb-1">No hay productos que coincidan.</p>
            <a href="${ctx}/panel/productos/nuevo" class="btn btn-tinta btn-sm mt-2">Crear el primero</a>
        </div>
    </c:if>

    <div class="bloque-dato p-3 mt-4">
        <p class="rotulo mb-1">Retirar no es lo mismo que eliminar</p>
        <p class="small mb-0"><strong>Retirar</strong> lo saca del catalogo pero lo conserva en la base,
            asi los pedidos que ya lo incluyen siguen cuadrando. <strong>Eliminar</strong> lo borra de
            verdad, y por eso solo se permite mientras el producto no se haya vendido nunca.</p>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
