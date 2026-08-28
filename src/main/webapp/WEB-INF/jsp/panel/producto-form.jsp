<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="p" value="${producto}" />

<section class="container py-5" style="max-width:900px;">
    <a href="${ctx}/panel/productos" class="enlace-volver">&larr; Volver a productos</a>
    <p class="rotulo mt-3 mb-1">${esNuevo ? 'Alta' : 'Edicion'}</p>
    <h1 class="mb-3">${esNuevo ? 'Nuevo producto' : p.nombre}</h1>

    <div class="cinta cinta-fina mb-4"></div>

    <form method="post" action="${ctx}/panel/productos/guardar" enctype="multipart/form-data">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <c:if test="${not esNuevo}"><input type="hidden" name="id" value="${p.id}"></c:if>

        <div class="row g-4">
            <div class="col-lg-7">
                <div class="ficha p-4 mb-3">
                    <h2 class="fs-5 mb-3">Identificacion</h2>

                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label small">Referencia (SKU)</label>
                            <c:choose>
                                <c:when test="${esNuevo}">
                                    <input class="form-control" value="Se genera al guardar" disabled>
                                    <p class="dato text-muted mb-0">Segun la categoria: CAM-001,
                                        PANT-011...</p>
                                </c:when>
                                <c:otherwise>
                                    <input class="form-control" value="${p.sku}" disabled>
                                    <p class="dato text-muted mb-0">Se genera sola; cambia si cambias
                                        la categoria.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="col-md-8">
                            <label class="form-label small" for="nombre">Nombre</label>
                            <input id="nombre" name="nombre" value="${p.nombre}" class="form-control"
                                   required maxlength="120">
                        </div>
                        <div class="col-12">
                            <label class="form-label small" for="descripcion">Descripcion</label>
                            <textarea id="descripcion" name="descripcion" rows="3" maxlength="500"
                                      class="form-control">${p.descripcion}</textarea>
                        </div>
                    </div>
                </div>

                <div class="ficha p-4 mb-3">
                    <h2 class="fs-5 mb-3">Clasificacion y precio</h2>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label small" for="categoriaId">Categoria</label>
                            <select id="categoriaId" name="categoriaId" class="form-select" required>
                                <option value="">Elige una categoria</option>
                                <c:forEach var="cat" items="${categorias}">
                                    <option value="${cat.id}"
                                        ${not empty p.categoria and p.categoria.id eq cat.id ? 'selected' : ''}>
                                        ${cat.nombre} (${cat.linea})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small" for="precio">Precio</label>
                            <input id="precio" name="precio" type="number" step="1" min="0"
                                   value="${empty p.precio ? '' : p.precio}" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small" for="tallas">Tallas (separadas por coma)</label>
                            <input id="tallas" name="tallas" value="${p.tallas}" class="form-control"
                                   maxlength="120" placeholder="S, M, L, XL">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small" for="color">Color</label>
                            <input id="color" name="color" value="${p.color}" class="form-control" maxlength="60">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small" for="material">Material</label>
                            <input id="material" name="material" value="${p.material}" class="form-control" maxlength="80">
                        </div>
                    </div>
                </div>

                <div class="ficha p-4">
                    <h2 class="fs-5 mb-3">Inventario y estado</h2>
                    <div class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label small" for="stock">Cantidad disponible</label>
                            <input id="stock" name="stock" type="number" min="0" step="1"
                                   value="${p.stock}" class="form-control" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small" for="stockMinimo">Nivel minimo</label>
                            <input id="stockMinimo" name="stockMinimo" type="number" min="0" step="1"
                                   value="${esNuevo ? 5 : p.stockMinimo}" class="form-control" required>
                            <p class="dato text-muted mb-0">Por debajo de este numero
                                el panel avisa que hay que reponer.</p>
                        </div>
                        <div class="col-md-4">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" id="activo" name="activo"
                                       value="true" ${esNuevo or p.activo ? 'checked' : ''}>
                                <label class="form-check-label small" for="activo">
                                    Publicado en el catalogo
                                </label>
                            </div>
                            <p class="dato text-muted mb-0">Si lo desmarcas, deja de verse en la tienda
                                pero sigue en la base y en el historial de pedidos.</p>
                        </div>
                    </div>
                </div>

                <div class="ficha p-4">
                    <h2 class="fs-5 mb-2">Comision de venta</h2>
                    <p class="dato text-muted mb-2">Lo que gana el vendedor por cada unidad de
                        este producto que se venda con un pedido asociado a el.</p>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label small" for="comisionPct">Porcentaje (%)</label>
                            <div class="input-group">
                                <input id="comisionPct" name="comisionPct" type="number" min="0" max="100"
                                       step="0.5" value="${empty p.comisionPct ? 0 : p.comisionPct}"
                                       class="form-control">
                                <span class="input-group-text">%</span>
                            </div>
                        </div>
                        <div class="col-md-8 d-flex align-items-end">
                            <p class="dato text-muted mb-0">Con 10% y un precio de $100.000, el
                                vendedor gana $10.000 por cada unidad vendida. Pon 0 si este
                                producto no da comision.</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="ficha p-4 mb-3">
                    <h2 class="fs-5 mb-3">Imagen</h2>

                    <c:if test="${p.tieneImagen}">
                        <img src="${ctx}${p.rutaImagen}" alt="${p.nombre}" class="vista-previa mb-3">
                    </c:if>

                    <label class="form-label small" for="archivoImagen">Subir un archivo</label>
                    <input id="archivoImagen" name="archivoImagen" type="file" class="form-control mb-1"
                           accept="image/jpeg,image/png,image/webp,image/gif">
                    <p class="dato text-muted">JPG, PNG, WEBP o GIF. Maximo 8 MB.</p>

                    <div class="cinta cinta-fina my-3"></div>

                    <label class="form-label small" for="imagenUrl">O pegar una direccion web</label>
                    <input id="imagenUrl" name="imagenUrl" class="form-control"
                           value="${not empty p.imagen and fn:startsWith(p.imagen, 'http') ? p.imagen : ''}"
                           placeholder="https://...">
                    <p class="dato text-muted mb-0">Si subes un archivo, el archivo tiene prioridad
                        sobre la direccion web.</p>
                </div>

                <c:if test="${tieneMovimientos}">
                    <div class="bloque-dato p-3 mb-3">
                        <p class="rotulo mb-1">Este producto ya se vendio</p>
                        <p class="small mb-0">No se puede eliminar sin romper el historial de pedidos.
                            Si ya no lo vendes, desmarca "Publicado en el catalogo".</p>
                    </div>
                </c:if>

                <button class="btn btn-hilo w-100 mb-2">
                    ${esNuevo ? 'Crear producto' : 'Guardar cambios'}
                </button>
                <a href="${ctx}/panel/productos" class="btn btn-contorno w-100">Cancelar</a>
            </div>
        </div>
    </form>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
