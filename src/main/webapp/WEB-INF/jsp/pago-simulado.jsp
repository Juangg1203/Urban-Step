<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:640px;">
    <a href="${ctx}/pedidos/${pedido.id}" class="enlace-volver">&larr; Volver al pedido</a>

    <p class="rotulo mb-1">Pasarela de pagos &middot; modo simulado</p>
    <h1 class="mb-1">${pedido.numero}</h1>
    <p class="text-muted">Esta es una pasarela local que reproduce el flujo real de Wompi.
        No se mueve dinero y no hace falta cuenta de comercio.</p>

    <div class="cinta cinta-fina my-4"></div>

    <div class="aviso-inventario p-3 mb-4">
        <p class="rotulo mb-1">Que es esto y que no es</p>
        <p class="small mb-0">El pedido recorre exactamente los mismos pasos que con la pasarela real:
            se genera una referencia unica, se firma el monto, la pasarela valida esa firma y el
            resultado se aplica al pedido de forma idempotente. Lo unico que cambia es quien responde:
            en vez de Wompi, este mismo servidor. Al conseguir las llaves del comercio, se cambia
            <span class="dato">app.pago.wompi.modo</span> a <span class="dato">real</span> y no hay
            que tocar nada mas.</p>
    </div>

    <div class="ficha p-4 mb-3">
        <div class="d-flex justify-content-between align-items-baseline mb-3">
            <span class="rotulo">Total a pagar</span>
            <span class="cifra acento">$<fmt:formatNumber value="${pedido.total}" maxFractionDigits="0" /></span>
        </div>
        <table class="table table-sm tabla-taller mb-3">
            <c:forEach var="it" items="${pedido.items}">
                <tr>
                    <td class="small">${it.cantidad} &times; ${it.nombreProducto}</td>
                    <td class="dato text-end">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                </tr>
            </c:forEach>
        </table>
        <p class="dato text-muted mb-0">Referencia: ${referencia}</p>
        <p class="dato text-muted mb-0">Monto en centavos: ${centavos}</p>
        <p class="dato text-muted mb-0" style="word-break:break-all;">Firma: ${fn:substring(firma,0,32)}...</p>
    </div>

    <form method="post" action="${ctx}/pagos/simulado/procesar" class="ficha p-4">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <input type="hidden" name="referencia" value="${referencia}">
        <input type="hidden" name="centavos"   value="${centavos}">
        <input type="hidden" name="firma"      value="${firma}">

        <h2 class="fs-6 mb-3">Elige el resultado de la transaccion</h2>

        <label class="form-label small" for="metodo">Metodo de pago</label>
        <select id="metodo" name="metodo" class="form-select form-select-sm mb-3">
            <option value="CARD">Tarjeta</option>
            <option value="NEQUI">Nequi</option>
            <option value="PSE">PSE</option>
            <option value="BANCOLOMBIA_TRANSFER">Transferencia Bancolombia</option>
        </select>

        <button name="resultado" value="APPROVED" class="btn btn-hilo w-100 mb-2">
            Pago aprobado
        </button>
        <button name="resultado" value="DECLINED" class="btn btn-contorno w-100 mb-2">
            El banco lo rechaza
        </button>
        <button name="resultado" value="PENDING" class="btn btn-contorno w-100">
            Queda pendiente
        </button>

        <p class="dato text-muted mt-3 mb-0">Poder provocar un rechazo a voluntad es justamente lo
            que permite demostrar que el sistema maneja bien el caso malo, no solo el bueno.</p>
    </form>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
