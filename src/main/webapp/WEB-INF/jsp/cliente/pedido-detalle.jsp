<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="p" value="${pedido}" />

<section class="container py-5">
    <a href="${ctx}/pedidos" class="enlace-volver">&larr; Volver a mis pedidos</a>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mt-3 mb-2">
        <div>
            <p class="rotulo mb-1">Pedido</p>
            <h1 class="mb-1">${p.numero}</h1>
            <p class="dato text-muted mb-0">Creado el ${p.fechaTexto}</p>
        </div>
        <div class="text-end">
            <span class="estado estado-${fn:toLowerCase(p.estado)} fs-6">${p.estado.etiqueta}</span>
            <div class="mt-2">
                <a href="${ctx}/pedidos/${p.id}/pdf" class="btn btn-contorno btn-sm">Descargar PDF</a>
            </div>
        </div>
    </div>

    <div class="cinta my-4"></div>

    <!-- ---------- linea de seguimiento ---------- -->
    <c:if test="${p.estado ne 'COTIZACION' and p.estado ne 'CANCELADO' and p.estado ne 'RECHAZADO'}">
        <h2 class="fs-5 mb-3">Donde va tu pedido</h2>
        <div class="ruta mb-4">
            <c:set var="pasos" value="Pedido generado,Pago en verificacion,Visto bueno del jefe,Pago confirmado,En preparacion,Despachado,Entregado" />
            <c:forEach var="etapa" items="${pasos}" varStatus="s">
                <div class="ruta-paso ${p.estado.paso >= s.index + 1 ? 'hecho' : ''}">
                    <span class="ruta-punto"></span>
                    <span class="ruta-texto">${etapa}</span>
                </div>
            </c:forEach>
        </div>
        <c:if test="${not empty p.numeroGuia}">
            <p class="small mb-4">Guia de la transportadora:
                <span class="dato fw-bold">${p.numeroGuia}</span>
                <c:if test="${not empty p.fechaDespachoTexto}">
                    <span class="text-muted"> &middot; despachado el ${p.fechaDespachoTexto}</span>
                </c:if>
            </p>
        </c:if>
    </c:if>

    <c:if test="${p.estado eq 'RECHAZADO'}">
        <div class="bloque-dato sensible p-4 mb-4">
            <p class="fw-bold mb-1">El jefe no aprobo esta compra.</p>
            <p class="mb-0 small">Motivo: ${p.motivoDecision}</p>
        </div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="ficha p-4">
                <h2 class="fs-5 mb-3">Productos</h2>
                <table class="table table-sm tabla-taller mb-0">
                    <thead><tr><th>Producto</th><th>Talla</th><th>Cant.</th><th class="text-end">Subtotal</th></tr></thead>
                    <tbody>
                    <c:forEach var="it" items="${p.items}">
                        <tr>
                            <td>${it.nombreProducto}</td>
                            <td class="dato">${empty it.talla ? '—' : it.talla}</td>
                            <td class="dato">${it.cantidad}</td>
                            <td class="dato text-end">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="ficha p-4 mb-3">
                <h2 class="fs-5 mb-3">Resumen</h2>
                <div class="d-flex justify-content-between small mb-1">
                    <span>Subtotal</span>
                    <span class="dato">$<fmt:formatNumber value="${p.subtotal}" maxFractionDigits="0" /></span>
                </div>
                <div class="d-flex justify-content-between small mb-2">
                    <span>Envio</span>
                    <span class="dato">
                        <c:choose>
                            <c:when test="${p.envioGratis}"><span style="color:var(--verde)">Gratis</span></c:when>
                            <c:otherwise>$<fmt:formatNumber value="${p.costoEnvio}" maxFractionDigits="0" /></c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="cinta cinta-fina my-2"></div>
                <div class="d-flex justify-content-between align-items-baseline">
                    <span class="rotulo">Total</span>
                    <span class="cifra acento">$<fmt:formatNumber value="${p.total}" maxFractionDigits="0" /></span>
                </div>
                <p class="dato text-muted mt-3 mb-0">Entrega: ${p.direccionEntrega}</p>
                <c:if test="${not empty p.medioPago}">
                    <p class="dato text-muted mb-0">Pago: ${p.medioPago}</p>
                </c:if>
                <c:if test="${not empty p.estadoPasarela}">
                    <p class="dato text-muted mb-0">Pasarela: ${p.estadoPasarela}
                        <c:if test="${not empty p.transaccionPasarela}">
                            &middot; tx ${p.transaccionPasarela}
                        </c:if>
                    </p>
                </c:if>
            </div>

            <!-- ---------- acciones segun el estado ---------- -->
            <c:if test="${p.estado eq 'COTIZACION'}">
                <form method="post" action="${ctx}/pedidos/${p.id}/enviar" class="ficha p-4">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <h2 class="fs-6 mb-2">Cotizacion guardada</h2>
                    <p class="small">Los precios de arriba quedan congelados. Cuando decidas,
                        continua para pagarla: no hace falta esperar aprobacion de nadie.</p>
                    <button class="btn btn-hilo w-100">Continuar y pagar</button>
                </form>
            </c:if>

            <c:if test="${p.estado eq 'PENDIENTE_PAGO' and pasarelaActiva}">
                <div class="ficha p-4 mb-3">
                    <h2 class="fs-6 mb-2">Pagar en linea</h2>
                    <p class="small">Puedes pagar con tarjeta, PSE o Nequi a traves de Wompi;
                        el cobro se confirma automaticamente.</p>
                    <a href="${ctx}/pagos/wompi/${p.id}" class="btn btn-hilo w-100">Pagar con Wompi</a>
                </div>
            </c:if>

            <c:if test="${p.estado eq 'PENDIENTE_PAGO'}">
                <form method="post" action="${ctx}/pedidos/${p.id}/pagar" class="ficha p-4"
                      enctype="multipart/form-data">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <h2 class="fs-6 mb-2">O reportar un pago manual</h2>
                    <p class="small">Si pagaste por transferencia o en tienda, registra aqui la
                        referencia. Adjuntar el comprobante agiliza la verificacion.</p>
                    <label class="form-label small" for="referenciaPago">Referencia</label>
                    <input id="referenciaPago" name="referenciaPago" class="form-control mb-2" required>
                    <label class="form-label small" for="comprobante">Comprobante (opcional)</label>
                    <input id="comprobante" name="comprobante" type="file" class="form-control mb-3"
                           accept="image/jpeg,image/png,image/webp,image/gif">
                    <button class="btn btn-hilo w-100">Reportar pago</button>
                </form>
            </c:if>

            <c:if test="${p.estado eq 'PAGO_EN_VERIFICACION'}">
                <div class="bloque-dato p-4">
                    <p class="fw-bold mb-1">Verificando tu pago</p>
                    <p class="small mb-0">Referencia <span class="dato">${p.referenciaPago}</span>.
                        <c:if test="${p.tieneComprobante}">Ya recibimos tu comprobante. </c:if>
                        Cuando el vendedor lo confirme, pasa al jefe para el ultimo visto bueno.</p>
                </div>
            </c:if>

            <c:if test="${p.estado eq 'PENDIENTE_ACEPTACION_JEFE'}">
                <div class="bloque-dato p-4">
                    <p class="fw-bold mb-1">Pago confirmado</p>
                    <p class="small mb-0">Tu pago ya esta verificado. Falta el visto bueno final
                        antes de pasar a bodega; te avisamos apenas se decida.</p>
                </div>
            </c:if>

            <c:if test="${p.estado eq 'DESPACHADO'}">
                <form method="post" action="${ctx}/pedidos/${p.id}/recibido" class="ficha p-4"
                      enctype="multipart/form-data">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <h2 class="fs-6 mb-2">Ya te llego?</h2>
                    <p class="small">Confirma que lo recibiste para cerrar el pedido y poder
                        dejar tu resena. Una foto es opcional pero ayuda si algo llega mal.</p>
                    <label class="form-label small" for="foto">Foto de lo recibido (opcional)</label>
                    <input id="foto" name="foto" type="file" class="form-control mb-3"
                           accept="image/jpeg,image/png,image/webp,image/gif">
                    <button class="btn btn-hilo w-100">Confirmar que lo recibi</button>
                </form>
            </c:if>

            <c:if test="${p.estado eq 'ENTREGADO'}">
                <div class="ficha p-4">
                    <h2 class="fs-6 mb-3">Deja tu resena</h2>
                    <c:forEach var="it" items="${p.items}">
                        <form method="post" action="${ctx}/pedidos/${p.id}/resena" class="mb-3 pb-3 border-bottom">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <input type="hidden" name="productoId" value="${it.producto.id}">
                            <p class="small fw-bold mb-1">${it.nombreProducto}</p>
                            <select name="calificacion" class="form-select form-select-sm mb-2" style="width:auto;">
                                <option value="5">5 - Excelente</option>
                                <option value="4">4 - Buena</option>
                                <option value="3">3 - Aceptable</option>
                                <option value="2">2 - Mala</option>
                                <option value="1">1 - Muy mala</option>
                            </select>
                            <textarea name="comentario" rows="2" class="form-control form-control-sm mb-2"
                                      placeholder="Que te parecio? (opcional)"></textarea>
                            <button class="btn btn-contorno btn-sm">Enviar resena</button>
                        </form>
                    </c:forEach>
                </div>
            </c:if>

            <c:if test="${p.estado.cancelablePorCliente}">
                <form method="post" action="${ctx}/pedidos/${p.id}/cancelar" class="mt-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input name="motivo" class="form-control form-control-sm mb-2"
                           placeholder="Motivo (opcional)">
                    <button class="btn btn-link btn-sm text-danger p-0">Cancelar este pedido</button>
                </form>
            </c:if>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
