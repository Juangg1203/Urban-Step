<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:960px;">
    <a href="${ctx}/carrito" class="enlace-volver">&larr; <spring:message code="comun.volverCarrito" /></a>
    <p class="rotulo mb-1"><spring:message code="checkout.rotulo" /></p>
    <h1 class="mb-1"><spring:message code="checkout.titulo" /></h1>
    <p class="text-muted"><spring:message code="checkout.subtitulo" /></p>

    <div class="cinta cinta-fina my-4"></div>

    <c:if test="${not empty faltantes}">
        <div class="aviso-inventario p-3 mb-4">
            <p class="rotulo mb-2">Revisa las cantidades</p>
            <ul class="mb-0 small">
                <c:forEach var="f" items="${faltantes}">
                    <li>${f.mensaje}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>


    <form method="post" action="${ctx}/checkout">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

        <div class="row g-4">
            <div class="col-lg-7">
                <div class="ficha p-4 mb-3">
                    <h2 class="fs-5 mb-0"><spring:message code="checkout.entrega" /></h2>

                    <div class="row g-2 mt-1">
                        <div class="col-md-6">
                            <label class="form-label small" for="pais">Pais</label>
                            <select id="pais" name="pais" class="form-select mb-2">
                                <option value="Colombia" selected>Colombia</option>
                            </select>
                            <p class="dato text-muted mb-0" style="margin-top:-.3rem;">
                                Por ahora solo enviamos dentro de Colombia.</p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small" for="ciudad">Ciudad</label>
                            <select id="ciudad" name="ciudad" class="form-select mb-2" required>
                                <option value="">Elige tu ciudad</option>
                                <option value="Bucaramanga" ${cliente.ciudad eq 'Bucaramanga' ? 'selected' : ''}>Bucaramanga</option>
                                <option value="Floridablanca" ${cliente.ciudad eq 'Floridablanca' ? 'selected' : ''}>Floridablanca</option>
                                <option value="Giron" ${cliente.ciudad eq 'Giron' ? 'selected' : ''}>Giron</option>
                                <option value="Piedecuesta" ${cliente.ciudad eq 'Piedecuesta' ? 'selected' : ''}>Piedecuesta</option>
                                <option value="Bogota" ${cliente.ciudad eq 'Bogota' ? 'selected' : ''}>Bogota</option>
                                <option value="Medellin" ${cliente.ciudad eq 'Medellin' ? 'selected' : ''}>Medellin</option>
                                <option value="Cali" ${cliente.ciudad eq 'Cali' ? 'selected' : ''}>Cali</option>
                                <option value="Barranquilla" ${cliente.ciudad eq 'Barranquilla' ? 'selected' : ''}>Barranquilla</option>
                                <option value="Cartagena" ${cliente.ciudad eq 'Cartagena' ? 'selected' : ''}>Cartagena</option>
                                <option value="Cucuta" ${cliente.ciudad eq 'Cucuta' ? 'selected' : ''}>Cucuta</option>
                                <option value="Otra">Otra ciudad</option>
                            </select>
                        </div>
                    </div>

                    <label class="form-label small" for="direccion">Direccion (calle, numero, apto)</label>
                    <input id="direccion" name="direccion" class="form-control mb-2"
                           placeholder="Calle 36 # 22-15, apto 402" required>
                    <p class="dato text-muted mb-0">Se guarda cifrada. El administrador no puede verla;
                        solo tu, el jefe y quien alista tu pedido en bodega.</p>
                </div>

                <div class="ficha p-4 mb-3">
                    <h2 class="fs-5 mb-3"><spring:message code="checkout.medioPago" /></h2>
                    <div class="row g-2">
                        <div class="col-md-6">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="medioPago"
                                       id="mpPse" value="PSE" checked>
                                <label class="form-check-label small" for="mpPse">PSE o transferencia</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="medioPago"
                                       id="mpTarjeta" value="TARJETA">
                                <label class="form-check-label small" for="mpTarjeta">Tarjeta debito o credito</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="medioPago"
                                       id="mpContra" value="CONTRA_ENTREGA">
                                <label class="form-check-label small" for="mpContra">Contra entrega</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="medioPago"
                                       id="mpEfectivo" value="EFECTIVO">
                                <label class="form-check-label small" for="mpEfectivo">Efectivo en tienda</label>
                            </div>
                        </div>
                    </div>
                    <p class="dato text-muted mt-3 mb-0">Aqui no se piden datos de tarjeta. El cobro lo
                        procesa la pasarela despues de que el jefe apruebe la compra.</p>
                </div>

                <div class="ficha p-4">
                    <h2 class="fs-5 mb-2"><spring:message code="checkout.observaciones" /></h2>
                    <textarea name="observaciones" rows="2" class="form-control"
                              placeholder="Punto de referencia, horario preferido, algo que debamos saber"></textarea>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="ficha p-4">
                    <h2 class="fs-5 mb-3"><spring:message code="checkout.tuPedido" /></h2>
                    <c:forEach var="it" items="${carrito.items}">
                        <div class="d-flex justify-content-between small mb-2">
                            <span>${it.cantidad} &times; ${it.nombre}
                                <c:if test="${not empty it.talla}"><span class="dato text-muted"> (${it.talla})</span></c:if>
                            </span>
                            <span class="dato">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></span>
                        </div>
                    </c:forEach>

                    <div class="cinta cinta-fina my-3"></div>

                    <div class="d-flex justify-content-between small mb-1">
                        <span><spring:message code="comun.subtotal" /></span>
                        <span class="dato">$<fmt:formatNumber value="${carrito.subtotal}" maxFractionDigits="0" /></span>
                    </div>
                    <div class="d-flex justify-content-between small mb-3">
                        <span><spring:message code="comun.envio" /></span>
                        <span class="dato">
                            <c:choose>
                                <c:when test="${carrito.envioGratis}"><span style="color:var(--verde)"><spring:message code="comun.gratis" /></span></c:when>
                                <c:otherwise>$<fmt:formatNumber value="${carrito.costoEnvio}" maxFractionDigits="0" /></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="d-flex justify-content-between align-items-baseline mb-4">
                        <span class="rotulo"><spring:message code="comun.total" /></span>
                        <span class="cifra acento">$<fmt:formatNumber value="${carrito.total}" maxFractionDigits="0" /></span>
                    </div>

                    <button name="soloCotizar" value="false" class="btn btn-hilo w-100 mb-2">
                        <spring:message code="checkout.enviarAprobacion" />
                    </button>
                    <button name="soloCotizar" value="true" class="btn btn-contorno w-100 mb-3">
                        <spring:message code="checkout.soloCotizar" />
                    </button>

                    <p class="small text-muted mb-0">La cotizacion congela estos precios sin comprometerte
                        a nada: la puedes continuar y pagar despues, desde Mis pedidos.</p>
                </div>
            </div>
        </div>
    </form>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
