<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:640px;">
    <a href="${ctx}/pedidos/${pedido.id}" class="enlace-volver">&larr; Volver al pedido</a>

    <p class="rotulo mb-1">Pago en linea</p>
    <h1 class="mb-1">${pedido.numero}</h1>
    <p class="text-muted">Vas a pagar a traves de Wompi. Los datos de tu tarjeta no pasan por
        nuestro servidor.</p>

    <div class="cinta cinta-fina my-4"></div>

    <div class="ficha p-4 mb-3">
        <div class="d-flex justify-content-between align-items-baseline mb-3">
            <span class="rotulo">Total a pagar</span>
            <span class="cifra acento">$<fmt:formatNumber value="${pedido.total}" maxFractionDigits="0" /></span>
        </div>
        <table class="table table-sm tabla-taller mb-0">
            <c:forEach var="it" items="${pedido.items}">
                <tr>
                    <td class="small">${it.cantidad} &times; ${it.nombreProducto}</td>
                    <td class="dato text-end">$<fmt:formatNumber value="${it.subtotal}" maxFractionDigits="0" /></td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <c:if test="${sandbox}">
        <div class="aviso-inventario p-3 mb-3">
            <p class="rotulo mb-1">Ambiente de pruebas</p>
            <p class="small mb-0">Esta conexion apunta al sandbox de Wompi: no se mueve dinero real.
                Usa las tarjetas de prueba de la documentacion (por ejemplo la que termina en 4242
                para aprobar y la que termina en 0002 para que el banco rechace).</p>
        </div>
    </c:if>

    <!-- El formulario se envia al checkout de Wompi. La firma se calculo en el
         servidor: si alguien edita el monto aqui, la firma deja de coincidir
         y la pasarela rechaza la transaccion. -->
    <form action="${urlCheckout}" method="GET">
        <input type="hidden" name="public-key"      value="${llavePublica}">
        <input type="hidden" name="currency"        value="${moneda}">
        <input type="hidden" name="amount-in-cents" value="${centavos}">
        <input type="hidden" name="reference"       value="${referencia}">
        <input type="hidden" name="signature:integrity" value="${firma}">
        <input type="hidden" name="redirect-url"    value="${urlRetorno}">
        <input type="hidden" name="customer-data:email"     value="${pedido.cliente.usuario.correo}">
        <input type="hidden" name="customer-data:full-name" value="${pedido.cliente.nombreCompleto}">

        <button class="btn btn-hilo w-100 mb-2">Pagar con Wompi</button>
    </form>

    <a href="${ctx}/pedidos/${pedido.id}" class="btn btn-contorno w-100">Pagar de otra forma</a>

    <div class="bloque-dato p-3 mt-4">
        <p class="rotulo mb-1">Por que no pedimos los datos de tu tarjeta</p>
        <p class="small mb-0">El numero de la tarjeta lo captura Wompi, que esta certificado para
            hacerlo. La tienda solo recibe un identificador de transaccion y despues le pregunta a la
            pasarela si el pago entro. Asi, aunque nuestra base de datos se filtrara, ahi no hay
            ningun dato de tarjeta que robar.</p>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
