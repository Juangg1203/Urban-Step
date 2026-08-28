<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:720px;">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>
    <p class="rotulo mb-1">Venta asistida</p>
    <h1 class="mb-2">A quien le vas a vender?</h1>
    <p class="text-muted">Busca al cliente por usuario, correo o nombre. La compra queda a
        su nombre y a ti te genera la comision, sin que tengas que pedirsela.</p>

    <div class="cinta cinta-fina my-4"></div>

    <form method="get" action="${ctx}/panel/venta-asistida" class="d-flex gap-2 mb-4">
        <input type="text" name="q" value="${busqueda}" class="form-control"
               placeholder="Usuario, correo o nombre del cliente" autofocus>
        <button class="btn btn-hilo">Buscar</button>
    </form>

    <c:if test="${not empty busqueda and empty resultados}">
        <div class="bloque-dato p-4">
            <p class="mb-0">No encontramos ningun cliente con "${busqueda}".</p>
        </div>
    </c:if>

    <c:forEach var="c" items="${resultados}">
        <div class="ficha p-3 mb-2 d-flex justify-content-between align-items-center">
            <div>
                <p class="fw-bold mb-0">${c.nombreCompleto}</p>
                <p class="dato text-muted mb-0">${c.ciudad}</p>
            </div>
            <form method="post" action="${ctx}/panel/venta-asistida/elegir-cliente">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="clienteId" value="${c.id}">
                <button class="btn btn-tinta btn-sm">Comprar por el</button>
            </form>
        </div>
    </c:forEach>

    <c:if test="${carrito.tieneClienteElegido}">
        <div class="bloque-dato p-3 mt-4">
            <p class="mb-1">Ya tienes una venta en curso para
                <strong>${carrito.clienteObjetivoNombre}</strong> (${carrito.totalUnidades} unidades).</p>
            <a href="${ctx}/panel/venta-asistida/armar" class="btn btn-hilo btn-sm">Continuar esa venta</a>
        </div>
    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
