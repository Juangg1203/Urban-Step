<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<header class="portada">
    <div class="patron"></div>
    <div class="container contenido">
        <div class="row align-items-end g-5">
            <div class="col-lg-7">
                <p class="rotulo text-white-50 mb-3"><spring:message code="inicio.rotulo" /></p>
                <h1><spring:message code="inicio.titulo1" /><br><span class="resalta"><spring:message code="inicio.titulo2" /></span></h1>
                <p class="lead mt-3 mb-4" style="max-width:46ch;">
                    Camisetas, pantalones, chaquetas, tenis y botas con la tabla de medidas al frente,
                    no escondida al final de la pagina.
                </p>
                <a href="${ctx}/catalogo" class="btn btn-hilo px-4 py-2"><spring:message code="inicio.verCatalogo" /></a>
                <a href="${ctx}/politica-datos" class="btn btn-contorno px-4 py-2">
                    Como cuidamos tus datos
                </a>
            </div>
            <div class="col-lg-5">
                <div class="ficha p-4">
                    <p class="rotulo mb-3"><spring:message code="inicio.tablaBase" /></p>
                    <table class="table table-sm table-borderless tabla-taller dato mb-0">
                        <tr><td>XS</td><td>Pecho 84 cm</td><td>Calzado 35</td></tr>
                        <tr><td>S</td><td>Pecho 90 cm</td><td>Calzado 37</td></tr>
                        <tr><td>M</td><td>Pecho 96 cm</td><td>Calzado 39</td></tr>
                        <tr><td>L</td><td>Pecho 102 cm</td><td>Calzado 41</td></tr>
                        <tr><td>XL</td><td>Pecho 110 cm</td><td>Calzado 43</td></tr>
                    </table>
                </div>
            </div>
        </div>
    </div>
</header>

<div class="cinta"></div>

<section class="container py-5">
    <div class="d-flex justify-content-between align-items-end mb-4">
        <div>
            <p class="rotulo mb-1"><spring:message code="inicio.seleccion" /></p>
            <h2 class="mb-0"><spring:message code="inicio.destacados" /></h2>
        </div>
        <a href="${ctx}/catalogo" class="btn btn-contorno btn-sm"><spring:message code="inicio.todoCatalogo" /></a>
    </div>

    <div class="row g-4">
        <c:forEach var="p" items="${destacados}">
            <div class="col-6 col-lg-4">
                <article class="ficha">
                    <div class="ficha-imagen ${p.categoria.linea eq 'CALZADO' ? 'calzado' : ''}">
                        <c:out value="${p.sku}" />
                    </div>
                    <div class="p-3">
                        <p class="rotulo mb-1">${p.categoria.nombre}</p>
                        <h3 class="fs-5 mb-1">${p.nombre}</h3>
                        <p class="dato text-muted mb-2">Tallas ${p.tallas}</p>
                        <p class="precio mb-3">
                            $<fmt:formatNumber value="${p.precio}" type="number" maxFractionDigits="0" />
                        </p>
                        <a href="${ctx}/producto/${p.id}" class="btn btn-tinta btn-sm w-100"><spring:message code="comun.verFicha" /></a>
                    </div>
                </article>
            </div>
        </c:forEach>
    </div>
</section>

<section style="background:var(--tinta);color:var(--tiza);" class="py-5">
    <div class="container">
        <p class="rotulo text-white-50 mb-2"><spring:message code="inicio.datosRotulo" /></p>
        <h2 class="text-white mb-4" style="max-width:22ch;"><spring:message code="inicio.datosTitulo" /></h2>
        <div class="row g-4">
            <div class="col-md-3">
                <p class="nivel nivel-publico d-inline-block mb-2">Publico</p>
                <p class="small mb-0">Nombre, ciudad y ocupacion. Es lo que el equipo necesita para
                    identificarte cuando escribes.</p>
            </div>
            <div class="col-md-3">
                <p class="nivel nivel-semiprivado d-inline-block mb-2">Semiprivado</p>
                <p class="small mb-0">Historial de compras y comportamiento de pago. Lo ven atencion
                    al cliente y marketing, y queda registrado quien lo consulto.</p>
            </div>
            <div class="col-md-3">
                <p class="nivel nivel-privado d-inline-block mb-2">Privado</p>
                <p class="small mb-0">Documento, direccion y telefono. Guardado cifrado. El
                    administrador del sistema no tiene acceso.</p>
            </div>
            <div class="col-md-3">
                <p class="nivel nivel-sensible d-inline-block mb-2">Sensible</p>
                <p class="small mb-0">Medidas corporales, alergias a materiales y condiciones de salud.
                    Solo tu los ves, requieren tu autorizacion y puedes borrarlos cuando quieras.</p>
            </div>
        </div>
        <a href="${ctx}/politica-datos" class="btn btn-hilo mt-4"><spring:message code="inicio.leerPolitica" /></a>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
